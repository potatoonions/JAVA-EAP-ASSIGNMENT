package com.crs.template;

public class EmailTemplates {
    private EmailTemplates() {}
 
    // Subject line templates
    public static final String SUBJECT_ACCOUNT_CREATED = "[CRS] Welcome to the Course Recovery System";
    public static final String SUBJECT_ACCOUNT_UPDATED = "[CRS] Your Account Has Been Updated";
    public static final String SUBJECT_ACCOUNT_DEACTIVATED = "[CRS] Your Account Has Been Deactivated";
    public static final String SUBJECT_PASSWORD_RESET = "[CRS] Password Reset Request";
    public static final String SUBJECT_RECOVERY_PLAN = "[CRS] Your Course Recovery Plan Is Ready";
    public static final String SUBJECT_PROGRESS_REMINDER = "[CRS] Recovery Milestone Reminder";
    public static final String SUBJECT_PERFORMANCE_REPORT = "[CRS] Your Academic Performance Report";
 
    // Body templates
    public static final String BODY_ACCOUNT_CREATED =
            "Dear {FULL_NAME},\n\n"
            + "Welcome to the Course Recovery System (CRS).\n\n"
            + "Your account has been successfully created with the following details:\n"
            + "  User ID  : {USER_ID}\n"
            + "  Role     : {ROLE}\n\n"
            + "You may now log in to the student portal to view your recovery plan and progress:\n"
            + "  {LOGIN_URL}\n\n"
            + "If you did not request this account, please contact us immediately at {SUPPORT_EMAIL}.\n\n"
            + "Regards,\n"
            + "{SENDER_NAME}";
 
    public static final String BODY_ACCOUNT_UPDATED =
            "Dear {FULL_NAME},\n\n"
            + "Your CRS account (ID: {USER_ID}) has been updated.\n\n"
            + "If you did not make this change, please contact support immediately at {SUPPORT_EMAIL}.\n\n"
            + "You can verify your account details by logging in at:\n"
            + "  {LOGIN_URL}\n\n"
            + "Regards,\n"
            + "{SENDER_NAME}";
 
    public static final String BODY_ACCOUNT_DEACTIVATED =
            "Dear {FULL_NAME},\n\n"
            + "Your CRS account (ID: {USER_ID}) has been deactivated.\n\n"
            + "If you believe this is an error or require further assistance, please contact:\n"
            + "  {SUPPORT_EMAIL}\n\n"
            + "Regards,\n"
            + "{SENDER_NAME}";
 
    public static final String BODY_PASSWORD_RESET =
            "Dear {FULL_NAME},\n\n"
            + "We received a request to reset the password for your CRS account (ID: {USER_ID}).\n\n"
            + "Click the link below to reset your password. This link is valid for 24 hours:\n\n"
            + "  {RESET_LINK}\n\n"
            + "If you did not request a password reset, you can safely ignore this email.\n"
            + "Your password will NOT be changed unless you click the link above.\n\n"
            + "Need help? Contact us at {SUPPORT_EMAIL}.\n\n"
            + "Regards,\n"
            + "{SENDER_NAME}";
 
    public static final String BODY_RECOVERY_PLAN =
            "Dear {FULL_NAME},\n\n"
            + "Your personalised Course Recovery Plan has been created and is now available in CRS.\n\n"
            + "Recovery Plan Summary\n"
            + "─────────────────────\n"
            + "{PLAN_DETAILS}\n\n"
            + "Please review your plan and complete all milestones before their respective deadlines.\n"
            + "Log in to track your progress:\n"
            + "  {LOGIN_URL}\n\n"
            + "If you have any questions, please contact your academic advisor or reach us at {SUPPORT_EMAIL}.\n\n"
            + "Regards,\n"
            + "{SENDER_NAME}";
 
    public static final String BODY_PROGRESS_REMINDER =
            "Dear {FULL_NAME},\n\n"
            + "This is a friendly reminder that you have outstanding milestones in your CRS "
            + "Recovery Plan.\n\n"
            + "Pending Milestones\n"
            + "──────────────────\n"
            + "{MILESTONES}\n\n"
            + "Please complete these tasks as soon as possible to stay on track with your recovery.\n"
            + "Log in to update your progress:\n"
            + "  {LOGIN_URL}\n\n"
            + "If you need support, contact us at {SUPPORT_EMAIL}.\n\n"
            + "Regards,\n"
            + "{SENDER_NAME}";
 
    public static final String BODY_PERFORMANCE_REPORT =
            "Dear {FULL_NAME},\n\n"
            + "Your academic performance report has been generated and is summarised below.\n\n"
            + "{REPORT_BODY}\n"
            + "For a detailed view, please log in to the CRS portal:\n"
            + "  {LOGIN_URL}\n\n"
            + "If you have questions about your results, please speak with your academic advisor\n"
            + "or contact us at {SUPPORT_EMAIL}.\n\n"
            + "Regards,\n"
            + "{SENDER_NAME}";
}
