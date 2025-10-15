<script lang="ts">
	import Header from '$lib/components/layout/Header.svelte';
	import Sidebar from '$lib/components/layout/Sidebar.svelte';
	import MobileNav from '$lib/components/layout/MobileNav.svelte';
	import { authStore } from '$lib/stores/auth.svelte';
	import { goto } from '$app/navigation';
	import '../../app.css';

	let { children, data } = $props();

	// 사이드바 열림/닫힘 상태 (모바일/태블릿용)
	let isSidebarOpen = $state(false);

	// +layout.ts에서 로드한 사용자 정보를 authStore에 동기화
	$effect(() => {
		if (data.user) {
			authStore.setUser(data.user);
		}
	});

	// authStore에서 사용자 정보 가져오기
	const user = $derived(authStore.user);

	function toggleSidebar() {
		isSidebarOpen = !isSidebarOpen;
	}

	function closeSidebar() {
		isSidebarOpen = false;
	}

	async function handleLogout() {
		await authStore.logout();
		goto('/login');
	}
</script>

<div class="min-h-screen bg-background">
	<!-- 헤더 -->
	<Header {user} onToggleSidebar={toggleSidebar} onLogout={handleLogout} />

	<!-- 사이드바 -->
	<Sidebar isOpen={isSidebarOpen} onClose={closeSidebar} />

	<!-- 메인 콘텐츠 -->
	<main class="pt-16 lg:pl-60 pb-16 lg:pb-0">
		<div class="container mx-auto p-4 lg:p-6 max-w-[1280px]">
			{@render children?.()}
		</div>
	</main>

	<!-- 모바일 하단 네비게이션 -->
	<MobileNav />
</div>
