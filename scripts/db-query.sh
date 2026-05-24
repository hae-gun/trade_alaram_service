#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${ROOT_DIR}/infra/docker/.env"
COMPOSE_ENV_ARGS=()
COMPOSE_MODE="images"
MODE="tables"
SQL=""
LIMIT="30"

usage() {
  cat <<'USAGE'
PostgreSQL 데이터베이스 값을 확인합니다.

사용법:
  ./scripts/db-query.sh [옵션]

옵션:
  --images          배포 이미지 실행 compose 파일 사용 (기본값)
  --dev             로컬 빌드 compose 파일 사용
  --tables          테이블 목록 조회 (기본값)
  --stocks          종목 데이터 조회
  --alerts          알림 조건 조회
  --events          알림 발송 이력 조회
  --sql "SQL"       직접 SQL 실행
  --limit N         조회 row 수 제한 (기본값: 30)
  -h, --help        도움말 출력

예시:
  ./scripts/db-query.sh --tables
  ./scripts/db-query.sh --stocks
  ./scripts/db-query.sh --stocks --limit 100
  ./scripts/db-query.sh --sql "select symbol, name, market from stocks where symbol = '005930';"
  ./scripts/db-query.sh --dev --stocks
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --images)
      COMPOSE_MODE="images"
      shift
      ;;
    --dev)
      COMPOSE_MODE="dev"
      shift
      ;;
    --tables)
      MODE="tables"
      shift
      ;;
    --stocks)
      MODE="stocks"
      shift
      ;;
    --alerts)
      MODE="alerts"
      shift
      ;;
    --events)
      MODE="events"
      shift
      ;;
    --sql)
      if [[ $# -lt 2 || -z "$2" ]]; then
        echo "--sql 옵션에는 실행할 SQL 문자열이 필요합니다." >&2
        exit 1
      fi
      MODE="sql"
      SQL="$2"
      shift 2
      ;;
    --limit)
      if [[ $# -lt 2 || ! "$2" =~ ^[0-9]+$ ]]; then
        echo "--limit 옵션에는 숫자 값이 필요합니다." >&2
        exit 1
      fi
      LIMIT="$2"
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "알 수 없는 옵션입니다: $1" >&2
      usage >&2
      exit 1
      ;;
  esac
done

if [[ "${COMPOSE_MODE}" == "dev" ]]; then
  COMPOSE_FILE="${ROOT_DIR}/infra/docker/docker-compose.yml"
else
  COMPOSE_FILE="${ROOT_DIR}/infra/docker/docker-compose.images.yml"
fi

if [[ -f "${ENV_FILE}" ]]; then
  COMPOSE_ENV_ARGS=(--env-file "${ENV_FILE}")
fi

if ! command -v docker >/dev/null 2>&1; then
  echo "docker 명령을 찾을 수 없습니다. Docker Desktop 또는 Docker Engine을 먼저 설치하세요." >&2
  exit 1
fi

if ! docker compose version >/dev/null 2>&1; then
  echo "docker compose 명령을 사용할 수 없습니다. Docker Compose v2를 확인하세요." >&2
  exit 1
fi

case "${MODE}" in
  tables)
    SQL="select table_schema, table_name from information_schema.tables where table_schema = current_schema() order by table_name;"
    ;;
  stocks)
    SQL="select symbol, name, market, enabled, updated_at from stocks order by symbol limit ${LIMIT};"
    ;;
  alerts)
    SQL="select ar.id, s.symbol, s.name, ar.type, ar.target_price, ar.change_rate, ar.enabled, ar.deleted, ar.created_at from alert_rules ar join stocks s on s.id = ar.stock_id order by ar.created_at desc limit ${LIMIT};"
    ;;
  events)
    SQL="select ae.id, s.symbol, s.name, ae.trigger_price, ae.trigger_change_rate, ae.status, ae.sent_at from alert_events ae join stocks s on s.id = ae.stock_id order by ae.sent_at desc limit ${LIMIT};"
    ;;
  sql)
    ;;
  *)
    echo "지원하지 않는 모드입니다: ${MODE}" >&2
    exit 1
    ;;
esac

echo "DB 값을 확인합니다."
echo "Compose: ${COMPOSE_FILE}"
echo "Mode:    ${MODE}"
echo

docker compose "${COMPOSE_ENV_ARGS[@]}" -f "${COMPOSE_FILE}" exec -T postgres \
  sh -c 'PGPASSWORD="${POSTGRES_PASSWORD}" psql -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" -v ON_ERROR_STOP=1 -P pager=off -c "$1"' \
  sh "${SQL}"
