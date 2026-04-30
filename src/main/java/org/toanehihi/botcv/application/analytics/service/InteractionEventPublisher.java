package org.toanehihi.botcv.application.analytics.service;

import org.toanehihi.botcv.interfaces.web.dtos.interaction.InteractionEvent;

import java.util.List;

public interface InteractionEventPublisher {
    void publish(List<InteractionEvent> events);
}
