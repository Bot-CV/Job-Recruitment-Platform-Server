package org.toanehihi.jobrecruitmentplatformserver.application.email.service;

import org.springframework.lang.Nullable;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Location;
import java.time.OffsetDateTime;

public interface EmailService {
    void sendPasswordResetEmail(String recieveEmail, String token);

    void sendVerificationEmail(String receiveEmail, String token);

    void sendCompanyVerificationResult(String receiveEmail, boolean isApproved, @Nullable String reason);

    void sendInterviewInvitationEmail(Location location, OffsetDateTime scheduledAt, String fullName,
            String candidateEmail);
    void sendInterviewUpdateEmail(Location location, OffsetDateTime oldScheduledAt, OffsetDateTime scheduledAt, String fullName, String candidateEmail);
}
