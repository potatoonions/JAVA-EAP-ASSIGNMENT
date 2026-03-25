package com.crs.reporting.exception;
 
public class ReportNotFoundException extends RuntimeException {
 
    public ReportNotFoundException(String studentId, String period) {
        super(String.format(
                "No academic records found for student '%s' in period '%s'.",
                studentId, period));
    }
}
 