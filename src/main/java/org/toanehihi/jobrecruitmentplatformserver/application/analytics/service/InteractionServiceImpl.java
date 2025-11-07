package org.toanehihi.jobrecruitmentplatformserver.application.analytics.service;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.toanehihi.jobrecruitmentplatformserver.domain.exception.AppException;
import org.toanehihi.jobrecruitmentplatformserver.domain.exception.ErrorCode;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Account;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.UserInteraction;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.AccountRepository;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.InteractionRepository;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.security.CurrentAccountProvider;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.interaction.InteractionRequest;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InteractionServiceImpl implements InteractionService {
    private final AccountRepository accountRepository;
    private final InteractionRepository interactionRepository;
    private final CurrentAccountProvider currentAccountProvider;

    @Override
    public void trackQuery(List<InteractionRequest> request) {
        Long accountId = currentAccountProvider.getCurrentAccountId();

        if (!accountRepository.existsById(accountId)) {
            throw new AppException(ErrorCode.ACCOUNT_NOT_FOUND);
        }

        List<UserInteraction> interactions = request.stream().map(r -> UserInteraction.builder()
                .accountId(accountId)
                .jobId(r.getJobId())
                .eventType(r.getEventType())
                .metadata(r.getMetadata())
                .occurredAt(r.getOccurredAt())
                .build()).toList();
        interactionRepository.saveAll(interactions);
    }
}
