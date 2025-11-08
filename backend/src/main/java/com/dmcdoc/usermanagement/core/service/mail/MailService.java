package com.dmcdoc.usermanagement.core.service.mail;

public interface MailService {
    void sendHtml(String to, String subject, String html);
}
