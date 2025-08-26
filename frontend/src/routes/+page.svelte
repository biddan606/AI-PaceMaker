<script lang="ts">
	import { onMount } from 'svelte';
	
	let greeting = $state('AI 페이스메이커에 오신 것을 환영합니다');
</script>

<div class="min-h-screen gradient-morning">
	<div class="container mx-auto px-4 py-16">
		<div class="text-center mb-12">
			<h1 class="text-5xl font-bold mb-4 text-primary">
				{greeting}
			</h1>
			<p class="text-xl text-gray-600 dark:text-gray-300">
				당신의 일상에 성장과 성찰을 더합니다
			</p>
		</div>
		
		<div class="grid md:grid-cols-3 gap-6 max-w-4xl mx-auto">
			<div class="card">
				<h2 class="text-2xl font-semibold mb-3 text-primary">🌅 아침 다짐</h2>
				<p class="text-gray-600 dark:text-gray-400 mb-4">
					하루를 시작하며 오늘의 목표와 다짐을 설정하세요
				</p>
				<button class="btn-primary w-full">
					시작하기
				</button>
			</div>
			
			<div class="card">
				<h2 class="text-2xl font-semibold mb-3 text-secondary">🌙 저녁 성찰</h2>
				<p class="text-gray-600 dark:text-gray-400 mb-4">
					하루를 마무리하며 배운 점과 감사를 기록하세요
				</p>
				<button class="btn-secondary w-full">
					성찰하기
				</button>
			</div>
			
			<div class="card">
				<h2 class="text-2xl font-semibold mb-3 text-accent">📊 기록 보기</h2>
				<p class="text-gray-600 dark:text-gray-400 mb-4">
					당신의 성장 여정을 돌아보세요
				</p>
				<button class="w-full bg-accent text-accent-foreground px-4 py-2 rounded-lg hover:opacity-90 transition-opacity">
					기록 확인
				</button>
			</div>
		</div>
	</div>
</div>