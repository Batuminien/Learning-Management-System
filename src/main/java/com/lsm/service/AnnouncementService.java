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
import java.util.List;
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
        ClassEntity classEntity = classEntityRepository.findById(dto.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found"));

        if (user.getRole().equals(Role.ROLE_STUDENT))
            throw new AccessDeniedException("Students can't create announcement");

        if (user.getRole().equals(Role.ROLE_TEACHER) && user.getTeacherDetails().getClasses()
                .stream()
                .noneMatch(c -> c.getId().equals(dto.getClassId())))
            throw new AccessDeniedException("Teachers can't create announcement to the classes which they don't teach");

        Announcement announcement = new Announcement();
        announcement.setTitle(dto.getTitle());
        announcement.setContent(dto.getContent());
        announcement.setClassEntity(classEntity);
        // announcement.setCreatedAt(LocalDateTime.now());

        announcement = announcementRepository.save(announcement);
        return convertToDTO(announcement);
    }

    @Transactional
    public List<AnnouncementDTO> getAnnouncementsByClassId(AppUser loggedInUser, Long classId)
            throws AccessDeniedException {
        AppUser user = appUserService.getCurrentUserWithDetails(loggedInUser.getId());
        if (user.getRole().equals(Role.ROLE_STUDENT) && !user.getStudentDetails().getClassEntity().equals(classId))
            throw new AccessDeniedException("Students can't get announcements of other classes");
        if (user.getRole().equals(Role.ROLE_TEACHER) && user.getTeacherDetails().getClasses()
                .stream()
                .noneMatch(c -> c.getId().equals(classId)))
            throw new AccessDeniedException("Teachers can't get announcements of the other classes which they don't teach");
        return announcementRepository.findByClassEntityId(classId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteAnnouncement(AppUser loggedInUser, Long id)
            throws AccessDeniedException, ResourceNotFoundException {
        AppUser user = appUserService.getCurrentUserWithDetails(loggedInUser.getId());
        if (user.getRole().equals(Role.ROLE_STUDENT))
            throw new AccessDeniedException("Students can't delete announcements");
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found"));
        if (user.getRole().equals(Role.ROLE_TEACHER) && user.getTeacherDetails().getClasses()
                .stream()
                .noneMatch(c -> c.getId().equals(announcement.getClassEntity().getId())))
            throw new AccessDeniedException("Teachers can't delete announcements of the other classes which they don't teach");
        if (!announcementRepository.existsById(id)) {
            throw new ResourceNotFoundException("Announcement not found");
        }
        announcementRepository.deleteById(id);
    }

    @Transactional
    public AnnouncementDTO updateAnnouncement(AppUser loggedInUser, Long id, AnnouncementDTO announcementDTO)
            throws AccessDeniedException, EntityNotFoundException {
        AppUser user = appUserService.getCurrentUserWithDetails(loggedInUser.getId());
        Announcement existingAnnouncement = announcementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Announcement not found with id: " + id));

        if (user.getRole().equals(Role.ROLE_STUDENT))
            throw new AccessDeniedException("Students can't update announcements");
        if (user.getRole().equals(Role.ROLE_TEACHER) && user.getTeacherDetails().getClasses().stream()
        .noneMatch(c -> c.getId().equals(existingAnnouncement.getClassEntity().getId())))
            throw new AccessDeniedException("Teachers can't update announcements of the other classes");

        // Update fields
        existingAnnouncement.setTitle(announcementDTO.getTitle());
        existingAnnouncement.setContent(announcementDTO.getContent());

        // If class is being changed, verify it exists
        if (announcementDTO.getClassId() != null) {
            ClassEntity classEntity = classEntityRepository.findById(announcementDTO.getClassId())
                    .orElseThrow(() -> new EntityNotFoundException("Class not found with id: " + announcementDTO.getClassId()));
            existingAnnouncement.setClassEntity(classEntity);
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
        if (user.getRole().equals(Role.ROLE_STUDENT) &&
                !user.getStudentDetails().getClassEntity().equals(announcement.getClassEntity().getId())) {
            throw new AccessDeniedException("Students can't access announcements of other classes");
        }
        if (user.getRole().equals(Role.ROLE_TEACHER) &&
                user.getTeacherDetails().getClasses().stream()
                        .noneMatch(c -> c.getId().equals(announcement.getClassEntity().getId()))) {
            throw new AccessDeniedException("Teachers can't access announcements of classes they don't teach");
        }

        return convertToDTO(announcement);
    }

    private AnnouncementDTO convertToDTO(Announcement announcement) {
        AnnouncementDTO dto = new AnnouncementDTO();
        dto.setId(announcement.getId());
        dto.setTitle(announcement.getTitle());
        dto.setContent(announcement.getContent());
        dto.setClassId(announcement.getClassEntity().getId());
        dto.setCreatedAt(announcement.getCreatedAt().toLocalDate());
        return dto;
    }
}