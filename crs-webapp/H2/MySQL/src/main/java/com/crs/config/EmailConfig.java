package com.crs.config;
 
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
 
@Component
public class EmailConfig {
 
    //  Static constants
 
    public static final String SENDER_NAME = "CRS – Course Recovery System";
    public static final String REPLY_TO_EMAIL = "support@crs.university.edu";
    public static final int DUPLICATE_SUPPRESSION_MINUTES = 60;
    public static final int MAX_REMINDERS_PER_DAY = 3;
    public static final int REMINDER_HOURS_BEFORE_DEADLINE = 48;
 
    public static String SENDER_EMAIL = "noreply@crs.university.edu";
    public static String BASE_URL = "http://localhost:8080/api";
    public static String LOGIN_URL = "http://localhost:8080/api";
    public static String PASSWORD_RESET_URL = "http://localhost:8080/api/reset?token=";
    public static boolean SIMULATION_MODE = true;
 
    //  Spring injection 
 
    @Value("${crs.email.sender:noreply@crs.university.edu}")
    public void setSenderEmail(String v) { SENDER_EMAIL = v; }
 
    @Value("${crs.email.base-url:http://localhost:8080/api}")
    public void setBaseUrl(String v) { BASE_URL = v; LOGIN_URL = v + "/login"; PASSWORD_RESET_URL = v + "/reset?token="; }
 
    @Value("${crs.email.simulation-mode:true}")
    public void setSimulationMode(boolean v) { SIMULATION_MODE = v; }
}
