package com.hzk.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hzk.common.utils.PageUtils;
import com.hzk.gulimall.order.entity.OrderEntity;

import java.util.Map;

/**
 * 订单
 *
 * @author kee
 * @email 1059308740@qq.com
 * @date 2022-08-15 09:32:02
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

