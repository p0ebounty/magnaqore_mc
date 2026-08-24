#!/usr/bin/env bash
# Pregen watchdog: keeps Chunky running across survival restarts and
# automatically starts the Nether pregen once the overworld finishes.
# Run in background: nohup infra/pregen-watchdog.sh >/dev/null 2>&1 &
# Stops itself when both worlds are done (marker files in infra/.pregen/).
set -u

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOG="$ROOT/minecraft/dev/survival/logs/latest.log"
MARKS="$ROOT/infra/.pregen"
mkdir -p "$MARKS"

send() { tmux send-keys -t mq-dev-survival "$1" Enter 2>/dev/null; }

while true; do
  sleep 300

  # server down? wait for it to come back
  tmux has-session -t mq-dev-survival 2>/dev/null || continue

  # finished markers
  if [ -f "$MARKS/nether-done" ]; then
    echo "$(date) all pregen done, watchdog exiting" >> "$MARKS/watchdog.log"
    exit 0
  fi

  recent="$(tail -n 400 "$LOG" 2>/dev/null)"

  if [ ! -f "$MARKS/overworld-done" ]; then
    if grep -q "Task finished for world\b" <<<"$recent" || grep -q "Task finished for world," <<<"$recent"; then
      touch "$MARKS/overworld-done"
      echo "$(date) overworld pregen finished, starting nether" >> "$MARKS/watchdog.log"
      send "chunky world world_nether"; sleep 2
      send "chunky shape square"; sleep 2
      send "chunky radius 625"; sleep 2
      send "chunky start"
      continue
    fi
    # not finished and not running -> nudge continue (e.g. after a restart)
    if ! grep -q "Task running for world\b" <<<"$(tail -n 60 "$LOG" 2>/dev/null)"; then
      echo "$(date) overworld task idle, sending chunky continue" >> "$MARKS/watchdog.log"
      send "chunky continue"
    fi
  else
    if grep -q "Task finished for world_nether" <<<"$recent"; then
      touch "$MARKS/nether-done"
      echo "$(date) nether pregen finished" >> "$MARKS/watchdog.log"
      continue
    fi
    if ! grep -q "Task running for world_nether" <<<"$(tail -n 60 "$LOG" 2>/dev/null)"; then
      echo "$(date) nether task idle, sending chunky continue" >> "$MARKS/watchdog.log"
      send "chunky continue"
    fi
  fi
done
