# Observability demo

Учебный стенд микросервисной системы с полным контуром наблюдаемости: метрики, логи и распределённые трассировки. События генерируются в Spring Boot, передаются через Kafka в Avro-формате, сохраняются в PostgreSQL и доступны через REST API.

## Как это работает

```text
event-producer ──Avro──> Kafka ──> event-consumer ──> PostgreSQL
                                              ↑
пользователь ──HTTP──> event-public-api ──HTTP┘

Spring Boot ──metrics──> Prometheus ──> Grafana
Docker logs ───────────> Alloy ───────> Loki
Spring Boot ──OTLP─────> Alloy ───────> Tempo
```

- `event-producer` раз в секунду создаёт событие и публикует его в топик `events`.
- `event-consumer` читает события и сохраняет их в PostgreSQL.
- `event-public-api` отдаёт сохранённое событие по `uid`.
- Schema Registry хранит Avro-схему, Kafka UI позволяет просматривать кластер и сообщения.
- Prometheus собирает метрики приложений и экспортёров.
- Alloy собирает Docker-логи в Loki и отправляет OTLP-трейсы в Tempo.

Подробные C4- и sequence-диаграммы находятся в [docs/architecture](docs/architecture/README.md).

## Стек

- Java 17, Spring Boot, Gradle
- Apache Kafka, ZooKeeper, Confluent Schema Registry, Avro
- PostgreSQL
- Prometheus, Grafana, Loki, Tempo, Grafana Alloy
- OpenTelemetry, Micrometer
- Docker Compose

## Быстрый старт

Понадобятся Docker с Compose v2 и не менее 6–8 ГБ свободной оперативной памяти для контейнеров.

```bash
git clone <repository-url>
cd observability
docker compose up --build -d
```

Первый запуск может занять несколько минут: Compose соберёт три Java-приложения и дождётся готовности инфраструктуры.

Проверить состояние контейнеров и посмотреть логи:

```bash
docker compose ps
docker compose logs -f event-producer event-consumer event-public-api
```

Остановить стенд:

```bash
docker compose down
```

Удалить стенд вместе с сохранёнными данными:

```bash
docker compose down -v
```

## Интерфейсы и порты

| Компонент | Адрес | Назначение |
|---|---|---|
| Public API | <http://localhost:8092> | Внешний REST API |
| Producer Actuator | <http://localhost:8090/actuator> | Health и метрики producer |
| Consumer Actuator | <http://localhost:8091/actuator> | Health и метрики consumer |
| Grafana | <http://localhost:3000> | Визуализация (`admin` / `admin`) |
| Prometheus | <http://localhost:9090> | Метрики и alerts |
| Kafka UI | <http://localhost:8070> | Топики, сообщения и схемы |
| Schema Registry | <http://localhost:8081> | Avro-схемы |
| Alloy | <http://localhost:9080> | Состояние telemetry pipeline |
| Loki | <http://localhost:3100> | API логов |
| Tempo | <http://localhost:3200> | API трассировок |
| PostgreSQL | `localhost:5432` | База `event_db` (`user` / `123`) |

Kafka-брокеры опубликованы на портах `9092`, `9093` и `9094`; OTLP-приёмники Alloy — на `4317` (gRPC) и `4318` (HTTP).

## Проверка сценария

Producer автоматически создаёт события. Откройте Kafka UI, выберите топик `events` и скопируйте `uid` любого сообщения. После сохранения consumer-ом запросите событие через публичный API:

```bash
curl http://localhost:8092/api/v1/events/<uid>
```

Пример ответа:

```json
{
  "uid": "c43d46f2-71d4-43c3-9549-e6468d372bcc",
  "subject": "subject",
  "description": "description"
}
```

Проверить Prometheus targets можно на <http://localhost:9090/targets>. Метрики каждого приложения доступны по `/actuator/prometheus` на его порту.

## Настройка Grafana

Datasources автоматически не создаются. После входа в Grafana добавьте их вручную, используя внутренние Docker-адреса:

| Тип | URL |
|---|---|
| Prometheus | `http://prometheus:9090` |
| Loki | `http://loki:3100` |
| Tempo | `http://tempo:3200` |

Примеры запросов в Explore:

```promql
up
```

```logql
{job="docker"}
```

## Разработка сервисов

Каждый сервис — самостоятельный Gradle-проект с собственным wrapper:

```bash
cd event-producer
./gradlew test
./gradlew bootRun
```

Аналогичные команды работают в `event-consumer` и `event-public-api`. Конфигурация приложений по умолчанию использует Docker DNS-имена (`kafka1`, `postgres`, `alloy`), поэтому для запуска сервиса напрямую на хосте адреса зависимостей нужно переопределить переменными окружения или отдельным Spring-профилем.

## Структура репозитория

```text
.
├── event-producer/       # генерация и публикация Avro-событий
├── event-consumer/       # чтение Kafka и сохранение в PostgreSQL
├── event-public-api/     # внешний REST API
├── monitoring/
│   ├── alloy/            # сбор логов и OTLP telemetry
│   ├── loki/             # хранение логов
│   ├── prometheus/       # scrape-конфигурация и alert rules
│   └── tempo/            # хранение трассировок
├── docs/architecture/    # PlantUML-диаграммы
├── remote-ubuntu/        # материалы для Node Exporter
└── docker-compose.yaml
```

## Известные ограничения

- Grafana datasources и dashboards не provisioned и настраиваются вручную.
- Alert `HighCPU` использует `node_cpu_seconds_total`, но Node Exporter не добавлен в `prometheus.yml`; до добавления scrape target правило не сработает. Инструкция по установке экспортёра есть в [remote-ubuntu/node_exporter_install.md](remote-ubuntu/node_exporter_install.md).
- Конфигурация предназначена для локальной демонстрации: пароли заданы открыто, а образы с тегом `latest` не зафиксированы. Не используйте её как production-конфигурацию.
- Сбор Docker-логов Alloy использует `/var/run/docker.sock` и `/var/lib/docker/containers`; на Docker Desktop доступ к файловому каталогу контейнеров может отличаться от Linux.

