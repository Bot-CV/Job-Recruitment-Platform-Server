package org.toanehihi.botcv.interfaces.web.controllers.interaction;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.toanehihi.botcv.application.analytics.service.InteractionService;
import org.toanehihi.botcv.interfaces.web.dtos.DataResponse;
import org.toanehihi.botcv.interfaces.web.dtos.interaction.InteractionEvent;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/interactions")
public class InteractionController {
    private final InteractionService interactionService;

    @PostMapping
    public DataResponse<String> trackInteraction(@RequestBody List<InteractionEvent> request) {
        interactionService.trackQuery(request);
        return DataResponse.<String>builder()
                .data("Interaction events published to stream for processing")
                .build();
    }
}
