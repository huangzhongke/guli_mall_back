package com.hzk.gulimall.seckill.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author kee
 * @version 1.0
 * @date 2023/3/31 9:56
 */
@Data
public class SeckillSkuVo {
    private Long id;

    private Long promotionId;

    private Long promotionSessionId;

    private Long skuId;

    private BigDecimal seckillPrice;

    private BigDecimal seckillCount;

    private BigDecimal seckillLimit;

    private Integer seckillSort;
}
