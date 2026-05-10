#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
COMPOSE_FILE="${ROOT_DIR}/infra/docker/docker-compose.images.yml"
ENV_FILE="${ROOT_DIR}/infra/docker/.env"
COMPOSE_ENV_ARGS=()

if [[ -f "${ENV_FILE}" ]]; then
  COMPOSE_ENV_ARGS=(--env-file "${ENV_FILE}")
fi

export IMAGE_TAG="${IMAGE_TAG:-develop}"
export BACKEND_IMAGE="${BACKEND_IMAGE:-ghcr.io/hae-gun/trade_alaram_service-backend:${IMAGE_TAG}}"
export FRONTEND_IMAGE="${FRONTEND_IMAGE:-ghcr.io/hae-gun/trade_alaram_service-frontend:${IMAGE_TAG}}"

if ! command -v docker >/dev/null 2>&1; then
  echo "docker 명령을 찾을 수 없습니다. Docker Desktop 또는 Docker Engine을 먼저 설치하세요." >&2
  exit 1
fi

if ! docker compose version >/dev/null 2>&1; then
  echo "docker compose 명령을 사용할 수 없습니다. Docker Compose v2를 확인하세요." >&2
  exit 1
fi

if [[ -n "${DOCKER_PLATFORM:-}" ]]; then
  export DOCKER_DEFAULT_PLATFORM="${DOCKER_PLATFORM}"
fi

echo "로컬 실행 이미지를 가져옵니다."
echo "Backend:  ${BACKEND_IMAGE}"
echo "Frontend: ${FRONTEND_IMAGE}"
if [[ -n "${DOCKER_DEFAULT_PLATFORM:-}" ]]; then
  echo "Platform: ${DOCKER_DEFAULT_PLATFORM}"
fi

docker compose "${COMPOSE_ENV_ARGS[@]}" -f "${COMPOSE_FILE}" pull backend frontend
docker compose "${COMPOSE_ENV_ARGS[@]}" -f "${COMPOSE_FILE}" up -d

echo
echo "서비스 실행 상태"
docker compose "${COMPOSE_ENV_ARGS[@]}" -f "${COMPOSE_FILE}" ps

echo
echo "Frontend: http://localhost:3000"
echo "Backend:  http://localhost:8080"
