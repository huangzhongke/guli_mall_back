package com.hzk.gulimall.seckill.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.hzk.common.utils.R;
import com.hzk.common.vo.MemberResponseVo;
import com.hzk.common.vo.SeckillOrderTo;
import com.hzk.gulimall.seckill.feign.CouponFeignService;
import com.hzk.gulimall.seckill.feign.ProductFeignService;
import com.hzk.gulimall.seckill.feign.WmsFeignService;
import com.hzk.gulimall.seckill.interceptor.LoginUserInterceptor;
import com.hzk.gulimall.seckill.service.SeckillService;
import com.hzk.gulimall.seckill.to.SecKillSkuRedisTo;
import com.hzk.gulimall.seckill.vo.SeckillSessionsWithSkusVo;
import com.hzk.gulimall.seckill.vo.SkuInfoVo;
import org.apache.commons.lang.StringUtils;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author kee
 * @version 1.0
 * @date 2023/3/30 16:57
 */
@Service
public class SeckillServiceImpl implements SeckillService {
    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    WmsFeignService wmsFeignService;

    private static final String SESSIONS_CACHE_PREFIX = "seckill:sessions:";
    private static final String SKUKILL_CACHE_PREFIX = "seckill:skus";
    private static final String SKU_STOCK_SEMAPHORE = "seckill:stock:"; //后面跟商品随机码

    /**
     *
     * 商家秒杀商品前查询库存系统 看是否满足秒杀商品的数量
     */
    @Override
    public void uploadSeckillSkuLatest3Days() {
        R r = couponFeignService.getLatest3DaySession();
        if (r.getCode() == 0) {
            List<SeckillSessionsWithSkusVo> seckillSessionsWithSkusVo = r.getData(new TypeReference<List<SeckillSessionsWithSkusVo>>() {
            });
            //缓存活动信息
            saveSessionInfos(seckillSessionsWithSkusVo);
            //缓存活动关联商品信息
            saveSessionSkuInfos(seckillSessionsWithSkusVo);
        }
    }

    @Override
    public List<SecKillSkuRedisTo> getCurrentSeckillSkus() {
        //查出当前时间的秒杀场次
        long time = new Date().getTime();
        Set<String> keys = redisTemplate.keys(SESSIONS_CACHE_PREFIX + "*");

        for (String key : keys) {
            String replace = key.replace(SESSIONS_CACHE_PREFIX, "");
            String[] split = replace.split("_");
            Long start = Long.parseLong(split[0]);
            Long end = Long.parseLong(split[1]);
            if (time >= start && time <= end) {
                List<String> range = redisTemplate.opsForList().range(key, -100, 100);
                BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
                List<String> objects = ops.multiGet(range);
                if (objects != null && objects.size() > 0) {
                    List<SecKillSkuRedisTo> collect = objects.stream().map(item -> {
                        SecKillSkuRedisTo redisTo = JSONObject.parseObject(item, SecKillSkuRedisTo.class);
                        return redisTo;
                    }).collect(Collectors.toList());
                    return collect;
                }
                break;
            }

        }
        //返回该场次的所有商品信息
        return null;
    }

    @Override
    public SecKillSkuRedisTo getSkuSeckillInfo(Long skuId) {
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);

        Set<String> keys = ops.keys();
        if (keys != null && keys.size() > 0) {
            String reg = "\\d_" + skuId;
            for (String key : keys) {
                if (Pattern.matches(reg, key)) {
                    String data = ops.get(key);
                    SecKillSkuRedisTo to = JSONObject.parseObject(data, SecKillSkuRedisTo.class);
                    long current = System.currentTimeMillis();
                    if (!(current >= to.getStartTime() && current <= to.getEndTime())) {
                        to.setRandomCode(null);
                    }
                    return to;
                }
            }
        }

        return null;
    }

    @Override
    public String kill(String killId, String key, Integer num) {
        MemberResponseVo responseVo = LoginUserInterceptor.threadLocal.get();
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
        String json = ops.get(killId);
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        SecKillSkuRedisTo redisTo = JSONObject.parseObject(json, SecKillSkuRedisTo.class);
        Long currentTime = System.currentTimeMillis();
        Long startTime = redisTo.getStartTime();
        Long endTime = redisTo.getEndTime();
        //判断时间
        if (!(currentTime >= startTime && currentTime <= endTime)) {
            return null;
        }
        //判断场次商品 和 随机码
        String randomCode = redisTo.getRandomCode();
        String skuId = redisTo.getPromotionSessionId() + "_" + redisTo.getSkuId();
        if (!(randomCode.equals(key) && skuId.equals(killId))) {
            return null;
        }
        //判断限购数量 购买数量大于限购数量返回null
        if (num > redisTo.getSeckillLimit().intValue()) {
            return null;
        }
        //验证是否已经购买过 在redis中设置一个占位符
        String redisKey = responseVo.getId() + "_" + skuId;
        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(), endTime - startTime, TimeUnit.MILLISECONDS);
        //true表示占位成功没有购买过 false则是购买过
        if (!aBoolean) {
            return null;
        }
        //拿信号量
        RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + key);
        boolean b = semaphore.tryAcquire(num);
        //秒杀成功
        //快速下单给MQ发消息
        if (!b) {
            return null;
        }
        String timeId = IdWorker.getTimeId();
        SeckillOrderTo orderTo = new SeckillOrderTo();
        orderTo.setOrderSn(timeId);
        orderTo.setSeckillPrice(redisTo.getSeckillPrice());
        orderTo.setNum(num);
        orderTo.setSkuId(redisTo.getSkuId());
        orderTo.setPromotionSessionId(redisTo.getPromotionSessionId());
        orderTo.setMemberId(responseVo.getId());

        rabbitTemplate.convertAndSend("order-event-exchange", "order.seckill.order", orderTo);
        return timeId;

    }

    private void saveSessionInfos(List<SeckillSessionsWithSkusVo> sessions) {

        sessions.stream().forEach(session -> {
            Long startTime = session.getStartTime().getTime();
            Long endTime = session.getEndTime().getTime();
            String key = SESSIONS_CACHE_PREFIX + startTime + "_" + endTime;
            Boolean hasKey = redisTemplate.hasKey(key);
            //
            if (!hasKey) {

                List<String> collect = session.getRelationSkus().stream()
                        .map(item -> item.getPromotionSessionId() + "_" + item.getSkuId().toString())
                        .collect(Collectors.toList());
                if (collect != null && collect.size() > 0) {
                    redisTemplate.opsForList().leftPushAll(key, collect);
                }
            }

        });

    }

    private void saveSessionSkuInfos(List<SeckillSessionsWithSkusVo> sessions) {
        sessions.stream().forEach(session -> {
            BoundHashOperations operations = redisTemplate.boundHashOps(SKUKILL_CACHE_PREFIX);
            session.getRelationSkus().stream().forEach(seckillSkuVo -> {
                String key = seckillSkuVo.getPromotionSessionId() + "_" + seckillSkuVo.getSkuId();
                if (!operations.hasKey(key)) {
                    Long startTime = session.getStartTime().getTime();
                    Long endTime = session.getEndTime().getTime();
                    SecKillSkuRedisTo secKillSkuRedisTo = new SecKillSkuRedisTo();
                    BeanUtils.copyProperties(seckillSkuVo, secKillSkuRedisTo);
                    secKillSkuRedisTo.setStartTime(startTime);
                    secKillSkuRedisTo.setEndTime(endTime);
                    R r = productFeignService.getSkuInfo(seckillSkuVo.getSkuId());
                    if (r.getCode() == 0) {
                        SkuInfoVo skuInfoVo = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                        });
                        secKillSkuRedisTo.setSkuInfoVo(skuInfoVo);
                    }
                    String token = UUID.randomUUID().toString().replace("-", "");
                    secKillSkuRedisTo.setRandomCode(token);
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + token);
                    semaphore.trySetPermits(seckillSkuVo.getSeckillCount().intValue());
                    String s = JSONObject.toJSONString(secKillSkuRedisTo);
                    operations.put(key, s);

                    //SeckillSkuLockVo seckillSkuLockVo = new SeckillSkuLockVo();
                    //seckillSkuLockVo.setSkuId(secKillSkuRedisTo.getSkuId());
                    //seckillSkuLockVo.setNum(secKillSkuRedisTo.getSeckillCount().intValue());
                    //wmsFeignService.seckillSkuLock(seckillSkuLockVo);

                }
            });
        });

    }
}
