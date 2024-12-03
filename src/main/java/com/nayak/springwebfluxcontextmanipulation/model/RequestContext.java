package com.nayak.springwebfluxcontextmanipulation.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestContext {
    private String requestId;
    private String userId;
    private String userAgent;
    private String correlationId;
    private Long timestamp;
}