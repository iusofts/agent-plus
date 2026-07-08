package com.iusofts.agentplus.plugin.document;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 文本分块器。
 *
 * <p>按知识库配置的 {@code chunkSize}(字符数)滑动切分,相邻块重叠 {@code chunkOverlap} 个字符,
 * 以保证跨块语义连续。切分优先在段落/句子边界回退,避免从词语中间截断。</p>
 *
 * @author Ivan
 */
@Component
public class TextChunker {

    /** 从 end 位置向前回退寻找边界的最大距离(占 chunkSize 的比例)。 */
    private static final double BOUNDARY_LOOKBACK_RATIO = 0.2;

    /** 优先切分的边界字符(段落 > 换行 > 句末标点)。 */
    private static final char[] BOUNDARY_CHARS = {'\n', '。', '！', '？', '.', '!', '?', '；', ';'};

    /**
     * 分块。
     *
     * @param text    原始纯文本
     * @param chunkSize    每块目标字符数(<=0 时用 512)
     * @param chunkOverlap 相邻块重叠字符数(<0 归零;>=chunkSize 时收敛为 chunkSize/4)
     * @return 分块内容列表(顺序即原文顺序);text 为空返回空列表
     */
    public List<String> split(String text, int chunkSize, int chunkOverlap) {
        List<String> chunks = new ArrayList<>();
        if (text == null) {
            return chunks;
        }
        String normalized = text.replace("\r\n", "\n").replace('\r', '\n').trim();
        if (normalized.isEmpty()) {
            return chunks;
        }

        int size = chunkSize > 0 ? chunkSize : 512;
        int overlap = chunkOverlap;
        if (overlap < 0) {
            overlap = 0;
        }
        if (overlap >= size) {
            overlap = size / 4;
        }

        int length = normalized.length();
        int start = 0;
        while (start < length) {
            int end = Math.min(start + size, length);
            // 未到文本末尾时,尝试在边界处收尾,避免截断句子
            if (end < length) {
                end = adjustToBoundary(normalized, start, end, size);
            }
            String chunk = normalized.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }
            if (end >= length) {
                break;
            }
            // 下一块起点回退 overlap,保证重叠;至少前进 1 防止死循环
            int next = end - overlap;
            if (next <= start) {
                next = end;
            }
            start = next;
        }
        return chunks;
    }

    /**
     * 在 [start, end] 内从 end 向前回退,寻找最近的句子/段落边界。
     * 回退距离不超过 size*BOUNDARY_LOOKBACK_RATIO,找不到则维持原 end。
     */
    private int adjustToBoundary(String text, int start, int end, int size) {
        int minBoundary = Math.max(start + 1, end - (int) (size * BOUNDARY_LOOKBACK_RATIO));
        for (int i = end - 1; i >= minBoundary; i--) {
            if (isBoundary(text.charAt(i))) {
                // 边界字符包含在当前块内,下一块从其后开始
                return i + 1;
            }
        }
        return end;
    }

    private boolean isBoundary(char c) {
        for (char b : BOUNDARY_CHARS) {
            if (c == b) {
                return true;
            }
        }
        return false;
    }
}
