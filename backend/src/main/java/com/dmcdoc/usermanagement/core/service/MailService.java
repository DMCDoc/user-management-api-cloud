package com.dmcdoc.usermanagement.core.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;

    public void sendMail(String to, String subject, String text) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);
            mailSender.send(message);
            log.info("[MailService] Email envoyé à {}", to);
        } catch (MessagingException e) {
            log.error("[MailService] Erreur d’envoi : {}", e.getMessage());
            throw new RuntimeException("Erreur d’envoi de mail", e);
        }
    }

    public void sendMagicLink(String to, String link) {
        String subject = "Connexion par lien magique";
        String body = "Cliquez ici pour vous connecter : " + link;
        sendMail(to, subject, body);
    }

}
