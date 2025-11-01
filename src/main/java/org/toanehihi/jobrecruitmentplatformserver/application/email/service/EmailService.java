package org.toanehihi.jobrecruitmentplatformserver.application.email.service;

import org.toanehihi.jobrecruitmentplatformserver.domain.model.Location;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.interview.CreateInterviewRequest;

import java.time.OffsetDateTime;

public interface EmailService {
    void sendPasswordResetEmail(String recieveEmail, String token);
    void sendVerificationEmail(String receiveEmail, String token);

    void sendInterviewInvitationEmail(Location location, OffsetDateTime scheduledAt,String fullName, String candidateEmail);


}
