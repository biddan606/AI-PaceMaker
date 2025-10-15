<script lang="ts">
	interface Props {
		src?: string;
		alt: string;
		fallback?: string;
		size?: 'sm' | 'md' | 'lg';
	}

	let { src, alt, fallback, size = 'md' }: Props = $props();

	// 이미지 로드 실패 시 fallback 표시
	let imageError = $state(false);

	function handleImageError() {
		imageError = true;
	}

	// 사이즈별 클래스
	const sizeClasses = {
		sm: 'w-8 h-8 text-sm',
		md: 'w-10 h-10 text-base',
		lg: 'w-16 h-16 text-2xl'
	};

	// fallback 텍스트가 없으면 alt의 첫 글자 사용
	const displayFallback = $derived(
		fallback || alt.charAt(0).toUpperCase() || '?'
	);
</script>

<div
	class="rounded-full bg-[#FF6B4A] text-white flex items-center justify-center font-semibold flex-shrink-0 {sizeClasses[size]}"
	title={alt}
>
	{#if src && !imageError}
		<img
			{src}
			{alt}
			class="w-full h-full rounded-full object-cover"
			onerror={handleImageError}
		/>
	{:else}
		<span>{displayFallback}</span>
	{/if}
</div>
