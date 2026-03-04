---
name: thesis-quality-guard
description: Enforce thesis-writing workflow and quality gates for repository-based论文撰写. Use when drafting or revising any thesis chapter based on openclaw必读资料 prompts, sample documents, and project code; includes paragraph-by-paragraph alignment with sample text and automated validation checks.
---

# Thesis Quality Guard

## Overview
Use this skill to enforce the mandatory paragraph-alignment workflow and run automated quality checks before committing thesis content.

## Workflow (Required)
1. Follow the paragraph-by-paragraph alignment workflow in `references/workflow.md`.
2. After writing, run the guard script and fix all failures before commit.

## Quality Gate Script
Run the validator on a repo root:

```bash
python3 scripts/thesis_guard.py --root <repo>
```

**Checks:**
- Summary is single paragraph (`00-摘要.md`).
- 1.3 研究现状 1000–1500 字 and references used (`02-第1章-绪论.md`, `08-参考文献.md`).
- Chapter 4 tables contain required columns and each table has a description.
- Chapter 5 test tables have preface paragraphs and results analysis references each table.

## When to Use
- Before drafting any chapter in a repo with `openclaw必读资料`.
- Before committing or pushing thesis content.
