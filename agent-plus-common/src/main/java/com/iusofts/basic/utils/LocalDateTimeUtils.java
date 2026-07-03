package com.iusofts.basic.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * LocalDateTime 工具类
 * 纯JDK8+实现，无第三方依赖
 * 包含：当天开始/结束时间、指定日期开始/结束时间、时间格式化、时间差、时间转换等常用方法
 */
public class LocalDateTimeUtils {

    // ====================== 常用时间格式常量 ======================
    /** 默认日期时间格式：yyyy-MM-dd HH:mm:ss */
    public static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    /** 紧凑日期时间格式：yyyyMMddHHmmss */
    public static final String COMPACT_PATTERN = "yyyyMMddHHmmss";

    /** 线程安全的格式化器 */
    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_PATTERN);
    private static final DateTimeFormatter COMPACT_FORMATTER = DateTimeFormatter.ofPattern(COMPACT_PATTERN);

    // ====================== 当天开始/结束时间 ======================

    /**
     * 获取当前日期的开始时间（当天 00:00:00）
     * @return LocalDateTime
     */
    public static LocalDateTime getCurrentDayStart() {
        return LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
    }

    /**
     * 获取当前日期的结束时间（当天 23:59:59.999999999）
     * @return LocalDateTime
     */
    public static LocalDateTime getCurrentDayEnd() {
        return LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
    }

    // ====================== 指定日期开始/结束时间 ======================

    /**
     * 获取指定 LocalDate 的开始时间
     * @param date 日期
     * @return LocalDateTime
     */
    public static LocalDateTime getDayStart(LocalDate date) {
        if (date == null) {
            return null;
        }
        return LocalDateTime.of(date, LocalTime.MIN);
    }

    /**
     * 获取指定 LocalDate 的结束时间
     * @param date 日期
     * @return LocalDateTime
     */
    public static LocalDateTime getDayEnd(LocalDate date) {
        if (date == null) {
            return null;
        }
        return LocalDateTime.of(date, LocalTime.MAX);
    }

    /**
     * 获取指定 LocalDateTime 当天的开始时间
     * @param dateTime 时间
     * @return LocalDateTime
     */
    public static LocalDateTime getDayStart(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toLocalDate().atStartOfDay();
    }

    /**
     * 获取指定 LocalDateTime 当天的结束时间
     * @param dateTime 时间
     * @return LocalDateTime
     */
    public static LocalDateTime getDayEnd(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return getDayEnd(dateTime.toLocalDate());
    }

    // ====================== 时间格式化 ======================

    /**
     * LocalDateTime 转字符串（默认格式 yyyy-MM-dd HH:mm:ss）
     * @param dateTime 时间
     * @return 格式化字符串
     */
    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DEFAULT_FORMATTER);
    }

    /**
     * LocalDateTime 转字符串（自定义格式）
     * @param dateTime 时间
     * @param pattern 格式
     * @return 格式化字符串
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null || pattern == null || pattern.isEmpty()) {
            return "";
        }
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    // ====================== 字符串转时间 ======================

    /**
     * 字符串转 LocalDateTime（默认格式 yyyy-MM-dd HH:mm:ss）
     * @param dateTimeStr 时间字符串
     * @return LocalDateTime
     */
    public static LocalDateTime parse(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, DEFAULT_FORMATTER);
    }

    /**
     * 字符串转 LocalDateTime（自定义格式）
     * @param dateTimeStr 时间字符串
     * @param pattern 格式
     * @return LocalDateTime
     */
    public static LocalDateTime parse(String dateTimeStr, String pattern) {
        if (dateTimeStr == null || dateTimeStr.isEmpty() || pattern == null || pattern.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(pattern));
    }

    // ====================== 时间差计算 ======================

    /**
     * 计算两个时间的秒数差（end - start）
     */
    public static long getSecondsDiff(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.SECONDS.between(start, end);
    }

    /**
     * 计算两个时间的分钟数差
     */
    public static long getMinutesDiff(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.MINUTES.between(start, end);
    }

    /**
     * 计算两个时间的小时数差
     */
    public static long getHoursDiff(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.HOURS.between(start, end);
    }

    /**
     * 计算两个时间的天数差
     */
    public static long getDaysDiff(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(start, end);
    }

    // ====================== 时间判断 ======================

    /**
     * 判断时间是否在当天内
     */
    public static boolean isToday(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        return dateTime.toLocalDate().isEqual(LocalDate.now());
    }

    // ====================== 时间转换 ======================

    /**
     * LocalDate 转 LocalDateTime（00:00:00）
     */
    public static LocalDateTime toLocalDateTime(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay();
    }

    public static LocalDateTime toLocalDateTime(Long timestamp) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(timestamp),
                ZoneId.systemDefault()
        );
    }
}