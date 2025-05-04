package com.example.asyncapi;


import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/invoke")
@RequiredArgsConstructor
public class ApiController {

    private final AsyncRestFactory asyncRestFactory;
    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    @PostMapping
    public Mono<ResponseEntity<String>> invoke(@RequestBody InvocationRequest request) {
        logger.info("Received request for URL: {}", request.getRequestDTO().getUrl());
        return asyncRestFactory.executeTarget(
                        request.getApiMethod(),
                        request.getRequestDTO(),
                        request.getTimeout(),
                        request.isUseSsl()
                ).map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    logger.error("Invocation failed: {}", e.getMessage(), e);
                    return Mono.just(ResponseEntity.internalServerError().body("Error: " + e.getMessage()));
                });
    }
}