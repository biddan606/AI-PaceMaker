# Shared

여러 기능에서 공통으로 사용하는 모듈입니다.

## 구조

```
shared/
├── components/
│   └── ui/        # 공통 UI 컴포넌트 (Button, Card, Dialog 등)
├── services/      # 공통 API 설정 (axios instance, error handler 등)
└── utils/         # 공통 유틸리티 함수 (날짜 포맷, 유효성 검사 등)
```

## 사용 원칙

- 2개 이상의 feature에서 사용하는 코드만 shared로 이동
- 도메인 로직이 없는 순수한 기능만 포함
- UI 컴포넌트는 shadcn-svelte 기반으로 구성