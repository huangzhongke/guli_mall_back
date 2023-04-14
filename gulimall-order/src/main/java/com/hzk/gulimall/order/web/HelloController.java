package com.hzk.gulimall.order.web;

import com.hzk.common.constant.RabbitMqConstant;
import com.hzk.gulimall.order.entity.OrderEntity;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.UUID;

/**
 * @author kee
 * @version 1.0
 * @date 2023/2/28 10:02
 */
@Controller
public class HelloController {

    @Autowired
    RabbitTemplate rabbitTemplate;
    @ResponseBody
    @GetMapping("/test/createOrder")
    public String createOrder(){
        OrderEntity entity = new OrderEntity();
        entity.setOrderSn(UUID.randomUUID().toString());
        entity.setModifyTime(new Date());
        rabbitTemplate.convertAndSend("order-event-exchange", RabbitMqConstant.ORDER_SECKILL_ORDER_ROUTING_KEY,entity);
        return "ok";
    }

    @GetMapping("/{page}.html")
    public String hello(@PathVariable("page") String page) {
        return page;
    }

}
