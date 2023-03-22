package com.hzk.gulimall.order.to;

import com.hzk.gulimall.order.entity.OrderEntity;
import com.hzk.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author kee
 * @version 1.0
 * @date 2023/3/8 10:02
 */
@Data
public class OrderCreateTo {

    private OrderEntity order;
    private List<OrderItemEntity> orderItems;
    private BigDecimal fare;
    private BigDecimal payPrice;
}
