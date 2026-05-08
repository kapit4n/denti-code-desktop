#!/usr/bin/env bash
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$REPO_ROOT"

DATA_DIR="${DENTICODE_DATA_DIR:-$HOME/.denticode-desktop}"
DB_PATH="$DATA_DIR/denti-code.db"

usage() {
  cat <<'EOF'
Usage:
  ./start.sh [--reset-db] [--clean] [-- <gradle args...>]

Options:
  --reset-db   Delete the local SQLite database (~/.denticode-desktop/denti-code.db by default)
  --clean      Run a Gradle clean before launching

Environment:
  DENTICODE_DATA_DIR  Override the data directory (maps to -Dapp.dataDir)

Examples:
  ./start.sh
  ./start.sh --reset-db
  DENTICODE_DATA_DIR=/tmp/dc ./start.sh
  ./start.sh -- --info
EOF
}

RESET_DB=0
DO_CLEAN=0
GRADLE_ARGS=()

while [[ $# -gt 0 ]]; do
  case "$1" in
    -h|--help)
      usage
      exit 0
      ;;
    --reset-db)
      RESET_DB=1
      shift
      ;;
    --clean)
      DO_CLEAN=1
      shift
      ;;
    --)
      shift
      GRADLE_ARGS+=("$@")
      break
      ;;
    *)
      GRADLE_ARGS+=("$1")
      shift
      ;;
  esac
done

if [[ $RESET_DB -eq 1 ]]; then
  mkdir -p "$DATA_DIR"
  if [[ -f "$DB_PATH" ]]; then
    echo "Deleting DB: $DB_PATH"
    rm -f "$DB_PATH"
  else
    echo "No DB found at: $DB_PATH"
  fi
fi

JVM_PROPS=("-Dapp.dataDir=$DATA_DIR")

if [[ $DO_CLEAN -eq 1 ]]; then
  ./gradlew --no-daemon clean
fi

exec ./gradlew --no-daemon run "${JVM_PROPS[@]}" "${GRADLE_ARGS[@]}"
