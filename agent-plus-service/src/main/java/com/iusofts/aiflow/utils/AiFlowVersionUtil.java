package com.iusofts.aiflow.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI流程版本工具类
 *
 * @author Ivan
 * @since 2026-06-12
 */
public class AiFlowVersionUtil {

    private static final Pattern VERSION_PATTERN = Pattern.compile("v(\\d+)\\.(\\d+)\\.(\\d+)");

    /**
     * 生成下一个版本号
     *
     * @param currentVersion 当前版本号（可为null）
     * @return 下一个版本号
     */
    public static String generateNextVersion(String currentVersion) {
        if (currentVersion == null) {
            return "v1.0.0";
        }

        Matcher matcher = VERSION_PATTERN.matcher(currentVersion);
        if (matcher.matches()) {
            int major = Integer.parseInt(matcher.group(1));
            int minor = Integer.parseInt(matcher.group(2));
            int patch = Integer.parseInt(matcher.group(3));

            // 后两位每次升1，最大20，超过进位
            patch++;
            if (patch > 20) {
                patch = 0;
                minor++;
                if (minor > 20) {
                    minor = 0;
                    major++;
                }
            }
            return String.format("v%d.%d.%d", major, minor, patch);
        } else {
            // 版本号格式不匹配，返回默认版本
            return "v1.0.0";
        }
    }

}
