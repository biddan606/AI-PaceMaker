<script lang="ts">
	import UserMenu from './UserMenu.svelte';
	import type { User } from '$lib/types/navigation';

	interface Props {
		user?: User;
		onToggleSidebar?: () => void;
		onLogout?: () => void;
	}

	let { user, onToggleSidebar, onLogout }: Props = $props();
</script>

<header class="fixed top-0 left-0 right-0 h-16 bg-[#FAFAFA] border-b border-[#E5E5E5] z-50">
	<div class="container h-full flex items-center justify-between px-4 mx-auto max-w-[1280px]">
		<!-- 좌측: 사이드바 토글(모바일/태블릿) + 로고 -->
		<div class="flex items-center gap-3">
			{#if onToggleSidebar}
				<button
					onclick={onToggleSidebar}
					class="lg:hidden p-2 hover:bg-[#F5F5F5] rounded-lg transition-colors"
					aria-label="메뉴 열기"
				>
					<svg class="w-6 h-6 text-[#404040]" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							stroke-width="2"
							d="M4 6h16M4 12h16M4 18h16"
						/>
					</svg>
				</button>
			{/if}

			<a href={user ? '/backlog' : '/'} class="text-xl font-semibold text-[#171717]">
				AI 페이스메이커
			</a>
		</div>

		<!-- 우측: 사용자 메뉴 또는 로그인 버튼 -->
		{#if user}
			<UserMenu {user} {onLogout} />
		{:else}
			<a
				href="/login"
				class="px-4 py-2 bg-[#FF6B4A] text-white rounded-lg hover:bg-[#FF5A39] transition-colors text-sm font-medium"
			>
				로그인
			</a>
		{/if}
	</div>
</header>
