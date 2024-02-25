package org.levell.demo.app;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@RestController
@Configuration
public class MyController {

    final RestClient restClient;
    final RestTemplate restTemplate;
    final StreamBridge streamBridge;

    public MyController(RestClient restClient, RestTemplate restTemplate, StreamBridge streamBridge) {
        this.restClient = restClient;
        this.restTemplate = restTemplate;
        this.streamBridge = streamBridge;
    }

    @GetMapping("/produce")
    public ResponseEntity<String> produce() {
        boolean sent = streamBridge.send("output-out-0", "hello");
        return ResponseEntity.ok("produced message: " + sent);
    }

    @GetMapping("/request1")
    public String request1() {
        return restClient.get().uri("https://dummy.restapiexample.com/api/v1/employee/{id}", 1).retrieve().body(String.class);
//        return restTemplate.getForEntity("https://dummy.restapiexample.com/api/v1/employee/{id}", String.class, 1);
    }

    @GetMapping("/request2")
    public ResponseEntity<String> request2() {
        return restTemplate.getForEntity("https://dummy.restapiexample.com/api/v1/employee/{id}", String.class, 1);
    }
}
