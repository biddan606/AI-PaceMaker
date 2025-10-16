<script lang="ts">
	/**
	 * LoadingButton 컴포넌트
	 * 로딩 상태를 시각적으로 표시하는 버튼
	 */
	interface Props {
		type?: 'submit' | 'button' | 'reset';
		loading?: boolean;
		disabled?: boolean;
		variant?: 'primary' | 'secondary';
		loadingText?: string;
		children?: import('svelte').Snippet;
	}

	let {
		type = 'submit',
		loading = false,
		disabled = false,
		variant = 'primary',
		loadingText = '처리 중...',
		children
	}: Props = $props();

	const isDisabled = $derived(loading || disabled);
</script>

<button
	{type}
	disabled={isDisabled}
	class="w-full px-4 py-3 rounded-lg transition-colors font-medium disabled:opacity-50 disabled:cursor-not-allowed"
	class:bg-[#FF6B4A]={variant === 'primary'}
	class:hover:bg-[#FF5A39]={variant === 'primary' && !isDisabled}
	class:text-white={variant === 'primary'}
	class:bg-gray-200={variant === 'secondary'}
	class:hover:bg-gray-300={variant === 'secondary' && !isDisabled}
	class:text-gray-700={variant === 'secondary'}
>
	{#if loading}
		<span class="flex items-center justify-center gap-2">
			<svg
				class="animate-spin h-5 w-5"
				xmlns="http://www.w3.org/2000/svg"
				fill="none"
				viewBox="0 0 24 24"
			>
				<circle
					class="opacity-25"
					cx="12"
					cy="12"
					r="10"
					stroke="currentColor"
					stroke-width="4"
				></circle>
				<path
					class="opacity-75"
					fill="currentColor"
					d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
				></path>
			</svg>
			{loadingText}
		</span>
	{:else if children}
		{@render children()}
	{/if}
</button>
