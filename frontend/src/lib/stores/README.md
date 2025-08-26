# Global Stores

애플리케이션 전역에서 사용하는 상태 관리 스토어입니다.

## 포함 내용

- 사용자 세션 정보
- 인증 상태
- 전역 설정 (테마, 언어 등)
- 전역 알림 상태

## 사용 원칙

- 진정한 "전역" 상태만 포함
- 기능별 상태는 각 feature의 stores에 관리
- Svelte 5 runes 활용 ($state, $derived)