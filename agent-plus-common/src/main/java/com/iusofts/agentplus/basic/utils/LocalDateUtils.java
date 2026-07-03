package com.iusofts.agentplus.basic.utils;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.Objects;

/**
 * Java8 LocalDate 日期工具类
 * 封装常用的日期操作：月初/月末、年初/年末、日期偏移、周几转换等
 * @author 自定义
 */
public class LocalDateUtils {

    /**
     * 私有化构造器，禁止实例化工具类
     */
    private LocalDateUtils() {
        throw new AssertionError("工具类禁止实例化");
    }

    // ===================== 核心：获取月初、月末相关 =====================
    /**
     * 获取指定日期的【当月第一天】（月初）
     * @param localDate 目标日期，null则返回当前日期的月初
     * @return 当月第一天 LocalDate
     */
    public static LocalDate getMonthStart(LocalDate localDate) {
        LocalDate date = Objects.isNull(localDate) ? LocalDate.now() : localDate;
        return date.with(TemporalAdjusters.firstDayOfMonth());
    }

    /**
     * 获取指定日期的【当月最后一天】（月末）
     * @param localDate 目标日期，null则返回当前日期的月末
     * @return 当月最后一天 LocalDate
     */
    public static LocalDate getMonthEnd(LocalDate localDate) {
        LocalDate date = Objects.isNull(localDate) ? LocalDate.now() : localDate;
        return date.with(TemporalAdjusters.lastDayOfMonth());
    }

    /**
     * 获取指定年月的【当月第一天】（月初）
     * @param year 年份
     * @param month 月份 (1-12)
     * @return 当月第一天 LocalDate
     */
    public static LocalDate getMonthStart(int year, int month) {
        return LocalDate.of(year, month, 1);
    }

    /**
     * 获取指定年月的【当月最后一天】（月末）
     * @param year 年份
     * @param month 月份 (1-12)
     * @return 当月最后一天 LocalDate
     */
    public static LocalDate getMonthEnd(int year, int month) {
        return LocalDate.of(year, month, 1).with(TemporalAdjusters.lastDayOfMonth());
    }

    // ===================== 扩展：年初、年末相关 =====================
    /**
     * 获取指定日期的【当年第一天】（年初）
     * @param localDate 目标日期，null则返回当前日期的年初
     * @return 当年第一天 LocalDate
     */
    public static LocalDate getYearStart(LocalDate localDate) {
        LocalDate date = Objects.isNull(localDate) ? LocalDate.now() : localDate;
        return date.with(TemporalAdjusters.firstDayOfYear());
    }

    /**
     * 获取指定日期的【当年最后一天】（年末）
     * @param localDate 目标日期，null则返回当前日期的年末
     * @return 当年最后一天 LocalDate
     */
    public static LocalDate getYearEnd(LocalDate localDate) {
        LocalDate date = Objects.isNull(localDate) ? LocalDate.now() : localDate;
        return date.with(TemporalAdjusters.lastDayOfYear());
    }

    // ===================== 扩展：日期偏移（加减） =====================
    /**
     * 日期加N天
     * @param localDate 目标日期，null则基于当前日期计算
     * @param days 要加的天数（正数加，负数减）
     * @return 偏移后的 LocalDate
     */
    public static LocalDate plusDays(LocalDate localDate, long days) {
        LocalDate date = Objects.isNull(localDate) ? LocalDate.now() : localDate;
        return date.plusDays(days);
    }

    /**
     * 日期加N个月
     * @param localDate 目标日期，null则基于当前日期计算
     * @param months 要加的月份（正数加，负数减）
     * @return 偏移后的 LocalDate（自动处理月末进位，如1.31+1月=2.28/29）
     */
    public static LocalDate plusMonths(LocalDate localDate, long months) {
        LocalDate date = Objects.isNull(localDate) ? LocalDate.now() : localDate;
        return date.plusMonths(months);
    }

    /**
     * 日期加N年
     * @param localDate 目标日期，null则基于当前日期计算
     * @param years 要加的年数（正数加，负数减）
     * @return 偏移后的 LocalDate（自动处理闰年，如2020.2.29+1年=2021.2.28）
     */
    public static LocalDate plusYears(LocalDate localDate, long years) {
        LocalDate date = Objects.isNull(localDate) ? LocalDate.now() : localDate;
        return date.plusYears(years);
    }

    // ===================== 扩展：周、月份、闰年相关 =====================
    /**
     * 获取指定日期是周几（中文描述：周一/周二...周日）
     * @param localDate 目标日期，null则返回当前日期的周几
     * @return 中文周几
     */
    public static String getChineseWeek(LocalDate localDate) {
        LocalDate date = Objects.isNull(localDate) ? LocalDate.now() : localDate;
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        switch (dayOfWeek) {
            case MONDAY: return "周一";
            case TUESDAY: return "周二";
            case WEDNESDAY: return "周三";
            case THURSDAY: return "周四";
            case FRIDAY: return "周五";
            case SATURDAY: return "周六";
            case SUNDAY: return "周日";
            default: return "";
        }
    }

    /**
     * 获取指定日期的月份总天数
     * @param localDate 目标日期，null则返回当前月份的天数
     * @return 月份天数（28/29/30/31）
     */
    public static int getMonthDays(LocalDate localDate) {
        LocalDate date = Objects.isNull(localDate) ? LocalDate.now() : localDate;
        return getMonthEnd(date).getDayOfMonth();
    }

    /**
     * 判断指定日期的年份是否为闰年
     * @param localDate 目标日期，null则判断当前年份
     * @return true=闰年，false=平年
     */
    public static boolean isLeapYear(LocalDate localDate) {
        LocalDate date = Objects.isNull(localDate) ? LocalDate.now() : localDate;
        return date.isLeapYear();
    }

    // ===================== 扩展：其他常用 =====================
    /**
     * 判断一个日期是否在两个日期之间（包含开始和结束日期）
     * @param target 目标日期
     * @param start 开始日期
     * @param end 结束日期
     * @return true=在区间内，false=不在
     */
    public static boolean isBetween(LocalDate target, LocalDate start, LocalDate end) {
        Objects.requireNonNull(target, "目标日期不能为空");
        Objects.requireNonNull(start, "开始日期不能为空");
        Objects.requireNonNull(end, "结束日期不能为空");
        return target.isAfter(start.minusDays(1)) && target.isBefore(end.plusDays(1));
    }

    /**
     * 计算LocalDate的上月同一天，自动处理月末边界问题
     * @param localDate 当期日期
     * @return 上月同一天（月末溢出则取上月最后一天）
     * @throws NullPointerException 入参为null时抛出
     */
    public static LocalDate lastMonthSameDay(LocalDate localDate) {
        LocalDate date = Objects.isNull(localDate) ? LocalDate.now() : localDate;
        // 核心逻辑：减1个月 + 保留原日期的天数（自动处理月末）
        return date.minusMonths(1)
                .withDayOfMonth(date.getDayOfMonth());
    }

    public static LocalDate longToLocalDate(Long timeStamp) {
        // 非空+合法校验：时间戳需大于0，时区非空
        if (timeStamp == null || timeStamp <= 0) {
            return null; // 或抛出自定义业务异常
        }
        // 假设默认是毫秒戳，可根据实际业务调整
        return Instant.ofEpochMilli(timeStamp)
                .atZone(ZoneId.of("Asia/Shanghai"))
                .toLocalDate();
    }

    public static LocalDate dateToLocalDate(Date date) {
        // 非空+合法校验：时间戳需大于0，时区非空
        if (date == null) {
            return null; // 或抛出自定义业务异常
        }
        // 假设默认是毫秒戳，可根据实际业务调整
        return date.toInstant()
                .atZone(ZoneId.of("Asia/Shanghai"))
                .toLocalDate();
    }
    
}