# 레이아웃 및 네비게이션 시스템 설계 명세서

**작업 ID:** v1.0.0/DS/00
**작성일:** 2025-10-09
**상태:** 설계 완료

---

## 1. 개요

### 1.1 목적
AI 페이스메이커 서비스 전체의 일관된 레이아웃 구조와 네비게이션 시스템을 정의하여, 사용자가 로그인 전후 모든 화면에서 직관적이고 효율적으로 기능에 접근할 수 있도록 한다.

### 1.2 사용자 스토리
> When 사용자가 서비스의 모든 기능에 접근하고 화면 간 이동할 때, 일관되고 직관적인 레이아웃과 네비게이션을 통해 원하는 기능을 빠르게 찾고 사용할 수 있어야 한다.

### 1.3 기술 스택
- **프레임워크**: SvelteKit 5 + Svelte 5 (Runes API)
- **스타일링**: Tailwind CSS
- **컴포넌트**: bits-ui (shadcn/ui 기반)
- **디자인 시스템**: `src/app.css` (컬러 토큰, 유틸리티 클래스 정의됨)

---

## 2. 반응형 디자인 전략

### 2.1 브레이크포인트 정의
디자인 컨셉 가이드라인 기준 (Mobile-first 접근):

| 디바이스 | 범위 | Tailwind 클래스 | 네비게이션 패턴 |
|---------|------|----------------|----------------|
| Mobile | < 768px | `(default)` | 하단 탭바 (64px 고정) |
| Tablet | 768px - 1024px | `md:` ~ `lg:` | 사이드바 (토글) |
| Desktop | ≥ 1025px | `lg:` | 사이드바 (고정, 240px) |

**레이아웃 시스템:**
- Grid: 12-column Grid System
- Container Max-width: 1280px
- Column Gap: 24px (Desktop) / 16px (Mobile)
- 여백: 8pt Grid System (4px, 8px, 12px, 16px, 24px, 32px, 48px, 64px)

### 2.2 레이아웃 패턴

#### 로그인 전 (Public Layout)
```
┌─────────────────────────────────────────┐
│ Header: [Logo] ────────── [로그인]      │
├─────────────────────────────────────────┤
│                                         │
│         Main Content                    │
│         (랜딩/로그인/회원가입)             │
│                                         │
└─────────────────────────────────────────┘
```

#### 로그인 후 - Desktop (≥1024px)
```
┌───────────────────────────────────────────────┐
│ Header: [Logo] ───────────── [User Menu ▼]   │
├──────────┬────────────────────────────────────┤
│          │                                    │
│ Sidebar  │  Main Content Area                 │
│ (240px)  │                                    │
│ ┌──────┐ │                                    │
│ │🏠 대시보드│                                    │
│ │📋 백로그 │                                    │
│ │🎯 스프린트│                                    │
│ │🔍 회고   │                                    │
│ │📈 벨로시티│                                    │
│ │⚙️ 설정   │                                    │
│ └──────┘ │                                    │
│          │                                    │
└──────────┴────────────────────────────────────┘
```

#### 로그인 후 - Mobile (<640px)
```
┌─────────────────────────────────┐
│ Header: [☰] [Logo] ──── [👤]   │
├─────────────────────────────────┤
│                                 │
│  Main Content Area              │
│                                 │
│                                 │
│                                 │
│                                 │
├─────────────────────────────────┤
│ BottomNav: [🏠][📋][🎯][🔍][📈] │
└─────────────────────────────────┘
```

---

## 3. 네비게이션 메뉴 구조

### 3.1 메뉴 항목 정의

디자인 컨셉 기준 4개 핵심 메뉴:

| 아이콘 | 메뉴명 | 경로 | 설명 | 표시 위치 |
|--------|--------|------|------|----------|
| 📋 | 백로그 | `/app/backlog` | 아이디어 및 태스크 풀, 빠른 생성 | 모바일 탭바 + 사이드바 |
| 🎯 | 이번 주 스프린트 | `/app/sprint` | 칸반 보드, 진행 중인 작업 | 모바일 탭바 + 사이드바 |
| 📊 | 히스토리 | `/app/history` | 지난 스프린트, KPT 리포트 | 모바일 탭바 + 사이드바 |
| ⚙️ | 설정 | `/app/settings` | 프로필 및 환경설정 | 사이드바 하단만 |

**메뉴 배치 원칙:**
- 모바일 하단 탭바: 최대 4개 (복잡도 감소)
- 사이드바: 상단 3개 메뉴 + 하단 설정
- 활성 상태: Coral 좌측 border(3px) + Coral 10% 배경 (데스크탑), Coral 아이콘+텍스트 (모바일)

### 3.2 모바일 하단 탭바 (64px 고정)
디자인 컨셉 기준 **4개 메뉴만 표시**:
- 백로그, 이번 주 스프린트, 히스토리 + (추가 메뉴는 향후 확장)
- 설정은 헤더 우측 프로필 버튼 통해 접근

**스타일:**
- 배경: White (`bg-card`)
- 상단 Shadow: `0 -2px 8px rgba(0,0,0,0.08)`
- 아이콘 + 라벨 세로 배치
- 활성: Coral 색상 (#FF6B4A)

### 3.3 활성 상태 표시 (디자인 컨셉 적용)
**데스크톱 사이드바:**
- 활성: Coral 좌측 border (3px) + Coral 10% opacity 배경 (`bg-[#FF6B4A]/10`)
- 비활성: Gray 700 텍스트, 호버 시 Gray 100 배경

**모바일 탭바:**
- 활성: Coral 아이콘 + 텍스트
- 비활성: Gray 400

---

## 4. 라우트 구조 설계

### 4.1 SvelteKit Route Groups 활용

```
src/routes/
├── (public)/                    # 로그인 전 레이아웃 그룹
│   ├── +layout.svelte          # 심플 헤더만 포함
│   ├── +page.svelte            # 랜딩 페이지
│   ├── login/
│   │   └── +page.svelte        # 로그인 페이지
│   └── signup/
│       └── +page.svelte        # 회원가입 페이지
│
└── (app)/                       # 로그인 후 레이아웃 그룹
    ├── +layout.svelte          # 헤더 + 사이드바 + 메인 콘텐츠
    ├── backlog/
    │   └── +page.svelte        # 백로그 목록 (빠른 생성, AI 구체화)
    ├── sprint/
    │   ├── +page.svelte        # 칸반 보드 (진행 중인 스프린트)
    │   └── plan/
    │       └── +page.svelte    # 스프린트 계획 화면
    ├── history/
    │   └── +page.svelte        # 히스토리 (지난 스프린트, Bento Grid)
    └── settings/
        └── +page.svelte        # 설정 (프로필, 환경설정)
```

**핵심 라우트 (디자인 컨셉 6개 화면 기준):**
1. `/login`, `/signup` - 회원가입/로그인
2. `/app/backlog` - 백로그 목록 (빠른 생성 + AI 구체화)
3. `/app/sprint/plan` - 스프린트 계획 화면
4. `/app/sprint` - 칸반 보드 (진행 중인 스프린트)
5. `/app/history` - 히스토리 페이지 (회고 화면 포함)
6. `/app/settings` - 설정

### 4.2 라우트 그룹 특징
- **`(public)`**: 괄호로 URL에 포함되지 않음, 로그인 불필요
- **`(app)`**: 괄호로 URL에 포함되지 않음, 인증 필요 (향후 가드 추가)

---

## 5. 컴포넌트 설계

### 5.1 컴포넌트 파일 구조

```
src/lib/components/layout/
├── Header.svelte           # 상단 헤더
├── Sidebar.svelte          # 데스크톱/태블릿 사이드바
├── MobileNav.svelte        # 모바일 하단 네비게이션
├── UserMenu.svelte         # 사용자 드롭다운 메뉴
└── NavItem.svelte          # 네비게이션 아이템 (재사용)
```

### 5.2 컴포넌트 명세

#### 5.2.1 Header.svelte

**책임:**
- 로고 표시 (클릭 시 대시보드로 이동)
- 사이드바 토글 버튼 (모바일/태블릿, `lg:` 미만)
- 사용자 메뉴 표시 (우측 상단)

**Props:**
```typescript
interface HeaderProps {
  user?: {
    name: string;
    email: string;
    avatar?: string;
  };
}
```

**스타일 (디자인 컨셉 적용):**
- 높이: `h-16` (64px)
- 배경: Gray 50 (`bg-[#FAFAFA]`)
- 테두리: `border-b border-[#E5E5E5]` (Gray 200)
- 고정: `fixed top-0 left-0 right-0 z-50`

**레이아웃:**
```svelte
<header class="fixed top-0 left-0 right-0 h-16 bg-card border-b border-border z-50">
  <div class="container h-full flex items-center justify-between px-4">
    <!-- 좌측: 사이드바 토글(���바일) + 로고 -->
    <div class="flex items-center gap-3">
      <button class="lg:hidden btn-ghost p-2">☰</button>
      <a href="/app/dashboard">Logo</a>
    </div>

    <!-- 우측: 사용자 메뉴 -->
    <UserMenu {user} />
  </div>
</header>
```

---

#### 5.2.2 Sidebar.svelte

**책임:**
- 네비게이션 메뉴 항목 표시
- 현재 경로 기반 활성 상태 표시
- 반응형 표시/숨김 처리

**Props:**
```typescript
interface SidebarProps {
  isOpen?: boolean;        // 모바일/태블릿 오버레이 모드 상태
  onClose?: () => void;    // 오버레이 닫기 콜백
}
```

**반응형 동작:**
- **Desktop (≥1024px)**: 항상 표시, 고정 (`fixed left-0`)
- **Tablet/Mobile (<1024px)**: `isOpen` prop에 따라 오버레이 모달로 표시

**스타일:**
- 너비: `w-60` (240px)
- 배경: `bg-card border-r border-border`
- 위치: `fixed top-16 bottom-0 left-0` (헤더 아래)
- 모바일 오버레이: `fixed inset-0 bg-background/80 backdrop-blur-sm`

**레이아웃:**
```svelte
<!-- Desktop: 고정 사이드바 -->
<aside class="hidden lg:block fixed top-16 bottom-0 left-0 w-60 bg-card border-r border-border">
  <nav class="p-4 space-y-1">
    {#each navItems as item}
      <NavItem {item} active={$page.url.pathname === item.href} />
    {/each}
  </nav>
</aside>

<!-- Mobile/Tablet: 오버레이 사이드바 -->
{#if isOpen}
  <div class="lg:hidden fixed inset-0 z-40 bg-background/80 backdrop-blur-sm">
    <aside class="fixed top-16 bottom-0 left-0 w-60 bg-card border-r border-border">
      <!-- 동일한 네비게이션 -->
    </aside>
  </div>
{/if}
```

---

#### 5.2.3 MobileNav.svelte

**책임:**
- 모바일 환경에서 하단 고정 네비게이션 바 표시
- 최대 5개 핵심 메뉴만 표시

**Props:**
```typescript
// Props 없음 (내부적으로 $page.url.pathname 사용)
```

**스타일:**
- 위치: `fixed bottom-0 left-0 right-0 z-50`
- 높이: `h-16` (64px)
- 배경: `bg-card border-t border-border`
- 표시 조건: `lg:hidden` (데스크톱에서 숨김)

**레이아웃:**
```svelte
<nav class="lg:hidden fixed bottom-0 left-0 right-0 h-16 bg-card border-t border-border z-50">
  <div class="h-full flex items-center justify-around px-2">
    {#each mobileNavItems as item}
      <a
        href={item.href}
        class="flex flex-col items-center justify-center gap-1 px-3 py-2 rounded-lg"
        class:active={$page.url.pathname === item.href}
      >
        <span class="text-xl">{item.icon}</span>
        <span class="text-xs">{item.label}</span>
      </a>
    {/each}
  </div>
</nav>
```

---

#### 5.2.4 UserMenu.svelte

**책임:**
- 사용자 아바타 및 이름 표시
- 드롭다운 메뉴 (프로필, 설정, 로그아웃)

**Props:**
```typescript
interface UserMenuProps {
  user: {
    name: string;
    email: string;
    avatar?: string;
  };
}
```

**드롭다운 메뉴 항목:**
1. 프로필 보기 (`/app/settings/profile`)
2. 설정 (`/app/settings`)
3. 구분선
4. 로그아웃 (로그아웃 액션)

**스타일:**
- bits-ui의 `DropdownMenu` 컴포넌트 활용
- 트리거: 사용자 아바타 + 이름

**레이아웃:**
```svelte
<DropdownMenu.Root>
  <DropdownMenu.Trigger class="flex items-center gap-2 btn-ghost px-3 py-2">
    <Avatar src={user.avatar} fallback={user.name[0]} />
    <span class="hidden md:inline">{user.name}</span>
  </DropdownMenu.Trigger>

  <DropdownMenu.Content>
    <DropdownMenu.Item href="/app/settings/profile">
      프로필
    </DropdownMenu.Item>
    <DropdownMenu.Item href="/app/settings">
      설정
    </DropdownMenu.Item>
    <DropdownMenu.Separator />
    <DropdownMenu.Item onclick={handleLogout}>
      로그아웃
    </DropdownMenu.Item>
  </DropdownMenu.Content>
</DropdownMenu.Root>
```

---

#### 5.2.5 NavItem.svelte

**책임:**
- 단일 네비게이션 메뉴 아이템 렌더링
- 활성 상태 스타일링

**Props:**
```typescript
interface NavItemProps {
  item: {
    icon: string;
    label: string;
    href: string;
  };
  active?: boolean;
}
```

**스타일:**
- 활성: `bg-primary text-primary-foreground`
- 비활성: `text-muted-foreground hover:bg-accent/10 hover:text-accent`

**레이아웃:**
```svelte
<a
  href={item.href}
  class="flex items-center gap-3 px-3 py-2 rounded-lg transition-colors"
  class:bg-primary={active}
  class:text-primary-foreground={active}
  class:text-muted-foreground={!active}
  class:hover:bg-accent/10={!active}
  class:hover:text-accent={!active}
>
  <span class="text-xl">{item.icon}</span>
  <span class="text-sm font-medium">{item.label}</span>
</a>
```

---

## 6. 레이아웃 구현

### 6.1 (public)/+layout.svelte

**기능:**
- 로그인 전 페이지의 심플한 레이아웃
- 헤더(로고 + 로그인 버튼) + 콘텐츠

**구현:**
```svelte
<script lang="ts">
  import '../../app.css';

  let { children } = $props();
</script>

<div class="min-h-screen bg-background">
  <!-- 심플 헤더 -->
  <header class="h-16 border-b border-border">
    <div class="container h-full flex items-center justify-between px-4">
      <a href="/" class="text-2xl font-bold text-primary">
        AI 페이스메이커
      </a>
      <a href="/login" class="btn-primary">
        로그인
      </a>
    </div>
  </header>

  <!-- 메인 콘텐츠 -->
  <main>
    {@render children?.()}
  </main>
</div>
```

---

### 6.2 (app)/+layout.svelte

**기능:**
- 로그인 후 페이지의 전체 레이아웃
- Header + Sidebar(Desktop) + MobileNav(Mobile) + 메인 콘텐츠

**상태 관리:**
```typescript
let isSidebarOpen = $state(false);

function toggleSidebar() {
  isSidebarOpen = !isSidebarOpen;
}

function closeSidebar() {
  isSidebarOpen = false;
}
```

**구현:**
```svelte
<script lang="ts">
  import Header from '$lib/components/layout/Header.svelte';
  import Sidebar from '$lib/components/layout/Sidebar.svelte';
  import MobileNav from '$lib/components/layout/MobileNav.svelte';

  let { children } = $props();
  let isSidebarOpen = $state(false);

  // TODO: 실제 사용자 데이터는 인증 스토어에서 가져오기
  const user = {
    name: '김성장',
    email: 'user@example.com',
    avatar: undefined
  };

  function toggleSidebar() {
    isSidebarOpen = !isSidebarOpen;
  }

  function closeSidebar() {
    isSidebarOpen = false;
  }
</script>

<div class="min-h-screen bg-background">
  <!-- 헤더 -->
  <Header {user} onToggleSidebar={toggleSidebar} />

  <!-- 사이드바 -->
  <Sidebar isOpen={isSidebarOpen} onClose={closeSidebar} />

  <!-- 메인 콘텐츠 -->
  <main class="pt-16 lg:pl-60 pb-16 lg:pb-0">
    <div class="container mx-auto p-4">
      {@render children?.()}
    </div>
  </main>

  <!-- 모바일 하단 네비게이션 -->
  <MobileNav />
</div>
```

**여백 계산:**
- `pt-16`: 헤더 높이만큼 상단 여백 (64px)
- `lg:pl-60`: 데스크톱에서 사이드바 너비만큼 좌측 여백 (240px)
- `pb-16`: 모바일에서 하단 네비게이션 높이만큼 하단 여백 (64px)
- `lg:pb-0`: 데스크톱에서 하단 여백 제거

---

## 7. 색상 및 스타일 가이드

### 7.1 컬러 토큰 (app.css 참조)

레이아웃 컴포넌트에서 사용하는 주요 컬러:

| 용도 | 클래스 | HSL (Light) | HSL (Dark) |
|------|--------|-------------|------------|
| 배경 | `bg-background` | `0 0% 100%` | `280 10% 8%` |
| 전경 | `text-foreground` | `280 10% 18%` | `0 0% 95%` |
| 카드 배경 | `bg-card` | `0 0% 100%` | `280 10% 8%` |
| 테두리 | `border-border` | `240 5.9% 90%` | `280 10% 20%` |
| 주요 색상 | `bg-primary` | `270 50% 60%` | `270 60% 70%` |
| Muted | `bg-muted` | `240 4% 95%` | `280 10% 20%` |

### 7.2 애니메이션 (app.css 참조)

사이드바 및 모바일 메뉴 전환 시 사용:

```css
/* 이미 app.css에 정의되어 있음 */
.fade-in { animation: fadeIn 200ms ease-in-out; }
.slide-in-from-left { /* 추가 필요 시 */ }
```

---

## 8. 접근성 (Accessibility)

### 8.1 키보드 네비게이션
- 모든 네비게이션 아이템은 `<a>` 태그 사용 (키보드 접근 가능)
- 사이드바 토글 버튼: `<button>` 태그 + `aria-label` 추가

### 8.2 스크린 리더
- 네비게이션 영역: `<nav aria-label="주요 네비게이션">`
- 활성 메뉴: `aria-current="page"` 속성 추가
- 사용자 메뉴: `aria-label="사용자 메뉴"`

### 8.3 포커스 스타일
`app.css`에 정의된 전역 포커스 스타일 활용:
```css
:focus-visible {
  outline: none;
  box-shadow:
    0 0 0 2px var(--color-background),
    0 0 0 4px var(--color-ring);
}
```

---

## 9. 성능 최적화

### 9.1 코드 스플리팅
- SvelteKit의 자동 라우트 기반 코드 스플리팅 활용
- 각 페이지는 필요할 때만 로드

### 9.2 레이아웃 컴포넌트 최적화
- 네비게이션 메뉴 항목은 상수로 정의 (재계산 방지)
- 활성 상태는 `$page.url.pathname`으로 반응적으로 계산

---

## 10. 완료 조건 (Acceptance Criteria)

### 설계 단계 (현재)
- [x] 로그인 전 레이아웃 와이어프레임 완성
- [x] 로그인 후 메인 레이아웃 (헤더, 사이드바/네비게이션) 와이어프레임 완성
- [x] 네비게이션 메뉴 항목 정의 (백로그, 스프린트, 회고 등)
- [x] 반응형 디자인 브레이크포인트 및 모바일 네비게이션 패턴 정의
- [x] 사용자 프로필 영역 및 로그아웃 버튼 위치 설계 완료
- [x] 개발팀이 구현 가능한 레이아웃 스펙 문서 완성

### 구현 단계 (다음)
- [ ] `src/lib/components/layout/` 디렉토리 생성
- [ ] Header.svelte 컴포넌트 구현
- [ ] Sidebar.svelte 컴포넌트 구현
- [ ] MobileNav.svelte 컴포넌트 구현
- [ ] UserMenu.svelte 컴포넌트 구현
- [ ] NavItem.svelte 컴포넌트 구현
- [ ] `(public)/+layout.svelte` 구현
- [ ] `(app)/+layout.svelte` 구현
- [ ] 반응형 동작 테스트 (Mobile/Tablet/Desktop)

---

## 11. 참고 자료

### 11.1 내부 문서
- [디자인 시스템 CSS](../../frontend/src/app.css)
- [프로젝트 개요](../../README.md)
- [기술 명세](../technical_spec.md)

### 11.2 외부 라이브러리
- [SvelteKit Routing](https://kit.svelte.dev/docs/routing)
- [Tailwind CSS Breakpoints](https://tailwindcss.com/docs/responsive-design)
- [bits-ui DropdownMenu](https://www.bits-ui.com/docs/components/dropdown-menu)

---

## 부록: 네비게이션 메뉴 데이터 구조

### TypeScript 타입 정의
```typescript
// src/lib/types/navigation.ts
export interface NavItem {
  icon: string;
  label: string;
  href: string;
  priority: 'P0' | 'P1';  // P0: 모바일에도 표시, P1: 데스크톱만
}

export const NAV_ITEMS: NavItem[] = [
  { icon: '🏠', label: '대시보드', href: '/app/dashboard', priority: 'P0' },
  { icon: '📋', label: '백로그', href: '/app/backlog', priority: 'P0' },
  { icon: '🎯', label: '스프린트', href: '/app/sprint', priority: 'P0' },
  { icon: '🔍', label: '회고', href: '/app/retrospective', priority: 'P0' },
  { icon: '📈', label: '벨로시티', href: '/app/velocity', priority: 'P0' },
  { icon: '⚙️', label: '설정', href: '/app/settings', priority: 'P1' },
];

// 모바일 하단 네비게이션용 (설정 제외)
export const MOBILE_NAV_ITEMS = NAV_ITEMS.filter(item =>
  item.priority === 'P0' && item.href !== '/app/settings'
);
```

---

**문서 끝**
