package org.levell.demo.app;

import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for {@link UcpMetrics}.
 */
@Configuration
@ConditionalOnClass(UcpMetrics.class)
public class UcpMetricsConfiguration {

	@Bean
	@ConditionalOnMissingBean({ UcpMetrics.class, UcpMetricsBinder.class })
	public UcpMetricsBinder ucpMetricsBinder(MeterRegistry meterRegistry) {
		return new UcpMetricsBinder(meterRegistry);
	}
}