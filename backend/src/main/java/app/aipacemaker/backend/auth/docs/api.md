# 인증 API 문서

## 개요

사용자 인증 및 JWT 토큰 기반 세션 관리를 제공하는 API입니다.

## 엔드포인트

### 1. 회원가입

사용자 계정을 생성하고 이메일 인증 토큰을 발송합니다.

**Endpoint:** `POST /api/users`

**Request Body:**
```json
{
  "name": "홍길동",
  "email": "user@example.com",
  "password": "password123"
}
```

**Validation:**
- `name`: 필수, 비어있지 않아야 함
- `email`: 필수, 유효한 이메일 형식
- `password`: 필수, 8자 이상, 영문자와 숫자 포함

**Success Response (201 Created):**
```json
{
  "userId": 1,
  "email": "user@example.com",
  "message": "회원가입이 완료되었습니다. 이메일을 확인하여 계정을 활성화해주세요."
}
```

**Error Responses:**
- `400 Bad Request`: 비밀번호 정책 위반
- `409 Conflict`: 이미 사용 중인 이메일

---

### 2. 이메일 인증

이메일로 전송된 인증 토큰을 통해 계정을 활성화합니다.

**Endpoint:** `PUT /api/users/verification/{token}`

**Path Parameters:**
- `token`: 이메일로 전송된 인증 토큰 (UUID 문자열)

**Success Response (200 OK):**
```json
{
  "userId": 1,
  "email": "user@example.com",
  "verified": true,
  "message": "이메일 인증이 완료되었습니다."
}
```

**Error Responses:**
- `400 Bad Request`: 만료된 인증 토큰
- `404 Not Found`: 유효하지 않은 인증 토큰

---

### 3. 로그인

이메일과 비밀번호로 로그인하여 Access Token과 Refresh Token을 발급받습니다.

**Endpoint:** `POST /api/auth/login`

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "deviceId": "device-uuid-or-identifier"
}
```

**Validation:**
- `email`: 필수, 유효한 이메일 형식
- `password`: 필수
- `deviceId`: 필수, 다중 디바이스 지원을 위한 디바이스 식별자

**Success Response (200 OK):**
```json
{
  "userId": 1,
  "email": "user@example.com",
  "emailVerified": true,
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response Fields:**
- `emailVerified`: 이메일 인증 여부 (false인 경우 프론트엔드에서 인증 안내 UI 표시)
- `accessToken`: 1시간 유효한 접근 토큰
- `refreshToken`: 30일 유효한 갱신 토큰

**Error Responses:**
- `401 Unauthorized`: 잘못된 이메일 또는 비밀번호

**참고사항:**
- 이메일 미인증 사용자도 로그인 가능 (emailVerified: false 반환)
- 동일 사용자가 다른 디바이스에서 로그인 시 각 디바이스별 독립적인 Refresh Token 발급
- 같은 디바이스에서 재로그인 시 기존 Refresh Token 갱신

---

### 4. Access Token 갱신

Refresh Token을 사용하여 새로운 Access Token을 발급받습니다.

**Endpoint:** `POST /api/auth/refresh`

**Request Body:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Success Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Error Responses:**
- `401 Unauthorized`: 유효하지 않거나 만료된 Refresh Token

**참고사항:**
- Refresh Token은 JWT 형식으로 userId와 deviceId를 포함
- DB에 저장된 토큰과 일치 여부 검증
- 만료된 Refresh Token은 재사용 불가

---

## JWT 토큰 구조

### Access Token
- **만료 시간:** 1시간
- **Claims:**
  - `sub`: userId (Long)
  - `email`: 사용자 이메일
  - `type`: "access"
  - `iat`: 발급 시간
  - `exp`: 만료 시간

### Refresh Token
- **만료 시간:** 30일
- **Claims:**
  - `sub`: userId (Long)
  - `deviceId`: 디바이스 식별자 (보안 및 다중 디바이스 지원)
  - `type`: "refresh"
  - `iat`: 발급 시간
  - `exp`: 만료 시간

---

## 에러 응답 형식 (RFC 7807 ProblemDetail)

모든 에러 응답은 RFC 7807 표준을 따릅니다:

```json
{
  "type": "about:blank",
  "title": "인증 실패",
  "status": 401,
  "detail": "이메일 또는 비밀번호가 올바르지 않습니다.",
  "instance": "/api/auth/login"
}
```

---

## 보안 고려사항

1. **비밀번호 암호화:** BCrypt (strength 10)
2. **JWT 서명:** HMAC SHA-256
3. **Refresh Token 저장:** DB에 암호화되지 않은 상태로 저장 (추후 암호화 고려)
4. **다중 디바이스 지원:** deviceId를 통한 디바이스별 토큰 관리
5. **토큰 재사용 방지:** Refresh Token은 DB 검증 필수

---

## 다중 디바이스 로그인 시나리오

### 시나리오 1: 모바일과 웹에서 동시 로그인
1. 사용자가 모바일 앱에서 로그인 (deviceId: "mobile-uuid")
   - Refresh Token A 발급 및 DB 저장
2. 동일 사용자가 웹에서 로그인 (deviceId: "web-uuid")
   - Refresh Token B 발급 및 DB 저장
3. 결과: 두 디바이스 모두 독립적으로 세션 유지

### 시나리오 2: 같은 디바이스에서 재로그인
1. 사용자가 모바일에서 로그인 (deviceId: "mobile-uuid")
   - Refresh Token A 발급 및 DB 저장
2. 앱 종료 후 다시 로그인 (deviceId: "mobile-uuid")
   - 기존 Refresh Token A를 새로운 토큰으로 교체 (UPDATE)
3. 결과: 최신 Refresh Token만 유효

---

## 구현 상태

✅ 회원가입 (이름, 이메일, 비밀번호)
✅ 이메일 인증
✅ 로그인 (다중 디바이스 지원)
✅ Access Token 갱신
✅ JWT 토큰 기반 인증
✅ 통합 테스트 (80%+ 커버리지)
✅ API 레이어 테스트
⏳ 인증 필터/인터셉터 (별도 태스크)

---

## 향후 작업: 인증 필터/인터셉터

현재 구현은 JWT 토큰 발급 및 검증 로직까지 완료되었으며, 실제 API 요청에서 토큰을 검증하고 인증된 사용자 정보를 추출하는 **인증 필터/인터셉터**는 별도 작업으로 진행될 예정입니다.

### 구현 예정 사항

1. **JWT 인증 필터**
   - HTTP 요청 헤더에서 `Authorization: Bearer <token>` 추출
   - JwtTokenProvider를 사용한 토큰 유효성 검증
   - 유효한 토큰인 경우 SecurityContext에 인증 정보 설정
   - 유효하지 않은 토큰인 경우 401 Unauthorized 응답

2. **Spring Security 통합**
   - SecurityFilterChain 설정
   - 인증이 필요한 엔드포인트와 공개 엔드포인트 구분
   - CORS 설정
   - CSRF 설정

3. **인증된 사용자 정보 접근**
   - `@AuthenticationPrincipal` 어노테이션 지원
   - SecurityContext에서 현재 사용자 정보 조회
   - 컨트롤러 메서드에서 인증된 사용자 정보 접근

### 공개 엔드포인트 vs 인증 필요 엔드포인트

**공개 엔드포인트 (인증 불필요):**
- `POST /api/users` - 회원가입
- `PUT /api/users/verification/{token}` - 이메일 인증
- `POST /api/auth/login` - 로그인
- `POST /api/auth/refresh` - 토큰 갱신

**인증 필요 엔드포인트 (향후 구현):**
- `GET /api/users/me` - 현재 사용자 정보 조회
- `PUT /api/users/me` - 사용자 정보 수정
- `POST /api/posts` - 게시글 작성
- 기타 사용자 전용 기능

### 주의사항

현재 구현된 `/api/auth/login`과 `/api/auth/refresh` 엔드포인트는 인증 필터가 적용되지 않은 상태에서도 정상 동작합니다. 이는 이들 엔드포인트가 인증을 **수행**하는 역할이지, 인증을 **요구**하는 역할이 아니기 때문입니다.

인증 필터는 이미 인증된 사용자만 접근할 수 있는 보호된 리소스에 대한 접근 제어를 위한 것이며, 별도의 작업으로 구현될 예정입니다.
