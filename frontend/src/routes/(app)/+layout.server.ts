import { redirect } from '@sveltejs/kit';
import type { LayoutServerLoad } from './$types';

export const load: LayoutServerLoad = async ({ cookies, url }) => {
	// TODO: 백엔드 /api/auth/me API 구현 후 실제 API 호출로 변경
	// 현재는 Cookie에 accessToken이 있는지만 확인하는 임시 방안 사용

	// Cookie에서 accessToken 확인
	const accessToken = cookies.get('accessToken');

	if (!accessToken) {
		// 토큰이 없으면 로그인 페이지로 리다이렉트
		throw redirect(303, `/login?redirectTo=${encodeURIComponent(url.pathname)}`);
	}

	// TODO: /api/auth/me 구현 후 실제 사용자 정보 조회
	// 현재는 임시 데이터 반환
	// 실제 사용자 정보는 API 요청 시 자동으로 토큰 검증됨
	return {
		user: {
			name: '사용자',
			email: 'user@example.com'
		}
	};
};
