# 인증 API 추가 요구사항

**작성일:** 2025-01-16
**작성자:** Frontend Team
**상태:** 요청됨
**우선순위:** 높음

---

## 개요

프론트엔드 인증 시스템 구현 완료 후, 사용자 세션 관리를 위해 다음 두 가지 API가 추가로 필요합니다.

---

## 1. 현재 사용자 정보 조회 API

### 기본 정보

**Endpoint:** `GET /api/me`

**인증:** Required (Access Token via HttpOnly Cookie)

**설명:** 현재 로그인한 사용자의 정보를 반환합니다. 프론트엔드에서 페이지 로드 시 사용자 인증 상태 확인 및 사용자 정보를 가져오는 데 사용됩니다.

### Request

**Headers:**

```
Cookie: accessToken=<jwt-token>
```

**Request Body:** 없음

### Success Response (200 OK)

```json
{
  "userId": 1,
  "email": "user@example.com",
  "name": "홍길동",
  "emailVerified": true
}
```

**Response Fields:**

- `userId` (Long): 사용자 ID
- `email` (String): 사용자 이메일
- `name` (String): 사용자 이름
- `emailVerified` (Boolean): 이메일 인증 여부

### Error Responses

**401 Unauthorized:**

```json
{
  "type": "about:blank",
  "title": "인증 실패",
  "status": 401,
  "detail": "유효하지 않거나 만료된 토큰입니다.",
  "instance": "/api/auth/me"
}
```

### 구현 요구사항

1. **토큰 검증:**

   - HttpOnly Cookie에서 `accessToken` 추출
   - JWT 토큰 유효성 검증 (서명, 만료 시간)
   - 토큰에서 userId 추출

2. **사용자 정보 조회:**

   - userId로 DB에서 사용자 정보 조회
   - 사용자가 존재하지 않으면 401 반환

3. **보안:**
   - Spring Security 인증 필터에서 처리
   - CORS 설정 확인

### 사용 시나리오

1. **페이지 로드 시:**

   - 사용자가 보호된 페이지(`/app/*`) 접근 시
   - SvelteKit Layout에서 자동 호출
   - 인증 실패 시 로그인 페이지로 리다이렉트

2. **세션 검증:**
   - 장시간 사용 후 세션 유효성 확인
   - 토큰 갱신 후 사용자 정보 재확인

---

## 2. 로그아웃 API

### 기본 정보

**Endpoint:** `POST /api/auth/logout`

**인증:** Required (Access Token via HttpOnly Cookie)

**설명:** 사용자를 로그아웃하고 Refresh Token을 무효화합니다. HttpOnly Cookie를 삭제하여 클라이언트 측 토큰을 제거합니다.

### Request

**Headers:**

```
Cookie: accessToken=<jwt-token>; refreshToken=<jwt-token>
Content-Type: application/json
```

**Request Body:**

```json
{
  "deviceId": "device-uuid-or-identifier"
}
```

**Body Fields:**

- `deviceId` (String, Required): 로그아웃할 디바이스 식별자

### Success Response (200 OK)

**추가 동작:**

- `Set-Cookie` 헤더를 통해 `accessToken`과 `refreshToken` 삭제 (MaxAge=0)

```
Set-Cookie: accessToken=; Path=/; HttpOnly; Max-Age=0
Set-Cookie: refreshToken=; Path=/; HttpOnly; Max-Age=0
```

### Error Responses

**401 Unauthorized:**

```json
{
  "type": "about:blank",
  "title": "인증 실패",
  "status": 401,
  "detail": "유효하지 않거나 만료된 토큰입니다.",
  "instance": "/api/auth/logout"
}
```

**400 Bad Request:**

```json
{
  "type": "about:blank",
  "title": "잘못된 요청",
  "status": 400,
  "detail": "디바이스 ID는 필수입니다.",
  "instance": "/api/auth/logout"
}
```

### 구현 요구사항

1. **토큰 검증:**

   - HttpOnly Cookie에서 `accessToken` 추출
   - JWT 토큰 유효성 검증
   - 토큰에서 userId 추출

2. **Refresh Token 삭제:**

   - Request Body에서 `deviceId` 추출
   - DB에서 `userId`와 `deviceId`에 해당하는 Refresh Token 삭제
   - 해당 디바이스의 세션 완전히 무효화

3. **Cookie 삭제:**

   - `accessToken` Cookie 삭제 (MaxAge=0)
   - `refreshToken` Cookie 삭제 (MaxAge=0)
   - Path, Domain, HttpOnly 속성 동일하게 설정

4. **보안:**
   - Spring Security 인증 필터에서 처리
   - CSRF 토큰 검증 (필요시)

### 사용 시나리오

1. **명시적 로그아웃:**

   - 사용자가 헤더 메뉴에서 "로그아웃" 클릭
   - 프론트엔드에서 localStorage의 deviceId 전송
   - 로그아웃 성공 후 로그인 페이지로 리다이렉트

2. **보안 로그아웃:**
   - 비밀번호 변경 후 모든 디바이스 로그아웃
   - 의심스러운 활동 감지 시 강제 로그아웃

---

## 구현 우선순위

### 1. 높음: `GET /api/me`

- **이유:** 프론트엔드 인증 가드에 필수
- **영향:** 이 API 없이는 보호된 페이지 접근 불가
- **구현 시점:** JWT 인증 필터 구현 직후

### 2. 중간: `POST /api/auth/logout`

- **이유:** 사용자 경험 개선에 필요
- **대안:** 현재는 프론트엔드에서 로컬 상태만 초기화 (토큰은 만료 대기)
- **구현 시점:** `/api/me` 완료 후

---

## 기술적 고려사항

### 1. 성능 최적화

**`GET /api/me` 호출 빈도:**

- 매 페이지 로드마다 호출될 수 있음
- JWT 토큰에 필요한 정보 포함하여 DB 조회 생략 가능

**권장 구현:**

```java
// 토큰에서 추출한 정보로 응답 구성 (DB 조회 없이)
Claims claims = jwtTokenProvider.parseClaims(accessToken);
return UserInfoResponse.builder()
    .userId(claims.get("sub", Long.class))
    .email(claims.get("email", String.class))
    .emailVerified(claims.get("emailVerified", Boolean.class))
    .name(claims.get("name", String.class)) // 토큰에 name 추가 필요
    .build();
```

### 2. 다중 디바이스 로그아웃

**현재 구현:** 디바이스별 Refresh Token 관리

- 로그아웃 시 해당 deviceId의 토큰만 삭제
- 다른 디바이스 세션은 유지

**추가 고려사항:**

- "모든 디바이스에서 로그아웃" 기능 필요 여부
- 특정 디바이스 세션 관리 UI 필요 여부

### 3. JWT Claims 구조 확장

**현재 Access Token Claims:**

```
{
  "sub": userId,
  "email": "user@example.com",
  "type": "access",
  "iat": 1234567890,
  "exp": 1234571490
}
```

**필요한 추가 Claims:**

```
{
  "sub": userId,
  "email": "user@example.com",
  "name": "홍길동",              // 추가 필요
  "emailVerified": true,          // 추가 필요
  "type": "access",
  "iat": 1234567890,
  "exp": 1234571490
}
```

---

## 의존성

### 선행 작업

- ✅ JWT 토큰 발급 로직 (완료)
- ⏳ JWT 인증 필터/인터셉터 구현
- ⏳ Spring Security 설정

### 관련 문서

- [백엔드 인증 API 문서](../../backend/src/main/java/app/aipacemaker/backend/auth/docs/api.md)
- [프론트엔드 인증 시스템 명세](../../specifications/v1.0.0/DS/01-authentication-system.md)

---

## 테스트 시나리오

### `GET /api/auth/me` 테스트

1. **정상 케이스:**

   - 유효한 accessToken으로 요청
   - 200 OK 및 사용자 정보 반환

2. **인증 실패:**
   - accessToken 없이 요청 → 401
   - 만료된 accessToken → 401
   - 잘못된 서명의 accessToken → 401
   - 존재하지 않는 userId → 401

### `POST /api/auth/logout` 테스트

1. **정상 케이스:**

   - 유효한 accessToken + deviceId로 요청
   - 200 OK 및 Cookie 삭제 확인
   - DB에서 Refresh Token 삭제 확인

2. **인증 실패:**

   - accessToken 없이 요청 → 401
   - 만료된 accessToken → 401

3. **잘못된 요청:**

   - deviceId 누락 → 400

4. **재로그인 불가:**
   - 로그아웃 후 refreshToken으로 갱신 시도 → 401

---

## 문의사항

질문이나 추가 요구사항이 있으면 프론트엔드 팀에 문의해주세요.

**연락처:** [프론트엔드 팀 연락처]
