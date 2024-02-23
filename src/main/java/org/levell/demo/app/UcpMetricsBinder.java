package org.levell.demo.app;

import java.util.Collections;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.tomcat.TomcatMetrics;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Manager;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.server.WebServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;

/**
 * Binds {@link UcpMetrics} to the {@link ApplicationStartedEvent}.
 */
public class UcpMetricsBinder implements ApplicationListener<ApplicationStartedEvent>, DisposableBean {

	private final MeterRegistry meterRegistry;

	private volatile UcpMetrics ucpMetrics;

	public UcpMetricsBinder(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}

	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		ApplicationContext applicationContext = event.getApplicationContext();
		this.ucpMetrics = new UcpMetrics();
		this.ucpMetrics.bindTo(this.meterRegistry);
	}

	@Override
	public void destroy() {
		if (this.ucpMetrics != null) {
			this.ucpMetrics.close();
		}
	}
}