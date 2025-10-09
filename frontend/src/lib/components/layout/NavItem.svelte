<script lang="ts">
	import { page } from '$app/stores';
	import type { NavItem } from '$lib/types/navigation';

	interface Props {
		item: NavItem;
		variant?: 'sidebar' | 'mobile';
	}

	let { item, variant = 'sidebar' }: Props = $props();

	// 현재 경로와 메뉴 href 비교하여 활성 상태 판단
	let isActive = $derived($page.url.pathname === item.href);
</script>

{#if variant === 'sidebar'}
	<!-- 데스크톱 사이드바용 네비게이션 아이템 -->
	<a
		href={item.href}
		class="flex items-center gap-3 px-4 py-3 rounded-lg transition-colors relative group"
		class:active={isActive}
		aria-current={isActive ? 'page' : undefined}
	>
		<!-- 활성 상태: Coral 좌측 border (3px) -->
		{#if isActive}
			<div class="absolute left-0 top-0 bottom-0 w-[3px] bg-[#FF6B4A] rounded-r"></div>
		{/if}

		<span class="text-xl" aria-hidden="true">{item.icon}</span>
		<span class="text-sm font-medium">{item.label}</span>
	</a>
{:else}
	<!-- 모바일 하단 탭바용 네비게이션 아이템 -->
	<a
		href={item.href}
		class="flex flex-col items-center justify-center gap-1 px-3 py-2 rounded-lg transition-colors min-w-[64px]"
		class:active-mobile={isActive}
		aria-current={isActive ? 'page' : undefined}
	>
		<span class="text-2xl" aria-hidden="true">{item.icon}</span>
		<span class="text-xs font-medium">{item.label}</span>
	</a>
{/if}

<style>
	/* 데스크톱 사이드바 스타일 */
	a {
		color: #404040; /* Gray 700 */
	}

	a:hover:not(.active) {
		background-color: #f5f5f5; /* Gray 100 */
	}

	a.active {
		background-color: rgba(255, 107, 74, 0.1); /* Coral 10% */
		color: #ff6b4a; /* Coral */
	}

	/* 모바일 탭바 스타일 */
	a.active-mobile {
		color: #ff6b4a; /* Coral */
	}

	a:not(.active-mobile) {
		color: #a3a3a3; /* Gray 400 */
	}
</style>
