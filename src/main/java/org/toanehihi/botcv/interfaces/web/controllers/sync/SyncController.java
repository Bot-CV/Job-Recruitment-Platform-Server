package org.toanehihi.botcv.interfaces.web.controllers.sync;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.toanehihi.botcv.application.outbox.service.OutboxReconciliationService;

@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
public class SyncController {
    private final OutboxReconciliationService outboxReconciliationService;

    @PostMapping
    public String syncData() {
        outboxReconciliationService.reconcileMissingJobEvents();
        return "Data synchronization initiated.";
    }
}
