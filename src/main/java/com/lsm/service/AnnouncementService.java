package com.lsm.service;

import com.lsm.model.DTOs.AnnouncementDTO;
import com.lsm.model.entity.Announcement;
import com.lsm.model.entity.ClassEntity;
import com.lsm.model.entity.base.AppUser;
import com.lsm.model.entity.enums.Role;
import com.lsm.repository.AnnouncementRepository;
import com.lsm.repository.ClassEntityRepository;
import com.lsm.exception.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnnouncementService {
    private final AnnouncementRepository announcementRepository;
    private final ClassEntityRepository classEntityRepository;
    private final AppUserService appUserService;

    @Transactional
    public AnnouncementDTO createAnnouncement(AppUser loggedInUser, AnnouncementDTO dto)
            throws AccessDeniedException, ResourceNotFoundException {
        AppUser user = appUserService.getCurrentUserWithDetails(loggedInUser.getId());

        // Verify all classes exist and user has access to them
        Set<ClassEntity> classes = new HashSet<>();
        for (Long classId : dto.getClassIds()) {
            ClassEntity classEntity = classEntityRepository.findById(classId)
                    .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));

            if (user.getRole().equals(Role.ROLE_STUDENT)) {
                throw new AccessDeniedException("Students can't create announcements");
            }

            if (user.getRole().equals(Role.ROLE_TEACHER) &&
                    user.getTeacherDetails().getClasses().stream()
                            .noneMatch(c -> c.getId().equals(classId))) {
                throw new AccessDeniedException("Teachers can't create announcements for classes they don't teach");
            }

            classes.add(classEntity);
        }

        Announcement announcement = new Announcement();
        announcement.setTitle(dto.getTitle());
        announcement.setContent(dto.getContent());
        announcement.setClasses(classes);

        announcement = announcementRepository.save(announcement);
        return convertToDTO(announcement);
    }

    @Transactional
    public List<AnnouncementDTO> getAnnouncementsByClassId(AppUser loggedInUser, Long classId)
            throws AccessDeniedException {
        AppUser user = appUserService.getCurrentUserWithDetails(loggedInUser.getId());

        if (user.getRole().equals(Role.ROLE_STUDENT) &&
                !user.getStudentDetails().getClassEntity().equals(classId)) {
            throw new AccessDeniedException("Students can't get announcements of other classes");
        }

        if (user.getRole().equals(Role.ROLE_TEACHER) &&
                user.getTeacherDetails().getClasses().stream()
                        .noneMatch(c -> c.getId().equals(classId))) {
            throw new AccessDeniedException("Teachers can't get announcements of classes they don't teach");
        }

        return announcementRepository.findByClassesId(classId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteAnnouncement(AppUser loggedInUser, Long id)
            throws AccessDeniedException, ResourceNotFoundException {
        AppUser user = appUserService.getCurrentUserWithDetails(loggedInUser.getId());
        if (user.getRole().equals(Role.ROLE_STUDENT)) {
            throw new AccessDeniedException("Students can't delete announcements");
        }

        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found"));

        // For teachers, check if they have access to all classes in the announcement
        if (user.getRole().equals(Role.ROLE_TEACHER)) {
            boolean hasAccessToAllClasses = announcement.getClasses().stream()
                    .allMatch(announcementClass -> user.getTeacherDetails().getClasses().stream()
                            .anyMatch(teacherClass -> teacherClass.getId().equals(announcementClass.getId())));

            if (!hasAccessToAllClasses) {
                throw new AccessDeniedException("Teachers can't delete announcements of classes they don't teach");
            }
        }

        announcementRepository.deleteById(id);
    }

    @Transactional
    public AnnouncementDTO updateAnnouncement(AppUser loggedInUser, Long id, AnnouncementDTO announcementDTO)
            throws AccessDeniedException, EntityNotFoundException {
        AppUser user = appUserService.getCurrentUserWithDetails(loggedInUser.getId());
        Announcement existingAnnouncement = announcementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Announcement not found with id: " + id));

        if (user.getRole().equals(Role.ROLE_STUDENT)) {
            throw new AccessDeniedException("Students can't update announcements");
        }

        // For teachers, verify they have access to all current and new classes
        if (user.getRole().equals(Role.ROLE_TEACHER)) {
            // Check access to current classes
            boolean hasAccessToCurrentClasses = existingAnnouncement.getClasses().stream()
                    .allMatch(announcementClass -> user.getTeacherDetails().getClasses().stream()
                            .anyMatch(teacherClass -> teacherClass.getId().equals(announcementClass.getId())));

            if (!hasAccessToCurrentClasses) {
                throw new AccessDeniedException("Teachers can't update announcements of classes they don't teach");
            }

            // Check access to new classes
            if (announcementDTO.getClassIds() != null) {
                boolean hasAccessToNewClasses = announcementDTO.getClassIds().stream()
                        .allMatch(classId -> user.getTeacherDetails().getClasses().stream()
                                .anyMatch(teacherClass -> teacherClass.getId().equals(classId)));

                if (!hasAccessToNewClasses) {
                    throw new AccessDeniedException("Teachers can't update announcements to include classes they don't teach");
                }
            }
        }
        // Update basic fields
        existingAnnouncement.setTitle(announcementDTO.getTitle());
        existingAnnouncement.setContent(announcementDTO.getContent());

        // Update classes if provided
        if (announcementDTO.getClassIds() != null && !announcementDTO.getClassIds().isEmpty()) {
            Set<ClassEntity> newClasses = announcementDTO.getClassIds().stream()
                    .map(classId -> classEntityRepository.findById(classId)
                            .orElseThrow(() -> new EntityNotFoundException("Class not found with id: " + classId)))
                    .collect(Collectors.toSet());
            existingAnnouncement.setClasses(newClasses);
        }

        // Save and convert to DTO
        Announcement updatedAnnouncement = announcementRepository.save(existingAnnouncement);
        return convertToDTO(updatedAnnouncement);
    }

    @Transactional
    public AnnouncementDTO getAnnouncementById(AppUser loggedInUser, Long announcementId)
            throws AccessDeniedException, ResourceNotFoundException {
        AppUser user = appUserService.getCurrentUserWithDetails(loggedInUser.getId());
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found"));

        // Check permissions
        if (user.getRole().equals(Role.ROLE_STUDENT)) {
            // Check if the announcement belongs to student's class
            boolean hasAccess = announcement.getClasses().stream()
                    .anyMatch(classEntity -> classEntity.getId().equals(user.getStudentDetails().getClassEntity()));
            if (!hasAccess) {
                throw new AccessDeniedException("Students can't access announcements of other classes");
            }
        }

        if (user.getRole().equals(Role.ROLE_TEACHER)) {
            // Check if teacher has access to any of the classes in the announcement
            boolean hasAccess = announcement.getClasses().stream()
                    .anyMatch(announcementClass -> user.getTeacherDetails().getClasses().stream()
                            .anyMatch(teacherClass -> teacherClass.getId().equals(announcementClass.getId())));
            if (!hasAccess) {
                throw new AccessDeniedException("Teachers can't access announcements of classes they don't teach");
            }
        }

        return convertToDTO(announcement);
    }

    private AnnouncementDTO convertToDTO(Announcement announcement) {
        AnnouncementDTO dto = new AnnouncementDTO();
        dto.setId(announcement.getId());
        dto.setTitle(announcement.getTitle());
        dto.setContent(announcement.getContent());
        dto.setClassIds(announcement.getClasses().stream()
                .map(ClassEntity::getId)
                .collect(Collectors.toList()));
        dto.setCreatedAt(announcement.getCreatedAt().toLocalDate());
        return dto;
    }
}