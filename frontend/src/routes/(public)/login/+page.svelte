<script lang="ts">
	import { authStore } from '$lib/stores/auth.svelte';
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';

	let email = $state('');
	let password = $state('');
	let errorMessage = $state('');
	let isLoading = $state(false);

	async function handleLogin(event: Event) {
		event.preventDefault();
		errorMessage = '';
		isLoading = true;

		try {
			const result = await authStore.login(email, password);

			if (result.success) {
				// 로그인 성공 시 redirectTo 파라미터 확인하여 원래 페이지로 복귀
				const redirectTo = $page.url.searchParams.get('redirectTo') || '/app/backlog';
				await goto(redirectTo);
			} else {
				errorMessage = result.error || '로그인에 실패했습니다.';
			}
		} catch (error) {
			errorMessage = '로그인 중 오류가 발생했습니다.';
			console.error('로그인 오류:', error);
		} finally {
			isLoading = false;
		}
	}
</script>

<div class="min-h-[calc(100vh-4rem)] flex items-center justify-center p-4">
	<div class="w-full max-w-md">
		<div class="card">
			<h1 class="text-2xl font-bold text-[#171717] mb-2">로그인</h1>
			<p class="text-sm text-[#737373] mb-6">AI 페이스메이커에 오신 것을 환영합니다</p>

			{#if errorMessage}
				<div class="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-600">
					{errorMessage}
				</div>
			{/if}

			<form onsubmit={handleLogin} class="space-y-4">
				<div>
					<label for="email" class="block text-sm font-medium text-[#404040] mb-1">
						이메일
					</label>
					<input
						type="email"
						id="email"
						class="input"
						placeholder="example@email.com"
						bind:value={email}
						disabled={isLoading}
						required
					/>
				</div>

				<div>
					<label for="password" class="block text-sm font-medium text-[#404040] mb-1">
						비밀번호
					</label>
					<input
						type="password"
						id="password"
						class="input"
						placeholder="••••••••"
						bind:value={password}
						disabled={isLoading}
						required
					/>
				</div>

				<button
					type="submit"
					class="w-full px-4 py-3 bg-[#FF6B4A] text-white rounded-lg hover:bg-[#FF5A39] transition-colors font-medium disabled:opacity-50 disabled:cursor-not-allowed"
					disabled={isLoading}
				>
					{isLoading ? '로그인 중...' : '로그인'}
				</button>
			</form>

			<div class="mt-6 text-center">
				<p class="text-sm text-[#737373]">
					계정이 없으신가요?
					<a href="/signup" class="text-[#FF6B4A] font-medium hover:underline">회원가입</a>
				</p>
			</div>
		</div>
	</div>
</div>
