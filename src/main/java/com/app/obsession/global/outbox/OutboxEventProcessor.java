package com.app.obsession.global.outbox;

public interface OutboxEventProcessor {

    boolean supports(String eventType);

    void process(OutboxEvent event);
}
