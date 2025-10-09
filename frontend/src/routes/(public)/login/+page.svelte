<script lang="ts">
	// TODO: 실제 로그인 로직 구현
	function handleLogin(event: Event) {
		event.preventDefault();
		// 임시: 앱 페이지로 리다이렉트
		window.location.href = '/app/backlog';
	}
</script>

<div class="min-h-[calc(100vh-4rem)] flex items-center justify-center p-4">
	<div class="w-full max-w-md">
		<div class="card">
			<h1 class="text-2xl font-bold text-[#171717] mb-2">로그인</h1>
			<p class="text-sm text-[#737373] mb-6">AI 페이스메이커에 오신 것을 환영합니다</p>

			<form onsubmit={handleLogin} class="space-y-4">
				<div>
					<label for="email" class="block text-sm font-medium text-[#404040] mb-1">
						이메일
					</label>
					<input
						type="email"
						id="email"
						class="input"
						placeholder="example@email.com"
						required
					/>
				</div>

				<div>
					<label for="password" class="block text-sm font-medium text-[#404040] mb-1">
						비밀번호
					</label>
					<input type="password" id="password" class="input" placeholder="••••••••" required />
				</div>

				<button
					type="submit"
					class="w-full px-4 py-3 bg-[#FF6B4A] text-white rounded-lg hover:bg-[#FF5A39] transition-colors font-medium"
				>
					로그인
				</button>
			</form>

			<div class="mt-6 text-center">
				<p class="text-sm text-[#737373]">
					계정이 없으신가요?
					<a href="/signup" class="text-[#FF6B4A] font-medium hover:underline">회원가입</a>
				</p>
			</div>
		</div>
	</div>
</div>
