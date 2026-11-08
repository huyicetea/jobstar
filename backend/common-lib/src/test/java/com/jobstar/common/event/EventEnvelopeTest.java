package com.jobstar.common.event;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

class EventEnvelopeTest {

    @Test
    void ofFillsIdTimestampAndCorrelationId() {
        MDC.put("correlationId", "corr-1");

        var envelope = EventEnvelope.of("job.created", 1, "job-service", "payload");

        assertThat(envelope.eventId()).isNotNull();
        assertThat(envelope.occurredAt()).isNotNull();
        assertThat(envelope.correlationId()).isEqualTo("corr-1");
        MDC.clear();
    }
}
