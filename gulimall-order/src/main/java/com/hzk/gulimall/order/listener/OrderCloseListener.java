package com.hzk.gulimall.order.listener;

import com.hzk.common.constant.RabbitMqConstant;
import com.hzk.gulimall.order.entity.OrderEntity;
import com.hzk.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author kee
 * @version 1.0
 * @date 2023/3/21 15:36
 */
@Slf4j
@RabbitListener(queues = RabbitMqConstant.ORDER_RELEASE_ORDER_QUEUE)
@Service
public class OrderCloseListener {
    @Autowired
    OrderService orderService;
    @RabbitHandler
    public void listenerReleaseOrder(OrderEntity entity,Message message, Channel channel) throws IOException {
        log.info("订单超时准备关闭订单,订单号为：{}",entity.getOrderSn());
        try {
            orderService.closeOrder(entity);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }
}
