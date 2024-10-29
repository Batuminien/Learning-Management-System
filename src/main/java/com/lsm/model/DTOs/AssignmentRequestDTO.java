package com.lsm.model.DTOs;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AssignmentRequestDTO {
    @NotNull(message = "Teacher ID is required.")
    private Long teacherId;

    @NotNull(message = "Title is required.")
    @Size(min = 3, max = 100, message = "Title should be between 3 and 100 characters.")
    private String title;

    @Size(max = 500, message = "Description should not exceed 500 characters.")
    private String description;

    @Future(message = "Due date should be in the future.")
    private LocalDate dueDate;

    @NotEmpty(message = "Student ID list cannot be empty.")
    private List<Long> studentIdList;


    // Default constructor
    public AssignmentRequestDTO() {
    }

    // Parameterized constructor
    public AssignmentRequestDTO(Long teacherId, String title, String description, LocalDate dueDate, List<Long> studentIdList) {
        this.teacherId = teacherId;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.studentIdList = studentIdList;
    }

    // Getters and Setters
    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public List<Long> getStudentIdList() {
        return studentIdList;
    }

    public void setStudentIdList(List<Long> studentIdList) {
        this.studentIdList = studentIdList;
    }
}

