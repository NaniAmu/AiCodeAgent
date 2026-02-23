---
description: Git workflow for DigiNest AI Receptionist V1
---

# Git Workflow for DigiNest AI Receptionist

## Branch Strategy

### Main Branches
- `main` - Production-ready code
- `develop` - Integration branch for features

### Feature Branches
- `feature/booking-engine` - New features
- `bugfix/validation-error` - Bug fixes
- `hotfix/security-patch` - Urgent production fixes

### Branch Naming
```
feature/<short-description>
bugfix/<issue-description>
hotfix/<critical-fix>
refactor/<component-name>
docs/<what-changed>
```

## Workflow Steps

### 1. Start New Feature
```bash
git checkout develop
git pull origin develop
git checkout -b feature/booking-api
git push -u origin feature/booking-api
```

### 2. Daily Development
```bash
git add .
git commit -m "feat: add booking validation logic"
git push origin feature/booking-api
```

### 3. Commit Message Format (Conventional Commits)
```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

**Types:**
- `feat:` New feature
- `fix:` Bug fix
- `docs:` Documentation changes
- `style:` Code style (formatting, no logic change)
- `refactor:` Code refactoring
- `test:` Adding/updating tests
- `chore:` Build, dependencies, maintenance

**Scopes for this project:**
- `booking` - Booking module
- `auth` - Authentication/Security
- `usage` - Usage tracking
- `hotel` - Hotel management
- `config` - Configuration
- `entity` - Database entities

### 4. Examples
```bash
feat(booking): add room availability check endpoint

fix(auth): resolve JWT token expiration issue

refactor(usage): optimize token counting query

docs(readme): update API documentation

chore(deps): upgrade Spring Boot to 3.2.3
```

### 5. Before Merging
```bash
git checkout develop
git pull origin develop
git checkout feature/booking-api
git rebase develop
# Resolve conflicts if any
git push origin feature/booking-api --force-with-lease
```

### 6. Merge to Develop
```bash
git checkout develop
git merge --no-ff feature/booking-api
git push origin develop
```

### 7. Delete Feature Branch
```bash
git branch -d feature/booking-api
git push origin --delete feature/booking-api
```

## Monolith-Specific Guidelines

### File Organization
- Keep related changes in single commit
- Don't mix feature and refactor commits
- Group entity + repository + service changes together

### Database Changes
- Always include DDL in commit message body
- Test migrations on fresh database
- Never modify existing migrations

### API Changes
- Document breaking changes in commit footer
- Update Postman/DTOs with endpoint changes

## Quick Reference

```bash
# Check status
git status

# View recent commits
git log --oneline -10

# Undo last commit (keep changes)
git reset --soft HEAD~1

# Stash changes
git stash push -m "work in progress"
git stash pop

# View diff
git diff

# Create PR from branch
git checkout feature/my-feature
gh pr create --base develop --title "feat: description"
```

## Pre-commit Checklist

- [ ] Code compiles: `./mvnw clean compile`
- [ ] No IDE files committed
- [ ] No secrets in code
- [ ] Commit message follows format
- [ ] Tests pass (if any)

## Emergency Procedures

### Revert Bad Commit
```bash
git revert <commit-hash>
git push origin develop
```

### Fix Last Commit
```bash
git add .
git commit --amend --no-edit
git push origin feature/branch --force-with-lease
```
