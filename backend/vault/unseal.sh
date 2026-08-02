#!/usr/bin/env bash
#
# Распечатывает Vault ключами из vault-keys.json в корне репозитория.
#
# Зачем: при file-хранилище Vault запечатывается при каждом пересоздании
# контейнера, и приложение перестаёт получать секреты. Скрипт избавляет от
# ручного ввода трёх ключей после каждого docker compose up.
#
# Компромисс: ключи лежат на диске в открытом виде. Для локальной разработки
# это приемлемо, для любого общего или продуктивного окружения — нет: там
# нужен auto-unseal (KMS/Transit) либо ручная процедура с ключами из
# менеджера паролей.
#
# Использование:  ./backend/vault/unseal.sh
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
KEYS_FILE="$REPO_ROOT/vault-keys.json"
CONTAINER="${VAULT_CONTAINER:-vault}"

if [ ! -f "$KEYS_FILE" ]; then
  echo "Не найден $KEYS_FILE — распечатать нечем." >&2
  echo "Ключи должны быть в менеджере паролей; восстановить их иначе невозможно." >&2
  exit 1
fi

if ! docker ps --format '{{.Names}}' | grep -qx "$CONTAINER"; then
  echo "Контейнер '$CONTAINER' не запущен." >&2
  exit 1
fi

status() {
  docker exec -e VAULT_ADDR=http://127.0.0.1:8200 "$CONTAINER" vault status -format=json 2>/dev/null || true
}

if [ "$(status | python3 -c 'import sys,json;print(json.load(sys.stdin).get("sealed"))' 2>/dev/null)" = "False" ]; then
  echo "Vault уже распечатан."
  exit 0
fi

# Порог — 3 ключа из 5.
for i in 0 1 2; do
  KEY=$(python3 -c "import json;print(json.load(open('$KEYS_FILE'))['unseal_keys_b64'][$i])")
  docker exec -e VAULT_ADDR=http://127.0.0.1:8200 "$CONTAINER" \
    vault operator unseal "$KEY" > /dev/null
done

SEALED=$(status | python3 -c 'import sys,json;print(json.load(sys.stdin).get("sealed"))')
if [ "$SEALED" = "False" ]; then
  echo "Vault распечатан."
else
  echo "Не удалось распечатать Vault." >&2
  exit 1
fi
