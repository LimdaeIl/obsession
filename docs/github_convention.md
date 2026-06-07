# GitHub Convention

본 프로젝트는 GitHub Flow 기반으로 운영하며, 모든 변경 사항은 **Issue → Branch → Commit → Pull Request → Merge** 흐름을 따릅니다.

---

# 1. Branch Strategy

## Branch Structure

| Branch | Description    |
| ------ | -------------- |
| main   | Production 브랜치 |

`main` 브랜치는 항상 배포 가능한 상태를 유지합니다.

모든 작업은 별도의 브랜치에서 진행한 뒤 Pull Request를 통해 병합합니다.

## Branch Naming Convention

```text
feature/#4-github-setup
fix/#12-login-bug
refactor/#18-order-domain
hotfix/#21-payment-error
```

### Format

```text
{type}/#{issue-number}-{short-description}
```

### Branch Types

| Type     | Description |
| -------- | ----------- |
| feature  | 신규 기능 개발    |
| fix      | 버그 수정       |
| refactor | 리팩토링        |
| hotfix   | 긴급 수정       |

---

# 2. Issue Convention

모든 작업은 Issue 생성 후 진행합니다.

## Issue Types

| Label                   | Description   |
| ----------------------- | ------------- |
| ✨ Features              | 신규 기능 개발      |
| 🐛 Bug                  | 버그 수정         |
| ♻️ Refactor             | 코드 개선         |
| 🏗️ Architecture        | 아키텍처 변경       |
| 🧵 Concurrency          | 동시성 처리        |
| ➕ Dependency            | 의존성 추가        |
| ➖ Dependency            | 의존성 제거        |
| 💚 CI                   | CI/CD 설정      |
| 🚀 Deploy               | 배포 관련 작업      |
| 🚑️ Hotfix              | 긴급 수정         |
| 📝 Documentation        | 문서 작성 및 수정    |
| 💡 Comments             | 주석 추가 및 수정    |
| 🌐 Internationalization | 다국어 처리        |
| 📈 Analytics            | 모니터링 및 분석     |
| 🔖 Release              | 릴리즈 관리        |
| 🙈 Ignore               | .gitignore 수정 |
| 🎉 Good First Issue     | 입문용 작업        |

## Issue Title Convention

```text
[FEAT] Redis 기반 재고 차감 구현
[FIX] 로그인 토큰 검증 오류 수정
[REFACTOR] 주문 도메인 구조 개선
[DOCS] README 업데이트
```

---

# 3. Commit Convention

작업 이력을 명확하게 추적하기 위해 이슈 번호 기반 커밋 메시지를 사용합니다.

## Commit Format

```text
{emoji}#{issue-number}: {title}

{description}
```

## Examples

```text
✨#4: 깃허브 리포지토리 전략 설정

템플릿을 구성합니다.
```

```text
🐛#12: 로그인 토큰 검증 오류 수정

만료된 JWT 검증 시 예외 처리가 누락된 문제를 수정합니다.
```

## Commit Types

| Emoji | Description      |
| ----- | ---------------- |
| ✨     | 신규 기능            |
| 🐛    | 버그 수정            |
| ♻️    | 리팩토링             |
| 🏗️   | 아키텍처 변경          |
| 🧵    | 동시성 처리           |
| ➕     | 의존성 추가           |
| ➖     | 의존성 제거           |
| 💚    | CI/CD            |
| 🚀    | 배포               |
| 🚑️   | 긴급 수정            |
| 📝    | 문서               |
| 💡    | 주석               |
| 🌐    | 국제화              |
| 📈    | 모니터링 및 분석        |
| 🔖    | 릴리즈              |
| 🙈    | Ignore           |
| 🎉    | Good First Issue |

## Commit Rules

* 이슈 번호를 반드시 포함합니다.
* 하나의 커밋은 하나의 목적만 가집니다.
* 커밋 제목만으로 변경 내용을 이해할 수 있어야 합니다.
* 변경 이유는 본문에 작성합니다.

---

# 4. Pull Request Convention

모든 변경 사항은 Pull Request를 통해 `main` 브랜치에 병합합니다.

## PR Title Convention

```text
{emoji} #{issue-number}: {title}
```

### Example

```text
📝 #4: 깃허브 리포지토리 전략 설정
```

## PR Description

PR 템플릿을 사용하여 다음 내용을 작성합니다.

* 작업 목적
* 작업 내용
* 주요 변경 사항
* 테스트 결과
* 설계 의사결정
* 회고

---

# 5. Code Review Policy

본 프로젝트는 개인 프로젝트로 운영됩니다.

별도의 승인 절차는 없으며 CodeRabbit을 활용하여 자동 코드 리뷰를 수행합니다.

## Review Process

```text
Issue 생성
    ↓
Branch 생성
    ↓
개발 및 Commit
    ↓
Pull Request 생성
    ↓
CodeRabbit 리뷰
    ↓
테스트 검증
    ↓
Merge
```

---

# 6. Branch Protection Rule

`main` 브랜치는 직접 수정할 수 없습니다.

## Protection Settings

* Require a pull request before merging
* Require status checks to pass before merging
* Require branches to be up to date before merging
* Restrict direct pushes to main

모든 변경 사항은 Pull Request를 통해서만 반영합니다.

---

# 7. Template Management

프로젝트의 일관성을 위해 GitHub Template을 사용합니다.

## Issue Templates

* Feature
* Bug
* Refactor

## Pull Request Template

* 작업 목적
* 작업 내용
* 주요 변경 사항
* 테스트 결과
* 설계 의사결정
* 회고

모든 Issue와 Pull Request는 템플릿을 기반으로 작성합니다.
