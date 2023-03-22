package com.hzk.gulimall.ware.config;

import com.hzk.common.constant.RabbitMqConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;



@Configuration
public class MyRabbitMQConfig {

    /**
     * 使用JSON序列化机制，进行消息转换
     * @return
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

     //@RabbitListener(queues = "stock.release.stock.queue")
     //public void handle(Message message) {
     //
     //}

    /**
     * 库存服务默认的交换机
     * @return
     */
    @Bean
    public Exchange stockEventExchange() {
        //String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        TopicExchange topicExchange = new TopicExchange(
                RabbitMqConstant.STOCK_EVENT_EXCHANGE,
                true, false);
        return topicExchange;
    }

    /**
     * 普通队列
     * @return
     */
    @Bean
    public Queue stockReleaseStockQueue() {
        //String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments
        Queue queue = new Queue(
                RabbitMqConstant.STOCK_RELEASE_STOCK_QUEUE,
                true, false, false);
        return queue;
    }


    /**
     * 延迟队列
     * @return
     */
    @Bean
    public Queue stockDelay() {

        HashMap<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "stock-event-exchange");
        arguments.put("x-dead-letter-routing-key", "stock.release");
        // 消息过期时间 2分钟
        arguments.put("x-message-ttl", 120000);

        Queue queue = new Queue(RabbitMqConstant.STOCK_DELAY_QUEUE,
                true, false, false,arguments);
        return queue;
    }


    /**
     * 交换机与普通队列绑定
     * @return
     */
    @Bean
    public Binding stockLocked() {
        //String destination, DestinationType destinationType, String exchange, String routingKey,
        // 			Map<String, Object> arguments
        Binding binding = new Binding(
                RabbitMqConstant.STOCK_RELEASE_STOCK_QUEUE,
                Binding.DestinationType.QUEUE,
                RabbitMqConstant.STOCK_EVENT_EXCHANGE,
                RabbitMqConstant.STOCK_RELEASE_ORDER_ROUTING_KEY,
                null);

        return binding;
    }


    /**
     * 交换机与延迟队列绑定
     * @return
     */
    @Bean
    public Binding stockLockedBinding() {
        return new Binding(
                RabbitMqConstant.STOCK_DELAY_QUEUE,
                Binding.DestinationType.QUEUE,
                RabbitMqConstant.STOCK_EVENT_EXCHANGE,
                RabbitMqConstant.STOCK_LOCKED_ROUTING_KEY,
                null);
    }


}
