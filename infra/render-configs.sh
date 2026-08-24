#!/usr/bin/env bash
# Renders secret-bearing / machine-local configs from committed templates.
# Run after clone, or when templates change. Idempotent.
#  - paper-global.yml        <- paper-global.template.yml (+ velocity forwarding.secret)
#  - server.properties       <- server.properties.template (only if missing; server rewrites it at runtime)
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

for env in dev prod; do
  base="$ROOT/minecraft/$env"
  [ -d "$base" ] || continue
  secret_file="$base/velocity/forwarding.secret"
  if [ ! -f "$secret_file" ]; then
    echo "skip $env: no forwarding.secret yet (boot velocity once to generate it)"
    continue
  fi
  SECRET="$(cat "$secret_file")"

  while IFS= read -r tpl; do
    out="${tpl%.template.yml}.yml"
    if [ -f "$out" ]; then
      sed -i "s|^\(\s*secret:\).*|\1 '$SECRET'|" "$out"
      echo "updated secret: ${out#"$ROOT/"}"
    else
      sed "s|\${VELOCITY_SECRET}|$SECRET|" "$tpl" > "$out"
      echo "rendered: ${out#"$ROOT/"}"
    fi
  done < <(find "$base" -name 'paper-global.template.yml')

  while IFS= read -r tpl; do
    out="${tpl%.template}"
    if [ ! -f "$out" ]; then
      cp "$tpl" "$out"
      echo "rendered: ${out#"$ROOT/"}"
    fi
  done < <(find "$base" -name 'server.properties.template')
done
echo "done."
