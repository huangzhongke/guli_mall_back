package com.hzk.gulimall.order.listener;

import com.hzk.common.constant.RabbitMqConstant;
import com.hzk.common.vo.SeckillOrderTo;
import com.hzk.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author kee
 * @version 1.0
 * @date 2023/4/11 16:39
 */
@Slf4j
@RabbitListener(queues = RabbitMqConstant.ORDER_SECKILL_ORDER_QUEUE)
@Component
public class OrderSeckillListener {

    @Autowired
    OrderService orderService;
    @RabbitHandler
    public void seckillOrder(SeckillOrderTo to, Message message, Channel channel) throws IOException {
        log.info("准备创建秒杀单的详细信息：{}",to);
        try {
            orderService.createSeckillOrder(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }
    }

}
