package com.crs.email.config;

public final class EmailConfig {
 
    private EmailConfig() {}

    //Sender identity
    public static final String SENDER_NAME = "CRS – Course Recovery System";
 
    public static final String SENDER_EMAIL = "noreply@crs.university.edu";
 
    public static final String REPLY_TO_EMAIL = "support@crs.university.edu";

    //System URLs
    public static final String BASE_URL = "https://crs.university.edu";

    public static final String PASSWORD_RESET_URL = BASE_URL + "/reset-password?token=";

    public static final String LOGIN_URL = BASE_URL + "/login";

    //Notification behavior
    public static final int DUPLICATE_SUPPRESSION_MINUTES = 60;

    public static final int MAX_REMINDERS_PER_DAY = 3;

    public static final int REMINDER_HOURS_BEFORE_DEADLINE = 48;

    //Simulation mode (for testing/demo purposes)
    public static final boolean SIMULATION_MODE = true;
}
