package org.levell.demo.app;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

/**
 * If you add a @Bean annotated with @Endpoint, any methods
 * annotated with @ReadOperation, @WriteOperation, or @DeleteOperation
 * are automatically exposed
 */
@Component
@Endpoint(id = "data")
public class CustomActuatorEndpoint {

    @ReadOperation
    public Pair<String, String> getData() {
        return Pair.of("theKey", "theValue");
    }
}
