package com.iusofts.basic.excel;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Excel日期时间终极转换工具类（Java 8）
 * 支持几乎所有Excel常见日期格式：
 * 1. 带时分秒/毫秒/12小时制（AM/PM/上午/下午）
 * 2. 完整/非完整日期（X年X月X日/X年X月/X月X日）
 * 3. 各种分隔符（- / . 空格 无分隔符）
 * 4. Excel数字日期值（如 45678 对应 2024-01-01）
 * 5. 补零/无补零、中英文格式兼容
 */
public class ExcelDateConverter {

    // 预定义所有Excel可能出现的日期时间格式（按优先级排序）
    private static final List<DateTimeFormatter> ALL_EXCEL_FORMATTERS = new ArrayList<>();

    // 匹配纯数字（Excel日期值）的正则
    private static final Pattern NUMERIC_DATE_PATTERN = Pattern.compile("^\\d+(\\.\\d+)?$");
    // Excel日期基准：1900-01-01（Excel的bug：认为1900是闰年，所以基准要调整）
    private static final LocalDate EXCEL_BASE_DATE = LocalDate.of(1899, 12, 30);

    static {
        // ==================== 1. 带时间（毫秒/12小时制/中英文标识） ====================
        // 24小时制 + 毫秒
        ALL_EXCEL_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withResolverStyle(ResolverStyle.SMART));
        ALL_EXCEL_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS").withResolverStyle(ResolverStyle.SMART));
        ALL_EXCEL_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss.SSS").withResolverStyle(ResolverStyle.SMART));
        ALL_EXCEL_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss.SSS").withResolverStyle(ResolverStyle.SMART));
        // 24小时制 + 秒
        ALL_EXCEL_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withResolverStyle(ResolverStyle.SMART));
        ALL_EXCEL_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").withResolverStyle(ResolverStyle.SMART));
        ALL_EXCEL_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss").withResolverStyle(ResolverStyle.SMART));
        ALL_EXCEL_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss").withResolverStyle(ResolverStyle.SMART));
        // 24小时制 + 分
        ALL_EXCEL_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withResolverStyle(ResolverStyle.SMART));
        ALL_EXCEL_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm").withResolverStyle(ResolverStyle.SMART));
        ALL_EXCEL_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm").withResolverStyle(ResolverStyle.SMART));
        // 12小时制（AM/PM）
        ALL_EXCEL_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a").withResolverStyle(ResolverStyle.SMART));
        ALL_EXCEL_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm a").withResolverStyle(ResolverStyle.SMART));
        // 12小时制（上午/下午）
        ALL_EXCEL_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy年MM月dd日 hh:mm:ss 上午").withResolverStyle(ResolverStyle.SMART));
        ALL_EXCEL_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy年MM月dd日 hh:mm 下午").withResolverStyle(ResolverStyle.SMART));

        // ==================== 2. 纯日期（各种分隔符/补零/无补零） ====================
        // 横线/斜杠/点分隔
        ALL_EXCEL_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy-MM-dd").withResolverStyle(ResolverStyle.SMART));
        ALL_EXCEL_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy/MM/dd").withResolverStyle(ResolverStyle.SMART));
        ALL_EXCEL_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy/M/d").withResolverStyle(ResolverStyle.SMART));
        ALL_EXCEL_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy.MM.dd").withResolverStyle(ResolverStyle.SMART));
        ALL_EXCEL_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyyMMdd").withResolverStyle(ResolverStyle.SMART));
        // 月日年格式
        ALL_EXCEL_FORMATTERS.add(DateTimeFormatter.ofPattern("MM-dd-yyyy").withResolverStyle(ResolverStyle.SMART));
        ALL_EXCEL_FORMATTERS.add(DateTimeFormatter.ofPattern("MM/dd/yyyy").withResolverStyle(ResolverStyle.SMART));
        ALL_EXCEL_FORMATTERS.add(DateTimeFormatter.ofPattern("MM.dd.yyyy").withResolverStyle(ResolverStyle.SMART));
        // 日月年格式
        ALL_EXCEL_FORMATTERS.add(DateTimeFormatter.ofPattern("dd-MM-yyyy").withResolverStyle(ResolverStyle.SMART));
        ALL_EXCEL_FORMATTERS.add(DateTimeFormatter.ofPattern("dd/MM/yyyy").withResolverStyle(ResolverStyle.SMART));
        ALL_EXCEL_FORMATTERS.add(DateTimeFormatter.ofPattern("dd.MM.yyyy").withResolverStyle(ResolverStyle.SMART));

        // ==================== 3. 中文日期（补零/无补零） ====================
        // 年-月-日 完整
        ALL_EXCEL_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy年MM月dd日").withResolverStyle(ResolverStyle.SMART));
        ALL_EXCEL_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy年M月d日").withResolverStyle(ResolverStyle.SMART));
        // 年-月
        ALL_EXCEL_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy年MM月").withResolverStyle(ResolverStyle.SMART));
        ALL_EXCEL_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy年M月").withResolverStyle(ResolverStyle.SMART));
        // 月-日（补当前年）
        ALL_EXCEL_FORMATTERS.add(DateTimeFormatter.ofPattern("MM月dd日").withResolverStyle(ResolverStyle.SMART));
        ALL_EXCEL_FORMATTERS.add(DateTimeFormatter.ofPattern("M月d日").withResolverStyle(ResolverStyle.SMART));

        // ==================== 4. 极简格式 ====================
        ALL_EXCEL_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy-MM").withResolverStyle(ResolverStyle.SMART));
        ALL_EXCEL_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy/MM").withResolverStyle(ResolverStyle.SMART));
        ALL_EXCEL_FORMATTERS.add(DateTimeFormatter.ofPattern("MM/dd").withResolverStyle(ResolverStyle.SMART));
        ALL_EXCEL_FORMATTERS.add(DateTimeFormatter.ofPattern("M/d").withResolverStyle(ResolverStyle.SMART));
        ALL_EXCEL_FORMATTERS.add(DateTimeFormatter.ofPattern("yyyy").withResolverStyle(ResolverStyle.SMART)); // 仅年份
    }

    /**
     * 核心转换方法：支持所有Excel日期格式，返回LocalDateTime
     * @param dateStr Excel日期字符串/数字值
     * @return LocalDateTime（非完整日期补全默认值，数字值转换为对应日期）
     * @throws DateTimeParseException 所有格式都不支持时抛出异常
     */
    public static LocalDateTime convertToLocalDateTime(String dateStr, boolean notNull) {
        // 空值校验
        if (dateStr == null || dateStr.trim().isEmpty()) {
            if (notNull) {
                throw new IllegalArgumentException("日期字符串不能为空");
            } else {
                return null;
            }
        }

        String cleanStr = preprocessDateStr(dateStr.trim());

        // 第一步：尝试解析为Excel数字日期值（如 45678.5 对应 2024-01-01 12:00:00）
        LocalDateTime numericResult = parseExcelNumericDate(cleanStr);
        if (numericResult != null) {
            return numericResult;
        }

        // 第二步：遍历所有预定义格式解析
        for (DateTimeFormatter formatter : ALL_EXCEL_FORMATTERS) {
            try {
                // 尝试解析为LocalDateTime（带时间）
                return LocalDateTime.parse(cleanStr, formatter);
            } catch (DateTimeParseException e1) {
                try {
                    // 尝试解析为LocalDate（纯日期），补全时间为00:00:00
                    LocalDate localDate = LocalDate.parse(cleanStr, formatter);
                    return localDate.atStartOfDay();
                } catch (DateTimeParseException e2) {
                    // 尝试解析为Year（仅年份），补全月日为01-01，时间为00:00:00
                    try {
                        Year year = Year.parse(cleanStr, formatter);
                        return year.atMonth(1).atDay(1).atStartOfDay();
                    } catch (Exception e3) {
                        continue;
                    }
                }
            }
        }

        // 第三步：处理"X月X日"格式（补当前年）
        LocalDateTime monthDayResult = parseMonthDayWithCurrentYear(cleanStr);
        if (monthDayResult != null) {
            return monthDayResult;
        }

        // 第四步：兜底解析（自适应匹配）
        LocalDateTime fallbackResult = fallbackParse(cleanStr);
        if (fallbackResult != null) {
            return fallbackResult;
        }

        // 所有解析方式失败
        throw new DateTimeParseException(
                "不支持的Excel日期格式：" + dateStr + "（已尝试所有预定义格式）",
                dateStr,
                0
        );
    }

    /**
     * 预处理日期字符串：统一格式，提升解析成功率
     * 1. 去除多余空格 2. 统一中英文时间标识 3. 替换全角字符为半角
     */
    private static String preprocessDateStr(String str) {
        return str
                .replaceAll("\\s+", " ") // 多个空格替换为单个
                .replace("上午", "AM")    // 中文上午转AM
                .replace("下午", "PM")    // 中文下午转PM
                .replace("ＡＭ", "AM")    // 全角AM
                .replace("ＰＭ", "PM")    // 全角PM
                .replace("：", ":")       // 全角冒号转半角
                .replace("．", ".")       // 全角点转半角
                .replace("，", ",")       // 全角逗号转半角
                .trim();
    }

    /**
     * 解析Excel数字日期值（Excel将日期存储为数字，1900-01-01=1）
     */
    private static LocalDateTime parseExcelNumericDate(String str) {
        if (!NUMERIC_DATE_PATTERN.matcher(str).matches()) {
            return null;
        }

        try {
            BigDecimal excelNum = new BigDecimal(str);
            // 提取整数部分（天数）和小数部分（时间）
            long days = excelNum.setScale(0, BigDecimal.ROUND_DOWN).longValue();
            BigDecimal fraction = excelNum.subtract(new BigDecimal(days));

            // 计算基准日期 + 天数
            LocalDate date = EXCEL_BASE_DATE.plusDays(days);
            // 计算小数部分对应的时间（24小时制）
            long seconds = fraction.multiply(new BigDecimal(86400)).setScale(0, BigDecimal.ROUND_DOWN).longValue();
            LocalTime time = LocalTime.ofSecondOfDay(seconds);

            return LocalDateTime.of(date, time);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 处理"X月X日"格式，自动补当前年
     */
    private static LocalDateTime parseMonthDayWithCurrentYear(String str) {
        int currentYear = Year.now().getValue();
        String[] monthDayPatterns = {"M月d日", "MM月dd日", "M/d", "MM/dd", "M-d", "MM-dd"};

        for (String pattern : monthDayPatterns) {
            try {
                DateTimeFormatter mdFormatter = DateTimeFormatter.ofPattern(pattern).withResolverStyle(ResolverStyle.SMART);
                // 拼接年份，构造完整日期
                String fullPattern = "yyyy年" + pattern.replace("/", "年").replace("-", "年");
                String fullStr = currentYear + "年" + str;
                LocalDate date = LocalDate.parse(fullStr, DateTimeFormatter.ofPattern(fullPattern).withResolverStyle(ResolverStyle.SMART));
                return date.atStartOfDay();
            } catch (Exception e) {
                continue;
            }
        }
        return null;
    }

    /**
     * 兜底解析：当预定义格式匹配失败时，尝试自适应解析
     */
    private static LocalDateTime fallbackParse(String str) {
        // 尝试常见的自适应格式
        String[] fallbackPatterns = {
                "yyyyMMddHHmmss",      // 无分隔符完整时间
                "yyyyMMddHHmm",        // 无分隔符日期+时分
                "yyMMdd",              // 两位年份+日期
                "yyyy-MM-dd HH:mm:ss a",// 12小时制带AM/PM
                "yyyy年M月d日 HH:mm:ss" // 中文日期+时间
        };

        for (String pattern : fallbackPatterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern).withResolverStyle(ResolverStyle.LENIENT);
                return LocalDateTime.parse(str, formatter);
            } catch (Exception e) {
                try {
                    LocalDate date = LocalDate.parse(str, DateTimeFormatter.ofPattern(pattern).withResolverStyle(ResolverStyle.LENIENT));
                    return date.atStartOfDay();
                } catch (Exception ex) {
                    continue;
                }
            }
        }
        return null;
    }

    /**
     * 转换为LocalDate（忽略时分秒）
     */
    public static LocalDate convertToLocalDate(String dateStr, boolean notNull) {
        // 空值校验
        if (dateStr == null || dateStr.trim().isEmpty()) {
            if (notNull) {
                throw new IllegalArgumentException("日期字符串不能为空");
            } else {
                return null;
            }
        }
        return convertToLocalDateTime(dateStr, notNull).toLocalDate();
    }

    /**
     * 转换为LocalDate（忽略时分秒）
     */
    public static LocalDate convertToLocalDate(String dateStr) {
        return convertToLocalDate(dateStr, false);
    }

    /**
     * 转换为Date（兼容旧API）
     */
    public static Date convertToDate(String dateStr, boolean notNull) {
        // 空值校验
        if (dateStr == null || dateStr.trim().isEmpty()) {
            if (notNull) {
                throw new IllegalArgumentException("日期字符串不能为空");
            } else {
                return null;
            }
        }
        LocalDateTime ldt = convertToLocalDateTime(dateStr, notNull);
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 转换为Date（兼容旧API）
     */
    public static Date convertToDate(String dateStr) {
        return convertToDate(dateStr, false);
    }

    /**
     * 安全解析（失败返回null，不抛异常）
     */
    public static LocalDateTime safeConvertToLocalDateTime(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return convertToLocalDateTime(dateStr, false);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 安全解析为LocalDate
     */
    public static LocalDate safeConvertToLocalDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return convertToLocalDate(dateStr, false);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 全格式测试用例：覆盖几乎所有Excel场景
     */
    public static void main(String[] args) {
        String[] testCases = {
                // 1. 带毫秒/12小时制
                "2024-01-27 15:30:45.123",
                "2024/01/27 03:30:45 PM",
                "2024年01月27日 08:30:45 上午",
                "2024.01.27 10:20:30.999",

                // 2. 各种分隔符/无分隔符
                "20240127153045",
                "202401271530",
                "2024-01-27",
                "2024/01/27",
                "2024.01.27",
                "01-27-2024",
                "01/27/2024",
                "27/01/2024",
                "27.01.2024",

                // 3. 中文日期（补零/无补零）
                "2024年1月5日",
                "2024年01月05日",
                "2024年1月",
                "2024年01月",
                "1月5日",
                "01月05日",

                // 4. Excel数字日期值
                "45678",          // 2024-01-01
                "45678.5",        // 2024-01-01 12:00:00
                "45678.75",       // 2024-01-01 18:00:00

                // 5. 极简格式/边缘格式
                "2024",           // 仅年份
                "240127",         // 两位年份+日期
                "2024年1月5日 下午3点30分", // 中文时间描述（预处理后转PM）
                "2024：01：27 15：30：45"  // 全角符号
        };

        // 执行测试
        for (String testStr : testCases) {
            try {
                LocalDateTime ldt = convertToLocalDateTime(testStr, false);
                LocalDate ld = convertToLocalDate(testStr, false);
                Date date = convertToDate(testStr, false);
                System.out.printf("✅ 原始：%-30s -> LocalDateTime：%s -> LocalDate：%s -> Date：%s%n",
                        testStr, ldt, ld, date);
            } catch (Exception e) {
                System.out.printf("❌ 原始：%-30s -> 解析失败：%s%n", testStr, e.getMessage());
            }
        }
    }
}