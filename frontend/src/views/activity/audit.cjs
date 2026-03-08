const fs = require('fs');
const content = fs.readFileSync('C:/Users/YCK/Desktop/xiaoyuanzhiyuanzhe/frontend/src/views/activity/ActivityDetail.vue', 'utf8');
let depth = 0;
const lines = content.split('\n');
let inStyle = false;

for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    if (line.includes('<style')) { inStyle = true; continue; }
    if (line.includes('</style>')) { inStyle = false; continue; }

    if (inStyle) {
        for (let j = 0; j < line.length; j++) {
            const char = line[j];
            if (char === '{') depth++;
            if (char === '}') {
                depth--;
                if (depth < 0) {
                    console.log(`Unmatched } at line ${i + 1}, char ${j + 1}: ${line.trim()}`);
                    process.exit(0);
                }
            }
        }
        if (i > 1180 && i < 1250) {
            console.log(`Line ${i + 1} (depth ${depth}): ${line.trim()}`);
        }
    }
}
console.log(`Final depth: ${depth}`);
process.exit(0);
