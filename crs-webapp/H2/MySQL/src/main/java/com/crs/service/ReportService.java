package com.crs.service;

import com.crs.exception.StudentNotFoundException;
import com.crs.entity.*;
import com.crs.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReportService {

    private final StudentRepository studentRepository;
    private final CourseResultRepository courseResultRepository;
    private final AcademicReportRepository reportRepository;
    private final EmailRecordService emailRecordService;

    // Read operations
    @Transactional
    public AcademicReportEntity generateSemesterReport(
            String studentId, String semester) {

        StudentEntity student = findStudent(studentId);
        List<CourseResultEntity> results =
            courseResultRepository.findByStudentUserIdAndSemester(
                studentId, semester);

        if (results.isEmpty())
            throw new IllegalArgumentException(
                "No course results found for student " + studentId
                + " in semester " + semester + ".");

        return buildAndSaveReport(student, results, semester,
            AcademicReportEntity.ReportType.SEMESTER);
    }

    @Transactional
    public AcademicReportEntity generateYearlyReport(
            String studentId, int year) {

        StudentEntity student = findStudent(studentId);
        List<CourseResultEntity> results =
            courseResultRepository.findByStudentUserIdAndAcademicYear(
                studentId, year);

        if (results.isEmpty())
            throw new IllegalArgumentException(
                "No course results found for student " + studentId
                + " in year " + year + ".");

        return buildAndSaveReport(student, results, "YEAR " + year,
            AcademicReportEntity.ReportType.YEARLY);
    }

    public List<AcademicReportEntity> getReportsForStudent(String studentId) {
        return reportRepository
            .findByStudentUserIdOrderByGeneratedAtDesc(studentId);
    }

    // Export & email
    public String exportReport(AcademicReportEntity report) {
        if (report == null)
            throw new IllegalArgumentException("Report must not be null.");

        String content = formatReportText(report);
        log.info("Report exported — student={}, period={}", 
            report.getStudent().getUserId(), report.getPeriod());
        return content;
    }

    @Transactional
    public void emailReport(AcademicReportEntity report, String email) {
        if (report == null)
            throw new IllegalArgumentException("Report must not be null.");
        if (email == null || !email.contains("@"))
            throw new IllegalArgumentException("A valid email address is required.");

        emailRecordService.record(
            email,
            EmailRecordEntity.EmailType.PERFORMANCE_REPORT,
            "[CRS] Your Academic Performance Report — " + report.getPeriod(),
            EmailRecordEntity.DeliveryStatus.SENT);

        log.info("Performance report emailed to {} [SIMULATED]", email);
    }

    // Private helpers
    private StudentEntity findStudent(String studentId) {
        return studentRepository.findById(studentId.toUpperCase())
            .orElseThrow(() -> new StudentNotFoundException(studentId));
    }

    private AcademicReportEntity buildAndSaveReport(
            StudentEntity student,
            List<CourseResultEntity> results,
            String period,
            AcademicReportEntity.ReportType type) {

        double weightedSum = results.stream()
            .mapToDouble(CourseResultEntity::getWeightedPoints).sum();
        int totalCredits = results.stream()
            .mapToInt(CourseResultEntity::getCreditHours).sum();
        double cgpa = totalCredits == 0 ? 0.0 : weightedSum / totalCredits;

        AcademicReportEntity entity = new AcademicReportEntity();
        entity.setStudent(student);
        entity.setReportType(type);
        entity.setPeriod(period);
        entity.setCgpa(BigDecimal.valueOf(cgpa).setScale(4, RoundingMode.HALF_UP));
        entity.setTotalCreditHours(totalCredits);
        entity.setTotalGradePoints(
            BigDecimal.valueOf(weightedSum).setScale(2, RoundingMode.HALF_UP));

        AcademicReportEntity saved = reportRepository.save(entity);
        log.info("Report saved — id={}, student={}, period={}, cgpa={}",
            saved.getId(), student.getUserId(), period, cgpa);
        return saved;
    }

    private String formatReportText(AcademicReportEntity r) {
        String line  = "=".repeat(68);
        String dline = "-".repeat(68);
        return line + "\n" +
            "    COURSE RECOVERY SYSTEM — ACADEMIC REPORT\n" +
            line + "\n" +
            String.format("  Student : %s (%s)%n",
                r.getStudent().getFullName(), r.getStudent().getUserId()) +
            String.format("  Period  : %s%n", r.getPeriod()) +
            String.format("  Type    : %s%n", r.getReportType()) +
            dline + "\n" +
            String.format("  Total Credit Hours : %d%n",  r.getTotalCreditHours()) +
            String.format("  Total Grade Points : %.2f%n",r.getTotalGradePoints()) +
            String.format("  CGPA               : %.4f%n",r.getCgpa()) +
            line + "\n";
    }
}
