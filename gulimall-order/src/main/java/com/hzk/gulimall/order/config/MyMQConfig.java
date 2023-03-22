package com.hzk.gulimall.order.config;

import com.hzk.common.constant.RabbitMqConstant;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kee
 * @version 1.0
 * @date 2023/3/17 10:27
 */
@Configuration
public class MyMQConfig {
    @Autowired
    AmqpAdmin amqpAdmin;

    /**
     * 队列一旦创建 修改属性之后不会改变，只有删除之后重新创建
     */
    //@RabbitListener(queues = "order.release.order.queue")
    //public void listener(OrderEntity entity, Message message, Channel channel) throws IOException {
    //    System.out.println("延时队列接收到信息释放订单 OrderID:" + entity.getOrderSn());
    //    channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    //}

    @Bean
    public Queue orderDelayQueue(){
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange","order-event-exchange");
        arguments.put("x-dead-letter-routing-key","order.release.order");
        arguments.put("x-message-ttl",60000);
        /**
         * String name,
         * boolean durable,
         * boolean exclusive,
         * boolean autoDelete,
         * @Nullable Map<String, Object> arguments
         */
        Queue queue = new Queue(RabbitMqConstant.ORDER_DELAY_QUEUE,true, false, false, arguments);
        return queue;
    }

    @Bean
    public Queue orderReleaseOrderQueue(){
        Queue queue = new Queue(RabbitMqConstant.ORDER_RELEASE_ORDER_QUEUE,true, false, false, null);
        return queue;
    }

    @Bean
    public Exchange orderEventExchange(){
        /**
         * String name,
         * boolean durable, 是否保持持久性
         * boolean autoDelete 是否自动删除
         */
        TopicExchange exchange = new TopicExchange(RabbitMqConstant.ORDER_EVENT_EXCHANGE, true, false);
        return exchange;
    }

    @Bean
    public Binding orderDelayBinding(){
        /**
         * String destination,
         * Binding.DestinationType destinationType,
         * String exchange,
         * String routingKey,
         * @Nullable Map<String, Object> arguments
         */
        Binding binding = new Binding(RabbitMqConstant.ORDER_DELAY_QUEUE, Binding.DestinationType.QUEUE,"order-event-exchange","order.create.order",null);
        return binding;
    }

    @Bean
    public Binding orderReleaseOrderBinding(){
        Binding binding = new Binding(RabbitMqConstant.ORDER_RELEASE_ORDER_QUEUE,
                Binding.DestinationType.QUEUE,
                RabbitMqConstant.ORDER_EVENT_EXCHANGE,
                RabbitMqConstant.ORDER_RELEASE_ORDER_ROUTING_KEY,
                null);
        return binding;
    }

    @Bean
    public Binding orderReleaseOtherBinding(){
        Binding binding = new Binding(RabbitMqConstant.STOCK_RELEASE_STOCK_QUEUE,
                Binding.DestinationType.QUEUE,
                RabbitMqConstant.ORDER_EVENT_EXCHANGE,
                RabbitMqConstant.ORDER_RELEASE_OTHER_ROUTING_KEY,
                null);
        return binding;
    }
}
