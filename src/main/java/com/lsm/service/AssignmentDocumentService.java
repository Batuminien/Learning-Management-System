package com.lsm.service;

import com.lsm.model.entity.Assignment;
import com.lsm.model.entity.AssignmentDocument;
import com.lsm.model.entity.base.AppUser;
import com.lsm.model.entity.enums.Role;
import com.lsm.repository.AssignmentDocumentRepository;
import com.lsm.repository.AssignmentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.UUID;

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

    private void validateDownloadAccess(AssignmentDocument document, AppUser currentUser) throws AccessDeniedException {
        if (currentUser.getRole() == Role.ROLE_ADMIN ||
                currentUser.getRole() == Role.ROLE_COORDINATOR) {
            return;
        }

        if (currentUser.getRole() == Role.ROLE_TEACHER &&
                !currentUser.equals(document.getAssignment().getAssignedBy())) {
            throw new AccessDeniedException("Teachers can only access their own assignment documents");
        }

        if (currentUser.getRole() == Role.ROLE_STUDENT &&
                !document.getAssignment().getClassEntity().getStudents().contains(currentUser)) {
            throw new AccessDeniedException("Students can only access documents for their assignments");
        }
    }
}
