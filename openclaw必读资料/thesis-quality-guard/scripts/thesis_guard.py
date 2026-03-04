#!/usr/bin/env python3
import argparse
import re
import sys
from pathlib import Path

REQUIRED_TABLE_COLUMNS = ["编号", "字段名", "类型", "长度", "是否非空", "是否主键", "注释"]


def read_text(path: Path) -> str:
    return path.read_text(encoding="utf-8")


def count_chinese_chars(text: str) -> int:
    return sum(1 for ch in text if "\u4e00" <= ch <= "\u9fff")


def extract_section(text: str, start_heading: str, next_heading: str | None = None) -> str:
    pattern = re.escape(start_heading) + r"\n+"
    if next_heading:
        pattern += r"([\s\S]*?)" + re.escape(next_heading)
    else:
        pattern += r"([\s\S]*)"
    match = re.search(pattern, text)
    return match.group(1).strip() if match else ""


def check_single_paragraph(summary_path: Path, issues: list[str]):
    if not summary_path.exists():
        issues.append(f"缺少摘要文件: {summary_path}")
        return
    text = read_text(summary_path)
    # remove title line
    lines = [line.strip() for line in text.splitlines()]
    body_lines = [line for line in lines if line and line not in {"摘  要", "摘要"}]
    paragraphs = []
    current = []
    for line in body_lines:
        if line:
            current.append(line)
        else:
            if current:
                paragraphs.append(" ".join(current))
                current = []
    if current:
        paragraphs.append(" ".join(current))
    if len(paragraphs) != 1:
        issues.append("摘要必须为单段落")


def check_research_status(chapter1_path: Path, refs_path: Path, issues: list[str]):
    if not chapter1_path.exists():
        issues.append(f"缺少绪论文件: {chapter1_path}")
        return
    text = read_text(chapter1_path)
    section = extract_section(text, "1.3 国内外研究现状")
    if not section:
        issues.append("未找到 1.3 国内外研究现状")
        return
    count = count_chinese_chars(section)
    if count < 1000 or count > 1500:
        issues.append(f"1.3 国内外研究现状字数不在范围内: {count}")
    if refs_path.exists():
        refs_text = read_text(refs_path)
        ref_nums = re.findall(r"\[(\d+)\]", refs_text)
        missing = [num for num in ref_nums if f"[{num}]" not in section]
        if missing:
            issues.append("1.3 未按顺序引用参考文献: " + ", ".join(missing))


def check_tables(chapter4_path: Path, issues: list[str]):
    if not chapter4_path.exists():
        issues.append(f"缺少第 4 章文件: {chapter4_path}")
        return
    text = read_text(chapter4_path)
    tables = re.split(r"(?=表\s*4-\d+)", text)
    for block in tables:
        if not block.strip().startswith("表 4-"):
            continue
        # table header check
        header_match = re.search(r"\|(.+?)\|\n\|\s*-+", block)
        if not header_match:
            issues.append("第 4 章表格缺少表头")
            continue
        header = header_match.group(1)
        for col in REQUIRED_TABLE_COLUMNS:
            if col not in header:
                issues.append(f"第 4 章表头缺少字段: {col}")
                break
        # description check
        after_table = re.split(r"\n\s*\|\s*-+.*\n", block, maxsplit=1)
        if len(after_table) > 1:
            tail = after_table[1]
            # strip rows
            tail = re.sub(r"^(\|.*\n)+", "", tail)
            next_marker = re.search(r"\n表\s*4-|\n4\.3", tail)
            desc = tail[: next_marker.start()] if next_marker else tail
            if not any(line.strip() for line in desc.splitlines()):
                issues.append("第 4 章表格后缺少说明文字")


def check_tests(chapter5_path: Path, issues: list[str]):
    if not chapter5_path.exists():
        issues.append(f"缺少第 5 章文件: {chapter5_path}")
        return
    text = read_text(chapter5_path)
    table_marks = re.findall(r"表\s*5-\d+", text)
    for mark in table_marks:
        idx = text.find(mark)
        prefix = text[:idx].splitlines()
        # find last non-empty line before table title
        last_line = next((line.strip() for line in reversed(prefix) if line.strip()), "")
        if "表" not in last_line:
            issues.append(f"{mark} 前缺少引出说明段落")
    # result analysis references
    result_section = extract_section(text, "5.3 测试结果分析")
    for mark in table_marks:
        if mark not in result_section:
            issues.append(f"结果分析未引用 {mark}")


def main():
    parser = argparse.ArgumentParser(description="Thesis quality guard")
    parser.add_argument("--root", required=True, help="repository root")
    args = parser.parse_args()
    root = Path(args.root)
    issues: list[str] = []

    check_single_paragraph(root / "00-摘要.md", issues)
    check_research_status(root / "02-第1章-绪论.md", root / "08-参考文献.md", issues)
    check_tables(root / "05-第4章-系统设计与实现.md", issues)
    check_tests(root / "06-第5章-系统测试.md", issues)

    if issues:
        print("FAIL")
        for item in issues:
            print("-", item)
        sys.exit(1)
    print("PASS")


if __name__ == "__main__":
    main()
