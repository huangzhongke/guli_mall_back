package com.hzk.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author kee
 * @version 1.0
 * @date 2022/9/8 16:11
 */
@Data
public class SkuReductionTo {
    private Long skuId;
    private int fullCount;
    private BigDecimal discount;
    private int countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private int priceStatus;
    private List<MemberPrice> memberPrice;
}
