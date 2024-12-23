package com.lsm.service;

import com.lsm.model.DTOs.AttendanceDTO;
import com.lsm.model.DTOs.AttendanceRequestDTO;
import com.lsm.model.DTOs.AttendanceStatsDTO;
import com.lsm.model.entity.Attendance;
import com.lsm.model.entity.ClassEntity;
import com.lsm.model.entity.Course;
import com.lsm.model.entity.base.AppUser;
import com.lsm.model.entity.enums.AttendanceStatus;
import com.lsm.model.entity.enums.Role;
import com.lsm.repository.AttendanceRepository;
import com.lsm.repository.AppUserRepository;
import com.lsm.repository.ClassEntityRepository;
import com.lsm.repository.CourseRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final AppUserRepository appUserRepository;
    private final CourseRepository courseRepository;
    private final ClassEntityRepository classEntityRepository;
    private final AppUserService appUserService;

    public AttendanceService(AttendanceRepository attendanceRepository, AppUserRepository appUserRepository, CourseRepository courseRepository, ClassEntityRepository classRepository, AppUserService appUserService) {
        this.attendanceRepository = attendanceRepository;
        this.appUserRepository = appUserRepository;
        this.courseRepository = courseRepository;
        this.classEntityRepository = classRepository;
        this.appUserService = appUserService;
    }

    @Transactional
    public Attendance markAttendance(AppUser loggedInUser, AttendanceRequestDTO attendanceRequest) throws AccessDeniedException {
        if (loggedInUser.getRole().equals(Role.ROLE_STUDENT))
            throw new AccessDeniedException("Students can't mark attendance");
        if (loggedInUser .getRole().equals(Role.ROLE_TEACHER)) {
            AppUser user = appUserService.getCurrentUserWithDetails(loggedInUser.getId());
            boolean found = user.getTeacherDetails().getClasses().stream()
                    .flatMap(classEntity -> classEntity.getCourses().stream())
                    .anyMatch(courseEntity -> courseEntity.getId().equals(attendanceRequest.getCourseId()));

            if (!found) {
                throw new AccessDeniedException("Teachers can only mark attendance to their courses");
            }
        }
        AppUser student = appUserRepository.findById(attendanceRequest.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Student with ID %d not found.", attendanceRequest.getStudentId())
                ));

        Attendance attendance = mapToAttendanceEntity(attendanceRequest, student);
        return attendanceRepository.save(attendance);
    }

    @Transactional
    public void markBulkAttendance(AppUser  loggedInUser , List<AttendanceRequestDTO> attendanceRequests)
            throws AccessDeniedException {
        // Check if the logged-in user is a student
        if (loggedInUser .getRole().equals(Role.ROLE_STUDENT)) {
            throw new AccessDeniedException("Students can't mark attendance");
        }

        // Check if the logged-in user is a teacher and validate their access to the courses
        if (loggedInUser .getRole().equals(Role.ROLE_TEACHER)) {
            AppUser  user = appUserService.getCurrentUserWithDetails(loggedInUser .getId());
            Set<Long> courseIds = user.getTeacherDetails().getClasses().stream()
                    .flatMap(classEntity -> classEntity.getCourses().stream())
                    .map(Course::getId)
                    .collect(Collectors.toSet());

            // Validate that all attendance requests are for courses the teacher is associated with
            for (AttendanceRequestDTO attendanceRequest : attendanceRequests) {
                if (!courseIds.contains(attendanceRequest.getCourseId())) {
                    throw new AccessDeniedException("Teachers can only mark attendance to their courses");
                }
            }
        }

        // Process each attendance request
        for (AttendanceRequestDTO attendanceRequest : attendanceRequests) {
            AppUser  student = appUserRepository.findById(attendanceRequest.getStudentId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            String.format("Student with ID %d not found.", attendanceRequest.getStudentId())
                    ));

            Attendance attendance = mapToAttendanceEntity(attendanceRequest, student);
            attendanceRepository.save(attendance);
        }
    }

    @Transactional
    public List<AttendanceDTO> getAttendanceByStudentId(AppUser loggedInUser , Long studentId, LocalDate startDate, LocalDate endDate)
            throws AccessDeniedException {
        if (Role.ROLE_STUDENT.equals(loggedInUser.getRole()) && !loggedInUser.getId().equals(studentId))
            throw new AccessDeniedException("Students can only access their own attendance records.");

        return attendanceRepository.findByStudentId(studentId)
                .stream()
                .filter(attendance -> startDate == null || endDate == null || !attendance.getDate().isBefore(startDate) && !attendance.getDate().isAfter(endDate))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<AttendanceStatsDTO> getAttendanceStatsByStudent(AppUser  loggedInUser , Long studentId, Long classId)
            throws AccessDeniedException {
        if (Role.ROLE_STUDENT.equals(loggedInUser.getRole()) && !loggedInUser.getId().equals(studentId))
            throw new AccessDeniedException("Students can only access their own attendance records.");

        AppUser  student = appUserRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        ClassEntity classEntity = classEntityRepository.getClassEntityById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Class not found"));

        List<Attendance> attendances = getAttendancesByStudent(studentId, classId);
        return processAttendances(attendances, student, classEntity, null);
    }

    @Transactional
    public List<AttendanceStatsDTO> getAttendanceStatsByCourse(
            AppUser loggedInUser,
            Long courseId,
            Long classId,
            LocalDate startDate,
            LocalDate endDate
    ) throws AccessDeniedException {
        if (loggedInUser.getRole().equals(Role.ROLE_STUDENT)) {
            throw new AccessDeniedException("Students can't view attendance stats of the course.");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        ClassEntity classEntity = null;
        if (classId != null)
            classEntity = classEntityRepository.getClassEntityById(classId)
                    .orElseThrow(() -> new IllegalArgumentException("Class not found"));

        List<Attendance> attendances = getAttendancesByCourse(courseId, classId, startDate, endDate);
        return processAttendances(attendances, null, classEntity, course);
    }

    @Transactional
    public Attendance updateAttendance(AppUser loggedInUser, Long attendanceId, AttendanceRequestDTO attendanceRequest)
            throws AccessDeniedException {
        // Check if the user is a student
        if (loggedInUser.getRole().equals(Role.ROLE_STUDENT)) {
            throw new AccessDeniedException("Students can't update attendance records");
        }

        // Find the existing attendance record
        Attendance existingAttendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Attendance record with ID %d not found.", attendanceId)
                ));

        // If user is a teacher, verify they have access to the course
        if (loggedInUser.getRole().equals(Role.ROLE_TEACHER)) {
            AppUser user = appUserService.getCurrentUserWithDetails(loggedInUser.getId());
            boolean hasAccess = user.getTeacherDetails().getClasses().stream()
                    .flatMap(classEntity -> classEntity.getCourses().stream())
                    .anyMatch(courseEntity -> courseEntity.getId().equals(existingAttendance.getCourseId()));

            if (!hasAccess) {
                throw new AccessDeniedException("Teachers can only update attendance for their courses");
            }
        }

        // Verify the student exists
        AppUser student = appUserRepository.findById(attendanceRequest.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Student with ID %d not found.", attendanceRequest.getStudentId())
                ));

        // Update the attendance record
        existingAttendance.setStudent(student);
        existingAttendance.setDate(attendanceRequest.getDate());
        existingAttendance.setStatus(attendanceRequest.getStatus());
        existingAttendance.setComment(attendanceRequest.getComment());
        existingAttendance.setClassId(attendanceRequest.getClassId());
        existingAttendance.setCourseId(attendanceRequest.getCourseId());

        return attendanceRepository.save(existingAttendance);
    }

    private List<Attendance> getAttendancesByStudent(Long studentId, Long classId) {
        if (classId != null) {
            return attendanceRepository.findByStudentIdAndClassId(studentId, classId);
        } else {
            return attendanceRepository.findByStudentId(studentId);
        }
    }

    private List<Attendance> getAttendancesByCourse(
            Long courseId,
            Long classId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (startDate != null && endDate != null) {
            if (classId != null) {
                return attendanceRepository.findByCourseIdAndClassIdAndDateBetween(
                        courseId, classId, startDate.atStartOfDay().toLocalDate(), endDate.atTime(LocalTime.MAX).toLocalDate());
            } else {
                return attendanceRepository.findByCourseIdAndDateBetween(
                        courseId, startDate.atStartOfDay().toLocalDate(), endDate.atTime(LocalTime.MAX).toLocalDate());
            }
        } else {
            if (classId != null) {
                return attendanceRepository.findByCourseIdAndClassId(courseId, classId);
            } else {
                return attendanceRepository.findByCourseId(courseId);
            }
        }
    }

    private List<AttendanceStatsDTO> processAttendances(List<Attendance> attendances, AppUser student, ClassEntity classEntity, Course course) {
        // Group attendances by either course or student based on the context
        Map<Long, List<Attendance>> groupedAttendances = (student != null)
                ? attendances.stream().collect(Collectors.groupingBy(Attendance::getCourseId))
                : attendances.stream().collect(Collectors.groupingBy(attendance -> attendance.getStudent().getId()));

        return groupedAttendances.entrySet().stream()
                .map(entry -> {
                    Long key = entry.getKey();
                    List<Attendance> courseOrStudentAttendances = entry.getValue();

                    // If processing by student, get the student details
                    AppUser currentStudent = student != null ? student : appUserRepository.findById(key)
                            .orElseThrow(() -> new IllegalArgumentException("Student not found"));

                    long totalClasses = courseOrStudentAttendances.size();
                    long presentCount = courseOrStudentAttendances.stream()
                            .filter(a -> AttendanceStatus.PRESENT.equals(a.getStatus()))
                            .count();
                    long absentCount = courseOrStudentAttendances.stream()
                            .filter(a -> AttendanceStatus.ABSENT.equals(a.getStatus()))
                            .count();
                    long lateCount = courseOrStudentAttendances.stream()
                            .filter(a -> AttendanceStatus.EXCUSED.equals(a.getStatus()))
                            .count();

                    double attendancePercentage = totalClasses > 0
                            ? (presentCount + lateCount) * 100.0 / totalClasses
                            : 0;

                    List<AttendanceDTO> recentAttendances = courseOrStudentAttendances.stream()
                            .sorted((a1, a2) -> a2.getDate().compareTo(a1.getDate()))
                            .limit(5)  // Get the 5 most recent attendance records
                            .filter(a -> !a.getStatus().equals(AttendanceStatus.PRESENT))
                            .map(attendance -> new AttendanceDTO(attendance, attendance.getComment()))
                            .toList();

                    if (classEntity != null) {
                        return AttendanceStatsDTO.builder()
                                .studentId(currentStudent.getId())
                                .studentName(currentStudent.getName())
                                .classId(classEntity.getId())
                                .className(classEntity.getName())
                                .courseId(course != null ? course.getId() : key)
                                .courseName(course != null ? course.getName() :
                                        courseRepository.findById(key)
                                                .map(Course::getName)
                                                .orElse("Unknown Course"))
                                .totalClasses(totalClasses)
                                .presentCount(presentCount)
                                .absentCount(absentCount)
                                .lateCount(lateCount)
                                .attendancePercentage(Math.round(attendancePercentage * 100.0) / 100.0)
                                .recentAttendance(recentAttendances)
                                .build();
                    }
                    // Handle class ID resolution safely
                    Long resolvedClassId = null;
                    if (course != null && currentStudent != null) {
                        resolvedClassId = course.getClasses().stream()
                                .filter(cls -> cls.getStudents().stream()
                                        .anyMatch(s -> s.getId().equals(currentStudent.getId())))
                                .map(ClassEntity::getId)
                                .findFirst()
                                .orElse(null);
                    }
                    Optional<ClassEntity> resolvedClass = classEntityRepository.getClassEntityById(resolvedClassId);
                    ClassEntity resolvedClassEntity = resolvedClass.orElse(null);

                    return AttendanceStatsDTO.builder()
                            .studentId(currentStudent.getId())
                            .studentName(currentStudent.getName())
                            .classId(resolvedClassId)
                            .className(resolvedClassEntity != null ? resolvedClassEntity.getName() : null)
                            .courseId(course != null ? course.getId() : key)
                            .courseName(course != null ? course.getName() :
                                    courseRepository.findById(key)
                                            .map(Course::getName)
                                            .orElse("Unknown Course"))
                            .totalClasses(totalClasses)
                            .presentCount(presentCount)
                            .absentCount(absentCount)
                            .lateCount(lateCount)
                            .attendancePercentage(Math.round(attendancePercentage * 100.0) / 100.0)
                            .recentAttendance(recentAttendances)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private Attendance mapToAttendanceEntity(AttendanceRequestDTO request, AppUser student) {
        return Attendance.builder()
                .student(student)
                .date(request.getDate())
                .status(request.getStatus())
                .comment(request.getComment())
                .classId(request.getClassId())
                .courseId(request.getCourseId())
                .build();
    }

    private AttendanceDTO convertToDTO(Attendance attendance) {
        return AttendanceDTO.builder()
                .attendanceId(attendance.getId())
                .studentId(attendance.getStudent().getId())
                .studentName(attendance.getStudent().getName())
                .studentSurname(attendance.getStudent().getSurname())
                .date(attendance.getDate())
                .status(attendance.getStatus())
                .comment(attendance.getComment())
                .classId(attendance.getClassId())
                .courseId(attendance.getCourseId())
                .build();
    }
}
