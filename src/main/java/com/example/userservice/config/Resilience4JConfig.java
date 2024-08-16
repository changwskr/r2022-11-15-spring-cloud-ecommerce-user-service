package com.example.userservice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class Resilience4JConfig {
    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> globalCustomConfiguration() {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(4) // circuitbreaker를 열지 결정하는 failure rate threshold percentage, default=50--> 50% 100번중 50번 오류일때 작동
                .waitDurationInOpenState(Duration.ofMillis(1000)) // circuitbreaker를 open한 상태를 유지하는 지속기간 의미, default=60초
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED) // circuit가 닫힐때 통화결과를 카운트기반 혹은 시간기반으로 할 것인지 결정
                .slidingWindowSize(2)
                .build();

        // supplier sevice에서 몇초동안 응답이 없는 경우 문제상황으로 인지 할지 결정-4초, default=1초
        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(4))
                .build();

        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .timeLimiterConfig(timeLimiterConfig)
                .circuitBreakerConfig(circuitBreakerConfig)
                .build()
        );

    }
}
