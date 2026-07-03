package com.iusofts.web.common.manager;

import com.iusofts.basic.ip.AddressUtils;
import com.iusofts.basic.utils.BeanLocatorUtil;
import com.iusofts.system.dto.OperLogAddParam;
import com.iusofts.system.interfaces.IOperLogService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

/**
 * 异步工厂（产生任务用）- 支持虚拟线程
 *
 * @author
 */
@Slf4j
public class AsyncFactory {

    /**
     * 操作日志记录（虚拟线程方式）
     *
     * @param param 操作日志信息
     * @return 任务runnable
     */
    public static Runnable recordOperVirtual(final OperLogAddParam param) {
        return () -> {
            try {
                // 远程查询操作地点
                param.setOperLocation(AddressUtils.getRealAddressByIP(param.getOperIp()));
                BeanLocatorUtil.getBean(IOperLogService.class).add(param);
            } catch (Exception e) {
                log.error("记录操作日志失败", e);
            }
        };
    }

    /**
     * 异步执行操作日志记录（基于CompletableFuture的虚拟线程方式）
     *
     * @param param 操作日志信息
     * @return CompletableFuture
     */
    public static CompletableFuture<Void> recordOperAsync(final OperLogAddParam param) {
        return CompletableFuture.runAsync(() -> {
            try {
                // 远程查询操作地点
                param.setOperLocation(AddressUtils.getRealAddressByIP(param.getOperIp()));
                BeanLocatorUtil.getBean(IOperLogService.class).add(param);
            } catch (Exception e) {
                log.error("记录操作日志失败", e);
            }
        }, CompletableFuture.delayedExecutor(10, java.util.concurrent.TimeUnit.MILLISECONDS));
    }

    /**
     * 操作日志记录（使用分组并发控制）
     *
     * @param param 操作日志信息
     * @return 任务runnable
     */
    public static Runnable recordOperVirtualWithLimit(final OperLogAddParam param) {
        return () -> {
            try {
                // 远程查询操作地点
                param.setOperLocation(AddressUtils.getRealAddressByIP(param.getOperIp()));
                BeanLocatorUtil.getBean(IOperLogService.class).add(param);
            } catch (Exception e) {
                log.error("记录操作日志失败", e);
            }
        };
    }
}
