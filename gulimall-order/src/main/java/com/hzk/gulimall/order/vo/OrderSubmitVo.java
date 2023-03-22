package com.hzk.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author kee
 * @version 1.0
 * @date 2023/3/7 15:24
 */
@Data
public class OrderSubmitVo {

    private Long addrId; //收货地址
    private String orderToken;//令牌
    private Integer payType;//支付方式
    private BigDecimal payPrice;//支付价格
    private String note;//备注
}
