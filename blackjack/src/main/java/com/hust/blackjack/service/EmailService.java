package com.hust.blackjack.service;

import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Component
public class EmailService {
    private final JavaMailSenderImpl emailSender;

    public EmailService(JavaMailSenderImpl emailSender) {
        this.emailSender = emailSender;
    }

    public void sendEmail(String to, String subject, String text) {
        MimeMessage message = emailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(
                    "<html><body>" +
                            "<h1>" + "Token Confirmation" + "</h1>" +
                            "<p>Hi there, </p>" +
                            "<p>" + "You're receiving this e-mail because you requested transaction (either add or withdraw credits) for your user account at ICT BlackJack Platform." + "</p>" +
                            "<p>" + "Note that the token information is secret and you should not share it with anyone else.</p>" +
                            "<p>Copy paste this token to continue your transaction: <b>" + text + "</b></p>" +
                            "<p style=\"color:#808080\"> @2021, ICTBLACKJACK, All rights reserved.</p>" +
                            "<p style=\"color:#808080\">Our mailing address is: " +
                                "<a href=\"mailto:ictblackjack@gmail.com\" target=\"_blank\">ictblackjack@gmail.com</a>" +
                            "</p></body></html>"
                    , true
            );
            emailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
