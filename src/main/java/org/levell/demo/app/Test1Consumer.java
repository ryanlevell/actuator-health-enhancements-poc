package org.levell.demo.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class Test1Consumer {

    Logger LOG = LoggerFactory.getLogger(Test1Consumer.class);

    @Bean
    public Consumer<String> input() {
        return message -> LOG.info(message);
    }
}
