package com.hzk.gulimall.seckill.schedule;

import com.hzk.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author kee
 * @version 1.0
 * @date 2023/3/30 16:54
 */
@Slf4j
@Service
public class SeckillSkuScheduled {
    @Autowired
    SeckillService seckillService;
    @Autowired
    RedissonClient redissonClient;

    private final String upload_lock = "seckill:upload:lock";
    /**
     * 每天晚上3点 上架近3天需要秒杀商品
     */
    @Scheduled(cron = "0 * * * * ?")
    public void uploadSeckillSkuLatest3Days(){
        log.info("秒杀商品上架");
        RLock lock = redissonClient.getLock(upload_lock);
        try {
            lock.lock(10, TimeUnit.SECONDS);
            seckillService.uploadSeckillSkuLatest3Days();
        }finally {
            lock.unlock();
        }

    }
}
