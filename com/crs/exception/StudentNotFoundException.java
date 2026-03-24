package com.crs.exception;

public class StudentNotFoundException extends RuntimeException {
    
    private final int studentId;

    public StudentNotFoundException(int studentId) {
        super("No student found with ID: " + studentId);
        this.studentId = studentId;
    }

    public int getStudentId() {
        return studentId;
    }
}