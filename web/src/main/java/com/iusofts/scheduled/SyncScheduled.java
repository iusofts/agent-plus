package com.iusofts.scheduled;

import com.iusofts.basic.redis.RedisLock;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@EnableAsync
@EnableScheduling
public class SyncScheduled {
    
    @Resource
    private RedisLock redisLock;

    /**
     * 1、在Spring中表达式是6位组成，不允许第七位的年份
     * 2、在周几的的位置,1-7代表周一到周日
     * 3、定时任务不该阻塞。默认是阻塞的
     * 1）、可以让业务以异步的方式，自己提交到线程池
     * CompletableFuture.runAsync(() -> {
     * },execute);
     * <p>
     * 2）、支持定时任务线程池；设置 TaskSchedulingProperties
     * spring.task.scheduling.pool.size: 5
     * <p>
     * 3）、让定时任务异步执行
     * 异步任务
     * <p>
     * 解决：使用异步任务 + 定时任务来完成定时任务不阻塞的功能
     */
    @Async
    //@Scheduled(cron = "*/1 * * * * ?")
    //@Scheduled(cron = "* * * * * ?") //每分钟
    //@Scheduled(cron = "0 0 4 * * ? ") //每天早上4点
    public void syncTest() {
        String lockKey = "sync:schedule:test";
        boolean acquired = redisLock.tryLock(lockKey, 5, TimeUnit.MINUTES);
        if (!acquired) {
            log.info("同步测试任务已被其他节点执行");
            return;
        }

        try {
            log.info("异步任务开始");
            LocalDate date = LocalDate.now();
        } finally {
            redisLock.releaseLock(lockKey);
        }
    }

}