package com.hzk.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author kee
 * @version 1.0
 * @date 2023/3/8 17:06
 */
@Data
public class WareSkuLockVo {
    private String orderSn;
    private List<OrderItemVo> locks;
}
