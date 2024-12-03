package com.nayak.springwebfluxcontextmanipulation.controller;

import com.nayak.springwebfluxcontextmanipulation.service.EnhancedContextService;
import com.nayak.springwebfluxcontextmanipulation.util.ContextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/enhanced")
@Slf4j
@RequiredArgsConstructor
public class EnhancedContextController {

    private final EnhancedContextService enhancedContextService;

    @GetMapping("/context/{userId}")
    public Mono<String> enhancedContextDemo(@PathVariable String userId) {
        return enhancedContextService.processWithEnhancedContext(userId)
                .transform(ContextUtils.withLogging());
    }

    @PostMapping("/context/enrich")
    public Mono<String> enrichContextDemo(@RequestBody Map<String, String> attributes) {
        return Mono.just("Enriching context")
                .transform(ContextUtils.withContextEnrichment(attributes))
                .transform(ContextUtils.withLogging())
                .transform(ContextUtils.withMetrics("context-enrichment"));
    }

    @GetMapping("/context/chain")
    public Mono<String> chainedOperationsDemo() {
        return enhancedContextService.chainedContextOperations()
                .transform(ContextUtils.withMetrics("chain-demo"));
    }
}