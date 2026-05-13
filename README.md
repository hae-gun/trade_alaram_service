# trade_alaram_service

국내 주식 관심종목을 등록하고, 사용자가 설정한 가격/등락률 조건에 도달하면 알림을 보내는 주식 가격 알림 서비스입니다.
자동매매는 포함하지 않으며, 현재 목표는 **검증 가능한 가격 알림 MVP**입니다.

## 주요 기능

- 카카오 소셜 로그인
- 국내 주식 종목 검색
- 관심종목 등록/삭제
- 종목 선택 시 알림 조건 팝업 생성
- 목표가 이상/이하, 상승률/하락률 알림 조건
- 관심종목 및 활성 알림 종목의 현재가 주기 갱신
- 알림 조건 평가 및 중복 발송 방지
- Slack Webhook 알림 송신
- 알림 발송 이력 조회
- Docker Compose 기반 로컬 실행
- GitHub Actions 기반 CI/CD 및 GHCR 이미지 배포

## 프로젝트 구조

```text
.
├── backend                 # Kotlin + Spring Boot API 서버
├── frontend                # Next.js 대시보드
├── infra/docker            # Docker Compose 설정
├── scripts                 # 로컬 실행 스크립트
└── .github/workflows       # CI/CD 워크플로우
```

## Backend

백엔드는 Kotlin/Spring Boot 기반 모듈형 모놀리식 구조입니다.

```text
backend/src/main/kotlin/com/tradealarm
├── domain
│   ├── auth                # 카카오 로그인
│   ├── user                # 사용자
│   ├── stock               # 종목
│   ├── watchlist           # 관심종목
│   ├── market              # KIS 현재가 조회 및 가격 스냅샷
│   ├── alert               # 알림 조건 및 평가
│   └── notification        # Slack 송신 및 발송 이력
└── global                  # 공통 설정, 예외, 보안, lock
```

### 시세 갱신

관심종목과 활성 알림 조건에 연결된 종목을 주기적으로 갱신합니다.

- 스케줄러: `MarketPriceRefreshScheduler`
- 기본 최초 지연: `MARKET_PRICE_INITIAL_DELAY_MS=30000`
- 기본 반복 주기: `MARKET_PRICE_REFRESH_DELAY_MS=60000`
- 종목 간 요청 간격: `MARKET_PRICE_REQUEST_DELAY_MS=1500`
- KIS가 비활성화되어 있거나 캐시가 없으면 MVP용 mock 스냅샷을 생성합니다.

운영 KIS를 쓰려면 `.env`에 운영 URL과 운영 키를 설정해야 합니다.

```env
KIS_ENABLED=true
KIS_BASE_URL=https://openapi.koreainvestment.com:9443
KIS_APP_KEY=
KIS_APP_SECRET=
```

모의투자 URL은 아래 값입니다.

```env
KIS_BASE_URL=https://openapivts.koreainvestment.com:29443
```

### 알림 평가

- 스케줄러: `AlertEvaluationScheduler`
- 평가 주기: 60초
- 평가 대상: `enabled=true`이고 `deleted=false`인 알림 조건
- 조건 충족 시 `alert_events`에 발송 이력을 저장합니다.

알림 조건:

```text
ABOVE_PRICE: 현재가 >= 목표가
BELOW_PRICE: 현재가 <= 목표가
UP_RATE:     현재 등락률 >= 설정 등락률
DOWN_RATE:   현재 등락률 <= -설정 등락률
```

반복 정책:

```text
ONCE:     1회 발송 후 조건 비활성화
COOLDOWN: 마지막 발송 후 30분이 지나야 재발송
```

삭제된 알림 조건은 soft delete 처리되어 발송 이력 참조는 보존하고, 목록/평가 대상에서는 제외합니다.

### Slack 알림

Slack은 Incoming Webhook 기반입니다. Webhook은 채널 단위이므로 기본적으로 하나의 Slack 채널에 메시지를 보냅니다.
사용자별 구분이 필요하면 Slack user id 또는 mention 값을 알림 채널로 등록해 메시지 앞에 mention을 붙입니다.

환경변수:

```env
SLACK_ENABLED=true
SLACK_WEBHOOK_URL=https://hooks.slack.com/services/...
```

사용자별 Slack mention 등록:

```bash
curl -X POST http://localhost:8080/api/notifications/channels/slack \
  -H "Content-Type: application/json" \
  -d '{"mention":"U123456"}'
```

알림 이력 상태:

```text
PENDING: Slack 비활성화
SENT:    Slack Webhook 전송 성공
FAILED:  Slack Webhook 전송 실패
```

## Frontend

프론트엔드는 Next.js 14, React 18, TypeScript 기반입니다.

```text
frontend/src
├── app
├── features
│   ├── auth
│   ├── stocks
│   ├── watchlist
│   ├── alerts
│   └── notifications
├── lib
└── styles
```

주요 화면 흐름:

- Kakao SNS 로그인 화면
- 종목 검색
- 관심종목 관리
- 종목 선택 시 알림 설정 팝업
- 알림 조건 팝업
- 발송 이력 팝업

프론트는 런타임 설정 API에서 카카오 공개 설정을 읽습니다.

```text
GET /api/runtime-config
```

## API

주요 API:

```text
GET    /api/users/me

GET    /api/stocks
GET    /api/stocks/{stockId}

GET    /api/watchlist
POST   /api/watchlist
DELETE /api/watchlist/{stockId}

GET    /api/market/stocks/{stockId}/price

GET    /api/alerts
POST   /api/alerts
PATCH  /api/alerts/{ruleId}
DELETE /api/alerts/{ruleId}

GET    /api/notifications/events
GET    /api/notifications/channels
POST   /api/notifications/channels/email
POST   /api/notifications/channels/slack

POST   /api/auth/kakao/login
```

## 환경변수

실제 값은 `infra/docker/.env`에 작성합니다. `.env`는 Git에 포함하지 않습니다.
샘플은 `.env.example`을 참고하세요.

필수 또는 주요 변수:

```env
DB_NAME=tradealarm
DB_SCHEMA=tradealarm_app
DB_USERNAME=tradealarm_app
DB_PASSWORD=tradealarm_app

KIS_ENABLED=false
KIS_BASE_URL=https://openapivts.koreainvestment.com:29443
KIS_APP_KEY=
KIS_APP_SECRET=

KAKAO_REST_API_KEY=
KAKAO_CLIENT_SECRET=
KAKAO_REDIRECT_URI=http://localhost:3000/auth/kakao/callback

SLACK_ENABLED=false
SLACK_WEBHOOK_URL=

NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
NEXT_PUBLIC_KAKAO_REST_API_KEY=
NEXT_PUBLIC_KAKAO_REDIRECT_URI=http://localhost:3000/auth/kakao/callback
```

주의:

- Kakao REST API 키는 브라우저에도 노출되는 공개 키입니다.
- Kakao Admin Key를 `NEXT_PUBLIC_KAKAO_REST_API_KEY`에 넣으면 안 됩니다.
- `KAKAO_CLIENT_SECRET`은 카카오 Developers에서 Client Secret을 활성화한 경우에만 설정합니다.
- Slack Webhook URL은 민감정보이므로 코드와 로그에 노출하지 않습니다.

## 로컬 실행

### 배포된 이미지로 실행

CI/CD에서 GHCR로 push된 이미지를 pull해서 실행합니다. 로컬 Docker 빌드는 수행하지 않습니다.

```bash
cp .env.example infra/docker/.env
# infra/docker/.env 값을 로컬 환경에 맞게 수정
scripts/run-local-images.sh
```

접속 주소:

```text
Frontend: http://localhost:3000
Backend:  http://localhost:8080
```

`.env` 변경 후 기존 컨테이너에 반영하려면 재생성이 필요합니다.

```bash
docker compose --env-file infra/docker/.env -f infra/docker/docker-compose.images.yml up -d --force-recreate backend frontend
```

완전히 다시 시작하려면:

```bash
docker compose --env-file infra/docker/.env -f infra/docker/docker-compose.images.yml down
scripts/run-local-images.sh
```

### 로컬 Docker Compose 개발 실행

개발용 Compose는 backend/frontend를 로컬 소스에서 빌드합니다.

```bash
docker compose --env-file infra/docker/.env -f infra/docker/docker-compose.yml up -d --build
```

프로젝트 규칙상 일반 작업 검증은 Docker Compose 기반으로 수행합니다.

## 로그 확인

전체 로그:

```bash
docker compose --env-file infra/docker/.env -f infra/docker/docker-compose.images.yml logs
```

서비스별 실시간 로그:

```bash
docker compose --env-file infra/docker/.env -f infra/docker/docker-compose.images.yml logs -f backend
docker compose --env-file infra/docker/.env -f infra/docker/docker-compose.images.yml logs -f frontend
docker compose --env-file infra/docker/.env -f infra/docker/docker-compose.images.yml logs -f postgres
docker compose --env-file infra/docker/.env -f infra/docker/docker-compose.images.yml logs -f redis
```

최근 100줄:

```bash
docker compose --env-file infra/docker/.env -f infra/docker/docker-compose.images.yml logs --tail=100 backend
```

컨테이너 상태:

```bash
docker compose --env-file infra/docker/.env -f infra/docker/docker-compose.images.yml ps
```

## 알림 동작 확인

발송 이력 API:

```bash
curl http://localhost:8080/api/notifications/events
```

DB 직접 확인:

```bash
docker compose --env-file infra/docker/.env -f infra/docker/docker-compose.images.yml exec postgres \
  psql -U tradealarm_app -d tradealarm -c \
  "select sent_at, message, status, trigger_price, trigger_change_rate from tradealarm_app.alert_events order by sent_at desc limit 20;"
```

Slack 송신을 확인하려면:

1. `infra/docker/.env`에 `SLACK_ENABLED=true`, `SLACK_WEBHOOK_URL` 설정
2. backend 컨테이너 재생성
3. 알림 조건 생성
4. 조건 충족 후 Slack 채널과 `alert_events.status` 확인

## 테스트

Backend:

```bash
docker run --rm \
  -v "$PWD:/workspace" \
  -v trade_alarm_gradle_cache:/home/gradle/.gradle \
  -w /workspace/backend \
  gradle:8.10-jdk21 gradle test
```

Frontend:

```bash
cd frontend
npm run build
```

Compose 설정 검증:

```bash
docker compose --env-file infra/docker/.env -f infra/docker/docker-compose.images.yml config --quiet
```

## CI/CD

GitHub Actions:

- CI: PR에서 backend test/build, frontend build, Docker Compose config 검증
- CD: `develop` push 시 backend/frontend 이미지를 GHCR에 push
- 이미지 태그:
  - `develop`
  - `develop-{commit-sha}`

기본 로컬 이미지:

```text
ghcr.io/hae-gun/trade_alaram_service-backend:develop
ghcr.io/hae-gun/trade_alaram_service-frontend:develop
```

Apple Silicon Mac에서도 실행할 수 있도록 multi-platform 이미지를 push합니다.

## 설계 원칙

- 자동매매와 가격 알림 MVP를 분리합니다.
- Controller에 비즈니스 로직을 넣지 않습니다.
- 알림 조건 평가는 테스트 가능한 도메인 로직으로 유지합니다.
- 민감정보는 환경변수로만 주입합니다.
- 발송 이력은 보존하고, 알림 조건 삭제는 soft delete로 처리합니다.
- 외부 송신 실패는 `alert_events.status`로 추적합니다.
