package com.lsm.service;

import com.lsm.model.DTOs.CourseScheduleDTO;
import com.lsm.model.entity.CourseSchedule;
import com.lsm.model.entity.TeacherCourse;
import com.lsm.model.entity.ClassEntity;
import com.lsm.repository.CourseScheduleRepository;
import com.lsm.repository.TeacherCourseRepository;
import com.lsm.repository.ClassEntityRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CourseScheduleService {

    private final CourseScheduleRepository courseScheduleRepository;
    private final TeacherCourseRepository teacherCourseRepository;
    private final ClassEntityRepository classEntityRepository;

    @Autowired
    public CourseScheduleService(
            CourseScheduleRepository courseScheduleRepository,
            TeacherCourseRepository teacherCourseRepository,
            ClassEntityRepository classEntityRepository) {
        this.courseScheduleRepository = courseScheduleRepository;
        this.teacherCourseRepository = teacherCourseRepository;
        this.classEntityRepository = classEntityRepository;
    }

    @Transactional
    public CourseScheduleDTO createSchedule(CourseScheduleDTO dto) {
        TeacherCourse teacherCourse = teacherCourseRepository.findById(dto.getTeacherCourseId())
                .orElseThrow(() -> new EntityNotFoundException("TeacherCourse not found with ID: " + dto.getTeacherCourseId()));

        ClassEntity classEntity = classEntityRepository.findById(dto.getClassId())
                .orElseThrow(() -> new EntityNotFoundException("Class not found with ID: " + dto.getClassId()));

        CourseSchedule schedule = CourseSchedule.builder()
                .teacherCourse(teacherCourse)
                .classEntity(classEntity)
                .dayOfWeek(dto.getDayOfWeek())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .location(dto.getLocation())
                .build();

        log.info("Creating new schedule for teacher course ID: {} and class ID: {}",
                dto.getTeacherCourseId(), dto.getClassId());
        CourseSchedule saved = courseScheduleRepository.save(schedule);
        return mapToDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<CourseScheduleDTO> getSchedulesByTeacherCourse(Long teacherCourseId) {
        log.info("Fetching schedules for teacher course ID: {}", teacherCourseId);
        if (!teacherCourseRepository.existsById(teacherCourseId)) {
            throw new EntityNotFoundException("TeacherCourse not found with ID: " + teacherCourseId);
        }
        return courseScheduleRepository.findByTeacherCourseId(teacherCourseId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseScheduleDTO> getSchedulesByClass(Long classId) {
        log.info("Fetching schedules for class ID: {}", classId);
        if (!classEntityRepository.existsById(classId)) {
            throw new EntityNotFoundException("Class not found with ID: " + classId);
        }
        return courseScheduleRepository.findByClassEntityId(classId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseScheduleDTO> getSchedulesByTeacher(Long teacherId) {
        log.info("Fetching schedules for teacher ID: {}", teacherId);
        if (!teacherCourseRepository.existsByTeacherId(teacherId)) {
            throw new EntityNotFoundException("No courses found for teacher with ID: " + teacherId);
        }
        return courseScheduleRepository.findByTeacherId(teacherId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseScheduleDTO> getSchedulesByStudent(Long studentId) {
        log.info("Fetching schedules for student ID: {}", studentId);
        return courseScheduleRepository.findByStudentId(studentId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteSchedule(Long scheduleId) {
        log.info("Deleting schedule with ID: {}", scheduleId);
        if (!courseScheduleRepository.existsById(scheduleId)) {
            throw new EntityNotFoundException("Schedule not found with ID: " + scheduleId);
        }
        courseScheduleRepository.deleteById(scheduleId);
    }

    @Transactional(readOnly = true)
    public boolean isTeachersCourse(Long teacherId, Long teacherCourseId) {
        return teacherCourseRepository.existsByIdAndTeacherId(teacherCourseId, teacherId);
    }

    @Transactional(readOnly = true)
    public boolean isTeachersSchedule(Long teacherId, Long scheduleId) {
        return courseScheduleRepository.findById(scheduleId)
                .map(schedule -> schedule.getTeacherCourse().getTeacher().getId().equals(teacherId))
                .orElse(false);
    }

    @Transactional
    public CourseScheduleDTO updateSchedule(Long scheduleId, CourseScheduleDTO dto) {
        log.info("Updating schedule with ID: {}", scheduleId);

        CourseSchedule existingSchedule = courseScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("Schedule not found with ID: " + scheduleId));

        TeacherCourse teacherCourse = teacherCourseRepository.findById(dto.getTeacherCourseId())
                .orElseThrow(() -> new EntityNotFoundException("TeacherCourse not found with ID: " + dto.getTeacherCourseId()));

        ClassEntity classEntity = classEntityRepository.findById(dto.getClassId())
                .orElseThrow(() -> new EntityNotFoundException("Class not found with ID: " + dto.getClassId()));

        existingSchedule.setTeacherCourse(teacherCourse);
        existingSchedule.setClassEntity(classEntity);
        existingSchedule.setDayOfWeek(dto.getDayOfWeek());
        existingSchedule.setStartTime(dto.getStartTime());
        existingSchedule.setEndTime(dto.getEndTime());
        existingSchedule.setLocation(dto.getLocation());

        CourseSchedule updatedSchedule = courseScheduleRepository.save(existingSchedule);
        return mapToDTO(updatedSchedule);
    }

    private CourseScheduleDTO mapToDTO(CourseSchedule schedule) {
        return CourseScheduleDTO.builder()
                .id(schedule.getId())
                .teacherCourseId(schedule.getTeacherCourse().getId())
                .classId(schedule.getClassEntity().getId())
                .dayOfWeek(schedule.getDayOfWeek())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .location(schedule.getLocation())
                .build();
    }
}