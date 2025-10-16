# 프론트엔드 임시 우회 방안

**작성일:** 2025-01-16
**상태:** 임시 조치
**관련 문서:** [인증 API 추가 요구사항](./auth-api-requirements.md)

---

## 개요

백엔드 `/api/auth/me` 및 `/api/auth/logout` API가 구현되기 전까지 프론트엔드에서 사용할 임시 우회 방안입니다.

---

## 1. 사용자 정보 조회 (`/api/auth/me`) 우회

### 현재 상황
- SvelteKit Layout (`/routes/(app)/+layout.ts`)에서 `/api/auth/me` 호출
- 해당 API가 백엔드에 구현되지 않음
- 보호된 페이지 접근 시 에러 발생

### 임시 해결 방법

**옵션 1: 로그인 후 사용자 정보를 로컬 상태에 저장**

로그인 성공 시 받은 사용자 정보를 authStore에 저장하고, Layout에서 authStore를 확인:

```typescript
// routes/(app)/+layout.ts
import { authStore } from '$lib/stores/auth.svelte';
import { redirect } from '@sveltejs/kit';
import type { LayoutLoad } from './$types';

export const load: LayoutLoad = async ({ url }) => {
	// authStore에서 사용자 정보 확인
	if (!authStore.isAuthenticated) {
		// 인증되지 않은 경우 로그인 페이지로 리다이렉트
		throw redirect(303, `/login?redirectTo=${encodeURIComponent(url.pathname)}`);
	}

	return {
		user: authStore.user
	};
};
```

**장점:**
- 빠른 페이지 로드 (API 호출 없음)
- 간단한 구현

**단점:**
- 페이지 새로고침 시 인증 상태 유실 (authStore는 메모리 기반)
- 다른 탭/창에서 로그아웃 시 동기화 안됨
- 토큰 만료 감지 불가

**옵션 2: 토큰 존재 여부만 확인 (권장)**

Cookie에 accessToken이 있는지만 확인하고, 실제 검증은 API 요청 시 자동으로 처리:

```typescript
// routes/(app)/+layout.ts
import { redirect } from '@sveltejs/kit';
import type { LayoutLoad } from './$types';

export const load: LayoutLoad = async ({ cookies, url }) => {
	const accessToken = cookies.get('accessToken');

	if (!accessToken) {
		// accessToken이 없으면 로그인 페이지로
		throw redirect(303, `/login?redirectTo=${encodeURIComponent(url.pathname)}`);
	}

	// 토큰이 있으면 일단 통과 (실제 검증은 API 호출 시 자동 처리)
	// 임시로 최소한의 사용자 정보 반환
	return {
		user: {
			email: 'user@example.com', // 임시
			name: '사용자' // 임시
		}
	};
};
```

**장점:**
- 서버 사이드에서 Cookie 확인 가능
- 토큰이 없으면 바로 리다이렉트
- API 구현 전까지 동작 가능

**단점:**
- 토큰 유효성 검증 안됨
- 실제 사용자 정보가 아닌 임시 데이터

---

## 2. 로그아웃 (`/api/auth/logout`) 우회

### 현재 상황
- authStore.logout()에서 `/api/auth/logout` 호출
- 해당 API가 백엔드에 구현되지 않음
- HttpOnly Cookie를 클라이언트에서 직접 삭제 불가

### 임시 해결 방법

**로컬 상태만 초기화:**

```typescript
// lib/stores/auth.svelte.ts
async logout() {
	try {
		// HttpOnly Cookie는 클라이언트에서 삭제 불가
		// 로컬 상태만 초기화
		this.setUser(null);

		// 로그인 페이지로 리다이렉트
		// 토큰은 만료되거나 다음 로그인 시 덮어씌워짐
	} catch (error) {
		console.error('로그아웃 오류:', error);
		this.setUser(null);
	}
}
```

**장점:**
- API 구현 없이 동작
- 사용자 경험상 로그아웃 된 것처럼 보임

**단점:**
- Cookie의 토큰은 여전히 유효 (만료까지 1시간)
- 보안상 완전한 로그아웃이 아님
- DB에 Refresh Token 남아있음 (30일)

**권장 방안:**
- API 구현 전까지는 위 방식 사용
- API 구현 후 즉시 통합
- 중요: 보안이 중요한 작업 전에는 반드시 API 구현 필요

---

## 3. 구현 코드

### `routes/(app)/+layout.ts` (옵션 2 - 권장)

```typescript
import { redirect } from '@sveltejs/kit';
import type { LayoutLoad } from './$types';

export const load: LayoutLoad = async ({ cookies, url }) => {
	// Cookie에서 accessToken 확인
	const accessToken = cookies.get('accessToken');

	if (!accessToken) {
		// 토큰이 없으면 로그인 페이지로 리다이렉트
		throw redirect(303, `/login?redirectTo=${encodeURIComponent(url.pathname)}`);
	}

	// TODO: /api/auth/me 구현 후 실제 사용자 정보 조회
	// 현재는 임시 데이터 반환
	return {
		user: {
			name: '사용자',
			email: 'user@example.com'
		}
	};
};
```

### `lib/stores/auth.svelte.ts` - logout 메서드

```typescript
// 로그아웃
async logout() {
	try {
		// TODO: /api/auth/logout 구현 후 API 호출
		// await apiPost('/api/auth/logout', { deviceId: getOrCreateDeviceId() });

		// 현재는 로컬 상태만 초기화
		this.setUser(null);
	} catch (error) {
		console.error('로그아웃 오류:', error);
		this.setUser(null);
	}
}
```

---

## 4. API 구현 후 통합 체크리스트

### `/api/auth/me` 구현 완료 시

- [ ] `routes/(app)/+layout.ts` 수정
  - [ ] 실제 `/api/auth/me` API 호출
  - [ ] 응답에서 실제 사용자 정보 사용
  - [ ] 에러 처리 (401 시 로그인 페이지 리다이렉트)

- [ ] `lib/stores/auth.svelte.ts` 수정
  - [ ] `fetchUser()` 메서드 활성화
  - [ ] 실제 API 호출로 변경

### `/api/auth/logout` 구현 완료 시

- [ ] `lib/stores/auth.svelte.ts` 수정
  - [ ] `logout()` 메서드에 API 호출 추가
  - [ ] deviceId 전송
  - [ ] 에러 처리

- [ ] 테스트
  - [ ] 로그아웃 후 토큰 삭제 확인
  - [ ] 로그아웃 후 보호된 페이지 접근 불가 확인
  - [ ] 로그아웃 후 토큰 갱신 불가 확인

---

## 5. 주의사항

### 보안
- 임시 방안은 완전한 인증 검증이 아님
- 프로덕션 배포 전 반드시 API 구현 필요
- 민감한 데이터 처리 시 주의

### 사용자 경험
- 페이지 새로고침 시 인증 상태 유실 가능
- 다중 탭/창에서 동기화 안됨
- 토큰 만료 시 갑작스러운 로그아웃 경험

### 개발
- TODO 주석으로 표시하여 추후 수정 위치 명확화
- API 구현 후 즉시 통합 테스트 진행
- 문서 업데이트

---

## 관련 이슈

- [ ] 백엔드: JWT 인증 필터 구현
- [ ] 백엔드: `/api/auth/me` 구현
- [ ] 백엔드: `/api/auth/logout` 구현
- [ ] 프론트엔드: API 구현 후 통합
