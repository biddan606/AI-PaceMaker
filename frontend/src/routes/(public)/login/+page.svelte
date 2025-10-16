<script lang="ts">
	import { authStore } from '$lib/stores/auth.svelte';
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import { onMount } from 'svelte';
	import AuthCard from '$lib/components/auth/AuthCard.svelte';
	import FormInput from '$lib/components/auth/FormInput.svelte';
	import LoadingButton from '$lib/components/auth/LoadingButton.svelte';
	import ErrorAlert from '$lib/components/auth/ErrorAlert.svelte';

	let email = $state('');
	let password = $state('');
	let errorMessage = $state('');
	let successMessage = $state('');
	let isLoading = $state(false);

	// URL 쿼리 파라미터에서 성공 메시지 읽기 (회원가입 성공 시)
	onMount(() => {
		const message = $page.url.searchParams.get('message');
		if (message) {
			successMessage = decodeURIComponent(message);
		}
	});

	async function handleLogin(event: Event) {
		event.preventDefault();
		errorMessage = '';
		successMessage = '';
		isLoading = true;

		try {
			const result = await authStore.login(email, password);

			if (result.success) {
				// 이메일 미인증 사용자 처리
				if (result.emailVerified === false) {
					await goto('/verify-email?email=' + encodeURIComponent(email));
					return;
				}

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

<AuthCard title="로그인" description="AI 페이스메이커에 오신 것을 환영합니다">
	{#snippet children()}
		<ErrorAlert message={successMessage} type="success" />
		<ErrorAlert message={errorMessage} type="error" />

		<form onsubmit={handleLogin} class="space-y-4">
			<FormInput
				id="email"
				label="이메일"
				type="email"
				placeholder="example@email.com"
				bind:value={email}
				required
				disabled={isLoading}
				autocomplete="email"
			/>

			<FormInput
				id="password"
				label="비밀번호"
				type="password"
				placeholder="••••••••"
				bind:value={password}
				required
				disabled={isLoading}
				autocomplete="current-password"
			/>

			<LoadingButton loading={isLoading} loadingText="로그인 중...">
				{#snippet children()}
					로그인
				{/snippet}
			</LoadingButton>
		</form>

		<div class="mt-6 text-center">
			<p class="text-sm text-[#737373]">
				계정이 없으신가요?
				<a href="/signup" class="text-[#FF6B4A] font-medium hover:underline">회원가입</a>
			</p>
		</div>
	{/snippet}
</AuthCard>
