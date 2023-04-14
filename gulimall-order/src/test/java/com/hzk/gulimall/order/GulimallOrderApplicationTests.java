package com.hzk.gulimall.order;

import com.hzk.common.constant.RabbitMqConstant;
import com.hzk.common.utils.HttpUtils;
import com.hzk.common.vo.SeckillOrderTo;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

//@Slf4j
//@SpringBootTest
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
        SeckillOrderTo to = new SeckillOrderTo();

        to.setOrderSn(UUID.randomUUID().toString().replace("-",""));
        to.setNum(1);
        to.setSkuId(13L);
        rabbitTemplate.convertAndSend(RabbitMqConstant.ORDER_EVENT_EXCHANGE,RabbitMqConstant.ORDER_SECKILL_ORDER_ROUTING_KEY,to);

    }

    @Test
    void createExchange() {
        /**
         * DirectExchange FanoutExchange CustomExchange TopicExchange  HeadersExchange
         *
         */
        DirectExchange directExchange = new DirectExchange("hello-java-exchange");
        amqpAdmin.declareExchange(directExchange);
        //log.info("交换机hello-java-exchange 创建成功");
    }

    @Test
    void createQueue() {
        /**
         *
         */
        Queue queue = new Queue("hello-java-queue");
        amqpAdmin.declareQueue(queue);
        //log.info("队列-hello-java-queue 创建成功");
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
        //log.info("绑定-hello-java  创建成功");
    }
}
