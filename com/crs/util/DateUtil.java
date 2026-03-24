package com.crs.util;
 
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public final class DateUtil {
    /* Supported formats */
    private static final List<DateTimeFormatter> SUPPORTED_FORMATS = List.of(
        DateTimeFormatter.ISO_LOCAL_DATE,                            // 2024-10-30
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),                   // 30-10-2024
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),                   // 30/10/2024
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),                   // 10/30/2024
        DateTimeFormatter.ofPattern("d MMM yyyy"),                   // 30 Oct 2024
        DateTimeFormatter.ofPattern("d MMMM yyyy"),                  // 30 October 2024
        DateTimeFormatter.ofPattern("yyyy/MM/dd")                    // 2024/10/30
    );

    public static final DateTimeFormatter DEFAULT_FORMAT =
        DateTimeFormatter.ofPattern("dd MMM yyyy");

    public static final DateTimeFormatter DATETIME_FORMAT =
        DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");

    public static final DateTimeFormatter ISO_FORMAT =
        DateTimeFormatter.ISO_LOCAL_DATE;

    private DateUtil() {}

    /* Current date and time utilities */
    public static LocalDate getCurrentDate() {
        return LocalDate.now();
    }

    public static LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }

    public static String getCurrentDateString() {
        return DEFAULT_FORMAT.format(getCurrentDate());
    }

    public static String getCurrentDateTimeString() {
        return DATETIME_FORMAT.format(getCurrentDateTime());
    }

    /* Parsing utilities */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank())
            throw new IllegalArgumentException("Date string must not be null or blank.");
 
        String trimmed = dateStr.trim();
        for (DateTimeFormatter fmt : SUPPORTED_FORMATS) {
            try {
                return LocalDate.parse(trimmed, fmt);
            } catch (DateTimeParseException ignored) {}
        }
 
        throw new IllegalArgumentException(
            "Cannot parse date '" + dateStr + "'. "
            + "Supported formats: yyyy-MM-dd, dd-MM-yyyy, dd/MM/yyyy, "
            + "MM/dd/yyyy, d MMM yyyy, d MMMM yyyy, yyyy/MM/dd.");
    }

    /* Formatting utilities */
    public static String formatDate(LocalDate date) {
        if (date == null) throw new IllegalArgumentException("Date must not be null.");
        return DEFAULT_FORMAT.format(date);
    }

    public static String formatDate(String dateStr) {
        return formatDate(parseDate(dateStr));
    }

    public static String toIsoString(LocalDate date) {
        return ISO_FORMAT.format(date);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) throw new IllegalArgumentException("DateTime must not be null.");
        return DATETIME_FORMAT.format(dateTime);
    }

    /* Comparison and duration */
    public static boolean isPastDeadline(String deadline) {
        return parseDate(deadline).isBefore(getCurrentDate());
    }

    public static boolean isPastDeadline(LocalDate deadline) {
        if (deadline == null) throw new IllegalArgumentException("Deadline must not be null.");
        return deadline.isBefore(getCurrentDate());
    }

    public static long daysBetween(String date1, String date2) {
        return ChronoUnit.DAYS.between(parseDate(date1), parseDate(date2));
    }

    public static long daysBetween(LocalDate from, LocalDate to) {
        if (from == null || to == null)
            throw new IllegalArgumentException("Both dates must not be null.");
        return ChronoUnit.DAYS.between(from, to);
    }

    public static long daysUntilDeadline(String deadline) {
        return ChronoUnit.DAYS.between(getCurrentDate(), parseDate(deadline));
    }

    /* Milestone/recovery plan helpers */
    public static String deadlineStatus(String deadlineStr) {
        LocalDate deadline = parseDate(deadlineStr);
        long days = daysUntilDeadline(deadlineStr);
        String formatted = formatDate(deadline);
 
        if (days > 0) return String.format("Due in %d day(s) — %s", days, formatted);
        if (days == 0) return "Due today — " + formatted;
        return String.format("OVERDUE by %d day(s) — %s", Math.abs(days), formatted);
    }

    public static String overdueMessage(String milestoneName, String deadlineStr) {
        if (!isPastDeadline(deadlineStr)) return null;
        long days = Math.abs(daysUntilDeadline(deadlineStr));
        return String.format("OVERDUE: '%s' was due %s (%d day(s) ago).",
            milestoneName, formatDate(deadlineStr), days);
    }

    public static boolean isWithinReminderWindow(String deadlineStr, int warningDays) {
        long remaining = daysUntilDeadline(deadlineStr);
        return remaining >= 0 && remaining <= warningDays;
    }

    public static List<String> listOverdueMilestones(List<String> milestones) {
        List<String> overdue = new ArrayList<>();
        if (milestones == null) return overdue;
        for (String entry : milestones) {
            String[] parts = entry.split("\\|", 2);
            if (parts.length != 2) continue;
            String msg = overdueMessage(parts[0].trim(), parts[1].trim());
            if (msg != null) overdue.add(msg);
        }
        return overdue;
    }

    /* Academic calendar helpers */
    public static String academicYear(LocalDate date) {
        if (date == null) throw new IllegalArgumentException("Date must not be null.");
        int year = date.getMonthValue() >= 7
            ? date.getYear()
            : date.getYear() - 1;
        return year + "/" + (year + 1);
    }

    public static String currentAcademicYear() {
        return academicYear(getCurrentDate());
    }

    public static String currentSemesterLabel() {
        LocalDate today   = getCurrentDate();
        String    acadYear = academicYear(today);
        String    sem     = today.getMonthValue() >= 7 ? "SEM1" : "SEM2";
        return sem + " " + acadYear;
    }
}