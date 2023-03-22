package com.hzk.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author kee
 * @version 1.0
 * @date 2023/2/28 16:42
 */
@Data
public class OrderItemVo {
    private Long skuId;


    private String title;

    private String image;

    /**
     * 商品套餐属性
     */
    private List<String> skuAttrValues;

    private BigDecimal price;

    private Integer count;

    private BigDecimal totalPrice;


}
