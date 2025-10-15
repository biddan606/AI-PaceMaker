# JWT 기반 인증 시스템 구현

## 개요

이 문서는 JWT (JSON Web Token) 기반 인증 시스템의 구현 내용을 설명합니다. 코드를 읽기 전에 전체적인 아키텍처와 설계 결정 사항을 이해하기 위한 문서입니다.

## 요구사항

1. 회원가입 시 이름, 이메일, 비밀번호 입력
2. 이메일 인증 필요 (미인증 시에도 로그인 가능하지만 제한된 기능)
3. 로그인 시 Access Token (1시간)과 Refresh Token (30일) 발급
4. Refresh Token을 통한 자동 세션 갱신
5. 다중 디바이스 로그인 지원
6. 만료/유효하지 않은 토큰에 대한 적절한 에러 처리

## 아키텍처

### Clean Architecture 레이어 구조

```
auth/
├── model/               # 도메인 모델 & 리포지토리 인터페이스
│   ├── User.java
│   ├── RefreshToken.java
│   ├── EmailVerificationToken.java
│   ├── UserRepository.java (JpaRepository 직접 확장)
│   ├── RefreshTokenRepository.java (JpaRepository 직접 확장)
│   └── EmailVerificationTokenRepository.java (JpaRepository 직접 확장)
├── usecase/            # 비즈니스 로직
│   ├── RegisterUser.java
│   ├── VerifyEmail.java
│   ├── LoginUser.java
│   └── RenewAccessToken.java
├── endpoint/           # API 엔드포인트
│   ├── AuthController.java
│   └── AuthExceptionHandler.java
├── infrastructure/     # 인프라 구현
│   └── JwtTokenProvider.java
└── fixture/           # 테스트 픽스처 (테스트 코드)
    ├── UserFixture.java
    ├── RefreshTokenFixture.java
    └── EmailVerificationTokenFixture.java
```

## 핵심 컴포넌트

### 1. User 엔티티

사용자 기본 정보를 저장하는 엔티티입니다.

**주요 필드:**
- `id`: 사용자 고유 식별자
- `name`: 사용자 이름 (회원가입 시 추가된 필드)
- `email`: 이메일 (유니크 제약조건)
- `password`: BCrypt로 암호화된 비밀번호
- `emailVerified`: 이메일 인증 여부
- `createdAt`: 계정 생성 시간

### 2. RefreshToken 엔티티

다중 디바이스 로그인을 위한 Refresh Token 정보를 저장합니다.

**주요 필드:**
- `id`: 토큰 고유 식별자
- `userId`: 토큰 소유자 ID
- `deviceId`: 디바이스 식별자 (다중 디바이스 지원)
- `token`: JWT Refresh Token 문자열
- `expiresAt`: 토큰 만료 시간

**설계 결정:**
- `userId + deviceId` 복합 유니크 제약 조건
- 같은 디바이스에서 재로그인 시 기존 토큰 업데이트 (DELETE + INSERT가 아닌 UPDATE)
- deviceId를 통한 디바이스별 독립적인 세션 관리
- `updateToken()` 메서드로 토큰 갱신

### 3. JwtTokenProvider

JWT 토큰 생성 및 검증을 담당하는 인프라스트럭처 컴포넌트입니다.

**주요 기능:**
- Access Token 생성 (userId, email 포함)
- Refresh Token 생성 (userId, deviceId 포함)
- 토큰 유효성 검증
- 토큰에서 정보 추출 (userId, email, deviceId)

**토큰 구조:**
- **Access Token**: userId, email, type, 발급/만료 시간 (1시간)
- **Refresh Token**: userId, deviceId, type, 발급/만료 시간 (30일)
- deviceId를 Refresh Token에 포함하여 디바이스별 토큰 관리 및 보안 강화

### 4. LoginUser UseCase

로그인 비즈니스 로직을 처리합니다.

**프로세스:**
1. 이메일로 사용자 조회
2. 비밀번호 검증 (BCrypt)
3. Access Token 생성
4. Refresh Token 생성 (deviceId 포함)
5. DB에서 기존 Refresh Token 조회 (userId + deviceId)
6. 존재하면 UPDATE, 없으면 INSERT
7. 토큰 정보 반환 (emailVerified 플래그 포함)

**특징:**
- 이메일 미인증 사용자도 로그인 가능 (emailVerified: false 반환)
- 다중 디바이스 지원 (deviceId 기반)
- @Transactional로 원자성 보장
- Command-Result 패턴 사용

### 5. RenewAccessToken UseCase

Refresh Token으로 새로운 Access Token을 발급합니다.

**프로세스:**
1. JWT 형식 검증
2. userId와 deviceId 추출
3. DB에서 해당 Refresh Token 조회 (userId + deviceId)
4. 토큰 일치 여부 검증
5. 만료 여부 검증
6. 새로운 Access Token 발급

**보안:**
- JWT 검증 + DB 검증 이중 체크
- deviceId 일치 여부 확인 (토큰 도용 방지)
- 만료된 토큰 재사용 불가

## Repository 구조

**설계 철학:** 불필요한 어댑터 계층을 제거하고 JpaRepository를 직접 확장하여 간결성 확보

**주요 Repository:**
- `UserRepository`: 사용자 조회 (이메일 기반, 존재 여부 확인)
- `RefreshTokenRepository`: Refresh Token 관리 (토큰 조회, userId+deviceId 조회, 삭제)
- `EmailVerificationTokenRepository`: 이메일 인증 토큰 관리 (토큰 조회, 사용자별 삭제)

각 Repository는 Spring Data JPA의 메서드 네이밍 규칙을 활용하여 커스텀 쿼리 메서드 정의

## API 엔드포인트

### AuthController

**회원가입 및 인증:**
- `POST /api/users` - 회원가입 (이름, 이메일, 비밀번호)
- `POST /api/users/verification` - 이메일 인증

**로그인 및 토큰 관리:**
- `POST /api/auth/login` - 로그인 (이메일, 비밀번호, deviceId)
- `POST /api/auth/refresh` - Access Token 갱신

### AuthExceptionHandler

**에러 응답 형식:** RFC 7807 ProblemDetail 표준

**HTTP 상태 코드별 예외 매핑:**
- `400 Bad Request`: 비밀번호 정책 위반, 만료된 인증 토큰
- `401 Unauthorized`: 잘못된 인증 정보, 유효하지 않거나 만료된 Refresh Token
- `404 Not Found`: 사용자 없음, 유효하지 않은 인증 토큰
- `409 Conflict`: 중복 이메일

## 테스트 전략

### 테스트 피라미드 (TDD 접근)

**1. 통합 테스트 (80%)**
- `RegisterUserTest`: 회원가입 Event-Driven 플로우 검증
- `VerifyEmailTest`: 이메일 인증 프로세스
- `LoginUserTest`: 로그인 및 다중 디바이스 시나리오
- `RenewAccessTokenTest`: 토큰 갱신 및 보안 검증

**2. API 테스트 (15%)**
- `AuthControllerTest`: HTTP 레이어 검증 (@WebMvcTest 사용)
- 요청/응답 직렬화, HTTP 상태 코드, 입력 검증 에러

**3. 단위 테스트 (5%)**
- `JwtTokenProviderTest`: JWT 생성/검증 로직

### 테스트 픽스처

재사용 가능한 테스트 데이터 생성 유틸리티로 테스트 코드 간소화:
- `UserFixture`: User 엔티티 빌더 (verifiedUser, unverifiedUser 등)
- `RefreshTokenFixture`: RefreshToken 엔티티 빌더 (유효/만료 토큰)
- `EmailVerificationTokenFixture`: 이메일 인증 토큰 빌더

## 보안 설계

### 비밀번호 암호화
- **알고리즘:** BCrypt
- **Strength:** 프로덕션 10, 테스트 4 (성능 최적화)
- **설정:** `application.yml` (프로덕션), `application-test.yml` (테스트)

### JWT 보안
- **서명 알고리즘:** HMAC SHA-256
- **Secret Key:** 환경변수 또는 설정 파일에서 주입
- **최소 키 길이:** 256 bits 이상 권장

### Refresh Token 보안
- **저장소:** PostgreSQL 데이터베이스
- **검증 전략:** JWT 서명 검증 + DB 일치 여부 이중 체크
- **디바이스 바인딩:** deviceId를 통한 디바이스 소유권 검증
- **재사용 방지:** 만료된 토큰 재사용 차단

### 다중 디바이스 보안
- 각 디바이스별 독립적인 Refresh Token 발급
- deviceId 불일치 시 InvalidRefreshTokenException 발생
- 토큰 도용 시나리오 방지 (deviceId 검증)

## 다중 디바이스 로그인 구현

### 데이터베이스 스키마

refresh_tokens 테이블에 `(user_id, device_id)` 복합 유니크 제약 조건 적용

### 시나리오별 동작

**시나리오 1: 새 디바이스 로그인**
- 사용자가 새로운 디바이스에서 로그인
- DB에 새로운 RefreshToken 레코드 INSERT
- 기존 디바이스의 세션은 영향받지 않음

**시나리오 2: 기존 디바이스 재로그인**
- 사용자가 같은 디바이스에서 재로그인
- 기존 RefreshToken 레코드 UPDATE (token, expiresAt 갱신)
- DELETE + INSERT가 아닌 UPDATE로 처리하여 트랜잭션 안정성 확보

**시나리오 3: 다른 디바이스 추가 로그인**
- 사용자가 여러 디바이스에서 동시 로그인
- 각 디바이스별 독립적인 RefreshToken 유지
- 한 디바이스의 로그아웃이 다른 디바이스에 영향 없음

## 구성 파일

### application.yml (프로덕션)
- JWT secret key 설정 (환경변수 사용 권장)
- Access Token 만료 시간: 3600000ms (1시간)
- Refresh Token 만료 시간: 2592000000ms (30일)
- BCrypt strength: 10

### application-test.yml (테스트)
- BCrypt strength: 4 (테스트 실행 속도 향상)
- 기타 설정은 프로덕션과 동일

## 향후 개선 사항

**1. 인증 필터/인터셉터 (별도 태스크)**
- JWT 기반 인증 필터 구현
- SecurityContext에 사용자 정보 저장
- @AuthenticationPrincipal 지원
- 공개/보호 엔드포인트 구분

**2. 보안 강화**
- Refresh Token 암호화 저장
- Token rotation 구현
- IP 주소 기반 이상 탐지
- Rate limiting 적용

**3. 모니터링**
- 로그인 실패 횟수 추적
- 의심스러운 활동 감지 및 알림
- 토큰 사용 패턴 분석

**4. 성능 최적화**
- Refresh Token 캐싱 (Redis)
- JWT 블랙리스트 관리
- DB 쿼리 최적화

## 결론

JWT 기반 인증 시스템이 성공적으로 구현되었으며, 다중 디바이스 로그인, 이메일 인증, 자동 세션 갱신 등 모든 요구사항을 충족합니다. TDD 방식으로 개발되어 높은 테스트 커버리지를 확보했으며, Clean Architecture를 따라 유지보수성과 확장성이 우수합니다.

구체적인 구현 코드는 해당 패키지의 소스 코드를 참조하시기 바랍니다.
