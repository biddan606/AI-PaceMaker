// 사용자 인증 상태를 관리하는 Svelte 5 Runes 기반 스토어
import type { User } from '$lib/types/navigation';
import { apiPost } from '$lib/api/client';
import { getOrCreateDeviceId } from '$lib/utils/device';

interface AuthState {
	user: User | null;
	isLoading: boolean;
	isAuthenticated: boolean;
}

interface LoginResponse {
	userId: number;
	email: string;
	emailVerified: boolean;
}

interface RegisterResponse {
	userId: number;
	email: string;
	message: string;
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

				const deviceId = getOrCreateDeviceId();
				const data = await apiPost<LoginResponse>('/api/auth/login', {
					email,
					password,
					deviceId
				});

				// 토큰은 HttpOnly Cookie로 자동 저장됨
				// 사용자 정보만 스토어에 저장
				this.setUser({
					name: email.split('@')[0], // 백엔드에서 name을 제공하지 않으므로 이메일에서 추출
					email: data.email,
					avatar: undefined
				});

				return { success: true, emailVerified: data.emailVerified };
			} catch (error) {
				state.isLoading = false;
				console.error('로그인 오류:', error);
				return {
					success: false,
					error: error instanceof Error ? error.message : '로그인에 실패했습니다.'
				};
			}
		},

		// 회원가입
		async register(name: string, email: string, password: string) {
			try {
				state.isLoading = true;

				const data = await apiPost<RegisterResponse>('/api/users', {
					name,
					email,
					password
				});

				state.isLoading = false;
				return { success: true, message: data.message };
			} catch (error) {
				state.isLoading = false;
				console.error('회원가입 오류:', error);
				return {
					success: false,
					error: error instanceof Error ? error.message : '회원가입에 실패했습니다.'
				};
			}
		},

		// 로그아웃
		async logout() {
			try {
				// TODO: 백엔드 /api/auth/logout API 구현 후 활성화
				// await apiPost('/api/auth/logout', { deviceId: getOrCreateDeviceId() });

				// 현재는 로컬 상태만 초기화 (HttpOnly Cookie는 클라이언트에서 삭제 불가)
				// 토큰은 만료되거나 다음 로그인 시 덮어씌워짐
				this.setUser(null);
			} catch (error) {
				console.error('로그아웃 오류:', error);
				this.setUser(null);
			}
		},

		// 현재 사용자 정보 가져오기 (세션 확인)
		async fetchUser() {
			try {
				state.isLoading = true;

				// apiClient를 사용하면 401 시 자동으로 토큰 갱신 시도
				const response = await fetch('/api/auth/me', {
					credentials: 'include'
				});

				if (response.ok) {
					const data = await response.json();
					this.setUser({
						name: data.name || data.email?.split('@')[0] || '사용자',
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
