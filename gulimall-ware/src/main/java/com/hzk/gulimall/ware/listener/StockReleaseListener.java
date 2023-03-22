package com.hzk.gulimall.ware.listener;

import com.hzk.common.constant.RabbitMqConstant;
import com.hzk.common.to.OrderTo;
import com.hzk.common.to.mq.StockLockedTo;
import com.hzk.gulimall.ware.service.WareSkuService;
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
 * @date 2023/3/20 17:35
 */
@Slf4j
@RabbitListener(queues = RabbitMqConstant.STOCK_RELEASE_STOCK_QUEUE)
@Service
public class StockReleaseListener {
    @Autowired
    WareSkuService wareSkuService;
    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo stockLockedTo, Message message, Channel channel) throws IOException {
        log.info("库存服务收到库存解锁消息,商品ID：{}",stockLockedTo.getDetailTo().getSkuId().toString());
        try {
            wareSkuService.unlockStock(stockLockedTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }

    }
    @RabbitHandler
    public void handleOrderCloseRelease(OrderTo orderTo,Message message,Channel channel) throws IOException {
        log.info("库存服务接收到订单关闭信息 开始解除库存锁定,订单号是:{}",orderTo.getOrderSn());
        try {
            wareSkuService.unlockStock(orderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        }catch (Exception e){
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
        }

    }

}
