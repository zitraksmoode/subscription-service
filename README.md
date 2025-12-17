Spring Boot сервис управления подписками (Basic/PRO) с кэш-сервисом.

Функционал:
- REST API активации/деактивации подписки с проверками
- Ежедневный биллинг (scheduled task)
- Отправка событий в RabbitMQ
- Кэш в Redis (активная подписка + счета с пагинацией)
- Fallback на PostgreSQL при недоступности Redis
- Обработка недоступности RabbitMQ

Стек: Java 24, Spring Boot 4.0, PostgreSQL, Redis, RabbitMQ, Lombok, JUnit 5 + Mockito
