package com.nayak.springwebfluxcontextmanipulation.service;

import com.nayak.springwebfluxcontextmanipulation.model.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class ContextAwareService {

    public Mono<String> processWithContext() {
        return Mono.deferContextual(contextView -> {
            RequestContext context = contextView.get("RequestContext");
            log.info("Processing request with ID: {} for user: {}",
                    context.getRequestId(),
                    context.getUserId());

            return Mono.just("Processed request for user: " + context.getUserId());
        });
    }

    public Mono<String> enrichContext() {
        return Mono.deferContextual(contextView -> {
            RequestContext existingContext = contextView.get("RequestContext");

            RequestContext enrichedContext = RequestContext.builder()
                    .requestId(existingContext.getRequestId())
                    .userId(existingContext.getUserId())
                    .userAgent(existingContext.getUserAgent())
                    .correlationId(existingContext.getCorrelationId())
                    .timestamp(System.currentTimeMillis())
                    .build();

            return Mono.just("Context enriched")
                    .contextWrite(context -> context.put("RequestContext", enrichedContext));
        });
    }
}