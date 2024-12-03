package com.nayak.springwebfluxcontextmanipulation.util;

import com.nayak.springwebfluxcontextmanipulation.model.RequestContext;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

import java.util.Map;
import java.util.function.Function;

@Slf4j
public class ContextUtils {

    public static Mono<RequestContext> getCurrentContext() {
        return Mono.deferContextual(contextView ->
                Mono.just(contextView.get("RequestContext")));
    }

    public static Context enrichContextWithAttribute(ContextView contextView, String key, String value) {
        RequestContext existingContext = contextView.get("RequestContext");

        RequestContext.RequestContextBuilder builder = RequestContext.builder()
                .requestId(existingContext.getRequestId())
                .userId(existingContext.getUserId())
                .userAgent(existingContext.getUserAgent())
                .correlationId(existingContext.getCorrelationId())
                .timestamp(existingContext.getTimestamp());

        switch (key) {
            case "userId" -> builder.userId(value);
            case "userAgent" -> builder.userAgent(value);
            case "correlationId" -> builder.correlationId(value);
            default -> log.warn("Unsupported context attribute: {}", key);
        }

        RequestContext updatedContext = builder.build();
        return Context.of(contextView).put("RequestContext", updatedContext);
    }

    public static <T> Function<Mono<T>, Mono<T>> withLogging() {
        return mono -> mono.doOnEach(signal -> {
            if (signal.isOnNext() || signal.isOnError()) {
                getCurrentContext()
                        .subscribe(context ->
                                log.info("Operation performed with RequestId: {} | UserId: {} | CorrelationId: {}",
                                        context.getRequestId(),
                                        context.getUserId(),
                                        context.getCorrelationId()));
            }
        });
    }

    public static <T> Function<Mono<T>, Mono<T>> withContextValidation() {
        return mono -> mono.filterWhen(data ->
                getCurrentContext()
                        .map(context -> context.getUserId() != null)
                        .defaultIfEmpty(false)
        ).switchIfEmpty(Mono.error(new IllegalStateException("User context not found")));
    }

    public static <T> Function<Mono<T>, Mono<T>> withContextEnrichment(Map<String, String> attributes) {
        return mono -> mono.contextWrite(contextView -> {
            Context updatedContext = Context.of(contextView);
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                updatedContext = enrichContextWithAttribute(updatedContext, entry.getKey(), entry.getValue());
            }
            return updatedContext;
        });
    }

    public static <T> Function<Mono<T>, Mono<T>> withMetrics(String operationName) {
        return mono -> mono.doOnEach(signal -> {
            if (signal.isOnNext()) {
                getCurrentContext()
                        .subscribe(context ->
                                log.info("Metrics - Operation: {} | RequestId: {} | Duration: {} ms",
                                        operationName,
                                        context.getRequestId(),
                                        System.currentTimeMillis() - context.getTimestamp()));
            }
        });
    }
}