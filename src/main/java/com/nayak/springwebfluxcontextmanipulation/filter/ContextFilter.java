package com.nayak.springwebfluxcontextmanipulation.filter;

import com.nayak.springwebfluxcontextmanipulation.model.RequestContext;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.UUID;

@Component
public class ContextFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestId = UUID.randomUUID().toString();
        String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-ID");
        String userAgent = exchange.getRequest().getHeaders().getFirst("User-Agent");
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-ID");

        RequestContext requestContext = RequestContext.builder()
                .requestId(requestId)
                .correlationId(correlationId)
                .userAgent(userAgent)
                .userId(userId)
                .timestamp(System.currentTimeMillis())
                .build();

        return chain.filter(exchange)
                .contextWrite(Context.of("RequestContext", requestContext))
                .doOnEach(signal -> {
                    if (signal.isOnNext() || signal.isOnError()) {
                        MDC.put("requestId", requestId);
                        MDC.put("correlationId", correlationId);
                        MDC.put("userId", userId);
                    }
                })
                .doFinally(signalType -> MDC.clear());
    }
}