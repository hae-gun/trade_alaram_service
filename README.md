# trade_alaram_service

국내 주식 관심종목을 등록하고, 사용자가 설정한 가격/변동률 조건에 도달했을 때 알림을 보내는 주식 가격 알리미 서비스입니다.

초기 목표는 자동매매가 아니라 **검증 가능한 가격 알림 MVP**입니다. 이후 모의매매, 전략 검증, 자동매매로 확장할 수 있도록 백엔드와 프론트엔드를 분리한 모노레포 구조로 시작합니다.

## 1차 MVP 범위

- 소셜 로그인 기반 사용자 관리
- 국내 주식 종목 목록/검색
- 관심종목 등록/삭제
- 종목별 가격/변동률 알림 조건 등록
- 관심종목 가격 감시
- 조건 충족 시 이메일 또는 카카오 알림 발송
- 알림 발송 이력 관리

## 프로젝트 구조

```text
.
├── backend
│   └── src
│       ├── main
│       │   ├── kotlin/com/tradealarm
│       │   │   ├── global
│       │   │   └── domain
│       │   └── resources
│       └── test/kotlin/com/tradealarm
├── frontend
│   └── src
│       ├── app
│       ├── components
│       ├── features
│       ├── lib
│       └── styles
├── infra
│   └── docker
```

## Backend

백엔드는 Kotlin 기반 Spring Boot 구조입니다. 도메인별로 API, 유스케이스, 도메인 모델, 외부 연동 코드를 분리합니다.

### `backend/src/main/kotlin/com/tradealarm/global`

전역 공통 관심사를 모아둡니다.

```text
global
├── config
├── security
├── exception
└── common
```

- `config`: Spring 설정, Jackson, CORS, Scheduler, OpenAPI 문서 설정 등
- `security`: OAuth2 로그인, JWT/세션, 인증 사용자 처리
- `exception`: 공통 예외, 에러 응답, 글로벌 예외 핸들러
- `common`: 공통 응답, 날짜/시간 유틸, 페이지 응답, 공통 상수

### `backend/src/main/kotlin/com/tradealarm/domain`

서비스의 핵심 업무 도메인을 분리합니다.

```text
domain
├── auth
├── user
├── stock
├── watchlist
├── alert
├── notification
└── market
```

각 도메인은 기본적으로 아래 계층을 가집니다.

```text
api
application
domain
infra
```

- `api`: Controller, request/response DTO
- `application`: 서비스 유스케이스, 트랜잭션 경계, 도메인 조합 로직
- `domain`: Entity, 값 객체, 도메인 정책, Repository 인터페이스
- `infra`: JPA 구현체, 외부 API Client, 메시지/메일 발송 어댑터

### `domain/auth`

소셜 로그인과 인증 흐름을 담당합니다.

- OAuth2 provider 연동
- 로그인 성공 후 사용자 식별
- 토큰 또는 세션 발급
- 인증 사용자 조회

### `domain/user`

사용자 프로필과 사용자별 설정을 담당합니다.

- 사용자 기본 정보
- 알림 수신 동의
- 기본 알림 채널
- 계정 상태 관리

### `domain/stock`

주식 종목 마스터 데이터를 담당합니다.

- 국내 주식 종목 코드/종목명/시장 구분 저장
- 종목 검색
- 종목 활성/비활성 관리
- 한국투자증권 또는 외부 데이터 기준 종목 동기화

### `domain/watchlist`

사용자 관심종목을 담당합니다.

- 관심종목 추가/삭제
- 사용자별 관심종목 목록 조회
- 가격 감시 대상 종목 산출

### `domain/alert`

가격 알림 규칙과 조건 평가를 담당합니다.

- 목표가 이상/이하 알림
- 전일 대비 상승률/하락률 알림
- 중복 알림 방지
- 알림 재발송 정책
- 조건 충족 이벤트 기록

### `domain/notification`

알림 채널과 실제 발송을 담당합니다.

- 이메일 발송
- 카카오 알림톡 연동
- 향후 Web Push/FCM 확장
- 발송 성공/실패 이력 관리

### `domain/market`

외부 시세 데이터 연동과 가격 감시 작업을 담당합니다.

- 한국투자증권 Open API 연동
- 현재가 조회
- 장 운영시간/휴장일 처리
- 관심종목 가격 polling 또는 websocket 구독
- 가격 스냅샷 저장

## Frontend

프론트엔드는 Next.js 기반 React 구조입니다. 모바일 웹/PWA 확장을 고려합니다.

```text
frontend/src
├── app
├── components
├── features
├── lib
└── styles
```

- `app`: 라우팅, 페이지, 앱 진입점
- `components`: 버튼, 입력창, 모달, 테이블 등 재사용 UI
- `features/auth`: 로그인/로그아웃 화면과 인증 상태
- `features/stocks`: 종목 검색, 종목 상세
- `features/watchlist`: 관심종목 목록과 편집
- `features/alerts`: 알림 조건 생성/수정/삭제
- `features/notifications`: 알림 이력과 알림 채널 설정
- `lib`: API client, date/number formatter, 공통 helper
- `styles`: 전역 스타일, 토큰, 레이아웃 스타일

## Docs

`docs`는 기획과 설계 문서를 관리합니다.

- API 명세
- 데이터 모델
- 알림 정책
- 한국투자증권 API 조사 내용
- 운영/배포 체크리스트
- 추후 자동매매 확장 설계

## Infra

`infra`는 로컬 개발과 배포 환경 구성을 관리합니다.

- `infra/docker`: PostgreSQL, Redis, backend, frontend 등의 Docker 설정
- 추후 AWS/GCP/Naver Cloud 배포 리소스 정의

## Scripts

`scripts`는 반복 작업을 자동화하는 스크립트를 관리합니다.

- 종목 마스터 동기화
- 로컬 개발 환경 초기화
- 테스트 데이터 생성
- 배포 보조 스크립트

## 설계 원칙

- 가격 알림 MVP와 자동매매 기능을 분리한다.
- 처음에는 관심종목만 감시한다.
- 알림은 중복 발송을 방지한다.
- 외부 API 연동 코드는 `infra`에 격리한다.
- 도메인 정책은 `domain`에 두고, Controller에 업무 로직을 넣지 않는다.
- 카카오 알림톡은 템플릿 심사와 정책 제약이 있으므로 이메일/Web Push와 병행 가능하게 설계한다.

## 다음 구현 순서

1. 한국투자증권 Open API 인증/현재가 조회 연동
2. 이메일 발송 어댑터 구현
3. PostgreSQL 프로필 기준 마이그레이션 도입
4. 소셜 로그인 provider 설정
5. 알림 조건 평가 결과를 실제 이메일/카카오 발송과 연결
6. 카카오 알림톡 템플릿/발송 연동

## 현재 소스 구성

### Backend

- Kotlin 1.9.25
- Spring Boot 3.3.5
- Spring Web
- Spring Data JPA
- Spring Security/OAuth2 Client
- H2 file DB
- PostgreSQL driver

현재 소셜 로그인은 제외하고, 모든 MVP API는 `admin@tradealarm.local` 계정을 기준으로 동작합니다.

로컬 기본 DB는 파일 기반 H2입니다.

```text
backend/data/tradealarm.mv.db
```

스키마 전략은 `spring.jpa.hibernate.ddl-auto=update`입니다. 로컬 DB 파일은 `.gitignore`에 포함되어 커밋하지 않습니다.

주요 API:

```text
GET    /api/users/me
GET    /api/stocks
GET    /api/stocks/{stockId}
GET    /api/watchlist
POST   /api/watchlist
DELETE /api/watchlist/{stockId}
GET    /api/alerts
POST   /api/alerts
PATCH  /api/alerts/{ruleId}
DELETE /api/alerts/{ruleId}
GET    /api/market/stocks/{stockId}/price
GET    /api/notifications/events
GET    /api/notifications/channels
POST   /api/notifications/channels/email
```

초기 실행 시 삼성전자, SK하이닉스, NAVER, 카카오 등 샘플 종목이 H2 DB에 적재됩니다. 현재 시세는 한국투자증권 연동 전 단계이므로 mock 가격을 생성합니다.

### Frontend

- Next.js 14
- React 18
- TypeScript
- App Router
- lucide-react

첫 화면은 MVP 대시보드입니다.

- 종목 검색 및 관심종목 등록
- 관심종목 조회/삭제
- 선택 종목 기준 알림 조건 생성
- 알림 조건 on/off 및 삭제
- 이메일 알림 채널 등록
- 알림 발송 이력 조회

프론트엔드는 `http://localhost:8080` 백엔드 API를 호출합니다.

## 테스트

### Backend

```bash
gradle :backend:test
```

테스트는 `test` 프로필로 실행되며, 로컬 개발 DB와 분리된 H2 인메모리 DB를 사용합니다. API 통합 테스트는 MockMvc로 HTTP 계약을 검증합니다.

검증 범위:

- `GET /api/users/me`
- `GET /api/stocks`
- `POST/GET/DELETE /api/watchlist`
- `POST/GET/PATCH/DELETE /api/alerts`
- `POST/GET /api/notifications/channels`
- 필수 요청값 누락 시 `400 Bad Request`

### Frontend

```bash
cd frontend
npm run build
```

## 로컬 실행

### Docker Compose

프론트엔드, 백엔드, PostgreSQL, Redis를 한 번에 실행하려면:

```bash
docker compose -f infra/docker/docker-compose.yml up -d --build
```

접속 주소:

```text
Frontend: http://localhost:3000
Backend:  http://localhost:8080
Postgres: localhost:5432
Redis:    localhost:6379
```

컨테이너 로그 확인:

```bash
docker compose -f infra/docker/docker-compose.yml logs -f backend
docker compose -f infra/docker/docker-compose.yml logs -f frontend
```

종료:

```bash
docker compose -f infra/docker/docker-compose.yml down
```

### Backend

```bash
./gradlew :backend:bootRun
```

현재 저장소에는 Gradle Wrapper가 아직 없을 수 있습니다. 이 경우 로컬 Gradle이 설치되어 있다면 아래 명령을 사용할 수 있습니다.

```bash
gradle :backend:bootRun
```

PostgreSQL/Redis를 함께 띄워 local 프로필로 실행하려면:

```bash
docker compose -f infra/docker/docker-compose.yml up -d
gradle :backend:bootRun --args='--spring.profiles.active=local'
```

현재 `local` 프로필은 로컬 PostgreSQL의 `tradealarm_app` 스키마를 사용합니다.

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tradealarm?currentSchema=tradealarm_app
    username: tradealarm_app
    password: tradealarm_app
```

`local` 프로필에서는 Flyway가 `backend/src/main/resources/db/migration`의 마이그레이션을 적용하고, Hibernate는 `ddl-auto=validate`로 엔티티와 스키마 정합성만 확인합니다.

Redis는 local 프로필에서 `localhost:6379`를 사용합니다. 현재 용도는 알림 평가 스케줄러의 중복 실행 방지 lock입니다.

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

프론트엔드 기본 주소는 `http://localhost:3000`입니다.
