// 네비게이션 메뉴 항목 타입 정의

export interface NavItem {
	icon: string;
	label: string;
	href: string;
	priority: 'P0' | 'P1'; // P0: 모바일 탭바에도 표시, P1: 사이드바만
}

export interface User {
	name: string;
	email: string;
	avatar?: string;
}

// 네비게이션 메뉴 항목 (디자인 컨셉 기준)
export const NAV_ITEMS: NavItem[] = [
	{ icon: '📋', label: '백로그', href: '/backlog', priority: 'P0' },
	{ icon: '🎯', label: '이번 주 스프린트', href: '/sprint', priority: 'P0' },
	{ icon: '📊', label: '히스토리', href: '/history', priority: 'P0' },
	{ icon: '⚙️', label: '설정', href: '/settings', priority: 'P1' }
];

// 모바일 하단 탭바용 메뉴 (설정 제외)
export const MOBILE_NAV_ITEMS = NAV_ITEMS.filter((item) => item.priority === 'P0');
