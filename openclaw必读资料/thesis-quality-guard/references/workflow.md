# Thesis Writing Workflow (Mandatory)

## Purpose
Enforce paragraph‑by‑paragraph alignment with the sample document and validate outputs before committing.

## Workflow
1. **Locate sample**
   - Find the sample document in `openclaw必读资料/` (typically `样例文档.docx`).
   - Extract the target chapter/section text and note the paragraph count, sentence rhythm, and target length per paragraph.

2. **Build paragraph alignment checklist**
   - For each paragraph in the sample, record: purpose, key terms, sentence rhythm, and approximate length range.
   - This checklist is the writing blueprint for the target chapter.

3. **Write paragraph-by-paragraph**
   - Write paragraph 1 and compare its length/phrasing to the checklist.
   - Only proceed to paragraph 2 after paragraph 1 aligns with the checklist.
   - Continue until the chapter is complete.

4. **Run the guard script**
   - Execute `python3 scripts/thesis_guard.py --root <repo>`.
   - If it fails, fix issues and re-run until PASS.

5. **Commit only after PASS**
   - Do not commit or push if the guard fails.
