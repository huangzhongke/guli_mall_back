package com.hzk.gulimall.seckill.to;

import com.hzk.gulimall.seckill.vo.SkuInfoVo;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author kee
 * @version 1.0
 * @date 2023/3/31 15:18
 */
@Data
public class SecKillSkuRedisTo {
    private Long id;
    private Long promotionId;
    private Long promotionSessionId;
    private Long skuId;
    private BigDecimal seckillPrice;
    private BigDecimal seckillCount;
    private BigDecimal seckillLimit;
    private Integer seckillSort;
    private SkuInfoVo skuInfoVo;
    private String randomCode;//秒杀商品的随机码
    //当前商品秒杀的开始时间
    private Long startTime;
    //当前商品秒杀的结束时间
    private Long endTime;
}
