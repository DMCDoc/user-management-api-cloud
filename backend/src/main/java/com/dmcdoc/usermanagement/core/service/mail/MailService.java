package com.dmcdoc.usermanagement.core.service.mail;

import org.springframework.stereotype.Service;

@Service
public interface MailService {
    void sendHtml(String to, String subject, String html);
}
