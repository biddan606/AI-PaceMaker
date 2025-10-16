<script lang="ts">
	import { goto } from '$app/navigation';
	import { authStore } from '$lib/stores/auth.svelte';
	import AuthCard from '$lib/components/auth/AuthCard.svelte';
	import FormInput from '$lib/components/auth/FormInput.svelte';
	import LoadingButton from '$lib/components/auth/LoadingButton.svelte';
	import ErrorAlert from '$lib/components/auth/ErrorAlert.svelte';

	let name = $state('');
	let email = $state('');
	let password = $state('');
	let passwordConfirm = $state('');

	let errorMessage = $state('');
	let fieldErrors = $state({
		name: '',
		email: '',
		password: '',
		passwordConfirm: ''
	});
	let isLoading = $state(false);

	// 클라이언트 사이드 유효성 검사
	function validateForm(): boolean {
		fieldErrors = {
			name: '',
			email: '',
			password: '',
			passwordConfirm: ''
		};

		let isValid = true;

		if (!name.trim()) {
			fieldErrors.name = '이름을 입력해주세요';
			isValid = false;
		}

		if (!email.trim()) {
			fieldErrors.email = '이메일을 입력해주세요';
			isValid = false;
		} else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
			fieldErrors.email = '올바른 이메일 주소를 입력해주세요';
			isValid = false;
		}

		if (!password) {
			fieldErrors.password = '비밀번호를 입력해주세요';
			isValid = false;
		} else if (password.length < 8) {
			fieldErrors.password = '비밀번호는 8자 이상이어야 합니다';
			isValid = false;
		}

		if (!passwordConfirm) {
			fieldErrors.passwordConfirm = '비밀번호 확인을 입력해주세요';
			isValid = false;
		} else if (password !== passwordConfirm) {
			fieldErrors.passwordConfirm = '비밀번호가 일치하지 않습니다';
			isValid = false;
		}

		return isValid;
	}

	async function handleSignup(event: Event) {
		event.preventDefault();
		errorMessage = '';

		if (!validateForm()) {
			return;
		}

		isLoading = true;

		try {
			const result = await authStore.register(name, email, password);

			if (result.success) {
				// 회원가입 성공 - 로그인 페이지로 이동하며 성공 메시지 전달
				await goto(`/login?message=${encodeURIComponent(result.message || '회원가입이 완료되었습니다.')}`);
			} else {
				errorMessage = result.error || '회원가입에 실패했습니다.';
			}
		} catch (error) {
			errorMessage = '회원가입 중 오류가 발생했습니다.';
			console.error('회원가입 오류:', error);
		} finally {
			isLoading = false;
		}
	}
</script>

<AuthCard title="회원가입" description="성장의 여정을 시작하세요">
	{#snippet children()}
		<ErrorAlert message={errorMessage} type="error" />

		<form onsubmit={handleSignup} class="space-y-4">
			<FormInput
				id="name"
				label="이름"
				type="text"
				placeholder="홍길동"
				bind:value={name}
				error={fieldErrors.name}
				required
				disabled={isLoading}
			/>

			<FormInput
				id="email"
				label="이메일"
				type="email"
				placeholder="example@email.com"
				bind:value={email}
				error={fieldErrors.email}
				required
				disabled={isLoading}
				autocomplete="email"
			/>

			<FormInput
				id="password"
				label="비밀번호"
				type="password"
				placeholder="8자 이상 입력"
				bind:value={password}
				error={fieldErrors.password}
				required
				disabled={isLoading}
				minlength={8}
				autocomplete="new-password"
			/>

			<FormInput
				id="password-confirm"
				label="비밀번호 확인"
				type="password"
				placeholder="비밀번호 재입력"
				bind:value={passwordConfirm}
				error={fieldErrors.passwordConfirm}
				required
				disabled={isLoading}
				minlength={8}
				autocomplete="new-password"
			/>

			<LoadingButton loading={isLoading} loadingText="회원가입 중...">
				{#snippet children()}
					회원가입
				{/snippet}
			</LoadingButton>
		</form>

		<div class="mt-6 text-center">
			<p class="text-sm text-[#737373]">
				이미 계정이 있으신가요?
				<a href="/login" class="text-[#FF6B4A] font-medium hover:underline">로그인</a>
			</p>
		</div>
	{/snippet}
</AuthCard>
