package com.crs.service;

import com.crs.exception.StudentNotFoundException;
import com.crs.logic.ReportGenerator;
import com.crs.entity.Entities;
import com.crs.entity.Student;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class ReportService {

    private static final Logger LOGGER = Logger.getLogger(ReportService.class.getName());

    // Dependencies & registry

    private final ReportGenerator generator;
    private final Map<String, Student> studentRegistry = new HashMap<>();

    // Constructors
    public ReportService() {
        this.generator = new ReportGenerator();
    }

    public ReportService(ReportGenerator generator) {
        if (generator == null) throw new IllegalArgumentException("ReportGenerator must not be null.");
        this.generator = generator;
    }

    // Student registry management
    public void registerStudent(Student student) {
        if (student == null) throw new IllegalArgumentException("Student must not be null.");
        studentRegistry.put(student.getStudentId(), student);
        LOGGER.info("[ReportService] Registered student: " + student);
    }

    public Student findStudent(String studentId) {
        Student s = studentRegistry.get(studentId);
        if (s == null) throw new StudentNotFoundException(studentId);
        return s;
    }

    // Core service methods
    public AcademicReport viewReport(String studentId, String semester) {
        validateString(studentId, "Student ID");
        validateString(semester,  "Semester");

        Student student = findStudent(studentId);
        AcademicReport report = generator.generateSemesterReport(student, semester);

        LOGGER.info(String.format(
                "[ReportService] viewReport() called — studentId=%s, semester=%s → %s", studentId, semester, report));

        return report;
    }

    public AcademicReport viewYearlyReport(String studentId, int year) {
        validateString(studentId, "Student ID");

        Student student = findStudent(studentId);
        AcademicReport report = generator.generateYearlyReport(student, year);

        LOGGER.info(String.format(
                "[ReportService] viewYearlyReport() called — studentId=%s, year=%d → %s", studentId, year, report));

        return report;
    }

    public String exportReport(AcademicReport report) {
        if (report == null) throw new IllegalArgumentException("Report must not be null.");

        String timestamp  = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName   = String.format("AcademicReport_%s_%s_%s.txt",
                sanitise(report.getStudentId()),
                sanitise(report.getSemester()),
                timestamp);

        String content = report.displayReportDetails();

        try (FileWriter fw = new FileWriter(fileName)) {
            fw.write(content);
            LOGGER.info("[ReportService] Report exported to file: " + fileName);
        } catch (IOException e) {
            LOGGER.warning("[ReportService] Could not write export file: " + e.getMessage());
            return "[EXPORT FAILED] " + e.getMessage();
        }

        return fileName;
    }

    public String emailReport(AcademicReport report, String email) {
        if (report == null) throw new IllegalArgumentException("Report must not be null.");
        validateString(email, "Email");

        // Email format validation
        if (!email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("Email address format appears invalid: " + email);
        }

        // Email simulation
        String subject = String.format("[CRS] Academic Report — %s (%s)",
                report.getStudentName(), report.getSemester());

        String body = report.displayReportDetails()
                + "\n\n[This is an automated message from the Course Recovery System.]\n";

        LOGGER.info(String.format(
                "[ReportService] *** SIMULATED EMAIL ***%n"
                        + "  To      : %s%n"
                        + "  Subject : %s%n"
                        + "  Body    : (%d chars)%n"
                        + "  Status  : SENT (simulated)",
                email, subject, body.length()));

        System.out.println("[EMAIL SIMULATION]");
        System.out.println("  To      : " + email);
        System.out.println("  Subject : " + subject);
        System.out.println("  Status  : SENT (simulated)");

        return String.format("Report successfully sent to %s [SIMULATED].", email);
    }

    // Private helpers
    private void validateString(String value, String fieldName) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException(fieldName + " must not be null or blank.");
    }

    private String sanitise(String input) {
        return input.replaceAll("[^a-zA-Z0-9\\-]", "_");
    }
}