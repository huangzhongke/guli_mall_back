package com.hzk.common.to;

import lombok.Data;

/**
 * @author kee
 * @version 1.0
 * @date 2022/9/30 14:05
 */
@Data
public class SkuHasStockVo {
    private Long skuId;
    private Boolean hasStock;
}
