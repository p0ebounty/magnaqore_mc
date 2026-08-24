#!/usr/bin/env bash
# MagnaQore DEV network control. Servers run in tmux sessions (mq-dev-*).
# Usage: infra/dev.sh start|stop|restart|status [server]
#        infra/dev.sh cmd <server> <console command...>
#        infra/dev.sh console <server>   (attach; detach with Ctrl+b d)
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_NAME=dev
BASE="$ROOT/minecraft/$ENV_NAME"

SERVERS=(lobby survival velocity)   # start order; stop order is reversed

jar_of()  { case "$1" in velocity) echo velocity.jar ;; *) echo paper.jar ;; esac; }
heap_of() { case "$1" in velocity) echo "-Xms256M -Xmx512M" ;; lobby) echo "-Xms512M -Xmx1G" ;; survival) echo "-Xms1G -Xmx2560M" ;; esac; }

AIKAR_FLAGS="-XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+DisableExplicitGC -XX:+AlwaysPreTouch -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1HeapRegionSize=8M -XX:G1ReservePercent=20 -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=4 -XX:InitiatingHeapOccupancyPercent=15 -XX:G1MixedGCLiveThresholdPercent=90 -XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32 -XX:+PerfDisableSharedMem -XX:MaxTenuringThreshold=1"
VELOCITY_FLAGS="-XX:+UseG1GC -XX:G1HeapRegionSize=4M -XX:+UnlockExperimentalVMOptions -XX:+ParallelRefProcEnabled -XX:+AlwaysPreTouch -XX:MaxInlineLevel=15"

session() { echo "mq-$ENV_NAME-$1"; }

flags_of() {
  case "$1" in
    velocity) echo "$VELOCITY_FLAGS" ;;
    *)        echo "$AIKAR_FLAGS" ;;
  esac
}

tail_args() { case "$1" in velocity) echo "" ;; *) echo "nogui" ;; esac; }

start_one() {
  local s="$1" sess dir
  sess="$(session "$s")"; dir="$BASE/$s"
  [ -f "$dir/$(jar_of "$s")" ] || { echo "!! $dir/$(jar_of "$s") missing — run infra/download.sh"; return 1; }
  if tmux has-session -t "$sess" 2>/dev/null; then echo "   $s already running"; return 0; fi
  tmux new-session -d -s "$sess" -c "$dir" \
    "java $(heap_of "$s") $(flags_of "$s") -jar $(jar_of "$s") $(tail_args "$s")"
  echo ">> started $s (tmux: $sess)"
}

stop_one() {
  local s="$1" sess cmd i
  sess="$(session "$s")"
  tmux has-session -t "$sess" 2>/dev/null || { echo "   $s not running"; return 0; }
  cmd=stop; [ "$s" = velocity ] && cmd=shutdown
  tmux send-keys -t "$sess" "$cmd" Enter
  for i in $(seq 1 45); do
    tmux has-session -t "$sess" 2>/dev/null || { echo ">> stopped $s"; return 0; }
    sleep 1
  done
  tmux kill-session -t "$sess" 2>/dev/null || true
  echo "!! $s killed after timeout"
}

status_all() {
  local s sess
  for s in "${SERVERS[@]}"; do
    sess="$(session "$s")"
    if tmux has-session -t "$sess" 2>/dev/null; then
      echo "RUNNING  $s ($sess)"
    else
      echo "stopped  $s"
    fi
  done
}

targets() { if [ $# -ge 1 ] && [ -n "${1:-}" ]; then echo "$1"; else echo "${SERVERS[@]}"; fi; }

case "${1:-}" in
  start)   for s in $(targets "${2:-}"); do start_one "$s"; done ;;
  stop)    rev=""; for s in $(targets "${2:-}"); do rev="$s $rev"; done
           for s in $rev; do stop_one "$s"; done ;;
  restart) "$0" stop "${2:-}"; "$0" start "${2:-}" ;;
  status)  status_all ;;
  cmd)     s="${2:?server}"; shift 2; tmux send-keys -t "$(session "$s")" "$*" Enter; echo "sent to $s: $*" ;;
  console) tmux attach -t "$(session "${2:?server}")" ;;
  *) echo "usage: $0 start|stop|restart|status [server] | cmd <server> <command...> | console <server>"; exit 1 ;;
esac
