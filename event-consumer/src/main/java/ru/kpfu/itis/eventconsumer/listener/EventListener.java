package ru.kpfu.itis.eventconsumer.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.kpfu.itis.avro.Event;
import ru.kpfu.itis.eventconsumer.model.EventEntity;
import ru.kpfu.itis.eventconsumer.repository.EventRepository;

@Component
public class EventListener {

    private static final Logger log = LoggerFactory.getLogger(EventListener.class);

    public final EventRepository eventRepository;

    public EventListener(EventRepository repository) {
        this.eventRepository = repository;
    }

    @KafkaListener(topics = "events", groupId = "event-group")
    public void listen(Event event) {
        EventEntity entity = new EventEntity();
        entity.setUid(event.getUid());
        entity.setSubject(event.getSubject());
        entity.setDescription(event.getDescription());

        eventRepository.save(entity);

        log.info("Saved entity: {}", entity);
    }
}
