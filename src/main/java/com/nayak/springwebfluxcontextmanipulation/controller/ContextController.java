package com.nayak.springwebfluxcontextmanipulation.controller;


import com.nayak.springwebfluxcontextmanipulation.model.RequestContext;
import com.nayak.springwebfluxcontextmanipulation.service.ContextAwareService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class ContextController {

    private final ContextAwareService contextAwareService;

    @GetMapping("/context-demo")
    public Mono<String> contextDemo() {
        return Mono.deferContextual(contextView -> {
            RequestContext context = contextView.get("RequestContext");
            log.info("Handling request in controller with context: {}", context);

            return contextAwareService.processWithContext()
                    .flatMap(result -> contextAwareService.enrichContext()
                            .map(enrichResult -> result + " | " + enrichResult));
        });
    }

    @GetMapping("/context-chain")
    public Mono<String> contextChainDemo() {
        return Mono.deferContextual(contextView -> {
            RequestContext context = contextView.get("RequestContext");
            log.info("Starting context chain with initial context: {}", context);

            return Mono.just("Initial")
                    .transformDeferredContextual((mono, ctx) ->
                            mono.map(str -> str + " | RequestId: " +
                                    ctx.get(RequestContext.class).getRequestId()))
                    .flatMap(str ->
                            contextAwareService.enrichContext()
                                    .map(enrichResult -> str + " | " + enrichResult));
        });
    }
}