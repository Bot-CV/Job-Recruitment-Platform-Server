package org.toanehihi.botcv.infrastructure.persistence.mappers.candidate;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.toanehihi.botcv.domain.exception.AppException;
import org.toanehihi.botcv.domain.exception.ErrorCode;
import org.toanehihi.botcv.domain.model.Candidate;
import org.toanehihi.botcv.domain.model.enums.ResourceType;
import org.toanehihi.botcv.infrastructure.persistence.mappers.location.LocationMapper;
import org.toanehihi.botcv.infrastructure.persistence.mappers.resource.ResourceMapper;
import org.toanehihi.botcv.infrastructure.persistence.mappers.skill.CandidateSkillMapper;
import org.toanehihi.botcv.infrastructure.persistence.repositories.ResourceRepository;
import org.toanehihi.botcv.interfaces.web.dtos.candidate.CandidateRequest;
import org.toanehihi.botcv.interfaces.web.dtos.candidate.CandidateResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CandidateMapper {
    private final LocationMapper locationMapper;
    private final CandidateSkillMapper candidateSkillMapper;
    private final ResourceRepository resourceRepository;
    private final ResourceMapper resourceMapper;

    public void updateCandidate(Candidate candidate, CandidateRequest request) {
        candidate.setFullName(request.getFullName());
        candidate.setPhone(request.getPhone());
        candidate.setSeniority(request.getSeniority());
        candidate.setSalaryExpectMin(request.getSalaryExpectMin());
        candidate.setSalaryExpectMax(request.getSalaryExpectMax());
        candidate.setCurrency(request.getCurrency());
        candidate.setRemotePref(request.getRemotePref());
        candidate.setRelocationPref(request.getRelocationPref());
        candidate.setBio(request.getBio());
    }

    public CandidateResponse toResponse(Candidate candidate) {
        return CandidateResponse.builder()
                .id(candidate.getId())
                .accountId(candidate.getAccount().getId())
                .fullName(candidate.getFullName())
                .phone(candidate.getPhone())
                .location(candidate.getLocation() != null ? locationMapper.toResponse(candidate.getLocation()) : null)
                .seniority(candidate.getSeniority())
                .salaryExpectMin(candidate.getSalaryExpectMin())
                .salaryExpectMax(candidate.getSalaryExpectMax())
                .currency(candidate.getCurrency())
                .remotePref(candidate.getRemotePref())
                .relocationPref(candidate.getRelocationPref())
                .email(candidate.getAccount().getEmail())
                .resource(resourceMapper.toResponse(resourceRepository.findByIdAndResourceType(candidate.getAvatarResourceId(), ResourceType.AVATAR)
                                .orElseThrow(() -> new AppException(ErrorCode.AVATAR_RESOURCE_NOT_FOUND))))
                .bio(candidate.getBio())
                .dateCreated(candidate.getDateCreated())
                .dateUpdated(candidate.getDateUpdated())
                .skills(candidate.getSkills().stream().map(candidateSkillMapper::toResponse)
                        .collect(Collectors.toSet()))
                .build();
    }
}
