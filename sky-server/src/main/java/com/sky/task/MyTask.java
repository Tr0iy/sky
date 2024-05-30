package com.sky.task;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 自定义定时任务类
 */
@Component//需要实例化，交给spring容器来创建对象
@Slf4j
public class MyTask {

    /**
     * 定时任务 每隔5秒触发一次
     */
    //   @Scheduled(cron = "0/5 * * * * ?")
    public void executeTask() {
        log.info("定时任务开始执行：{}", new Date());
    }
}
