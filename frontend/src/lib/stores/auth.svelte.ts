// 사용자 인증 상태를 관리하는 Svelte 5 Runes 기반 스토어
import type { User } from '$lib/types/navigation';

interface AuthState {
	user: User | null;
	isLoading: boolean;
	isAuthenticated: boolean;
}

// 초기 상태
const initialState: AuthState = {
	user: null,
	isLoading: true,
	isAuthenticated: false
};

// Svelte 5 Runes 기반 인증 스토어
function createAuthStore() {
	let state = $state<AuthState>(initialState);

	return {
		// 읽기 전용 getter
		get user() {
			return state.user;
		},
		get isLoading() {
			return state.isLoading;
		},
		get isAuthenticated() {
			return state.isAuthenticated;
		},

		// 사용자 정보 설정
		setUser(user: User | null) {
			state.user = user;
			state.isAuthenticated = !!user;
			state.isLoading = false;
		},

		// 로딩 상태 설정
		setLoading(loading: boolean) {
			state.isLoading = loading;
		},

		// 로그인
		async login(email: string, password: string) {
			try {
				state.isLoading = true;

				const response = await fetch('/api/auth/login', {
					method: 'POST',
					headers: {
						'Content-Type': 'application/json'
					},
					body: JSON.stringify({ email, password }),
					credentials: 'include' // HttpOnly Cookie 포함
				});

				if (!response.ok) {
					throw new Error('로그인에 실패했습니다.');
				}

				const data = await response.json();

				// 사용자 정보 저장 (백엔드 응답 구조에 따라 조정 필요)
				this.setUser({
					name: data.name || data.username || email.split('@')[0],
					email: data.email || email,
					avatar: data.avatar
				});

				return { success: true };
			} catch (error) {
				state.isLoading = false;
				console.error('로그인 오류:', error);
				return { success: false, error: '로그인에 실패했습니다.' };
			}
		},

		// 로그아웃
		async logout() {
			try {
				await fetch('/api/auth/logout', {
					method: 'POST',
					credentials: 'include'
				});
			} catch (error) {
				console.error('로그아웃 오류:', error);
			} finally {
				// 로컬 상태 초기화
				this.setUser(null);
			}
		},

		// 현재 사용자 정보 가져오기 (세션 확인)
		async fetchUser() {
			try {
				state.isLoading = true;

				const response = await fetch('/api/auth/me', {
					credentials: 'include'
				});

				if (response.ok) {
					const data = await response.json();
					this.setUser({
						name: data.name || data.username || data.email?.split('@')[0] || '사용자',
						email: data.email,
						avatar: data.avatar
					});
				} else {
					// 인증 실패 시 사용자 정보 초기화
					this.setUser(null);
				}
			} catch (error) {
				console.error('사용자 정보 가져오기 오류:', error);
				this.setUser(null);
			}
		}
	};
}

// 싱글톤 인스턴스 생성 및 export
export const authStore = createAuthStore();
