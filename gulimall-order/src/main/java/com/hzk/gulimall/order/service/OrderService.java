package com.hzk.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hzk.common.utils.PageUtils;
import com.hzk.common.vo.SeckillOrderTo;
import com.hzk.gulimall.order.entity.OrderEntity;
import com.hzk.gulimall.order.vo.*;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author kee
 * @email 1059308740@qq.com
 * @date 2022-08-15 09:32:02
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo orderConfirm() throws ExecutionException, InterruptedException;

    SubmitOrderResponseVo submitOrder(OrderSubmitVo vo);

    OrderEntity getOrderByOrderSn(String orderSn);

    void closeOrder(OrderEntity entity);

    PayVo getPayVoByOrderSn(String orderSn);

    PageUtils queryPageWithItem(Map<String, Object> params);



    String handlePayResult(PayAsyncVo asyncVo);

    void createSeckillOrder(SeckillOrderTo to);
}

