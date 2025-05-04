package com.example.asyncapi;


import io.netty.handler.ssl.SslContextBuilder;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.*;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.util.retry.Retry;


import javax.net.ssl.SSLException;
import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AsyncRestFactory {

    private WebClient defaultClient;
    private static final Logger logger = LoggerFactory.getLogger(AsyncRestFactory.class);
    private WebClient sslClient;

    private static final ConnectionProvider CONNECTION_PROVIDER = ConnectionProvider.builder("custom")
            .maxConnections(1000)
            .pendingAcquireTimeout(Duration.ofSeconds(30))
            .maxIdleTime(Duration.ofMinutes(10))
            .build();

    @PostConstruct
    public void init() {
        HttpClient httpClient = HttpClient.create(CONNECTION_PROVIDER);

        defaultClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(logRequest())
                .filter(logResponse())
                .build();

        HttpClient sslHttpClient = HttpClient.create(CONNECTION_PROVIDER)
                .secure(spec -> {
                    try {
                        spec.sslContext(SslContextBuilder.forClient().build());
                    } catch (SSLException e) {
                        throw new RuntimeException("Failed to build SSL context", e);
                    }
                });

        sslClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(sslHttpClient))
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }


    public Mono<String> executeTarget(ApiMethod method, RequestDTO dto, int timeoutMs, boolean useSsl) {
        if (dto.getUrl() == null || dto.getUrl().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Host (URL) is not specified"));
        }

        if (!dto.getUrl().startsWith("http://") && !dto.getUrl().startsWith("https://")) {
            return Mono.error(new IllegalArgumentException("Invalid URL format: " + dto.getUrl()));
        }
        WebClient client = useSsl ? sslClient : defaultClient;

        WebClient.RequestBodySpec request = client.method(HttpMethod.valueOf(method.name()))
                .uri(dto.getUrl())
                .headers(httpHeaders -> {
                    if (dto.getHeaderVariables() != null) {
                        httpHeaders.setAll(dto.getHeaderVariables());
                        httpHeaders.add("X-Correlation-ID", UUID.randomUUID().toString());
                    }
                })
                .accept(MediaType.APPLICATION_JSON);

        if (dto.getRequestBody() != null) {
            request.contentType(MediaType.valueOf(dto.getBodyType()));
            request.body(BodyInserters.fromValue(dto.getRequestBody()));
        }

        return request.retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(timeoutMs))
                .retryWhen(Retry.fixedDelay(3, Duration.ofMillis(100))
                        .filter(ex -> ex instanceof IOException || (ex instanceof WebClientResponseException wex && wex.getStatusCode().is5xxServerError())))
                .doOnSubscribe(sub -> logger.info("Dispatching HTTP request to {}", dto.getUrl()))
                .doOnSuccess(response -> logger.info("Received response from {}", dto.getUrl()))
                .onErrorResume(WebClientResponseException.class,
                        ex -> Mono.error(new RuntimeException("Downstream Error: " + ex.getStatusCode() + " - " + ex.getResponseBodyAsString())))
                .onErrorResume(throwable -> Mono.error(new RuntimeException("Invocation error: " + throwable.getMessage(), throwable)));
    }


    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            logger.info("Request: {} {}", clientRequest.method(), clientRequest.url());
            return Mono.just(clientRequest);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            logger.info("Response Status: {}", clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }
}
