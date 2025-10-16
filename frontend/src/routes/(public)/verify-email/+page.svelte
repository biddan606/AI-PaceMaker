<script lang="ts">
	import { onMount } from 'svelte';
	import { page } from '$app/stores';
	import { goto } from '$app/navigation';
	import AuthCard from '$lib/components/auth/AuthCard.svelte';
	import ErrorAlert from '$lib/components/auth/ErrorAlert.svelte';
	import LoadingButton from '$lib/components/auth/LoadingButton.svelte';
	import { apiPost } from '$lib/api/client';

	let email = $state('');
	let token = $state('');
	let isVerifying = $state(false);
	let isVerified = $state(false);
	let errorMessage = $state('');
	let successMessage = $state('');

	onMount(() => {
		// URL 쿼리 파라미터에서 이메일과 토큰 읽기
		email = $page.url.searchParams.get('email') || '';
		token = $page.url.searchParams.get('token') || '';

		// 토큰이 있으면 자동으로 인증 시도
		if (token) {
			verifyEmail();
		}
	});

	async function verifyEmail() {
		if (!token) {
			errorMessage = '유효하지 않은 인증 링크입니다.';
			return;
		}

		isVerifying = true;
		errorMessage = '';

		try {
			const response = await apiPost<{
				userId: number;
				email: string;
				verified: boolean;
				message: string;
			}>('/api/users/verification', { token });

			isVerified = true;
			successMessage = response.message || '이메일 인증이 완료되었습니다.';

			// 3초 후 로그인 페이지로 이동
			setTimeout(() => {
				goto('/login?message=' + encodeURIComponent('이메일 인증이 완료되었습니다. 로그인해주세요.'));
			}, 3000);
		} catch (error) {
			errorMessage = error instanceof Error ? error.message : '이메일 인증에 실패했습니다.';
		} finally {
			isVerifying = false;
		}
	}

	function handleGoToLogin() {
		goto('/login');
	}
</script>

<AuthCard title="이메일 인증" description="계정을 활성화하기 위해 이메일을 확인해주세요">
	{#snippet children()}
		<ErrorAlert message={errorMessage} type="error" />
		<ErrorAlert message={successMessage} type="success" />

		{#if isVerifying}
			<div class="text-center py-8">
				<div class="animate-spin h-12 w-12 border-4 border-[#FF6B4A] border-t-transparent rounded-full mx-auto mb-4"></div>
				<p class="text-[#737373]">이메일 인증 중...</p>
			</div>
		{:else if isVerified}
			<div class="text-center py-8">
				<div class="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
					<svg class="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
					</svg>
				</div>
				<p class="text-lg font-medium text-[#171717] mb-2">인증이 완료되었습니다!</p>
				<p class="text-sm text-[#737373]">잠시 후 로그인 페이지로 이동합니다...</p>
			</div>
		{:else if !token}
			<!-- 토큰이 없는 경우: 인증 대기 화면 -->
			<div class="text-center py-8">
				<div class="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-4">
					<svg class="w-8 h-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
						<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"></path>
					</svg>
				</div>
				<p class="text-lg font-medium text-[#171717] mb-2">이메일을 확인해주세요</p>
				{#if email}
					<p class="text-sm text-[#737373] mb-4">
						<span class="font-medium text-[#404040]">{email}</span>로<br />
						인증 링크를 전송했습니다.
					</p>
				{:else}
					<p class="text-sm text-[#737373] mb-4">
						회원가입 시 입력하신 이메일로<br />
						인증 링크를 전송했습니다.
					</p>
				{/if}
				<p class="text-sm text-[#737373] mb-6">
					이메일의 인증 링크를 클릭하여<br />
					계정을 활성화해주세요.
				</p>

				<div class="bg-gray-50 border border-gray-200 rounded-lg p-4 text-left text-sm text-[#737373]">
					<p class="font-medium text-[#404040] mb-2">💡 이메일이 오지 않았나요?</p>
					<ul class="space-y-1 list-disc list-inside">
						<li>스팸 메일함을 확인해주세요</li>
						<li>이메일 주소가 정확한지 확인해주세요</li>
						<li>몇 분 후에 다시 시도해주세요</li>
					</ul>
				</div>
			</div>

			<LoadingButton variant="secondary" onclick={handleGoToLogin}>
				{#snippet children()}
					로그인 페이지로 이동
				{/snippet}
			</LoadingButton>
		{/if}
	{/snippet}
</AuthCard>
