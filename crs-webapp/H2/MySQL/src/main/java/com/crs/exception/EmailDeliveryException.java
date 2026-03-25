package com.crs.email.exception;

public class EmailDeliveryException extends RuntimeException {
 
    private final String recipientEmail;
 
    public EmailDeliveryException(String recipientEmail, String message) {
        super(message);
        this.recipientEmail = recipientEmail;
    }
 
    public EmailDeliveryException(String recipientEmail, String message, Throwable cause) {
        super(message, cause);
        this.recipientEmail = recipientEmail;
    }
 
    public String getRecipientEmail() { return recipientEmail; }
}
