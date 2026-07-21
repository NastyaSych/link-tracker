## Запуск:

1. Создать .env файл с {TELEGRAM_TOKEN}, {TELEGRAM_BOT_NAME}, {GITHUB_API_TOKEN}, {STACKOVERFLOW_API_KEY}

2.
```bash
sbt docker:publishLocal
```

3.
```bash
docker-compose up
```

## Архитектура

Scrapper Service

↓

Kafka topic: link.raw-updates

↓

AI Agent Service

↓

Kafka topic: link.processed-updates

↓

Bot Service

↓

Telegram API

Три независимых сервиса:

- bot: Telegram бот для взаимодействия с пользователем
- scrapper: сервис для отслеживания ссылок и хранения данных
- ai agent: получает и обрабатывает сообщения об обновлении (фильтрация по стоп-словам и авторам)

## Детали
- В application.conf можно выбрать механизм отправки сообщений (kafka/http) и database access-type (sql/orm)

  Модель сообщения для kafka/http одна и та же (см. notification), сообщения передаются в JSON-формате
- База данных PostgreSQL, миграции Liquibase
- Кэширование GET /list с помощью Valkey
- Есть Timeout, Retry, Circuit Breaker, Fallback, Rate Limiting

  Параметры задаются через конфигурацию.







