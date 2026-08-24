# RUNBOOK — операционные процедуры

## Dev-сеть

```bash
infra/dev.sh start            # поднять всё (lobby, survival, velocity)
infra/dev.sh stop             # погасить всё
infra/dev.sh status           # что запущено
infra/dev.sh restart survival # перезапустить один сервер
infra/dev.sh cmd survival op Nickname   # команда в консоль сервера
infra/dev.sh console velocity # живая консоль (выход: Ctrl+b, затем d)
```

Логи: `minecraft/dev/<server>/logs/latest.log` (velocity: `minecraft/dev/velocity/logs/latest.log`).

## Бинарники (jar-ы)

Всё пинится в `infra/manifest.json`. После клона репозитория или правки манифеста:

```bash
infra/download.sh
```

Обновление плагина: поменять `version`/`url`/`sha256` в манифесте → `infra/download.sh` → перезапуск dev → проверка → тот же jar попадает в prod при следующем rollout.

## Бэкапы

```bash
infra/backup.sh          # миры dev+prod, дампы БД, секреты → /backups
```

Бэкапы хранятся в `/backups` (последние 7 каждого вида). Архив `magnaqore-secrets-*` содержит пароли — переносить только в защищённое место.

## Перенос на другой VPS

1. Новый VPS: поставить `git`, `openjdk-21+`, `postgresql`, `tmux`, `jq`, `unzip`.
2. `git clone` репозитория в `/projects/server`.
3. Восстановить `infra/secrets/` из бэкапа секретов.
4. `infra/download.sh` — скачает все jar-ы.
5. Восстановить миры из `magnaqore-worlds-*.tar.gz` в `minecraft/<env>/...`.
6. Восстановить БД: `gunzip -c magnaqore-db-*.sql.gz | psql -h 127.0.0.1 -U magnaqore <db>` (создав роль/базы, см. docs/ARCHITECTURE.md).
7. Переключить DNS-записи домена на новый IP.

## PostgreSQL

- Подключение: `psql "postgresql://magnaqore:<пароль из infra/secrets/db.env>@127.0.0.1:5432/magnaqore_dev"`
- Базы: `magnaqore_dev`, `magnaqore_prod`. Наружу порт не открыт.

## Браузер / скриншоты (для ресёрча)

```bash
npx playwright screenshot --browser chromium <url> out.png
```
