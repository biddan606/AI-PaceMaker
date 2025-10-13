package app.aipacemaker.backend.config.mail;

public interface EmailSender {
    void sendEmail(String to, String subject, String body);
}
