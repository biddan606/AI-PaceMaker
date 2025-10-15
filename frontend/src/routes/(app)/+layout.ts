import { redirect } from '@sveltejs/kit';
import type { LayoutLoad } from './$types';

export const load: LayoutLoad = async ({ fetch, url }) => {
	try {
		// 세션 확인: /api/auth/me 엔드포인트 호출
		const response = await fetch('/api/auth/me', {
			credentials: 'include'
		});

		if (!response.ok) {
			// 인증 실패 시 로그인 페이지로 리디렉션
			// 현재 URL을 쿼리 파라미터로 저장하여 로그인 후 복귀 가능
			throw redirect(303, `/login?redirectTo=${encodeURIComponent(url.pathname)}`);
		}

		const user = await response.json();

		return {
			user: {
				name: user.name || user.username || user.email?.split('@')[0] || '사용자',
				email: user.email,
				avatar: user.avatar
			}
		};
	} catch (error) {
		// redirect는 throw되므로 그대로 전파
		if (error instanceof Response) {
			throw error;
		}

		// 기타 에러 시 로그인 페이지로 리디렉션
		throw redirect(303, `/login?redirectTo=${encodeURIComponent(url.pathname)}`);
	}
};
