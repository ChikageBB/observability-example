# Архитектурные диаграммы observability

Пакет описывает архитектуру репозитория по модели C4 и основные runtime-сценарии. Диаграммы хранятся как PlantUML (`.puml`), поэтому их можно просматривать в IDE с PlantUML-плагином или собирать командой:

```bash
plantuml -tsvg "docs/architecture/**/*.puml"
```

## C4

| Уровень | Диаграмма | Назначение |
|---|---|---|
| C1 | [01-system-context.puml](c4/01-system-context.puml) | Пользователи и внешняя граница системы |
| C2 | [02-containers.puml](c4/02-containers.puml) | Сервисы, хранилища и observability-контур |
| C3 | [03-producer-components.puml](c4/03-producer-components.puml) | Компоненты `event-producer` |
| C3 | [04-consumer-components.puml](c4/04-consumer-components.puml) | Компоненты `event-consumer` |
| Deployment | [05-deployment.puml](c4/05-deployment.puml) | Размещение в Docker Compose |

## Sequence

| Сценарий | Диаграмма |
|---|---|
| Публикация и сохранение события | [01-event-flow.puml](sequence/01-event-flow.puml) |
| Регистрация/получение Avro-схемы | [02-avro-schema.puml](sequence/02-avro-schema.puml) |
| Сбор и просмотр метрик | [03-metrics-scrape.puml](sequence/03-metrics-scrape.puml) |
| Оценка alert rules | [04-alert-evaluation.puml](sequence/04-alert-evaluation.puml) |

## Границы и допущения

- Диаграммы отражают код и конфигурацию репозитория, а не целевую архитектуру.
- `event-consumer` существует в исходниках и указан как scrape target, но отсутствует в `docker-compose.yaml`; на deployment-диаграмме он помечен как **не развёрнут**.
- `event-public-api`/`public-api` есть только в `prometheus.yml`; реализации и Compose-сервиса в репозитории нет, поэтому он показан как внешний/ожидаемый target.
- Правило `HighCPU` использует метрики Node Exporter, но scrape job для Node Exporter в текущем `prometheus.yml` отсутствует.
- Kafka UI — операторский интерфейс; Grafana — интерфейс просмотра метрик. Настроенный datasource/provisioning Grafana в репозитории отсутствует.

Обозначения C4 (`Person`, `System`, `Container`, `Component`) переданы стандартными элементами PlantUML без внешних include-зависимостей, чтобы пакет рендерился офлайн.
