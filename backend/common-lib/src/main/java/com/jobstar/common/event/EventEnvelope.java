package com.jobstar.common.event;

import com.jobstar.common.error.GlobalExceptionHandler;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.MDC;

public record EventEnvelope<T>(
        UUID eventId,
        String type,
        int version,
        Instant occurredAt,
        String producer,
        String correlationId,
        T data) {

    public static <T> EventEnvelope<T> of(String type, int version, String producer, T data) {
        return new EventEnvelope<>(
                UUID.randomUUID(),
                type,
                version,
                Instant.now(),
                producer,
                MDC.get(GlobalExceptionHandler.CORRELATION_ID_KEY),
                data);
    }
}
