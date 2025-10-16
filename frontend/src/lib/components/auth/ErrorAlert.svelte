<script lang="ts">
	/**
	 * ErrorAlert 컴포넌트
	 * 전역 에러/성공 메시지 표시
	 */
	interface Props {
		message: string;
		type?: 'error' | 'success' | 'info';
	}

	let { message, type = 'error' }: Props = $props();

	const styles = $derived({
		error: {
			bg: 'bg-red-50',
			border: 'border-red-200',
			text: 'text-red-600'
		},
		success: {
			bg: 'bg-green-50',
			border: 'border-green-200',
			text: 'text-green-600'
		},
		info: {
			bg: 'bg-blue-50',
			border: 'border-blue-200',
			text: 'text-blue-600'
		}
	}[type]);
</script>

{#if message}
	<div
		class="mb-4 p-3 rounded-lg text-sm border {styles.bg} {styles.border} {styles.text}"
		role="alert"
	>
		{message}
	</div>
{/if}
