package org.toanehihi.botcv.application.email.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.lang.Nullable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.toanehihi.botcv.domain.exception.AppException;
import org.toanehihi.botcv.domain.exception.ErrorCode;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.toanehihi.botcv.domain.model.Location;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender emailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String sourceEmail;

    private static final String HTML_ENCODING = "UTF-8";
    private static final String TIME_ZONE = "Asia/Ho_Chi_Minh";

    private String loadTemplate(String templateName) {
        try {
            ClassPathResource resource = new ClassPathResource("templates/email/" + templateName);
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
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
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, HTML_ENCODING);

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
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, HTML_ENCODING);

            helper.setFrom(sourceEmail);
            helper.setTo(receiveEmail);
            helper.setSubject("Xác thực tài khoản - Bot-CV");

            String verificationUrl = frontendUrl + "/verify-email?token=" + token;

            String htmlContent = loadTemplate("account-verification.html")
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
    @Async
    public void sendCompanyVerificationResult(String receiveEmail, boolean isApproved, @Nullable String reason) {
        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, HTML_ENCODING);

            helper.setFrom(sourceEmail);
            helper.setTo(receiveEmail);

            String subject;
            String htmlContent;

            if (isApproved) {
                subject = "Xác thực công ty - Thành công";
                htmlContent = loadTemplate("company-verification-success.html");
            } else {
                subject = "Xác thực công ty - Thất bại";
                htmlContent = loadTemplate("company-verification-failure.html")
                        .replace("{{reason}}", reason != null ? reason : "Không có lý do cụ thể.");
            }

            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            emailSender.send(mimeMessage);
            log.info("Company verification result email sent to: {}", receiveEmail);
        } catch (MessagingException e) {
            log.error("Failed to send company verification result email to: {}", receiveEmail, e);
            throw new AppException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    public void sendInterviewInvitationEmail(Location location, OffsetDateTime scheduledAt, String fullName,
            String candidateEmail) {
        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, HTML_ENCODING);

            helper.setFrom(sourceEmail);
            helper.setTo(candidateEmail);
            helper.setSubject("Thư mời phỏng vấn - Bot-CV");

            ZonedDateTime hcmTime = scheduledAt.atZoneSameInstant(java.time.ZoneId.of(TIME_ZONE));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                    .withLocale(Locale.forLanguageTag("vi-VN"));

            String htmlContent = loadTemplate("interview.html")
                    .replace("{{fullName}}", fullName)
                    .replace("{{scheduledAt}}", hcmTime.format(formatter))
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

    @Override
    public void sendInterviewUpdateEmail(Location location, OffsetDateTime oldScheduledAt, OffsetDateTime scheduledAt,
            String fullName, String candidateEmail) {
        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, HTML_ENCODING);

            helper.setFrom(sourceEmail);
            helper.setTo(candidateEmail);
            helper.setSubject("Cập nhật lịch phỏng vấn - Bot-CV");

            ZonedDateTime oldHcmTime = oldScheduledAt.atZoneSameInstant(java.time.ZoneId.of(TIME_ZONE));
            ZonedDateTime newHcmTime = scheduledAt.atZoneSameInstant(java.time.ZoneId.of(TIME_ZONE));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                    .withLocale(Locale.forLanguageTag("vi-VN"));
            String htmlContent = loadTemplate("update-interview.html")
                    .replace("{{fullName}}", fullName)
                    .replace("{{oldScheduledAt}}", oldHcmTime.format(formatter))
                    .replace("{{newScheduledAt}}", newHcmTime.format(formatter))
                    .replace("{{streetAddress}}", location.getStreetAddress())
                    .replace("{{ward}}", location.getWard())
                    .replace("{{district}}", location.getDistrict())
                    .replace("{{provinceCity}}", location.getProvinceCity())
                    .replace("{{country}}", location.getCountry());

            helper.setText(htmlContent, true);

            emailSender.send(mimeMessage);
            log.info("Interview update email sent successfully to: {}", candidateEmail);
        } catch (MessagingException e) {
            log.error("Failed to send interview invitation email to: {}", candidateEmail, e);
            throw new AppException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

}
