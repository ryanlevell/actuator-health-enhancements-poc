package org.levell.demo.app;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@Configuration
public class MyController {

    final RestTemplate restTemplate;
    final StreamBridge streamBridge;

    public MyController(RestTemplate restTemplate, StreamBridge streamBridge) {
        this.restTemplate = restTemplate;
        this.streamBridge = streamBridge;
    }

    @GetMapping("/produce")
    public ResponseEntity<String> produce() {
        boolean sent = streamBridge.send("output-out-0", "hello");
        return ResponseEntity.ok("produced message: " + sent);
    }

    @GetMapping("/request")
    public ResponseEntity<String> request() {
        return restTemplate.getForEntity("https://dummy.restapiexample.com/api/v1/employee/{id}", String.class, 1);
    }
}
