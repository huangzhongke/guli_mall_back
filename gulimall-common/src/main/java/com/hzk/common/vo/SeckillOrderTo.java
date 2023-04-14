package com.hzk.common.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author kee
 * @version 1.0
 * @date 2023/4/13 16:27
 */
@Data
public class SeckillOrderTo {
    private String orderSn;
    private Long promotionSessionId;
    private Long skuId;
    private BigDecimal seckillPrice;
    private Integer num;
    private Long memberId;
}
