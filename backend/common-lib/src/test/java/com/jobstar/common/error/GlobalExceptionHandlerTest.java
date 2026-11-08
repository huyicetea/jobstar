package com.jobstar.common.error;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void apiExceptionMapsToItsCodeAndStatus() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/jobs/1");

        var response = handler.handleApi(new ApiException(ErrorCode.NOT_FOUND, "Job not found"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().code()).isEqualTo("NOT_FOUND");
        assertThat(response.getBody().path()).isEqualTo("/api/v1/jobs/1");
    }

    @Test
    void unexpectedExceptionDoesNotLeakMessage() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/jobs");

        var response = handler.handleUnexpected(new IllegalStateException("db password"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().message()).doesNotContain("db password");
    }
}
