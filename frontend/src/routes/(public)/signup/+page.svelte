<script lang="ts">
	// TODO: 실제 회원가입 로직 구현
	function handleSignup(event: Event) {
		event.preventDefault();
		// 임시: 로그인 페이지로 리다이렉트
		window.location.href = '/login';
	}
</script>

<div class="min-h-[calc(100vh-4rem)] flex items-center justify-center p-4">
	<div class="w-full max-w-md">
		<div class="card">
			<h1 class="text-2xl font-bold text-[#171717] mb-2">회원가입</h1>
			<p class="text-sm text-[#737373] mb-6">성장의 여정을 시작하세요</p>

			<form onsubmit={handleSignup} class="space-y-4">
				<div>
					<label for="name" class="block text-sm font-medium text-[#404040] mb-1"> 이름 </label>
					<input type="text" id="name" class="input" placeholder="홍길동" required />
				</div>

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
					<input
						type="password"
						id="password"
						class="input"
						placeholder="8자 이상 입력"
						minlength="8"
						required
					/>
				</div>

				<div>
					<label for="password-confirm" class="block text-sm font-medium text-[#404040] mb-1">
						비밀번호 확인
					</label>
					<input
						type="password"
						id="password-confirm"
						class="input"
						placeholder="비밀번호 재입력"
						minlength="8"
						required
					/>
				</div>

				<button
					type="submit"
					class="w-full px-4 py-3 bg-[#FF6B4A] text-white rounded-lg hover:bg-[#FF5A39] transition-colors font-medium"
				>
					회원가입
				</button>
			</form>

			<div class="mt-6 text-center">
				<p class="text-sm text-[#737373]">
					이미 계정이 있으신가요?
					<a href="/login" class="text-[#FF6B4A] font-medium hover:underline">로그인</a>
				</p>
			</div>
		</div>
	</div>
</div>
