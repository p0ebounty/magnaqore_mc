#!/usr/bin/env bash
# Builds MagnaQoreCore and deploys the jar to the dev survival server.
# Usage: infra/build-core.sh [--deploy-only]
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SRC="$ROOT/plugins-src/magnaqore-core"

if [ "${1:-}" != "--deploy-only" ]; then
  (cd "$SRC" && gradle build -q)
fi

JAR="$(ls -1t "$SRC"/build/libs/MagnaQoreCore-*.jar | head -1)"
cp -f "$JAR" "$ROOT/minecraft/dev/survival/plugins/MagnaQoreCore.jar"
echo "deployed: $(basename "$JAR") -> minecraft/dev/survival/plugins/MagnaQoreCore.jar"
