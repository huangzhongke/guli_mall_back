package com.hzk.gulimall.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzk.common.utils.PageUtils;
import com.hzk.common.utils.Query;
import com.hzk.gulimall.order.dao.OrderReturnReasonDao;
import com.hzk.gulimall.order.entity.OrderEntity;
import com.hzk.gulimall.order.entity.OrderReturnReasonEntity;
import com.hzk.gulimall.order.service.OrderReturnReasonService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;


@RabbitListener(queues = {"hello-java-queue"})
@Service("orderReturnReasonService")
public class OrderReturnReasonServiceImpl extends ServiceImpl<OrderReturnReasonDao, OrderReturnReasonEntity> implements OrderReturnReasonService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderReturnReasonEntity> page = this.page(
                new Query<OrderReturnReasonEntity>().getPage(params),
                new QueryWrapper<OrderReturnReasonEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * @param message 有消息头+消息体
     * @param content 消息类型
     * @param channel 通道
     */


    //public void receiveMessage(Message message,
    //                           OrderReturnReasonEntity content,
    //                           Channel channel){
    //    MessageProperties properties = message.getMessageProperties();
    //    byte[] body = message.getBody();
    //    System.out.println("接收到了消息 " + content);
    //
    //}

    /**
     * @RabbitListener(queues = {"hello-java-queue"}) 可以写在方法和类上
     * @RabbitHandler 只能写在方法上
     */

    @RabbitHandler
    public void receiveMessage(Message message,
                               OrderReturnReasonEntity content,
                               Channel channel) {

        System.out.println("RabbitListener接收到了消息 " + content);
        byte[] body = message.getBody();
        MessageProperties messageProperties = message.getMessageProperties();
        long deliveryTag = messageProperties.getDeliveryTag();

        //接收消息
        try {
            //第一个参数是Message中一个自增的类似id的东西，第二个参数表示是否批量接收 multiple
            channel.basicAck(deliveryTag,false);
        } catch (IOException e) {
            //网络中断或者服务器宕机
        }
        //拒绝消息
        //   channel.basicReject();
        // delivertag mutilple requeue = true表示拒收重新返回队列 false表示拒收并且丢弃
        //   channel.basicNack();

        System.out.println("deliveryTag===>" + deliveryTag);
    }


    //@RabbitListener(queues = {"hello-java-queue"})
    //@RabbitHandler
    public void receiveMessage(OrderEntity content) {
        System.out.println("RabbitHandler " + content);

    }
}