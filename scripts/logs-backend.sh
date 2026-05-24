#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${ROOT_DIR}/infra/docker/.env"
COMPOSE_ENV_ARGS=()
COMPOSE_MODE="images"
FOLLOW="true"
TAIL_LINES="200"

usage() {
  cat <<'USAGE'
백엔드 컨테이너 로그를 확인합니다.

사용법:
  ./scripts/logs-backend.sh [옵션]

옵션:
  --images       배포 이미지 실행 compose 파일 사용 (기본값)
  --dev          로컬 빌드 compose 파일 사용
  --tail N       마지막 N줄부터 표시 (기본값: 200)
  --no-follow    실시간 follow 없이 현재 로그만 출력
  -h, --help     도움말 출력

예시:
  ./scripts/logs-backend.sh
  ./scripts/logs-backend.sh --tail 500
  ./scripts/logs-backend.sh --no-follow
  ./scripts/logs-backend.sh --dev
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
    --tail)
      if [[ $# -lt 2 || ! "$2" =~ ^[0-9]+$ ]]; then
        echo "--tail 옵션에는 숫자 값이 필요합니다." >&2
        exit 1
      fi
      TAIL_LINES="$2"
      shift 2
      ;;
    --no-follow)
      FOLLOW="false"
      shift
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

LOG_ARGS=(--tail "${TAIL_LINES}")
if [[ "${FOLLOW}" == "true" ]]; then
  LOG_ARGS+=(-f)
fi

echo "백엔드 로그를 확인합니다."
echo "Compose: ${COMPOSE_FILE}"
echo "Tail:    ${TAIL_LINES}"
echo "Follow:  ${FOLLOW}"
echo

docker compose "${COMPOSE_ENV_ARGS[@]}" -f "${COMPOSE_FILE}" logs "${LOG_ARGS[@]}" backend
