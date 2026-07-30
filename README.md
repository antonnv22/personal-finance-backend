# Personal Finance

Учёт личных финансов: счета, категории, транзакции, отчёты и календарь регулярных
расходов с планом и фактом.

- **Backend** — Java 21, Spring Boot 3, PostgreSQL, Flyway, Spring Security (JWT),
  MapStruct, Spring AI MCP-сервер, секреты в HashiCorp Vault, логи в ELK
- **Frontend** — React 18, TypeScript, Vite, Material UI, React Router,
  TanStack Query, Axios, Recharts

Запуск и деплой описаны в [DEPLOYMENT.md](DEPLOYMENT.md).

## Календарь регулярных расходов

Обычный учёт фиксирует уже случившиеся траты. Регулярные платежи — подписки,
аренда, кредиты — известны заранее, и функция добавляет поверх учёта слой
планирования.

### Как это устроено

**Правило** (`RecurringExpense`) описывает повторяющийся расход: «Netflix,
15 EUR, каждое 5-е число». Само по себе оно денег не двигает.

**Запланированный платёж** (`RecurringExpenseOccurrence`) — конкретное ожидаемое
списание, порождённое правилом: 05.08.2026, 15 EUR, статус `PLANNED`.
Транзакция при этом **не создаётся**.

Когда платёж действительно прошёл, пользователь подтверждает его, указывая
реальные дату и сумму. Только в этот момент создаётся настоящая `Transaction`
через существующий `TransactionService`, и платёж связывается с ней. Разница
между планом и фактом становится видна.

```
RecurringExpense ──1:N──> RecurringExpenseOccurrence ──0..1──> Transaction
```

### Статусы

| Статус | Значение | Цвет в интерфейсе |
|---|---|---|
| `PLANNED` | Платёж ожидается | серый |
| `COMPLETED` | Расход создан | зелёный |
| `SKIPPED` | Пользователь пропустил платёж | жёлтый |
| `OVERDUE` | `PLANNED`, но плановая дата прошла | красный |

`OVERDUE` в базе не хранится — это производная величина, которая меняется со
временем сама по себе. Бэкенд отдаёт признак `overdue` рядом с обычным статусом,
и план, и фронтенд считают его одинаково.

### Генерация платежей

`RecurringExpenseScheduler` материализует будущие платежи на горизонт
`app.recurring.horizon-months` (по умолчанию 12 месяцев). Генерация **идемпотентна** —
её защищает уникальный ключ `(recurring_expense_id, planned_date)`, поэтому она
запускается из трёх мест без риска дублей:

- по расписанию (`app.recurring.generation-cron`, раз в сутки);
- сразу при создании или изменении правила — иначе новое правило не показывало
  бы платежей до срабатывания фоновой задачи;
- при открытии календаря на месяц за пределами уже сгенерированного горизонта.

Прошлое не заполняется: генерация всегда начинается не раньше сегодняшнего дня,
иначе правило со старым `startDate` сразу порождало бы десятки просроченных
платежей.

Если в месяце меньше дней, чем указано в правиле (31-е число в феврале), дата
схлопывается к последнему дню месяца.

### Ограничение по валюте

`Transaction` не хранит валюту — сумма всегда в валюте счёта. Поэтому валюта
правила обязана совпадать с валютой счёта, иначе сравнение «план 15 EUR против
факта 1500 RUB» было бы бессмысленным. При несовпадении API отвечает 400, а в
интерфейсе список счетов отфильтрован по валюте правила.

## API

Все эндпоинты требуют заголовок `Authorization: Bearer <JWT>` и работают только
с данными текущего пользователя.

### Правила

| Метод | Путь | Назначение |
|---|---|---|
| `GET` | `/api/recurring-expenses` | Список правил |
| `GET` | `/api/recurring-expenses/{id}` | Правило по идентификатору |
| `POST` | `/api/recurring-expenses` | Создать правило и сразу сгенерировать платежи |
| `PUT` | `/api/recurring-expenses/{id}` | Изменить правило и пересобрать будущие платежи |
| `DELETE` | `/api/recurring-expenses/{id}` | Удалить, либо архивировать при наличии истории |
| `GET` | `/api/recurring-expenses/upcoming?limit=5` | Ближайшие платежи для дашборда |

### Платежи

| Метод | Путь | Назначение |
|---|---|---|
| `GET` | `/api/recurring-expenses/occurrences/{id}` | Один платёж |
| `POST` | `/api/recurring-expenses/occurrences/{id}/complete` | Подтвердить: создать расход |
| `POST` | `/api/recurring-expenses/occurrences/{id}/skip` | Пропустить |

### Календарь

| Метод | Путь | Назначение |
|---|---|---|
| `GET` | `/api/calendar/{year}/{month}` | Платежи месяца и сводка |
| `GET` | `/api/calendar/{year}/{month}/summary` | Только сводка «план против факта» |

## Пример использования

Создать правило «Netflix, 15 EUR, каждое 5-е число»:

```bash
curl -X POST http://localhost:8080/api/recurring-expenses \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"name":"Netflix","plannedAmount":15.00,"currency":"EUR","accountId":"<uuid>","categoryId":"<uuid>","recurrenceType":"MONTHLY","dayOfMonth":5,"startDate":"2026-08-01"}'
```

Посмотреть август:

```bash
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/calendar/2026/8
```

```json
{
  "year": 2026,
  "month": 8,
  "items": [
    {
      "occurrenceId": "...",
      "date": "2026-08-05",
      "name": "Netflix",
      "plannedAmount": 15.00,
      "actualAmount": null,
      "deviation": null,
      "currency": "EUR",
      "status": "PLANNED",
      "overdue": false,
      "transactionId": null
    }
  ],
  "summary": { "plannedTotal": 15.00, "actualTotal": 0, "deviation": -15.00 }
}
```

Списание прошло 7 августа и оказалось дороже — подтверждаем:

```bash
curl -X POST http://localhost:8080/api/recurring-expenses/occurrences/<id>/complete \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"actualDate":"2026-08-07","actualAmount":17.99,"accountId":"<uuid>","comment":"Netflix subscription"}'
```

В ответе `status: "COMPLETED"`, `deviation: 2.99` и `transactionId` созданного
расхода. Транзакция появляется в общем списке, баланс счёта уменьшается,
а в календаре платёж становится зелёным.

## Границы API

Функциональность повторяет фактические возможности бэкенда, а он не везде
предоставляет полный CRUD:

- **Accounts** — полный CRUD; удаление архивирует счёт, если по нему есть транзакции
- **Categories** — создание, чтение, переименование; эндпоинта удаления нет
- **Transactions** — создание, чтение, удаление; эндпоинта обновления нет
- **Recurring expenses** — полный CRUD; удаление архивирует правило при наличии
  оплаченных платежей

## Тесты

```bash
cd backend && ./gradlew test
```

Тесты используют Testcontainers (нужен запущенный Docker) и не требуют ни Vault,
ни ELK: Vault отключается в `AbstractIntegrationTest`, а импорт конфигурации
помечен как `optional:`.
