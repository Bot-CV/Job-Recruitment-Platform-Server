package org.toanehihi.jobrecruitmentplatformserver.application.email.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.OffsetDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.toanehihi.jobrecruitmentplatformserver.domain.exception.AppException;
import org.toanehihi.jobrecruitmentplatformserver.domain.exception.ErrorCode;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Location;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender emailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String sourceEmail;

    private String loadTemplate(String templateName) {
        try {
            ClassPathResource resource = new ClassPathResource("templates/email/" + templateName);
            return Files.readString(resource.getFile().toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to load email template: {}", templateName, e);
            throw new AppException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String recieveEmail, String token) {
        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(sourceEmail);
            helper.setTo(recieveEmail);
            helper.setSubject("Đặt lại mật khẩu");

            String resetUrl = frontendUrl + "/reset-password?token=" + token;

            String htmlContent = loadTemplate("password-reset.html")
                    .replace("{{resetUrl}}", resetUrl);

            helper.setText(htmlContent, true);

            emailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    @Override
    @Async
    public void sendVerificationEmail(String receiveEmail, String token) {
        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(sourceEmail);
            helper.setTo(receiveEmail);
            helper.setSubject("Xác thực tài khoản - Bot-CV");

            String verificationUrl = frontendUrl + "/verify-email?token=" + token;

            String htmlContent = loadTemplate("verification.html")
                    .replace("{{verificationUrl}}", verificationUrl)
                    .replace("{{receiveEmail}}", receiveEmail);

            helper.setText(htmlContent, true);

            emailSender.send(mimeMessage);
            log.info("Verification email sent successfully to: {}", receiveEmail);
        } catch (MessagingException e) {
            log.error("Failed to send verification email to: {}", receiveEmail, e);
            throw new AppException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    @Override
    public void sendInterviewInvitationEmail(Location location, OffsetDateTime scheduledAt, String fullName, String candidateEmail) {
        try{
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(sourceEmail);
            helper.setTo(candidateEmail);
            helper.setSubject("Thư mời phỏng vấn - Bot-CV");

            String htmlContent = loadTemplate("interview-invitation.html")
                    .replace("{{fullName}}", fullName)
                    .replace("{{scheduledAt}}", scheduledAt.toString())
                    .replace("{{streetAddress}}", location.getStreetAddress())
                    .replace("{{ward}}", location.getWard())
                    .replace("{{district}}", location.getDistrict())
                    .replace("{{provinceCity}}", location.getProvinceCity())
                    .replace("{{country}}", location.getCountry());

            helper.setText(htmlContent, true);

            emailSender.send(mimeMessage);
            log.info("Interview invitation email sent successfully to: {}", candidateEmail);
        } catch (MessagingException e) {
            log.error("Failed to send interview invitation email to: {}", candidateEmail, e);
            throw new AppException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

}
