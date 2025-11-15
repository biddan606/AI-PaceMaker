import { goto } from '$app/navigation';
import type { User } from '$lib/types/navigation';

/**
 * 인증 상태 관리 스토어
 *
 * Svelte 5 runes를 활용한 전역 인증 상태 관리
 */
class AuthStore {
	user = $state<User | null>({
		name: '김성장',
		email: 'user@example.com'
	});

	isAuthenticated = $derived(this.user !== null);

	/**
	 * 사용자 로그인
	 * @param user 사용자 정보
	 */
	login(user: User) {
		this.user = user;
	}

	/**
	 * 사용자 로그아웃
	 * - 사용자 정보 초기화
	 * - 로그인 페이지로 이동
	 */
	logout() {
		this.user = null;
		goto('/login');
	}

	/**
	 * 사용자 정보 업데이트
	 * @param updates 업데이트할 사용자 정보
	 */
	updateUser(updates: Partial<User>) {
		if (this.user) {
			this.user = { ...this.user, ...updates };
		}
	}
}

export const authStore = new AuthStore();
