import os
import re

files_to_process = [
    "/Users/mymac/Desktop/Antigravity/selenium-framework/src/main/java/com/govn/pages/login/LoginPage.java",
    "/Users/mymac/Desktop/Antigravity/selenium-framework/src/test/java/com/govn/base/BaseTest.java",
    "/Users/mymac/Desktop/Antigravity/selenium-framework/src/test/java/com/govn/tests/login/LoginTest.java",
    "/Users/mymac/Desktop/Antigravity/selenium-framework/src/main/java/com/govn/framework/base/BasePage.java",
    "/Users/mymac/Desktop/Antigravity/selenium-framework/src/main/java/com/govn/framework/listeners/ExtentReportListener.java",
    "/Users/mymac/Desktop/Antigravity/selenium-framework/src/main/java/com/govn/framework/utils/WaitUtils.java",
    "/Users/mymac/Desktop/Antigravity/selenium-framework/src/main/java/com/govn/framework/utils/DataUtils.java",
    "/Users/mymac/Desktop/Antigravity/selenium-framework/src/main/java/com/govn/framework/utils/ScreenshotUtils.java",
    "/Users/mymac/Desktop/Antigravity/selenium-framework/src/main/java/com/govn/framework/utils/ActionUtils.java",
    "/Users/mymac/Desktop/Antigravity/selenium-framework/src/main/java/com/govn/framework/driver/DriverFactory.java",
    "/Users/mymac/Desktop/Antigravity/selenium-framework/src/main/java/com/govn/framework/config/ConfigReader.java"
]

for filepath in files_to_process:
    with open(filepath, 'r') as f:
        content = f.read()

    # Determine if import is needed
    needs_import = 'package com.govn.framework.utils;' not in content

    # Remove LogManager and Logger imports
    content = re.sub(r'import\s+org\.apache\.logging\.log4j\.LogManager;\n?', '', content)
    content = re.sub(r'import\s+org\.apache\.logging\.log4j\.Logger;\n?', '', content)

    # Add LogUtils import if needed
    if needs_import and 'import com.govn.framework.utils.LogUtils;' not in content:
        imports = re.findall(r'^import\s+.*?;$', content, re.MULTILINE)
        if imports:
            last_import = imports[-1]
            content = content.replace(last_import, last_import + '\nimport com.govn.framework.utils.LogUtils;')
        else:
            package_match = re.search(r'^package\s+.*?;$', content, re.MULTILINE)
            if package_match:
                content = content.replace(package_match.group(0), package_match.group(0) + '\n\nimport com.govn.framework.utils.LogUtils;')

    # Remove logger declaration
    content = re.sub(r'^\s*(?:private\s+)?(?:static\s+)?(?:final\s+)?Logger\s+(?:log|logger)\s*=\s*LogManager\.getLogger\([^)]*\);\s*$', '', content, flags=re.MULTILINE)

    # Replace log.xxx or logger.xxx with LogUtils.xxx
    content = re.sub(r'\b(?:log|logger)\.(info|debug|error|warn|trace)\b', r'LogUtils.\1', content)

    with open(filepath, 'w') as f:
        f.write(content)
    
    print(f"Processed {filepath}")
