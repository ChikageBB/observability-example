package ru.kpfu.itis.eventconsumer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.kpfu.itis.eventconsumer.model.EventEntity;

public interface EventRepository extends JpaRepository<EventEntity, String> {
}
