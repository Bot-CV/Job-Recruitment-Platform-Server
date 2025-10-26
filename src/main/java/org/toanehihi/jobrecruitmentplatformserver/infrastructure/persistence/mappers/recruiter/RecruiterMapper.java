package org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.mappers.recruiter;

import org.springframework.stereotype.Component;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Recruiter;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.recruiter.RecruiterResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RecruiterMapper {
    public RecruiterResponse toReponse(Recruiter recruiter) {
        return RecruiterResponse.builder()
                .id(recruiter.getId())
                .accountId(recruiter.getAccount().getId())
                .fullName(recruiter.getFullName())
                .phone(recruiter.getPhone())
                .avatarResourceId(recruiter.getAvatarResourceId())
                .company(recruiter.getCompany())
                .dateCreated(recruiter.getDateCreated())
                .dateUpdated(recruiter.getDateUpdated())
                .build();
    }
}
