# AI 페이스메이커 Git Branch 전략

## 1. 개요

### 목적
AI 페이스메이커 프로젝트의 효율적인 버전 관리와 배포를 위한 Git Branch 전략을 정의합니다.

### 핵심 원칙
- **버전별 독립성**: v1.0.0과 v2.0.0의 동일 이슈 번호 구분
- **팀별 명확성**: DS, BE, FE, DO 팀 구분
- **부분 작업 지원**: 큰 작업을 여러 PR로 분할 가능
- **의존성 관리**: 선행 작업 완료 후 후속 작업 진행
- **안전한 배포**: develop 검증 후 main 배포

## 2. 브랜치 구조

```
main (프로덕션 배포)
├── develop (개발 통합 브랜치)
│   ├── v1.0.0/DS/01-wireframe
│   ├── v1.0.0/DS/01-components
│   ├── v1.0.0/BE/01-user-auth
│   ├── v1.0.0/FE/02-chat-ui
│   └── v2.0.0/DS/01-new-design
└── hotfix/critical-auth-fix
```

### 브랜치 역할

| 브랜치 | 역할 | 보호 설정 |
|--------|------|-----------|
| `main` | 프로덕션 환경 배포용, 핫픽스 기준점 | ✅ PR 필수, 리뷰 필수 |
| `develop` | 개발 통합 및 테스트 | ✅ PR 필수 |
| `작업 브랜치` | 개별 기능/수정 작업 | ❌ 자유롭게 푸시 |
| `hotfix/*` | 긴급 수정 (main 기반) | ✅ PR 필수, 리뷰 필수 |

## 3. 브랜치 네이밍 규칙

### 일반/부분 작업
```
{버전}/{팀}/{이슈번호}[-설명]
```

**예시:**
- `v1.0.0/DS/01-wireframe` - DS-01의 와이어프레임 작업
- `v1.0.0/DS/01-components` - DS-01의 컴포넌트 작업
- `v1.0.0/BE/03-streaming-api` - BE-03의 스트리밍 API
- `v2.0.0/DS/01-new-design` - 버전 2.0.0의 DS-01 (v1과 구분)

### 핫픽스 (main 기반)
```
hotfix/{간단한-설명}
```

**예시:**
- `hotfix/auth-token-fix`
- `hotfix/memory-leak-patch`

**중요**: 핫픽스는 항상 main 브랜치에서 생성하여 개발 중인 기능과 분리

### 네이밍 주의사항

**❌ 피해야 할 네이밍:**
```bash
v1.0.0/DS/DS-01  # DS 중복
feature/auth     # 버전/팀 정보 누락
fix-bug         # 버전/팀/이슈 정보 누락
```

**✅ 올바른 네이밍:**
```bash
v1.0.0/DS/01-auth-flow
v1.0.0/BE/02-jwt-implementation
v1.0.0/FE/03-login-form
```

## 4. 작업 플로우

### 4.1 일반 작업 플로우

```bash
# 1. develop에서 최신 코드 가져오기
git checkout develop
git pull origin develop

# 2. 작업 브랜치 생성
git checkout -b v1.0.0/DS/01-wireframe

# 3. 작업 수행 후 커밋
git add .
git commit -m "feat: 사용자 인증 와이어프레임 초안 작성"

# 4. 원격 저장소에 푸시
git push origin v1.0.0/DS/01-wireframe

# 5. GitHub에서 develop으로 PR 생성
```

### 4.2 부분 작업 관리

하나의 이슈를 여러 PR로 나누어 처리:

```
DS-01 이슈 (사용자 인증 UI/UX 설계)
├── v1.0.0/DS/01-wireframe → PR #12 (일반 Merge) ✅
├── v1.0.0/DS/01-components → PR #15 (일반 Merge) ✅
└── v1.0.0/DS/01-final-review → PR #18 (일반 Merge, 이슈 완료) ✅
```

**부분 작업 처리 순서:**
1. 첫 번째 부분 작업 완료 → PR → **일반 Merge**
2. develop에서 최신 상태로 두 번째 브랜치 생성
3. 두 번째 부분 작업 완료 → PR → **일반 Merge**
4. 마지막 부분에서 이슈 완전 종료 → **일반 Merge**

**일반 Merge 사용 이유:**
- 와이어프레임 → 컴포넌트 → 최종 검토의 각 단계별 구현 과정 보존
- 디자인 시스템 구축 과정의 의사결정 기록 유지
- 다음 버전에서 참고할 수 있는 설계 과정 보존

### 4.3 의존성 처리

선행 작업이 필요한 경우:

```bash
# 1. 선행 작업(DO-01) 완료 후 develop 머지 확인
git checkout develop
git pull origin develop

# 2. 후속 작업(BE-01) 브랜치 생성
git checkout -b v1.0.0/BE/01-user-auth

# 3. DO-01의 결과물을 활용하여 작업 진행
```

### 4.4 핫픽스 처리

긴급 수정이 필요한 경우 (main에서 직접 수정 후 배포):

```bash
# 1. main에서 핫픽스 브랜치 생성
git checkout main
git pull origin main
git checkout -b hotfix/auth-token-fix

# 2. 수정 작업
git commit -m "fix: JWT 토큰 만료 처리 오류 수정"
git push origin hotfix/auth-token-fix

# 3. main으로 PR 생성 및 즉시 배포
# 4. 배포 완료 후 develop에 핫픽스 내용 동기화
git checkout develop
git merge main  # 또는 체리픽 사용
git push origin develop
```

## 5. PR 및 Merge 전략

### 5.1 Squash Merge

**정의**: 여러 커밋을 하나로 압축하여 머지

**사용 케이스:**
- 핫픽스 PR (긴급 수정의 명확한 기록)
- 실험적/임시 작업이 많은 PR
- WIP 커밋만으로 구성된 단순 PR

**장점:**
- 깔끔한 히스토리
- 불필요한 임시 커밋 제거
- 롤백 용이

**예시:**
```bash
# 작업 브랜치의 커밋들
- wip: 임시 저장
- fix: 타이포 수정
- wip: 다시 임시 저장
- fix: 스타일 수정

# Squash 후 → 단일 커밋
- fix: 긴급 인증 오류 수정 (#PR번호)
```

### 5.2 일반 Merge (Merge Commit)

**정의**: 모든 커밋 히스토리를 보존하며 머지 커밋 생성

**사용 케이스:**
- **부분 작업 PR** (의존성 구현, 기능 단계별 구현)
- **완성된 기능 PR**
- 의미있는 커밋 히스토리가 있는 PR
- 여러 개발자가 협업한 PR

**장점:**
- 상세한 작업 과정 추적
- 각 변경사항 의도 파악 용이
- 문제 발생 시 원인 추적 용이
- 부분 작업의 구현 맥락 보존

**예시:**
```bash
# 부분 작업 - 모든 커밋 보존 + 머지 커밋
- Merge pull request #45 from v1.0.0/BE/03-part1-database-schema
  ├─ feat: User 테이블 스키마 정의
  ├─ feat: JWT 토큰 관련 필드 추가
  ├─ test: 스키마 마이그레이션 테스트
  └─ docs: 데이터베이스 스키마 문서화

# 완성 작업 - 모든 커밋 보존 + 머지 커밋
- Merge pull request #123 from v1.0.0/BE/03-complete-api
  ├─ feat: API 엔드포인트 추가
  ├─ test: 단위 테스트 작성
  ├─ docs: API 문서 업데이트
  └─ refactor: 에러 처리 개선
```

### 5.3 전략 선택 기준

| 상황 | 전략 | 이유 |
|------|------|------|
| **부분 작업 완료** | **일반 Merge** | **의미있는 구현 과정 보존, 의존성/단계별 구현 맥락 유지** |
| **기능 완전 완성** | **일반 Merge** | **전체 구현 과정 추적 가능** |
| **핫픽스** | **Squash Merge** | **긴급 수정의 명확하고 단순한 기록** |
| 실험/임시 작업 | Squash Merge | 불필요한 WIP 커밋 정리 |
| 리팩토링 | 상황에 따라 | 단순하면 Squash, 복잡하면 일반 Merge |

### 5.4 부분 작업에서 일반 Merge를 사용하는 이유

**시나리오 예시:**
1. **큰 작업 분할**: BE-03을 데이터베이스 → API → 테스트로 나눈 경우
2. **의존성 우선 구현**: FE-01이 필요로 하는 BE API만 먼저 구현
3. **단계별 기능 구현**: 인증 → 권한 → 세션 관리 순서로 구현

**보존해야 할 정보:**
- 각 단계별 구현 의도와 방법
- 테스트 케이스 추가 과정
- 문서화 및 리팩토링 과정
- 버그 수정 및 개선 과정

## 6. 이슈-PR 연결 방법

### 6.1 키워드 활용

**부분 완료 시:**
```
PR 본문에 작성:
Partially addresses #DS-01
```

**최종 완료 시:**
```
PR 본문에 작성:
Closes #DS-01
```

### 6.2 PR 제목 규칙

```
[{상태}] {팀}-{이슈번호} {간단한 설명}
```

**예시:**
- `[부분] DS-01 사용자 인증 와이어프레임`
- `[완료] BE-03 AI 백로그 스트리밍 API 구현`
- `[핫픽스] 토큰 만료 처리 오류 수정`

### 6.3 PR 템플릿 체크리스트

```markdown
## 작업 유형
- [ ] 부분 작업 (이슈 진행 중)
- [ ] 완료 작업 (이슈 종료)
- [ ] 핫픽스

## 관련 이슈
- Partially addresses #이슈번호 (부분 완료)
- Closes #이슈번호 (완전 완료)

## 테스트
- [ ] 로컬 테스트 완료
- [ ] 의존성 확인 완료
```

## 7. 배포 전략

### 7.1 배포 타이밍

**기능 단위 배포:**
- 주요 기능 완성 시 수시 배포
- develop → main PR 생성
- 리뷰 완료 후 배포

**스프린트 종료 배포:**
- 스프린트 마감 시 정기 배포
- 누적된 기능들 일괄 배포

### 7.2 배포 프로세스

```bash
# 방법 1: 직접 PR 방식 (권장)
# 1. develop → main 직접 PR 생성
# 2. 리뷰 완료 후 main에 머지
# 3. main에서 프로덕션 배포

# 방법 2: Release 브랜치 방식
# 1. develop 브랜치 상태 확인
git checkout develop
git pull origin develop

# 2. 배포용 release 브랜치 생성
git checkout main
git checkout -b release/sprint-1-deploy

# 3. develop 내용을 release 브랜치로 머지
git merge develop

# 4. main으로 PR 생성 (리뷰 필수)
# 5. 승인 후 main에 머지
# 6. main에서 프로덕션 배포
```

### 7.3 배포 후 처리

```bash
# 1. main에서 태그 생성
git tag v1.0.0-release-20240315
git push origin v1.0.0-release-20240315

# 2. develop 브랜치 동기화 (필요 시)
git checkout develop
git merge main
git push origin develop
```

## 8. 실전 예시 시나리오

### 8.1 DS-01 부분 작업 시나리오

**상황**: DS-01을 3개 부분으로 나누어 작업

```bash
# 1단계: 와이어프레임 작업
git checkout develop
git checkout -b v1.0.0/DS/01-wireframe
# 의미있는 커밋들로 작업 수행
git commit -m "feat: 로그인/회원가입 와이어프레임 초안"
git commit -m "design: 사용자 플로우 다이어그램 추가"
git commit -m "docs: 와이어프레임 설계 의도 문서화"
git push origin v1.0.0/DS/01-wireframe
# PR: "Partially addresses #DS-01" (일반 merge)

# 2단계: UI 컴포넌트 작업
git checkout develop
git pull origin develop  # 1단계 반영된 상태
git checkout -b v1.0.0/DS/01-components
# 의미있는 커밋들로 작업 수행
git commit -m "feat: 로그인 폼 컴포넌트 구현"
git commit -m "feat: 에러 메시지 컴포넌트 추가"
git commit -m "style: 반응형 디자인 적용"
git commit -m "test: 컴포넌트 스토리북 추가"
git push origin v1.0.0/DS/01-components
# PR: "Partially addresses #DS-01" (일반 merge)

# 3단계: 최종 검토 및 완료
git checkout develop
git pull origin develop  # 2단계까지 반영된 상태
git checkout -b v1.0.0/DS/01-final-review
# 의미있는 커밋들로 작업 수행
git commit -m "review: 접근성 가이드라인 준수 확인"
git commit -m "polish: 최종 디자인 시스템 정합성 검토"
git commit -m "docs: 컴포넌트 사용 가이드 완성"
git push origin v1.0.0/DS/01-final-review
# PR: "Closes #DS-01" (일반 merge) → 이슈 자동 종료
```

### 8.2 의존성 작업 시나리오

**상황**: FE-01이 DS-01, BE-01, BE-02에 의존

```bash
# 선행 작업들 완료 대기
# DS-01 ✅, BE-01 ✅, BE-02 ✅
# → 모두 develop에 머지 완료

# FE-01 작업 시작
git checkout develop
git pull origin develop  # 모든 선행 작업 반영된 상태
git checkout -b v1.0.0/FE/01-auth-integration

# 선행 작업들의 결과물 활용하여 개발
# - DS-01의 UI 컴포넌트 사용
# - BE-01, BE-02의 API 연동

git push origin v1.0.0/FE/01-auth-integration
# PR: "Closes #FE-01" (일반 merge)
```

### 8.3 긴급 핫픽스 시나리오

**상황**: 프로덕션에서 JWT 토큰 만료 오류 발생

```bash
# 1. main에서 핫픽스 브랜치 생성
git checkout main
git pull origin main
git checkout -b hotfix/auth-token-expiry

# 2. 문제 수정
git commit -m "fix: JWT 토큰 갱신 로직 오류 수정"
git push origin hotfix/auth-token-expiry

# 3. main으로 PR 생성 → 리뷰 및 승인
# PR: "[핫픽스] JWT 토큰 갱신 로직 오류 수정 Closes #긴급이슈번호"
# (Squash merge 사용 - 긴급 수정의 명확한 단일 기록)

# 4. main 머지 후 즉시 프로덕션 배포

# 5. develop에 핫픽스 내용 동기화
git checkout develop
git pull origin develop
git merge main
# 또는 충돌 방지를 위해 체리픽 사용:
# git cherry-pick <핫픽스_커밋_해시>
git push origin develop
```

**핫픽스 동기화 전략:**
- **충돌이 없는 경우**: `git merge main` 사용
- **충돌이 예상되는 경우**: `git cherry-pick` 사용하여 해당 수정사항만 적용

## 9. 주의사항 및 베스트 프랙티스

### 9.1 주의사항

**브랜치명 작성 시:**
- 팀명 중복 피하기: `v1.0.0/DS/DS-01` ❌ → `v1.0.0/DS/01` ✅
- 버전 정보 누락 피하기: `DS/01` ❌ → `v1.0.0/DS/01` ✅
- 너무 긴 브랜치명 피하기 (50자 이내 권장)

**이슈 연결 시:**
- 부분 작업에 `Closes` 사용 금지
- 마지막 PR에만 `Closes` 사용
- PR 제목에 상태 명시 (`[부분]`, `[완료]`, `[핫픽스]`)

**핫픽스 작업 시:**
- 반드시 main에서 브랜치 생성
- develop의 개발 중인 기능과 절대 섞이지 않음
- 배포 후 develop 동기화 필수

### 9.2 베스트 프랙티스

**작업 전 습관:**

일반 작업:
```bash
# 항상 최신 develop에서 브랜치 생성
git checkout develop
git pull origin develop
git checkout -b 브랜치명
```

핫픽스 작업:
```bash
# 항상 최신 main에서 브랜치 생성
git checkout main
git pull origin main
git checkout -b hotfix/브랜치명
```

**커밋 메시지 규칙:**
```bash
git commit -m "feat: 사용자 로그인 API 구현"
git commit -m "fix: 토큰 갱신 오류 수정"
git commit -m "docs: API 문서 업데이트"
```

**PR 생성 전 체크리스트:**
- [ ] 최신 develop 반영 확인
- [ ] 로컬 테스트 완료
- [ ] 커밋 메시지 정리
- [ ] 이슈 번호 연결 확인
- [ ] PR 템플릿 작성 완료

---

## 10. FAQ (자주 묻는 질문)

### Q1: 브랜치명에서 이슈 번호 앞에 팀 코드를 또 붙여야 하나요?
**A**: 아니요. `v1.0.0/DS/DS-01` (❌) → `v1.0.0/DS/01` (✅)로 사용하세요.

### Q2: 핫픽스 후 develop 동기화를 잊으면 어떻게 되나요?
**A**: develop의 후속 작업에서 이미 수정된 버그가 다시 나타날 수 있습니다. 반드시 동기화하세요.

### Q3: 부분 작업이 너무 작아도 일반 Merge를 써야 하나요?
**A**: 의미있는 구현 과정이 있다면 일반 Merge, 단순한 WIP만 있다면 Squash Merge를 사용하세요.

### Q4: develop에 직접 푸시해도 되나요?
**A**: 안 됩니다. 모든 변경사항은 PR을 통해 코드 리뷰를 거쳐야 합니다.

### Q5: 의존성이 있는 작업을 동시에 시작할 수 있나요?
**A**: Mock/Stub을 사용하여 병렬 작업이 가능하지만, 인터페이스 정의를 먼저 합의해야 합니다.

---

*이 문서는 프로젝트 진행에 따라 지속적으로 업데이트됩니다.*