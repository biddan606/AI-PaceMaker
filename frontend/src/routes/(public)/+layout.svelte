<script lang="ts">
	import Header from '$lib/components/layout/Header.svelte';
	import '../../app.css';

	let { children } = $props();
</script>

<div class="min-h-screen bg-background">
	<!-- 로그인 전 헤더 (사용자 정보 없음) -->
	<Header />

	<!-- 메인 콘텐츠 (헤더 높이만큼 상단 여백) -->
	<main class="pt-16">
		{@render children?.()}
	</main>
</div>
