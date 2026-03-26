package com.crs.exception;

public class StudentNotFoundException extends RuntimeException {
    private final String studentId;
    public StudentNotFoundException(String studentId) {
        super("No student found with ID: " + studentId);
        this.studentId = studentId;
    }
    public String getStudentId() { return studentId; }
}
