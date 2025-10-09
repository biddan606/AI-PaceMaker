<script lang="ts">
	import Header from '$lib/components/layout/Header.svelte';
	import Sidebar from '$lib/components/layout/Sidebar.svelte';
	import MobileNav from '$lib/components/layout/MobileNav.svelte';
	import '../../app.css';

	let { children } = $props();

	// 사이드바 열림/닫힘 상태 (모바일/태블릿용)
	let isSidebarOpen = $state(false);

	// TODO: 실제 사용자 데이터는 인증 스토어에서 가져오기
	const user = {
		name: '김성장',
		email: 'user@example.com'
	};

	function toggleSidebar() {
		isSidebarOpen = !isSidebarOpen;
	}

	function closeSidebar() {
		isSidebarOpen = false;
	}

	function handleLogout() {
		// TODO: 실제 로그아웃 로직 구현 (인증 스토어와 연동)
		console.log('로그아웃');
		window.location.href = '/login';
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
