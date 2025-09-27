package com.multillm.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;

    public void send(String to, String subject, String body){
        log.debug("Sending email to {} with subject {}", to, subject);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("shawon.sarowar@naztech.us.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
        log.trace("Email dispatched to {}", to);
    }
}
