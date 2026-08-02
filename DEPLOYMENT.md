# Deployment

Полный стек: PostgreSQL + Spring Boot backend + React/nginx frontend, поверх существующей инфраструктуры Vault + ELK.

## Структура

```
personal-finance-backend/
├── docker-compose.yml            ← прикладной стек (postgres + backend + frontend)
├── .env                          ← секреты, НЕ коммитить (в .gitignore)
├── .env.example                  ← шаблон
├── backend/
│   ├── docker-compose.yaml       ← инфраструктура: Vault, Elasticsearch, Kibana, Logstash
│   ├── Dockerfile
│   └── src/…
└── frontend/
    ├── Dockerfile
    ├── nginx.conf
    └── src/…
```

Два compose-файла разделены намеренно: инфраструктура живёт своим циклом и
переживает пересборки приложения. Прикладной стек подключается к её сети
`backend_default`, поэтому backend видит `vault` и `logstash` по именам сервисов.

## Запуск

```bash
cp .env.example .env
```

Заполнить в `.env`: `VAULT_TOKEN`, `POSTGRES_PASSWORD`, `JWT_SECRET`
(секрет сгенерировать: `openssl rand -base64 48`).

```bash
docker compose -f backend/docker-compose.yaml up -d
```

```bash
docker compose up -d --build
```

- Frontend: http://localhost:8081
- Backend API: http://localhost:8080/api
- Swagger UI: http://localhost:8080/swagger-ui.html
- Kibana: http://localhost:5601
- Vault: http://localhost:8200

## Схема сети

```
браузер → frontend:80 (nginx) ─┬─ статика (React SPA)
                                └─ /api/* → backend:8080 ─┬─ postgres:5432   (сеть app)
                                                           ├─ vault:8200     (сеть backend_default)
                                                           └─ logstash:5000  (сеть backend_default)
```

Фронтенд и API отдаются с одного origin, поэтому CORS в этой схеме не задействован.

## Конфигурация: Vault против переменных окружения

Backend читает секреты из Vault (`spring.config.import: vault://`), в
`secret/personal-finance` лежат `spring.datasource.url` / `username` / `password`.

Есть нюанс: в Vault `url` указывает на `localhost:5432` — это верно при запуске из
IDE, но внутри контейнера `localhost` означает сам контейнер. Поэтому в
`docker-compose.yml` адрес и креды БД заданы переменными окружения — в Spring Boot
они имеют более высокий приоритет, чем config data из Vault, и перекрывают его.

Два адреса вынесены в переменные с текущими значениями по умолчанию, так что
**запуск из IntelliJ IDEA продолжает работать без изменений**:

| Переменная | По умолчанию (IDE) | В Docker |
|---|---|---|
| `VAULT_URI` | `http://localhost:8200` | `http://vault:8200` |
| `LOGSTASH_DESTINATION` | `localhost:5001` | `logstash:5000` |

Для запуска из IDEA по-прежнему нужен только `VAULT_TOKEN` в переменных окружения
run-конфигурации.

## Vault: распечатывание и восстановление

Vault использует file-хранилище и **запечатывается при каждом пересоздании
контейнера**. Пока он запечатан, приложение не получит секреты (стартовать оно
всё же будет — импорт помечен `optional:`, а в Docker параметры БД приходят
переменными окружения).

Распечатать:

```bash
./backend/vault/unseal.sh
```

Скрипт берёт ключи из `vault-keys.json` в корне репозитория. Файл закрыт в
`.gitignore` и содержит 5 unseal-ключей (порог 3) и root-токен.

**Ключи хранить в менеджере паролей.** Если они потеряны, содержимое Vault
восстановить невозможно: хранилище зашифровано мастер-ключом, который защищён
этими ключами. Единственный выход — пересоздать Vault с нуля:

```bash
docker compose -f backend/docker-compose.yaml stop vault
docker compose -f backend/docker-compose.yaml rm -f vault
docker volume rm backend_vault-data
docker compose -f backend/docker-compose.yaml up -d vault
docker exec -e VAULT_ADDR=http://127.0.0.1:8200 vault \
  vault operator init -key-shares=5 -key-threshold=3 -format=json > vault-keys.json
./backend/vault/unseal.sh
```

Затем включить KV и записать секреты (root-токеном из `vault-keys.json`):

```bash
vault secrets enable -path=secret -version=2 kv
vault kv put secret/personal-finance \
  spring.datasource.url="jdbc:postgresql://localhost:5433/personal_finance" \
  spring.datasource.username="postgres" \
  spring.datasource.password="<пароль>"
```

Приложение ходит в Vault не root-токеном, а отдельным — с политикой
`personal-finance`, дающей только чтение своего пути. Токен живёт 32 дня
(системный максимум); когда истечёт, выпустить новый:

```bash
vault token create -policy=personal-finance -ttl=768h
```

и заменить `VAULT_TOKEN` в `.env`.

## Локальная разработка фронтенда

Backend запускается из IDEA (или через compose), затем:

```bash
cd frontend && npm install && npm run dev
```

Откроется http://localhost:5173, Vite проксирует `/api` на `localhost:8080`.
CORS в `SecurityConfig` уже разрешает `http://localhost:*`.

Если backend поднят не на 8080, задать в `frontend/.env`:
`VITE_DEV_PROXY_TARGET=http://localhost:<порт>`.

## Важно про порт 5432

На машине разработки стоит локальный PostgreSQL, занимающий 5432. Контейнерный
Postgres публикуется на **5433** (`POSTGRES_HOST_PORT`), чтобы не конфликтовать.
Это две разные базы — контейнерная живёт в томе `pf-postgres-data`.

## Границы API

Фронтенд повторяет фактический контракт бэкенда, а он не везде полный CRUD:

- **Accounts** — полный CRUD (удаление архивирует счёт, если по нему есть транзакции)
- **Categories** — создание, чтение, переименование; эндпоинта удаления нет
- **Transactions** — создание, чтение, удаление; эндпоинта обновления нет
- **Recurring expenses** — полный CRUD (удаление архивирует правило, если по нему
  есть оплаченные платежи); подтверждение и пропуск запланированных платежей

Описание календаря регулярных расходов и примеры запросов — в [README.md](README.md).

## Что доделать перед продакшеном

- `JWT_SECRET` в `application.yml` имеет небезопасное значение по умолчанию —
  приложение стартует даже без заданного секрета. Стоит убрать fallback.
- `GlobalExceptionHandler` возвращает клиенту `ex.getMessage()` для любого
  необработанного исключения и ничего не логирует.
- `JwtAuthFilter` не ловит исключения парсинга токена — просроченный или
  повреждённый JWT приводит к 500 вместо 401.
- Нет rate limiting на `/api/auth/**`.
- Swagger открыт без аутентификации (`permitAll`).
