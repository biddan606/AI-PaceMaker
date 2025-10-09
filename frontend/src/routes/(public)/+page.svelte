<script lang="ts">
	// 랜딩 페이지 - 서비스 소개
</script>

<div class="min-h-[calc(100vh-4rem)] bg-white">
	<!-- Hero Section -->
	<section class="container mx-auto px-4 py-16 md:py-24 max-w-6xl">
		<div class="text-center mb-16">
			<h1 class="text-4xl md:text-5xl lg:text-6xl font-bold text-[#171717] mb-6 leading-tight">
				소음 속에서 <span class="text-[#FF6B4A]">신호</span>를 찾아<br />
				지속 가능한 성장을 시작하세요
			</h1>
			<p class="text-lg md:text-xl text-[#737373] mb-8 max-w-2xl mx-auto">
				AI 페이스메이커는 당신의 목표를 구체적인 행동으로 변환하고,<br />
				매주의 진전을 데이터로 증명하는 지능형 성장 시스템입니다
			</p>
			<div class="flex flex-col sm:flex-row gap-4 justify-center">
				<a
					href="/signup"
					class="px-8 py-4 bg-[#FF6B4A] text-white rounded-lg hover:bg-[#FF5A39] transition-colors text-lg font-semibold"
				>
					무료로 시작하기
				</a>
				<a
					href="/login"
					class="px-8 py-4 bg-white text-[#171717] border-2 border-[#E5E5E5] rounded-lg hover:bg-[#F5F5F5] transition-colors text-lg font-semibold"
				>
					로그인
				</a>
			</div>
		</div>
	</section>

	<!-- Features Section -->
	<section class="bg-[#FAFAFA] py-16 md:py-24">
		<div class="container mx-auto px-4 max-w-6xl">
			<h2 class="text-3xl md:text-4xl font-bold text-[#171717] text-center mb-12">
				핵심 가치
			</h2>
			<div class="grid md:grid-cols-3 gap-8">
				<!-- Feature 1 -->
				<div class="bg-white rounded-xl p-8 shadow-sm hover:shadow-md transition-shadow">
					<div class="text-4xl mb-4">🎯</div>
					<h3 class="text-xl font-bold text-[#171717] mb-3">희미한 신호를 강력한 첫 걸음으로</h3>
					<p class="text-[#737373]">
						AI와의 대화로 막연한 목표를 명확하고 실행 가능한 구체적인 태스크로 변환합니다
					</p>
				</div>

				<!-- Feature 2 -->
				<div class="bg-white rounded-xl p-8 shadow-sm hover:shadow-md transition-shadow">
					<div class="text-4xl mb-4">📊</div>
					<h3 class="text-xl font-bold text-[#171717] mb-3">막연한 노력을 명확한 증거로</h3>
					<p class="text-[#737373]">
						'열심히 했다'는 감정이 아닌 벨로시티라는 측정 가능한 데이터로 성장을 증명합니다
					</p>
				</div>

				<!-- Feature 3 -->
				<div class="bg-white rounded-xl p-8 shadow-sm hover:shadow-md transition-shadow">
					<div class="text-4xl mb-4">🔄</div>
					<h3 class="text-xl font-bold text-[#171717] mb-3">자책을 학습의 기회로</h3>
					<p class="text-[#737373]">
						계획이 틀어졌을 때도 회고를 통해 다음 주를 위한 소중한 데이터로 활용합니다
					</p>
				</div>
			</div>
		</div>
	</section>

	<!-- How it works Section -->
	<section class="py-16 md:py-24">
		<div class="container mx-auto px-4 max-w-6xl">
			<h2 class="text-3xl md:text-4xl font-bold text-[#171717] text-center mb-12">
				이렇게 사용합니다
			</h2>
			<div class="space-y-12">
				<!-- Step 1 -->
				<div class="flex flex-col md:flex-row gap-8 items-center">
					<div class="flex-1">
						<div class="text-[#FF6B4A] font-bold text-sm mb-2">STEP 1</div>
						<h3 class="text-2xl font-bold text-[#171717] mb-3">백로그에 아이디어 추가</h3>
						<p class="text-[#737373] text-lg">
							'로그인 기능 만들기' 같은 막연한 목표를 빠르게 기록하고, AI가 구체적인 태스크로
							분해합니다
						</p>
					</div>
					<div class="flex-1 bg-[#F5F5F5] rounded-lg p-8 min-h-[200px] flex items-center justify-center">
						<span class="text-[#A3A3A3]">📋 백로그 화면 이미지</span>
					</div>
				</div>

				<!-- Step 2 -->
				<div class="flex flex-col md:flex-row-reverse gap-8 items-center">
					<div class="flex-1">
						<div class="text-[#FF6B4A] font-bold text-sm mb-2">STEP 2</div>
						<h3 class="text-2xl font-bold text-[#171717] mb-3">주간 스프린트 계획</h3>
						<p class="text-[#737373] text-lg">
							이번 주에 집중할 태스크를 선택하고, 칸반 보드로 진행 상황을 실시간으로 추적합니다
						</p>
					</div>
					<div class="flex-1 bg-[#F5F5F5] rounded-lg p-8 min-h-[200px] flex items-center justify-center">
						<span class="text-[#A3A3A3]">🎯 칸반 보드 이미지</span>
					</div>
				</div>

				<!-- Step 3 -->
				<div class="flex flex-col md:flex-row gap-8 items-center">
					<div class="flex-1">
						<div class="text-[#FF6B4A] font-bold text-sm mb-2">STEP 3</div>
						<h3 class="text-2xl font-bold text-[#171717] mb-3">회고로 성장 확인</h3>
						<p class="text-[#737373] text-lg">
							Keep-Problem-Try 회고를 통해 이번 주의 배움을 정리하고, 벨로시티로 성장을 시각화합니다
						</p>
					</div>
					<div class="flex-1 bg-[#F5F5F5] rounded-lg p-8 min-h-[200px] flex items-center justify-center">
						<span class="text-[#A3A3A3]">📊 히스토리 화면 이미지</span>
					</div>
				</div>
			</div>
		</div>
	</section>

	<!-- CTA Section -->
	<section class="bg-[#FF6B4A] py-16 md:py-20">
		<div class="container mx-auto px-4 max-w-4xl text-center">
			<h2 class="text-3xl md:text-4xl font-bold text-white mb-4">
				오늘부터 당신의 성장을 시작하세요
			</h2>
			<p class="text-white/90 text-lg mb-8">
				소음을 걸러내고 중요한 신호에 집중하는 여정, 지금 무료로 시작하세요
			</p>
			<a
				href="/signup"
				class="inline-block px-8 py-4 bg-white text-[#FF6B4A] rounded-lg hover:bg-[#F5F5F5] transition-colors text-lg font-semibold"
			>
				무료로 시작하기
			</a>
		</div>
	</section>

	<!-- Footer -->
	<footer class="bg-[#171717] text-white py-8">
		<div class="container mx-auto px-4 max-w-6xl text-center">
			<p class="text-[#A3A3A3]">© 2025 AI 페이스메이커. All rights reserved.</p>
		</div>
	</footer>
</div>
