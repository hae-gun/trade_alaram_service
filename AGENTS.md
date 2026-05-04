# AGENTS.md

## Project

이 프로젝트는 한국 주식 가격 알림 서비스(MVP)이다.  
자동매매 기능은 포함하지 않으며, 사용자가 설정한 조건에 따라 알림을 제공하는 것을 목표로 한다.

---

## Mandatory Workflow (필수 작업 규칙)

### 1. Git 브랜치 / 커밋 / PR 규칙

모든 작업은 반드시 별도 브랜치에서 수행한다.

#### 작업 흐름

1. `develop` 브랜치 기준으로 시작한다.
2. 최신 코드를 pull 한다.
3. 새로운 작업 브랜치를 생성한다.
4. 요청된 범위 내에서만 작업한다.
5. 검증을 수행한다.
6. 작업 단위로 commit 한다.
7. `develop` 브랜치로 PR을 생성한다.
8. 변경사항, 검증 결과, 리스크를 기록한다.
9. 검증 완료 후 merge 한다.

#### 브랜치 네이밍

feature/{작업명}  
fix/{작업명}  
refactor/{작업명}  
docs/{작업명}

#### 커밋 메시지 규칙 (한국어)

feat: 알림 조건(AlertRule) 도메인 추가  
fix: 중복 알림 발생 문제 수정  
refactor: 알림 평가 로직 분리  
docs: 알림 시나리오 문서 추가  

#### 금지 사항

- main / develop 브랜치 직접 작업 금지

#### 작업 시작 전 필수 확인

git status  
git branch --show-current  

---

### 2. Build 및 검증 규칙 (Docker 기반)

모든 빌드 및 테스트는 Docker Compose 기반으로 수행한다.

#### 기본 명령어

docker compose up -d  
docker compose ps  
docker compose logs  
docker compose exec backend ./gradlew test  
docker compose exec backend ./gradlew build  

#### 프론트엔드가 있는 경우

docker compose exec frontend npm run build  

#### 완료 기준

- 테스트 통과  
- 빌드 성공  

#### 실패 시

1. 실패 원인 설명  
2. 수정  
3. 재검증  
4. 결과 기록  

---

### 3. 민감정보 처리 규칙 (보안)

민감 정보는 절대 코드에 하드코딩하지 않는다.

#### 환경 변수 사용 예시

KIS_APP_KEY=  
KIS_APP_SECRET=  
JWT_SECRET=  
DB_USERNAME=  
DB_PASSWORD=  
REDIS_PASSWORD=  
SMTP_USERNAME=  
SMTP_PASSWORD=  

#### 필수 파일

.env.example  
application-local.yml  
application-docker.yml  

#### 규칙

- .env 파일은 Git에 포함 금지  
- .env.example은 포함  
- 로그에 민감정보 출력 금지  

#### .gitignore

.env  
.env.*  
!.env.example  

---

## Language Policy (언어 정책)

- 모든 설명은 한국어로 작성  
- 코드 주석은 한국어  
- 커밋/PR 설명 한국어  

---

## Codex 작업 규칙

- AGENTS.md 먼저 읽기  
- 작업 계획 먼저 수립  
- 작은 단위로 구현  
- 테스트 코드 필수  

---

## Codex Task Completion Report

## Task Summary
- Branch:
- Commit:
- PR Target:
- Changed Files:
- Verification:
- Sensitive Information Check:
- Remaining Risks:

---

## Architecture Rules

- 모듈형 모놀리식 구조  
- Controller에 비즈니스 로직 금지  
- 도메인 로직 테스트 가능  

---

## MVP Domain Rules

- 관심 종목 등록 가능  
- 알림 조건 설정 가능  
- 조건 충족 시 알림 발생  
- 중복 알림 방지  

---

## 금지 사항

- 테스트 없는 코드  
- 민감정보 하드코딩  
- develop 직접 작업  

---
## 빌드관련 동작 조건
- 작성된 도커 이미지는 push 만 진행하고 직접 빌드 하지 않는다.
- 빌드가 필요한 경우, PR 생성 후 CI에서 빌드가 진행되도록 한다.
- 로컬에서 직접 사용하는 경우 사용자가 사용할수 있는 빌드 쉘을 구성하여 사용자가 실행하도록 한다.

