package com.comunidade.app.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class ResilienceConfig {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig bulkVeiculoConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50.0f)
                .slowCallRateThreshold(50.0f)
                .slowCallDurationThreshold(Duration.ofSeconds(2))
                .permittedNumberOfCallsInHalfOpenState(3)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .build();

        CircuitBreakerConfig salvarVeiculoConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(60.0f)
                .slowCallRateThreshold(60.0f)
                .slowCallDurationThreshold(Duration.ofSeconds(5))
                .permittedNumberOfCallsInHalfOpenState(5)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .waitDurationInOpenState(Duration.ofSeconds(5))
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(bulkVeiculoConfig);
        registry.addConfiguration("salvarVeiculo", salvarVeiculoConfig);

        registry.getEventPublisher()
                .onEntryAdded(event -> logCircuitBreakerStateChange(event));
        
        return registry;
    }

    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig bulkVeiculoRetry = RetryConfig.custom()
                .maxAttempts(3)
                .intervalFunction(io.github.resilience4j.core.IntervalFunction
                        .ofExponentialBackoff(500, 2))
                .retryOnException(e -> !(e instanceof IllegalArgumentException))
                .build();

        RetryConfig salvarVeiculoRetry = RetryConfig.custom()
                .maxAttempts(5)
                .intervalFunction(io.github.resilience4j.core.IntervalFunction
                        .ofExponentialBackoff(300, 1.5))
                .retryOnException(e -> !(e instanceof IllegalArgumentException))
                .build();

        RetryRegistry registry = RetryRegistry.of(bulkVeiculoRetry);
        registry.addConfiguration("salvarVeiculo", salvarVeiculoRetry);

        return registry;
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringRedisSerializer);
        template.setValueSerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        template.setHashValueSerializer(stringRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }

    private void logCircuitBreakerStateChange(EntryAddedEvent<CircuitBreaker> event) {
        CircuitBreaker circuitBreaker = event.getAddedEntry();
        circuitBreaker.getEventPublisher()
                .onStateTransition(stateTransitionEvent ->
                        System.out.println("CircuitBreaker " + circuitBreaker.getName() +
                                " transitioned from " + stateTransitionEvent.getStateTransition().getFromState() +
                                " to " + stateTransitionEvent.getStateTransition().getToState())
                );
    }
}
