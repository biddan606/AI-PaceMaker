/**
 * API 클라이언트 - 자동 토큰 갱신 인터셉터 포함
 *
 * 401 Unauthorized 에러 발생 시 자동으로 토큰을 갱신하고,
 * 원래 요청을 재시도합니다.
 */

import { goto } from '$app/navigation';

// 토큰 갱신 중복 방지를 위한 Promise 저장소
let refreshTokenPromise: Promise<boolean> | null = null;

/**
 * Refresh Token을 사용하여 Access Token 갱신
 *
 * @returns {Promise<boolean>} 갱신 성공 여부
 */
async function refreshAccessToken(): Promise<boolean> {
	try {
		const response = await fetch('/api/auth/refresh', {
			method: 'POST',
			credentials: 'include' // HttpOnly Cookie 전송
		});

		if (!response.ok) {
			return false;
		}

		// Access Token이 HttpOnly Cookie로 설정됨
		return true;
	} catch (error) {
		console.error('토큰 갱신 실패:', error);
		return false;
	}
}

/**
 * API 요청 헬퍼 함수 (자동 토큰 갱신 포함)
 *
 * @param url - 요청 URL
 * @param options - fetch options
 * @param retryCount - 재시도 횟수 (내부 사용)
 * @returns {Promise<Response>}
 */
export async function apiClient(
	url: string,
	options: RequestInit = {},
	retryCount = 0
): Promise<Response> {
	// credentials: 'include'를 기본값으로 설정 (Cookie 전송)
	const finalOptions: RequestInit = {
		...options,
		credentials: 'include'
	};

	try {
		const response = await fetch(url, finalOptions);

		// 401 Unauthorized 에러 처리
		if (response.status === 401 && retryCount === 0) {
			// 토큰 갱신이 이미 진행 중이면 해당 Promise를 재사용
			if (!refreshTokenPromise) {
				refreshTokenPromise = refreshAccessToken().finally(() => {
					refreshTokenPromise = null;
				});
			}

			const refreshSuccess = await refreshTokenPromise;

			if (refreshSuccess) {
				// 토큰 갱신 성공 시 원래 요청 재시도
				return apiClient(url, options, retryCount + 1);
			} else {
				// 토큰 갱신 실패 시 로그인 페이지로 리다이렉트
				goto(`/login?redirectTo=${encodeURIComponent(window.location.pathname)}`);
				throw new Error('인증이 만료되었습니다. 다시 로그인해주세요.');
			}
		}

		return response;
	} catch (error) {
		// 네트워크 오류 등
		throw error;
	}
}

/**
 * API GET 요청
 */
export async function apiGet<T>(url: string): Promise<T> {
	const response = await apiClient(url, { method: 'GET' });

	if (!response.ok) {
		throw new Error(`API 요청 실패: ${response.status}`);
	}

	return response.json();
}

/**
 * API POST 요청
 */
export async function apiPost<T>(url: string, data?: unknown): Promise<T> {
	const response = await apiClient(url, {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json'
		},
		body: data ? JSON.stringify(data) : undefined
	});

	if (!response.ok) {
		throw new Error(`API 요청 실패: ${response.status}`);
	}

	return response.json();
}

/**
 * API PUT 요청
 */
export async function apiPut<T>(url: string, data?: unknown): Promise<T> {
	const response = await apiClient(url, {
		method: 'PUT',
		headers: {
			'Content-Type': 'application/json'
		},
		body: data ? JSON.stringify(data) : undefined
	});

	if (!response.ok) {
		throw new Error(`API 요청 실패: ${response.status}`);
	}

	return response.json();
}

/**
 * API DELETE 요청
 */
export async function apiDelete<T>(url: string): Promise<T> {
	const response = await apiClient(url, { method: 'DELETE' });

	if (!response.ok) {
		throw new Error(`API 요청 실패: ${response.status}`);
	}

	return response.json();
}
