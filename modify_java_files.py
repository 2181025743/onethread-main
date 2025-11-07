#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
批量修改Java文件中的作者信息和删除项目群信息
"""

import os
import re
from pathlib import Path

def modify_java_file(file_path):
    """修改单个Java文件"""
    try:
        # 读取文件内容
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()

        original_content = content
        modified = False

        # 1. 将 "作者：马丁" 替换为 "作者：杨潇"
        if '作者：马丁' in content:
            content = content.replace('作者：马丁', '作者：杨潇')
            modified = True

        # 2. 删除包含"加项目群"的整行
        # 匹配模式：可能有换行符，然后是空格和*，然后是"加项目群"开头的内容，直到行尾
        if '加项目群' in content:
            # 使用正则表达式删除包含"加项目群"的整行
            content = re.sub(r'\n\s*\*\s*加项目群：[^\n]*', '', content)
            # 处理Windows换行符
            content = re.sub(r'\r\n\s*\*\s*加项目群：[^\r\n]*', '', content)
            modified = True

        # 如果内容有修改，写回文件
        if modified and content != original_content:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(content)
            return True

        return False

    except Exception as e:
        print(f"错误 - {file_path}: {e}")
        return False

def main():
    """主函数"""
    project_root = Path(r'c:\Users\a3351\Desktop\onethread-main')

    # 查找所有Java文件
    java_files = list(project_root.rglob('*.java'))

    print(f"找到 {len(java_files)} 个 Java 文件")
    print("开始批量修改...\n")

    modified_count = 0

    for java_file in java_files:
        if modify_java_file(java_file):
            modified_count += 1
            print(f"✓ 已修改: {java_file.relative_to(project_root)}")

    print(f"\n{'='*50}")
    print(f"修改完成！")
    print(f"共处理文件: {len(java_files)} 个")
    print(f"成功修改: {modified_count} 个")
    print(f"{'='*50}")

if __name__ == '__main__':
    main()
