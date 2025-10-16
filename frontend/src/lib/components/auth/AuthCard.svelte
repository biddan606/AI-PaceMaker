<script lang="ts">
	/**
	 * AuthCard 컴포넌트
	 * 인증 화면의 중앙 카드 래퍼
	 */
	interface Props {
		title: string;
		description: string;
		children?: import('svelte').Snippet;
	}

	let { title, description, children }: Props = $props();
</script>

<div class="min-h-[calc(100vh-4rem)] flex items-center justify-center p-4">
	<div class="w-full max-w-md">
		<div class="card">
			<h1 class="text-2xl font-bold text-[#171717] mb-2">{title}</h1>
			<p class="text-sm text-[#737373] mb-6">{description}</p>

			{#if children}
				{@render children()}
			{/if}
		</div>
	</div>
</div>
