package com.jobstar.common.error;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        String correlationId,
        List<String> details) {
}
