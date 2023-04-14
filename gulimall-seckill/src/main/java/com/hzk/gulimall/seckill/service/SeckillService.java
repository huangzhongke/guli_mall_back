package com.hzk.gulimall.seckill.service;

import com.hzk.gulimall.seckill.to.SecKillSkuRedisTo;

import java.util.List;

/**
 * @author kee
 * @version 1.0
 * @date 2023/3/30 16:57
 */
public interface SeckillService {
    void uploadSeckillSkuLatest3Days();

    List<SecKillSkuRedisTo> getCurrentSeckillSkus();


    SecKillSkuRedisTo getSkuSeckillInfo(Long skuId);

    String kill(String killId, String key, Integer num);
}
