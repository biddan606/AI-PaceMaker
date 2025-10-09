<script lang="ts">
	import NavItem from './NavItem.svelte';
	import { MOBILE_NAV_ITEMS } from '$lib/types/navigation';
</script>

<!-- 모바일 하단 네비게이션 바 (64px 고정) -->
<nav
	class="lg:hidden fixed bottom-0 left-0 right-0 h-16 bg-white border-t border-[#E5E5E5] z-50 shadow-mobile-nav"
	aria-label="모바일 네비게이션"
>
	<div class="h-full flex items-center justify-around px-2">
		{#each MOBILE_NAV_ITEMS as item}
			<NavItem {item} variant="mobile" />
		{/each}
	</div>
</nav>

<style>
	/* 모바일 네비게이션 상단 그림자 */
	.shadow-mobile-nav {
		box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.08);
	}
</style>
