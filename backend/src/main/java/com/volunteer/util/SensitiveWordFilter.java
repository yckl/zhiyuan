package com.volunteer.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 敏感词过滤工具
 * <p>
 * 使用 DFA（确定性有限自动机）算法实现高效敏感词匹配。
 * 基础词库内置于类中，也可通过 {@link #addWords(Collection)} 动态扩展。
 * </p>
 */
@Slf4j
@Component
public class SensitiveWordFilter {

    /** DFA 前缀树根节点 */
    private final Map<Character, Object> rootMap = new HashMap<>();

    /** 标记词尾的键 */
    private static final String END_FLAG = "isEnd";

    /** 基础敏感词词库 */
    private static final String[] DEFAULT_WORDS = {
            // ---- 暴力 / 恐怖 / 政治 ----
            "杀人", "砍人", "炸弹", "恐怖袭击", "枪支", "爆炸", "颠覆政权", "分裂国家",
            // ---- 色情 / 毒品 / 欺诈 ----
            "色情", "裸照", "嫖娼", "卖淫", "淫秽", "诈骗", "洗钱", "贩毒", "吸毒", "赌博",
            // ---- 人身攻击 / 粗口 (王者荣耀/游戏社区风格) ----
            "废物", "垃圾人", "去死", "滚蛋", "脑残", "智障", "白痴", "傻逼", "畜生", "贱人", "妈的", "靠",
            "操", "草", "日你妈", "草泥马", "曹尼玛", "尼玛", "叼", "逼", "傻X", "SB", "TMD", "NND", "滚",
            "王八蛋", "生儿子没屁眼", "贱货", "婊子", "烂货", "下贱", "脸都不要了",
            "孤儿", "司马", "死全家", "亲妈挂了", "你妈死了", "煞笔", "傻diao", "脑瘫", "杂种", "狗杂种",
            "nmsl", "mlgb", "cnm", "caonima", "fw", "nt", "wsnd", "djb", "bichi", "shabi",
            "卧槽", "我操", "握草", "我草", "尼玛比", "尼玛币", "泥马勒戈壁", "你是狗", "你是猪",
            "鸡巴", "屌", "吊", "骚货", "卖肉", "约炮", "挂逼", "彩笔", "坑货", "演员", "垃圾队友",
            "孤儿院", "全家桶", "祖宗十八代", "操你全家", "艹", "肏", "你妈比", "他妈的",
            // ---- 广告 / 破坏平衡 ----
            "代开发票", "刷单", "兼职日结", "加微信", "免费领取", "联系方式", "代练", "外挂", "脚本", "透视"
    };

    public SensitiveWordFilter() {
        addWords(Arrays.asList(DEFAULT_WORDS));
        log.info("敏感词过滤器初始化完成，共加载 {} 个词条", DEFAULT_WORDS.length);
    }

    // ==================== 公开 API ====================

    /**
     * 检查文本是否包含敏感词
     *
     * @param text 待检查文本
     * @return true=包含敏感词
     */
    public boolean containsSensitiveWord(String text) {
        if (text == null || text.isBlank())
            return false;

        // 1. 基础检查（处理原貌，包含英文 SB/TMD 等）
        String lowerText = text.toLowerCase();
        for (int i = 0; i < lowerText.length(); i++) {
            if (checkDFA(lowerText, i) > 0)
                return true;
        }

        // 2. 增强检查：去除干扰字符（空格、标点、数字、特殊符号）再查一遍
        // 这样可以拦截 "草!泥!马", "草 泥 马", "草1泥2马"
        String cleanText = text.replaceAll("[\\s\\p{P}\\p{S}\\d]", "");
        if (!cleanText.isEmpty()) {
            for (int i = 0; i < cleanText.length(); i++) {
                if (checkDFA(cleanText, i) > 0)
                    return true;
            }
        }

        return false;
    }

    /**
     * 获取文本中命中的所有敏感词
     *
     * @param text 待检查文本
     * @return 命中的敏感词集合（去重）
     */
    public Set<String> getSensitiveWords(String text) {
        Set<String> result = new LinkedHashSet<>();
        if (text == null || text.isEmpty())
            return result;
        text = text.toLowerCase();
        for (int i = 0; i < text.length(); i++) {
            int length = checkDFA(text, i);
            if (length > 0) {
                result.add(text.substring(i, i + length));
                i += length - 1; // 跳过已匹配的部分
            }
        }
        return result;
    }

    /**
     * 将敏感词替换为 ***
     *
     * @param text 原始文本
     * @return 脱敏后文本
     */
    public String replaceSensitiveWords(String text) {
        if (text == null || text.isEmpty())
            return text;
        String lower = text.toLowerCase();
        StringBuilder sb = new StringBuilder(text);
        for (int i = 0; i < lower.length(); i++) {
            int length = checkDFA(lower, i);
            if (length > 0) {
                for (int j = i; j < i + length; j++) {
                    sb.setCharAt(j, '*');
                }
                i += length - 1;
            }
        }
        return sb.toString();
    }

    /**
     * 动态添加敏感词
     */
    @SuppressWarnings("unchecked")
    public void addWords(Collection<String> words) {
        for (String word : words) {
            if (word == null || word.isBlank())
                continue;
            word = word.toLowerCase().trim();
            Map<Character, Object> current = rootMap;
            for (int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);
                Object node = current.get(c);
                if (node instanceof Map) {
                    current = (Map<Character, Object>) node;
                } else {
                    Map<Character, Object> newNode = new HashMap<>();
                    current.put(c, newNode);
                    current = newNode;
                }
            }
            current.put(END_FLAG.charAt(0), END_FLAG);
        }
    }

    // ==================== 内部 DFA 匹配 ====================

    @SuppressWarnings("unchecked")
    private int checkDFA(String text, int startIndex) {
        Map<Character, Object> current = rootMap;
        int matchLength = 0;
        int lastMatchLength = 0;

        for (int i = startIndex; i < text.length(); i++) {
            char c = text.charAt(i);
            Object node = current.get(c);
            if (node == null)
                break;
            matchLength++;
            if (node instanceof Map) {
                current = (Map<Character, Object>) node;
                if (current.containsKey(END_FLAG.charAt(0))) {
                    lastMatchLength = matchLength; // 贪心匹配最长词
                }
            }
        }
        return lastMatchLength;
    }
}
