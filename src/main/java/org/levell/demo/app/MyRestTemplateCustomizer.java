package org.levell.demo.app;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.httpcomponents.hc5.PoolingHttpClientConnectionManagerMetricsBinder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.apache.hc.client5.http.classic.HttpClient;

import java.lang.reflect.Field;

@Configuration
public class MyRestTemplateCustomizer {

    @Bean
    public RestTemplateCustomizer myRestTemplateCustomizer(MeterRegistry meterRegistry) {
        return restTemplate -> {

            if(!(restTemplate.getRequestFactory() instanceof  HttpComponentsClientHttpRequestFactory)) {
                return;
            }

            try {
                 HttpClient httpClient = ((HttpComponentsClientHttpRequestFactory) restTemplate.getRequestFactory())
                        .getHttpClient();

                Field field = httpClient.getClass().getDeclaredField("connManager");

                field.setAccessible(true);
                PoolingHttpClientConnectionManager connectionManager = (PoolingHttpClientConnectionManager) field.get(httpClient);

                new PoolingHttpClientConnectionManagerMetricsBinder(connectionManager, connectionManager.toString())
                        .bindTo(meterRegistry);

            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
