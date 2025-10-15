<script lang="ts">
	import type { User } from '$lib/types/navigation';

	interface Props {
		user: User;
		onLogout?: () => void;
	}

	let { user, onLogout }: Props = $props();

	let isOpen = $state(false);

	function toggleMenu() {
		isOpen = !isOpen;
	}

	function closeMenu() {
		isOpen = false;
	}

	function handleLogout() {
		closeMenu();
		if (onLogout) {
			onLogout();
		} else {
			// 기본 로그아웃 동작 (향후 인증 스토어와 연동)
			window.location.href = '/login';
		}
	}

	// 사용자 이름의 첫 글자 (아바타 폴백)
	const initials = $derived(user.name.charAt(0).toUpperCase());

	// ESC 키로 메뉴 닫기
	$effect(() => {
		if (!isOpen) return;

		function handleKeydown(event: KeyboardEvent) {
			if (event.key === 'Escape') {
				closeMenu();
			}
		}

		window.addEventListener('keydown', handleKeydown);
		return () => window.removeEventListener('keydown', handleKeydown);
	});
</script>

<!-- 사용자 메뉴 컨테이너 -->
<div class="relative">
	<!-- 트리거 버튼 -->
	<button
		onclick={toggleMenu}
		class="flex items-center gap-2 px-3 py-2 rounded-lg hover:bg-[#F5F5F5] transition-colors"
		aria-label="사용자 메뉴"
		aria-expanded={isOpen}
	>
		<!-- 아바타 -->
		<div
			class="w-8 h-8 rounded-full bg-[#FF6B4A] text-white flex items-center justify-center text-sm font-semibold"
		>
			{#if user.avatar}
				<img src={user.avatar} alt={user.name} class="w-full h-full rounded-full object-cover" />
			{:else}
				{initials}
			{/if}
		</div>

		<!-- 사용자 이름 (모바일에서 숨김) -->
		<span class="hidden md:inline text-sm font-medium text-[#171717]">
			{user.name}
		</span>

		<!-- 드롭다운 아이콘 -->
		<svg
			class="w-4 h-4 text-[#737373] transition-transform"
			class:rotate-180={isOpen}
			fill="none"
			stroke="currentColor"
			viewBox="0 0 24 24"
		>
			<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7" />
		</svg>
	</button>

	<!-- 드롭다운 메뉴 -->
	{#if isOpen}
		<!-- 오버레이 (메뉴 외부 클릭 시 닫기) -->
		<button
			onclick={closeMenu}
			class="fixed inset-0 z-40"
			aria-label="메뉴 닫기"
			tabindex="-1"
		></button>

		<!-- 메뉴 콘텐츠 -->
		<div
			class="absolute right-0 mt-2 w-56 bg-white rounded-lg shadow-lg border border-[#E5E5E5] py-2 z-50 animate-in fade-in slide-in-from-top"
			role="menu"
			aria-orientation="vertical"
		>
			<!-- 사용자 정보 -->
			<div class="px-4 py-3 border-b border-[#E5E5E5]">
				<p class="text-sm font-medium text-[#171717]">{user.name}</p>
				<p class="text-xs text-[#737373] mt-1">{user.email}</p>
			</div>

			<!-- 메뉴 아이템 -->
			<div class="py-1">
				<a
					href="/app/settings/profile"
					onclick={closeMenu}
					class="block px-4 py-2 text-sm text-[#404040] hover:bg-[#F5F5F5] transition-colors"
					role="menuitem"
				>
					프로필
				</a>
				<a
					href="/app/settings"
					onclick={closeMenu}
					class="block px-4 py-2 text-sm text-[#404040] hover:bg-[#F5F5F5] transition-colors"
					role="menuitem"
				>
					설정
				</a>
			</div>

			<!-- 구분선 -->
			<div class="border-t border-[#E5E5E5] my-1"></div>

			<!-- 로그아웃 -->
			<button
				onclick={handleLogout}
				class="w-full text-left px-4 py-2 text-sm text-[#DC2626] hover:bg-[#FEF2F2] transition-colors"
				role="menuitem"
			>
				로그아웃
			</button>
		</div>
	{/if}
</div>

<style>
	/* 드롭다운 애니메이션 */
	.animate-in {
		animation: slideInFromTop 150ms ease-out;
	}

	@keyframes slideInFromTop {
		from {
			opacity: 0;
			transform: translateY(-8px);
		}
		to {
			opacity: 1;
			transform: translateY(0);
		}
	}
</style>
