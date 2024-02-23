package org.levell.demo.app;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.Search;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class TomcatHealthIndicator implements HealthIndicator {

    final MeterRegistry meterRegistry;

    public TomcatHealthIndicator(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Health health() {

        Map<String, Object> details = Map.of(
                "busyThreads", metric("tomcat.threads.busy")
        );

        return Health.up().withDetails(details).build();
    }

    public double metric(String metricName) {
        Search producerCountSearch = meterRegistry.find(metricName);
        return Optional.ofNullable(producerCountSearch.gauge())
                .map(Gauge::value)
                .orElse(0d);
    }
}