<script lang="ts">
	import NavItem from './NavItem.svelte';
	import { NAV_ITEMS } from '$lib/types/navigation';

	interface Props {
		isOpen?: boolean;
		onClose?: () => void;
	}

	let { isOpen = false, onClose }: Props = $props();

	// ESC 키로 사이드바 닫기
	$effect(() => {
		if (!isOpen) return;

		function handleKeydown(event: KeyboardEvent) {
			if (event.key === 'Escape') {
				onClose?.();
			}
		}

		window.addEventListener('keydown', handleKeydown);
		return () => window.removeEventListener('keydown', handleKeydown);
	});
</script>

<!-- Desktop: 고정 사이드바 -->
<aside
	class="hidden lg:block fixed top-16 bottom-0 left-0 w-60 bg-white border-r border-[#E5E5E5] z-30"
>
	<nav class="p-4 space-y-1" aria-label="주요 네비게이션">
		{#each NAV_ITEMS as item}
			<NavItem {item} variant="sidebar" />
		{/each}
	</nav>
</aside>

<!-- Mobile/Tablet: 오버레이 사이드바 -->
{#if isOpen}
	<!-- 오버레이 배경 (클릭 시 닫기) -->
	<button
		onclick={onClose}
		onkeydown={(e) => e.key === 'Escape' && onClose?.()}
		class="lg:hidden fixed inset-0 bg-black/50 backdrop-blur-sm z-40"
		aria-label="사이드바 닫기"
		tabindex="-1"
	></button>

	<!-- 사이드바 콘텐츠 -->
	<aside
		class="lg:hidden fixed top-16 bottom-0 left-0 w-60 bg-white border-r border-[#E5E5E5] z-50 animate-slide-in"
		role="dialog"
		aria-modal="true"
		aria-label="네비게이션 메뉴"
	>
		<nav class="p-4 space-y-1" aria-label="주요 네비게이션">
			{#each NAV_ITEMS as item}
				<NavItem {item} variant="sidebar" />
			{/each}
		</nav>
	</aside>
{/if}

<style>
	/* 사이드바 슬라이드 애니메이션 */
	.animate-slide-in {
		animation: slideInFromLeft 200ms ease-out;
	}

	@keyframes slideInFromLeft {
		from {
			transform: translateX(-100%);
		}
		to {
			transform: translateX(0);
		}
	}
</style>
