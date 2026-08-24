#!/usr/bin/env bash
# MagnaQore backup: worlds + PostgreSQL dumps + secrets snapshot.
# Usage: infra/backup.sh [dev|prod|all]   (default: all)
# Output: /backups/magnaqore-<what>-<timestamp>.tar.gz ; keeps last 7 per kind.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
WHAT="${1:-all}"
TS="$(date +%Y%m%d-%H%M%S)"
OUT=/backups
mkdir -p "$OUT"

flush_env() { # ask running servers to flush world data before tar
  local env="$1" s
  for s in lobby survival; do
    if tmux has-session -t "mq-$env-$s" 2>/dev/null; then
      tmux send-keys -t "mq-$env-$s" "save-all flush" Enter
    fi
  done
  sleep 3
}

backup_env() {
  local env="$1"
  [ -d "$ROOT/minecraft/$env" ] || return 0
  flush_env "$env" || true
  local file="$OUT/magnaqore-worlds-$env-$TS.tar.gz"
  tar -czf "$file" -C "$ROOT/minecraft" \
    $(cd "$ROOT/minecraft" && find "$env" -maxdepth 2 -type d -name 'world*' 2>/dev/null) \
    2>/dev/null || true
  echo "worlds($env): $file ($(du -h "$file" | cut -f1))"
}

backup_db() {
  set -a; . "$ROOT/infra/secrets/db.env"; set +a
  for db in "$POSTGRES_DB_DEV" "$POSTGRES_DB_PROD"; do
    local file="$OUT/magnaqore-db-$db-$TS.sql.gz"
    PGPASSWORD="$POSTGRES_PASSWORD" pg_dump -h "$POSTGRES_HOST" -U "$POSTGRES_USER" "$db" | gzip > "$file"
    echo "db($db): $file ($(du -h "$file" | cut -f1))"
  done
}

backup_secrets() {
  local file="$OUT/magnaqore-secrets-$TS.tar.gz"
  tar -czf "$file" -C "$ROOT/infra" secrets
  chmod 600 "$file"
  echo "secrets: $file (store off-site, contains credentials!)"
}

case "$WHAT" in
  dev)  backup_env dev; backup_db ;;
  prod) backup_env prod; backup_db ;;
  all)  backup_env dev; backup_env prod; backup_db; backup_secrets ;;
  *) echo "usage: $0 [dev|prod|all]"; exit 1 ;;
esac

# retention: keep last 7 of each kind
for prefix in worlds-dev worlds-prod db-magnaqore_dev db-magnaqore_prod secrets; do
  ls -1t "$OUT"/magnaqore-$prefix-* 2>/dev/null | tail -n +8 | xargs -r rm -f
done
echo "done."
