package com.lsm.service;

import com.lsm.model.entity.Assignment;
import com.lsm.model.entity.AssignmentDocument;
import com.lsm.model.entity.StudentSubmission;
import com.lsm.model.entity.base.AppUser;
import com.lsm.model.entity.enums.Role;
import com.lsm.repository.AssignmentDocumentRepository;
import com.lsm.repository.AssignmentRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class AssignmentDocumentService {
    private final AssignmentRepository assignmentRepository;
    private final AssignmentDocumentRepository documentRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Transactional
    public AssignmentDocument uploadDocument(MultipartFile file, Long assignmentId,
                                             AppUser currentUser)
            throws IOException {
        if (currentUser.getRole() == Role.ROLE_STUDENT)
            throw new AccessDeniedException("You are not allowed to upload a student document");

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));

        // First, delete existing document if any
        if (assignment.getTeacherDocument() != null) {
            AssignmentDocument oldDoc = assignment.getTeacherDocument();
            assignment.setTeacherDocument(null);
            Files.deleteIfExists(Paths.get(oldDoc.getFilePath()));
            documentRepository.delete(oldDoc);
        }
        assignmentRepository.save(assignment);

        // Create directory if it doesn't exist
        String dirPath = uploadDir + "/" + assignmentId;
        Files.createDirectories(Paths.get(dirPath));

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String uniqueFilename = UUID.randomUUID() + fileExtension;
        String filePath = dirPath + "/" + uniqueFilename;

        // Save file
        Files.copy(file.getInputStream(), Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);

        // Create and save new document
        AssignmentDocument document = AssignmentDocument.builder()
                .fileName(originalFilename)
                .filePath(filePath)
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .uploadTime(LocalDateTime.now())
                .uploadedBy(currentUser)
                .assignment(assignment)
                .build();

        // Save document first
        document = documentRepository.save(document);

        // Update assignment with the new document
        assignment.setTeacherDocument(document);
        // assignmentRepository.save(assignment);

        return document;
    }

    @Transactional
    public Resource downloadDocument(Long documentId, AppUser currentUser) throws IOException {
        AssignmentDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found"));

        // Validate access permissions
        validateDownloadAccess(document, currentUser);

        Path path = Paths.get(document.getFilePath());
        Resource resource = new UrlResource(path.toUri());

        if (resource.exists() || resource.isReadable()) {
            return resource;
        } else {
            throw new RuntimeException("Could not read the file!");
        }
    }

    @Transactional(readOnly = true)
    public Resource bulkDownloadSubmissions(Long assignmentId, AppUser currentUser) throws IOException {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));

        // Validate access
        if (currentUser.getRole() == Role.ROLE_STUDENT) {
            throw new AccessDeniedException("Students cannot bulk download submissions");
        }

        if (currentUser.getRole() == Role.ROLE_TEACHER &&
                !currentUser.getId().equals(assignment.getAssignedBy().getId())) {
            throw new AccessDeniedException("Teachers can only download submissions for their own assignments");
        }

        // Create a temporary zip file
        Path tempDir = Files.createTempDirectory("submissions_");
        Path zipFile = tempDir.resolve("submissions.zip");

        try (FileOutputStream fos = new FileOutputStream(zipFile.toFile());
             ZipOutputStream zipOut = new ZipOutputStream(fos)) {

            for (StudentSubmission submission : assignment.getStudentSubmissions()) {
                if (submission.getDocument() != null) {
                    // Create student-specific folder name
                    String folderName = submission.getStudent().getUsername() + "_" +
                            submission.getStudent().getId() + "/";

                    // Add the submission document to the zip
                    Path docPath = Paths.get(submission.getDocument().getFilePath());
                    if (Files.exists(docPath)) {
                        ZipEntry zipEntry = new ZipEntry(folderName +
                                submission.getDocument().getFileName());
                        zipOut.putNextEntry(zipEntry);
                        Files.copy(docPath, zipOut);
                        zipOut.closeEntry();
                    }
                }
            }
        }

        // Create resource from the zip file
        Resource resource = new UrlResource(zipFile.toUri());

        // Schedule file deletion after response is sent
        tempDir.toFile().deleteOnExit();
        zipFile.toFile().deleteOnExit();

        return resource;
    }

    @Transactional
    public void deleteDocument(Long documentId, AppUser currentUser) throws IOException {
        AssignmentDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found"));

        Assignment assignment = document.getAssignment();

        // Delete file from filesystem
        Path filePath = Paths.get(document.getFilePath());
        Files.deleteIfExists(filePath);

        // Remove document reference from assignment
        if (document.equals(assignment.getTeacherDocument())) {
            assignment.setTeacherDocument(null);
        }

        // Delete document from database
        documentRepository.delete(document);

        // Delete directory if empty
        Path dirPath = filePath.getParent();
        if (Files.exists(dirPath) && isDirectoryEmpty(dirPath)) {
            Files.delete(dirPath);
        }
    }

    private boolean isDirectoryEmpty(Path path) throws IOException {
        try (DirectoryStream<Path> directory = Files.newDirectoryStream(path)) {
            return !directory.iterator().hasNext();
        }
    }

    public AssignmentDocument findById(Long id) {
        return documentRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Assignment not found"));
    }

    private void validateDownloadAccess(AssignmentDocument document, AppUser currentUser) throws AccessDeniedException {
        // Admin and coordinator have full access
        if (currentUser.getRole() == Role.ROLE_ADMIN ||
                currentUser.getRole() == Role.ROLE_COORDINATOR) {
            return;
        }

        Assignment assignment = document.getAssignment();

        if (currentUser.getRole() == Role.ROLE_TEACHER) {
            // Teachers can download:
            // 1. Their own assignment documents
            // 2. Student submissions for their assignments
            boolean isAssignmentCreator = currentUser.getId().equals(assignment.getAssignedBy().getId());

            if (!isAssignmentCreator && document.equals(assignment.getTeacherDocument())) {
                throw new AccessDeniedException("Teachers can only access their own assignment documents");
            }
            return;
        }

        if (currentUser.getRole() == Role.ROLE_STUDENT) {
            // Students can download:
            // 1. Teacher documents for their assignments
            // 2. Their own submissions
            boolean isStudentInClass = assignment.getClassEntity().getStudents().stream()
                    .anyMatch(s -> s.getId().equals(currentUser.getId()));

            if (!isStudentInClass) {
                throw new AccessDeniedException("Students can only access documents for their assignments");
            }

            // If it's a submission document, verify it belongs to the student
            if (!document.equals(assignment.getTeacherDocument())) {
                boolean isOwnSubmission = assignment.getStudentSubmissions().stream()
                        .anyMatch(submission ->
                                submission.getStudent().getId().equals(currentUser.getId()) &&
                                        submission.getDocument() != null &&
                                        submission.getDocument().getId().equals(document.getId())
                        );

                if (!isOwnSubmission) {
                    throw new AccessDeniedException("Students can only download their own submissions");
                }
            }
            return;
        }
    }
}
