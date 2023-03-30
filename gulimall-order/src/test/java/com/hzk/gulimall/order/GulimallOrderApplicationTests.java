package com.hzk.gulimall.order;

import com.hzk.common.utils.HttpUtils;
import com.hzk.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@SpringBootTest
class GulimallOrderApplicationTests {

    @Autowired
    AmqpAdmin amqpAdmin;

    @Autowired
    RabbitTemplate rabbitTemplate;
    @Test
    void sendPost() throws Exception {
        Map<String, String> params =new HashMap<>();
        params.put("name","hello");
        HttpResponse post = HttpUtils.post("https://d3fc-125-115-204-247.ap.ngrok.io/payed/notify", null, params);
        String s = EntityUtils.toString(post.getEntity());
        System.out.println(s);
    }
    @Test
    void sendTextMessage() {
        OrderReturnReasonEntity entity = new OrderReturnReasonEntity();
        for (int i = 0; i < 10; i++) {
            entity.setId(1L);
            entity.setCreateTime(new Date());
            entity.setName("哈哈哈" + i);
            rabbitTemplate.convertAndSend("hello-java-exchange", "hello-java", entity);
            log.info("信息发送成功:{}", entity);
        }

    }

    @Test
    void createExchange() {
        /**
         * DirectExchange FanoutExchange CustomExchange TopicExchange  HeadersExchange
         *
         */
        DirectExchange directExchange = new DirectExchange("hello-java-exchange");
        amqpAdmin.declareExchange(directExchange);
        log.info("交换机hello-java-exchange 创建成功");
    }

    @Test
    void createQueue() {
        /**
         *
         */
        Queue queue = new Queue("hello-java-queue");
        amqpAdmin.declareQueue(queue);
        log.info("队列-hello-java-queue 创建成功");
    }

    @Test
    void createBinding() {
        /**
         * String destination,
         * Binding.DestinationType destinationType,
         * String exchange,
         * String routingKey,
         * @Nullable Map<String, Object> arguments
         */
        Binding binding = new Binding("hello-java-queue",
                Binding.DestinationType.QUEUE,
                "hello-java-exchange",
                "hello-java",
                null);
        amqpAdmin.declareBinding(binding);
        log.info("绑定-hello-java  创建成功");
    }
}
