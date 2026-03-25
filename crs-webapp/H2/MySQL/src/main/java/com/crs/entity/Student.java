package com.crs.model;

import java.util.ArrayList;
import java.util.List;

public class Student {
    private int studentId;
    private String name;
    private int currentLevel;
    private int currentSemester;
    private List<CourseResult> courseResults;
    private EnrollmentStatus enrollmentStatus;
 
    public enum EnrollmentStatus {
        PENDING,
        ENROLLED,
        INELIGIBLE
    }
 
    public Student(int studentId, String name, int currentLevel, int currentSemester) {
        if (studentId <= 0) {
            throw new IllegalArgumentException("Student ID must be a positive integer.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Student name must not be null or blank.");
        }
        this.studentId = studentId;
        this.name = name;
        this.currentLevel = currentLevel;
        this.currentSemester = currentSemester;
        this.courseResults = new ArrayList<>();
        this.enrollmentStatus = EnrollmentStatus.PENDING;
    }
 
    /* Getters */ 
    public int getStudentId() { return studentId; }
    public String getName() { return name; }
    public int getCurrentLevel() { return currentLevel; }
    public int getCurrentSemester() { return currentSemester; }
    public List<CourseResult> getCourseResults() { return courseResults; }
    public EnrollmentStatus getEnrollmentStatus() { return enrollmentStatus; }
 
    /* Setters */
    public void setName(String name) { this.name = name; }
    public void setCurrentLevel(int currentLevel) { this.currentLevel = currentLevel; }
    public void setCurrentSemester(int currentSemester) { this.currentSemester = currentSemester; }
    public void setCourseResults(List<CourseResult> courseResults) { this.courseResults = courseResults; }
    public void setEnrollmentStatus(EnrollmentStatus status) { this.enrollmentStatus = status; }
 
    public void addCourseResult(CourseResult result) {
        if (result == null) {
            throw new IllegalArgumentException("CourseResult must not be null.");
        }
        this.courseResults.add(result);
    }
 
    @Override
    public String toString() {
        return String.format(
                "Student{studentId=%d, name='%s', level=%d, semester=%d, status=%s}", studentId, name, currentLevel, currentSemester, enrollmentStatus);
    }
}
