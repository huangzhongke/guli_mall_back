package com.hzk.gulimall.seckill.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author kee
 * @version 1.0
 * @date 2023/3/30 16:53
 */
@EnableAsync
@EnableScheduling
@Slf4j
@Configuration
public class ScheduleConfig {
}
