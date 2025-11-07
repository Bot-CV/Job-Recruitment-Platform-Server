package org.toanehihi.jobrecruitmentplatformserver.interfaces.web.controllers.interaction;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.toanehihi.jobrecruitmentplatformserver.application.analytics.service.InteractionService;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.security.CurrentAccountProvider;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.DataResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.interaction.InteractionRequest;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/interactions")
public class InteractionController {
    private final InteractionService interactionService;

    @PostMapping
    public DataResponse<?> trackInteraction(
            @RequestBody List<InteractionRequest> request
    ) {
        interactionService.trackQuery(request);
        return DataResponse.builder().data("Interaction tracked successfully").build();
    }
}
