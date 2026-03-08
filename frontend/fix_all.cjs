const fs = require('fs');
const path = require('path');
const srcDir = path.join(__dirname, 'src');

function fixFile(filePath) {
    let content = fs.readFileSync(filePath, 'utf-8');
    const original = content;

    // Specific fixes based on patterns:
    content = content.replace(/'计算机学\? \}\}/g, "'计算机学院' }}");
    content = content.replace(/'计算机学\?/g, "'计算机学院'");
    content = content.replace(/'管理员\?/g, "'管理员'");
    content = content.replace(/'普通用户\?/g, "'普通用户'");
    content = content.replace(/'匿名用\?/g, "'匿名用户'");
    content = content.replace(/'匿名志愿\?/g, "'匿名志愿者'");
    content = content.replace(/'系统管理\?/g, "'系统管理员'");

    // Fix unclosed strings in templates:
    // e.g. '去考? }} -> '去考试' }}
    content = content.replace(/'去考\? \}\}/g, "'去考试' }}");

    if (content !== original) {
        fs.writeFileSync(filePath, content, 'utf-8');
        console.log(`Fixed strings in: ${filePath}`);
        return true;
    }
    return false;
}

function scan(dir) {
    for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
        if (entry.isDirectory()) {
            scan(path.join(dir, entry.name));
        } else if (entry.name.endsWith('.vue') || entry.name.endsWith('.ts') || entry.name.endsWith('.js')) {
            fixFile(path.join(dir, entry.name));
        }
    }
}

scan(srcDir);
console.log('Done fixing hardcoded string corruptions.');
