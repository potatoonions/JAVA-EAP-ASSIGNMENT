package com.crs.email.exception;

public class DuplicateNotificationException extends RuntimeException {
    
     private final String recipientEmail;
    private final String notificationType;
 
    public DuplicateNotificationException(String recipientEmail, String notificationType) {
        super(String.format(
            "Duplicate notification suppressed — type='%s', recipient='%s'.",
                notificationType, recipientEmail));
        this.recipientEmail   = recipientEmail;
        this.notificationType = notificationType;
    }
 
    public String getRecipientEmail()   { return recipientEmail; }
    public String getNotificationType() { return notificationType; }
}
