<script lang="ts">
	/**
	 * FormInput 컴포넌트
	 * 레이블, 입력 필드, 에러 메시지를 통합한 폼 입력 컴포넌트
	 */
	interface Props {
		id: string;
		label: string;
		type?: 'text' | 'email' | 'password';
		placeholder?: string;
		value: string;
		error?: string;
		required?: boolean;
		disabled?: boolean;
		autocomplete?: string;
		minlength?: number;
		onInput?: (value: string) => void;
	}

	let {
		id,
		label,
		type = 'text',
		placeholder = '',
		value = $bindable(),
		error = '',
		required = false,
		disabled = false,
		autocomplete,
		minlength,
		onInput
	}: Props = $props();

	function handleInput(event: Event) {
		const target = event.target as HTMLInputElement;
		value = target.value;
		if (onInput) {
			onInput(target.value);
		}
	}
</script>

<div>
	<label for={id} class="block text-sm font-medium text-[#404040] mb-1">
		{label}
		{#if required}
			<span class="text-red-500" aria-label="필수">*</span>
		{/if}
	</label>
	<input
		{id}
		{type}
		{placeholder}
		{value}
		{disabled}
		{required}
		autocomplete={autocomplete}
		minlength={minlength}
		class="input"
		class:border-red-500={error}
		aria-invalid={!!error}
		aria-describedby={error ? `${id}-error` : undefined}
		aria-required={required}
		oninput={handleInput}
	/>
	{#if error}
		<p id="{id}-error" class="mt-1 text-sm text-red-600" role="alert">
			{error}
		</p>
	{/if}
</div>
