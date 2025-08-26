# Features

각 기능별로 독립적으로 관리되는 모듈입니다.

## 구조

```
features/
├── morning-resolution/    # 아침 다짐 기능
├── evening-reflection/    # 저녁 회고 기능
├── history/              # 히스토리 조회 기능
└── onboarding/           # 온보딩 기능
```

## 각 기능 모듈 구조

```
feature-name/
├── components/   # 해당 기능 전용 컴포넌트
├── stores/       # 해당 기능 상태 관리
├── services/     # 해당 기능 API 통신
└── types/        # 해당 기능 타입 정의
```

## 원칙

- 각 기능은 독립적으로 개발/테스트 가능해야 함
- 기능 간 직접적인 의존성을 피하고, 필요시 shared 모듈 활용
- PRD 문서의 Epic과 1:1 매핑