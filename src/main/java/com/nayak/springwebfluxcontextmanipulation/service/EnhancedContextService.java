package com.nayak.springwebfluxcontextmanipulation.service;

import com.nayak.springwebfluxcontextmanipulation.util.ContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.Map;

@Service
@Slf4j
public class EnhancedContextService {

    public Mono<String> processWithEnhancedContext(String userId) {
        return Mono.just("Processing request")
                .transform(ContextUtils.withContextValidation())
                .transform(ContextUtils.withContextEnrichment(Map.of(
                        "userId", userId,
                        "correlationId", "enhanced-" + System.currentTimeMillis()
                )))
                .transform(ContextUtils.withMetrics("enhanced-process"))
                .transform(ContextUtils.withLogging());
    }

    public Mono<String> chainedContextOperations() {
        return Mono.just("Initial")
                .transform(mono -> mono.flatMap(str ->
                        Mono.deferContextual(contextView -> {
                            Context enrichedCtx = ContextUtils.enrichContextWithAttribute(
                                    contextView,
                                    "correlationId",
                                    "CHAIN-" + System.currentTimeMillis()
                            );
                            return Mono.just(str + " | Enriched")
                                    .contextWrite(enrichedCtx);
                        })
                ))
                .transform(ContextUtils.withLogging())
                .transform(ContextUtils.withMetrics("chained-ops"));
    }
}