#!/usr/bin/env bash
# Downloads all jars pinned in infra/manifest.json into their destinations.
# Idempotent: skips files whose sha256 already matches (when sha256 is set).
# This is the single source of truth for binaries — jars are NOT stored in git.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
MANIFEST="$ROOT/infra/manifest.json"
CACHE="$ROOT/infra/cache"
mkdir -p "$CACHE"

command -v jq >/dev/null || { echo "jq is required"; exit 1; }

fail=0
while IFS= read -r item; do
  name=$(jq -r .name <<<"$item")
  version=$(jq -r .version <<<"$item")
  url=$(jq -r .url <<<"$item")
  sha=$(jq -r '.sha256 // empty' <<<"$item")
  cachefile="$CACHE/${name}-${version}.jar"

  if [ ! -f "$cachefile" ] || { [ -n "$sha" ] && ! echo "$sha  $cachefile" | sha256sum -c --quiet 2>/dev/null; }; then
    echo ">> downloading $name $version"
    curl -fsSL --retry 3 --retry-delay 2 -o "$cachefile.tmp" "$url"
    mv "$cachefile.tmp" "$cachefile"
    if [ -n "$sha" ] && ! echo "$sha  $cachefile" | sha256sum -c --quiet; then
      echo "!! sha256 mismatch for $name $version"; fail=1; continue
    fi
  else
    echo "   cached: $name $version"
  fi

  while IFS= read -r dest; do
    [ -z "$dest" ] && continue
    path="$ROOT/$dest"
    mkdir -p "$(dirname "$path")"
    cp -f "$cachefile" "$path"
    echo "   -> $dest"
  done < <(jq -r '.dest[]' <<<"$item")
done < <(jq -c '.downloads[]' "$MANIFEST")

exit $fail
