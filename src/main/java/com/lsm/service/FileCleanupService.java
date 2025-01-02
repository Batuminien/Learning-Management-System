package com.lsm.service;

import com.lsm.model.entity.AssignmentDocument;
import com.lsm.repository.AssignmentDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileCleanupService {
    /*
    private final AssignmentDocumentRepository assignmentDocumentRepository;
    private final AssignmentDocumentService fileStorageService;

    @Scheduled(cron = "0 0 1 * * ?") // Runs at 1 AM daily
    public void cleanupOldFiles() {
        LocalDateTime threeMonthsAgo = LocalDateTime.now().minusMonths(3);

        List<AssignmentDocument> oldDocuments = assignmentDocumentRepository
                .findByUploadTimeBefore(threeMonthsAgo);

        for (AssignmentDocument doc : oldDocuments) {
            try {
                fileStorageService.deleteDocument(doc.getId(), null);
                assignmentDocumentRepository.delete(doc);
                log.info("Deleted old assignment file: {}", doc.getFilePath());
            } catch (Exception e) {
                log.error("Failed to delete file: {}", doc.getFilePath(), e);
            }
        }
    }
     */
}
