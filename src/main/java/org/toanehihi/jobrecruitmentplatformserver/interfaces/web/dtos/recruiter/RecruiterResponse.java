package org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.recruiter;

import java.time.OffsetDateTime;

import org.toanehihi.jobrecruitmentplatformserver.domain.model.Company;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecruiterResponse {
    private Long id;
    private Long accountId;
    private String fullName;
    private String phone;
    private Long avatarResourceId;
    private Company company;
    private OffsetDateTime dateCreated;
    private OffsetDateTime dateUpdated;
}
