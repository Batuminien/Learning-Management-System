package com.lsm.mapper;

import com.lsm.model.DTOs.AssignmentDocumentDTO;
import com.lsm.model.entity.AssignmentDocument;
import org.springframework.stereotype.Component;

@Component
public class AssignmentDocumentMapper {
    public AssignmentDocumentDTO convertToDTO(AssignmentDocument document) {
        return AssignmentDocumentDTO.builder()
                .assignmentId(document.getAssignment().getId())
                .documentId(document.getId())
                .fileName(document.getFileName())
                .fileType(document.getFileType())
                .filePath(document.getFilePath())
                .fileSize(document.getFileSize())
                .uploadTime(document.getUploadTime())
                .uploadedByUsername(document.getUploadedBy().getUsername())
                .build();
    }
}
