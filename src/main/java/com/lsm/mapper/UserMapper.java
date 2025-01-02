package com.lsm.mapper;

import com.lsm.model.DTOs.StudentResponseDTO;
import com.lsm.model.DTOs.TeacherCourseResponseDTO;
import com.lsm.model.DTOs.TeacherResponseDTO;
import com.lsm.model.DTOs.UserResponseDTO;
import com.lsm.model.DTOs.auth.LoginResponseDTO;
import com.lsm.model.DTOs.auth.RegisterResponseDTO;
import com.lsm.model.entity.ClassEntity;
import com.lsm.model.entity.StudentDetails;
import com.lsm.model.entity.TeacherDetails;
import com.lsm.model.entity.base.AppUser;
import com.lsm.model.entity.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserMapper {
    public LoginResponseDTO toLoginResponse(AppUser user, String accessToken, String refreshToken, Long expiresIn) {
        return LoginResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .surname(user.getSurname())
                .email(user.getEmail())
                .role(user.getRole())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expiresIn)
                .issuedAt(Instant.now())
                .build();
    }

    public RegisterResponseDTO toRegisterResponse(AppUser user) {
        return RegisterResponseDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .surname(user.getSurname())
                .email(user.getEmail())
                .role(user.getRole())
                .message("Registration successful")
                .registeredAt(Instant.now())
                .nextSteps(getNextSteps(user))
                .build();
    }

    public UserResponseDTO toUserResponse(AppUser user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getName())
                .lastName(user.getSurname())
                .role(user.getRole())
                .build();
    }

    public StudentResponseDTO toStudentResponse(AppUser user) {
        StudentDetails details = user.getStudentDetails();
        if (details == null) {
            throw new IllegalStateException("Student details not found");
        }

        return StudentResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getName())
                .lastName(user.getSurname())
                .role(user.getRole())
                .phone(details.getPhone())
                .tc(details.getTc())
                .birthDate(details.getBirthDate())
                .registrationDate(details.getRegistrationDate())
                .parentName(details.getParentName())
                .parentPhone(details.getParentPhone())
                .classId(details.getClassEntity().getId())
                .build();
    }

    public TeacherResponseDTO toTeacherResponse(AppUser user) {
        TeacherDetails details = user.getTeacherDetails();
        if (details == null) {
            throw new IllegalStateException("Teacher details not found");
        }

        List<TeacherCourseResponseDTO> teacherCourses = details.getTeacherCourses().stream()
                .map(tc -> TeacherCourseResponseDTO.builder()
                        .teacherId(tc.getId())
                        .courseId(tc.getCourse().getId())
                        .courseName(tc.getCourse().getName())
                        .classIdsAndNames(tc.getClasses().stream()
                                .collect(Collectors.toMap(
                                        ClassEntity::getId,
                                        ClassEntity::getName)))
                        .build())
                .collect(Collectors.toList());

        return TeacherResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getName())
                .lastName(user.getSurname())
                .role(user.getRole())
                .phone(details.getPhone())
                .tc(details.getTc())
                .birthDate(details.getBirthDate())
                .teacherCourses(teacherCourses)
                .build();
    }

    private List<String> getNextSteps(AppUser user) {
        List<String> steps = new ArrayList<>();
        steps.add("Please check your email for verification instructions");

        if (user.getRole() == Role.ROLE_STUDENT) {
            steps.add("Complete your student profile");
            steps.add("Join your class groups");
        } else if (user.getRole() == Role.ROLE_TEACHER) {
            steps.add("Complete your teacher profile");
            steps.add("Create your first class");
        }

        return steps;
    }
}
