package com.example;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailNotification implements Notifiable {
    private final String smtpHost;
    private final int smtpPort;
    private final String username;
    private final String password;
    private final String fromEmail;
    private final boolean useTLS;

    public EmailNotification(String smtpHost, int smtpPort, String username,
                             String password, String fromEmail, boolean useTLS) {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.username = username;
        this.password = password;
        this.fromEmail = fromEmail;
        this.useTLS = useTLS;
    }

    @Override
    public void sendNotification(String recipient, String message) throws NotificationException {
        Properties props = new Properties();

        // Basic email configuration
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");  // Enable STARTTLS
        props.put("mail.smtp.host", this.smtpHost);
        props.put("mail.smtp.port", this.smtpPort);  // Use port 587 for STARTTLS

        // Timeout settings to prevent connection hang
        props.put("mail.smtp.connectiontimeout", "20000");  // Increase timeout
        props.put("mail.smtp.timeout", "20000");
        props.put("mail.smtp.writetimeout", "20000");

        // Debugging (only for testing)
        props.put("mail.debug", "false");  // Turn off debug messages in production

        // Create a session with authentication
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            // Try to send the email
            Message emailMessage = new MimeMessage(session);
            emailMessage.setFrom(new InternetAddress(fromEmail));
            emailMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            emailMessage.setSubject("Healthcare Notification");
            emailMessage.setText(message);

            // Use the transport to send the message
            Transport.send(emailMessage);
            System.out.println("Email sent successfully to " + recipient);  // Log success
        } catch (MessagingException e) {
            // Handle errors and provide clear messages for debugging
            throw new NotificationException("Failed to send email. Check the following:\n"
                + "1. Internet connection\n"
                + "2. Firewall settings\n"
                + "3. Gmail account permissions\n"
                + "Error: " + e.getMessage(), e);
        }
    }
}
