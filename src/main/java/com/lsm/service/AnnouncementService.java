package com.lsm.service;

import com.lsm.model.DTOs.AnnouncementDTO;
import com.lsm.model.entity.Announcement;
import com.lsm.model.entity.AnnouncementReadStatus;
import com.lsm.model.entity.ClassEntity;
import com.lsm.model.entity.base.AppUser;
import com.lsm.model.entity.enums.Role;
import com.lsm.repository.AnnouncementReadStatusRepository;
import com.lsm.repository.AnnouncementRepository;
import com.lsm.repository.ClassEntityRepository;
import com.lsm.exception.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnnouncementService {
    private final AnnouncementRepository announcementRepository;
    private final ClassEntityRepository classEntityRepository;
    private final AppUserService appUserService;
    private final AnnouncementReadStatusRepository readStatusRepository;

    @Transactional
    public AnnouncementDTO createAnnouncement(AppUser loggedInUser, AnnouncementDTO dto)
            throws AccessDeniedException, ResourceNotFoundException {
        AppUser user = appUserService.getCurrentUserWithDetails(loggedInUser.getId());

        Set<ClassEntity> classes = new HashSet<>();
        for (Long classId : dto.getClassIds()) {
            ClassEntity classEntity = classEntityRepository.findById(classId)
                    .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));

            if (user.getRole().equals(Role.ROLE_STUDENT)) {
                throw new AccessDeniedException("Students can't create announcements");
            }

            if (user.getRole().equals(Role.ROLE_TEACHER)) {
                boolean hasAccess = user.getTeacherDetails().getTeacherCourses().stream()
                        .anyMatch(tc -> tc.getClasses().stream()
                                .anyMatch(c -> c.getId().equals(classId)));
                if (!hasAccess) {
                    throw new AccessDeniedException("Teachers can't create announcements for classes they don't teach");
                }
            }

            classes.add(classEntity);
        }

        Announcement announcement = new Announcement();
        announcement.setTitle(dto.getTitle());
        announcement.setContent(dto.getContent());
        announcement.setClasses(classes);
        announcement.setCreatedBy(user);

        announcement = announcementRepository.save(announcement);
        return convertToDTO(announcement, user);
    }

    @Transactional
    public List<AnnouncementDTO> getAnnouncementsByClassId(AppUser loggedInUser, Long classId)
            throws AccessDeniedException {
        AppUser user = appUserService.getCurrentUserWithDetails(loggedInUser.getId());

        if (user.getRole().equals(Role.ROLE_STUDENT) &&
                !user.getStudentDetails().getClassEntity().getId().equals(classId)) {
            throw new AccessDeniedException("Students can't get announcements of other classes");
        }

        if (user.getRole().equals(Role.ROLE_TEACHER)) {
            boolean hasAccess = user.getTeacherDetails().getTeacherCourses().stream()
                    .anyMatch(tc -> tc.getClasses().stream()
                            .anyMatch(c -> c.getId().equals(classId)));
            if (!hasAccess) {
                throw new AccessDeniedException("Teachers can't get announcements of classes they don't teach");
            }
        }

        return announcementRepository.findByClassesId(classId).stream()
                .map(announcement -> convertToDTO(announcement, loggedInUser))
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

        if (user.getRole().equals(Role.ROLE_TEACHER)) {
            boolean hasAccess = announcement.getClasses().stream()
                    .allMatch(announcementClass -> user.getTeacherDetails().getTeacherCourses().stream()
                            .anyMatch(tc -> tc.getClasses().stream()
                                    .anyMatch(c -> c.getId().equals(announcementClass.getId()))));

            if (!hasAccess) {
                throw new AccessDeniedException("Teachers can't delete announcements of classes they don't teach");
            }
        }

        if (user.getRole().equals(Role.ROLE_TEACHER) &&
                (announcement.getCreatedBy().getRole().equals(Role.ROLE_ADMIN)
                || announcement.getCreatedBy().getRole().equals(Role.ROLE_COORDINATOR))) {
            throw new AccessDeniedException("Teachers can't delete announcements which is created by admin or coordinator.");
        }

        if (user.getRole().equals(Role.ROLE_COORDINATOR) && announcement.getCreatedBy().getRole().equals(Role.ROLE_ADMIN))
            throw new AccessDeniedException("Coordinator can't delete announcements which is created by admin.");

        // First delete all read status records for this announcement
        readStatusRepository.deleteByAnnouncementId(id);

        // Then delete the announcement
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

        if (user.getRole().equals(Role.ROLE_TEACHER)) {
            // Check access to current classes
            boolean hasAccessToCurrentClasses = existingAnnouncement.getClasses().stream()
                    .allMatch(announcementClass -> user.getTeacherDetails().getTeacherCourses().stream()
                            .anyMatch(tc -> tc.getClasses().stream()
                                    .anyMatch(c -> c.getId().equals(announcementClass.getId()))));

            if (!hasAccessToCurrentClasses) {
                throw new AccessDeniedException("Teachers can't update announcements of classes they don't teach");
            }

            // Check access to new classes
            if (announcementDTO.getClassIds() != null) {
                boolean hasAccessToNewClasses = announcementDTO.getClassIds().stream()
                        .allMatch(classId -> user.getTeacherDetails().getTeacherCourses().stream()
                                .anyMatch(tc -> tc.getClasses().stream()
                                        .anyMatch(c -> c.getId().equals(classId))));

                if (!hasAccessToNewClasses) {
                    throw new AccessDeniedException("Teachers can't update announcements to include classes they don't teach");
                }
            }
        }

        // If the classes are being changed, we should delete the read statuses
        // as the announcement might be visible to a different set of users
        if (announcementDTO.getClassIds() != null && !announcementDTO.getClassIds().isEmpty() &&
                !existingAnnouncement.getClasses().stream()
                        .map(ClassEntity::getId)
                        .collect(Collectors.toSet())
                        .equals(new HashSet<>(announcementDTO.getClassIds()))) {

            readStatusRepository.deleteByAnnouncementId(id);

            Set<ClassEntity> newClasses = announcementDTO.getClassIds().stream()
                    .map(classId -> classEntityRepository.findById(classId)
                            .orElseThrow(() -> new EntityNotFoundException("Class not found with id: " + classId)))
                    .collect(Collectors.toSet());
            existingAnnouncement.setClasses(newClasses);
        }

        existingAnnouncement.setTitle(announcementDTO.getTitle());
        existingAnnouncement.setContent(announcementDTO.getContent());

        return convertToDTO(announcementRepository.save(existingAnnouncement), user);
    }

    @Transactional
    public AnnouncementDTO getAnnouncementById(AppUser loggedInUser, Long announcementId)
            throws AccessDeniedException, ResourceNotFoundException {
        AppUser user = appUserService.getCurrentUserWithDetails(loggedInUser.getId());
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found"));

        if (user.getRole().equals(Role.ROLE_STUDENT)) {
            boolean hasAccess = announcement.getClasses().stream()
                    .anyMatch(classEntity -> classEntity.getId().equals(user.getStudentDetails().getClassEntity().getId()));
            if (!hasAccess) {
                throw new AccessDeniedException("Students can't access announcements of other classes");
            }
        }

        if (user.getRole().equals(Role.ROLE_TEACHER)) {
            boolean hasAccess = announcement.getClasses().stream()
                    .anyMatch(announcementClass -> user.getTeacherDetails().getTeacherCourses().stream()
                            .anyMatch(tc -> tc.getClasses().stream()
                                    .anyMatch(c -> c.getId().equals(announcementClass.getId()))));
            if (!hasAccess) {
                throw new AccessDeniedException("Teachers can't access announcements of classes they don't teach");
            }
        }

        return convertToDTO(announcement, user);
    }

    @Transactional
    public void markAsRead(AppUser user, Long announcementId) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found"));

        AnnouncementReadStatus readStatus = readStatusRepository
                .findByAnnouncementIdAndUserId(announcementId, user.getId())
                .orElseGet(() -> {
                    AnnouncementReadStatus newStatus = new AnnouncementReadStatus();
                    newStatus.setAnnouncement(announcement);
                    newStatus.setUser(user);
                    return newStatus;
                });

        readStatus.setReadAt(LocalDateTime.now());
        readStatusRepository.save(readStatus);
    }

    @Transactional
    public void markAsUnread(AppUser user, Long announcementId) {
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found"));

        // Delete the read status if it exists
        readStatusRepository.deleteByAnnouncementIdAndUserId(announcementId, user.getId());
    }

    @Transactional
    public List<AnnouncementDTO> getAnnouncementsByUser(AppUser loggedInUser) throws AccessDeniedException {
        List<Announcement> announcements;
        AppUser user = appUserService.getCurrentUserWithDetails(loggedInUser.getId());

        if (user.getRole().equals(Role.ROLE_STUDENT)) {
            // For students, get announcements from their class
            Long classId = user.getStudentDetails().getClassEntity().getId();
            announcements = announcementRepository.findByClassesId(classId);
        } else if (user.getRole().equals(Role.ROLE_TEACHER)) {
            // For teachers, get announcements from their classes
            Set<Long> classIds = user.getTeacherDetails().getTeacherCourses().stream()
                    .flatMap(tc -> tc.getClasses().stream())
                    .map(ClassEntity::getId)
                    .collect(Collectors.toSet());
            announcements = announcementRepository.findByClassesIdIn(classIds);
        } else {
            // For admin and coordinator, get all announcements
            announcements = announcementRepository.findAll();
        }

        return announcements.stream()
                .map(announcement -> convertToDTO(announcement, user))
                .collect(Collectors.toList());
    }

    @Transactional
    public List<AnnouncementDTO> getAnnouncementsByTheUser(AppUser loggedInUser, Long userId) throws AccessDeniedException {
        List<Announcement> announcements;
        AppUser user = appUserService.getCurrentUserWithDetails(loggedInUser.getId());

        if (user.getRole().equals(Role.ROLE_STUDENT)) {
            // For students, get announcements from their class
            Long classId = user.getStudentDetails().getClassEntity().getId();
            announcements = announcementRepository.findByClassesId(classId);
        } else {
            // For teachers, admins, and coordinators get announcements they created
            announcements = announcementRepository.findByCreatedById(userId);
        }

        return announcements.stream()
                .map(announcement -> convertToDTO(announcement, user))
                .collect(Collectors.toList());
    }

    private AnnouncementDTO convertToDTO(Announcement announcement, AppUser currentUser) {
        Optional<AnnouncementReadStatus> readStatus = readStatusRepository
                .findByAnnouncementIdAndUserId(announcement.getId(), currentUser.getId());

        return AnnouncementDTO.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .content(announcement.getContent())
                .classIds(announcement.getClasses().stream()
                        .map(ClassEntity::getId)
                        .collect(Collectors.toList()))
                .createdAt(announcement.getCreatedAt())
                .isRead(readStatus.isPresent())
                .readAt(readStatus.map(AnnouncementReadStatus::getReadAt).orElse(null))
                .createdById(announcement.getCreatedBy().getId())
                .createdByName(announcement.getCreatedBy().getName() + " " + announcement.getCreatedBy().getSurname())
                .build();
    }
}