package com.hzk.gulimall.order.vo;

import com.hzk.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @author kee
 * @version 1.0
 * @date 2023/3/7 16:43
 */
@Data
public class SubmitOrderResponseVo {
    private OrderEntity order;
    private Integer code; //除了0都是失败
}
