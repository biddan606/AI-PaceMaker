<script lang="ts">
	import Avatar from '$lib/components/ui/Avatar.svelte';
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
		<Avatar src={user.avatar} alt={user.name} fallback={user.name.charAt(0)} size="sm" />

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
			<div class="px-4 py-3 border-b border-[#E5E5E5] flex items-center gap-3">
				<Avatar src={user.avatar} alt={user.name} fallback={user.name.charAt(0)} size="md" />
				<div class="flex-1 min-w-0">
					<p class="text-sm font-medium text-[#171717] truncate">{user.name}</p>
					<p class="text-xs text-[#737373] mt-0.5 truncate">{user.email}</p>
				</div>
			</div>

			<!-- 메뉴 아이템 -->
			<div class="py-1">
				<a
					href="/app/settings/profile"
					onclick={closeMenu}
					class="flex items-center gap-3 px-4 py-2 text-sm text-[#404040] hover:bg-[#F5F5F5] transition-colors"
					role="menuitem"
				>
					<svg class="w-4 h-4 text-[#737373]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
						/>
					</svg>
					<span>프로필</span>
				</a>
				<a
					href="/app/settings"
					onclick={closeMenu}
					class="flex items-center gap-3 px-4 py-2 text-sm text-[#404040] hover:bg-[#F5F5F5] transition-colors"
					role="menuitem"
				>
					<svg class="w-4 h-4 text-[#737373]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"
						/>
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
						/>
					</svg>
					<span>설정</span>
				</a>
			</div>

			<!-- 구분선 -->
			<div class="border-t border-[#E5E5E5] my-1"></div>

			<!-- 로그아웃 -->
			<button
				onclick={handleLogout}
				class="w-full flex items-center gap-3 px-4 py-2 text-sm text-[#DC2626] hover:bg-[#FEF2F2] transition-colors"
				role="menuitem"
			>
				<svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
					<path
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"
					/>
				</svg>
				<span>로그아웃</span>
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
