package ru.kpfu.itis.eventpublicapi.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import ru.kpfu.itis.eventpublicapi.dto.Event;

@RestController
@RequestMapping("/api/v1/events")
public class EventRestControllerV1 {

    private static final Logger log = LoggerFactory.getLogger(EventRestControllerV1.class);

    private final RestTemplate restTemplate;

    @Value("${event-consumer.url}")
    private String eventConsumerUrl;

    public EventRestControllerV1(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/{uid}")
    public ResponseEntity<Event> getEvent(
            @PathVariable String uid
    ) {
        MDC.put("uid", uid);
        try {
            log.info("Calling event-consumer");
            String url = eventConsumerUrl + "/internal/api/v1/events/" + uid;

            Event event = restTemplate.getForObject(url, Event.class);

            if (event != null) {
                MDC.put("subject", event.subject());
                MDC.put("description", event.description());
                log.info("Received event from event-consumer");
                return ResponseEntity.ok(event);
            } else {
                log.warn("Event not found in event-consumer");
                return ResponseEntity.notFound().build();
            }
        } finally {
            MDC.clear();
        }
    }
}
