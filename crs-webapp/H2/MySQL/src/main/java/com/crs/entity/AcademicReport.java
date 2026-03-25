package com.crs.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AcademicReport {

    public static class CourseResult {
        private final String courseCode;
        private final String courseTitle;
        private final int creditHours;
        private final String grade;
        private final double gradePoint;

        public CourseResult(String courseCode, String courseTitle, int creditHours, String grade, double gradePoint) {
            this.courseCode = courseCode;
            this.courseTitle = courseTitle;
            this.creditHours = creditHours;
            this.grade = grade;
            this.gradePoint = gradePoint;
        }

        public String getCourseCode() { return courseCode; }
        public String getCourseTitle() { return courseTitle; }
        public int getCreditHours() { return creditHours; }
        public String getGrade() { return grade; }
        public double getGradePoint() { return gradePoint; }

        @Override
        public String toString() {
            return String.format("%-10s %-35s %2d cr  %-4s  %.1f GP",
                courseCode, courseTitle, creditHours, grade, gradePoint);
        }
    }

    private final String studentName;
    private final String studentId;
    private final String program;
    private final String semester;
    private final List<CourseResult> courseList;
    private final double cgpa;
    private final LocalDateTime generatedAt;

    public AcademicReport(String studentName, String studentId, String program, String semester, List<CourseResult> courseList, double cgpa) {
        this.studentName = studentName;
        this.studentId = studentId;
        this.program = program;
        this.semester = semester;
        this.courseList = courseList != null ? courseList : new ArrayList<>();
        this.cgpa = Math.round(cgpa * 10000.0) / 10000.0;
        this.generatedAt = LocalDateTime.now();
    }

    public String getStudentName() { return studentName; }
    public String getStudentId() { return studentId; }
    public String getProgram() { return program; }
    public String getSemester() { return semester; }
    public List<CourseResult> getCourseList() { return courseList; }
    public double getCgpa() { return cgpa; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }

    public String toEmailBody() {
        String line = "=".repeat(68);
        String dline = "-".repeat(68);
        StringBuilder sb = new StringBuilder();
        sb.append(line).append("\n");
        sb.append("  COURSE RECOVERY SYSTEM — ACADEMIC PERFORMANCE REPORT\n");
        sb.append(line).append("\n");
        sb.append(String.format("  Student   : %s (%s)%n", studentName, studentId));
        sb.append(String.format("  Programme : %s%n", program));
        sb.append(String.format("  Period    : %s%n", semester));
        sb.append(String.format("  Generated : %s%n",
            generatedAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))));
        sb.append(dline).append("\n");
        for (CourseResult cr : courseList) sb.append("  ").append(cr).append("\n");
        sb.append(dline).append("\n");
        sb.append(String.format("  CGPA : %.4f%n", cgpa));
        sb.append(line).append("\n");
        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("AcademicReport{student='%s', semester='%s', cgpa=%.4f}",
            studentId, semester, cgpa);
    }
}
