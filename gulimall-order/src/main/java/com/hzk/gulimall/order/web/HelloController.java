package com.hzk.gulimall.order.web;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author kee
 * @version 1.0
 * @date 2023/2/28 10:02
 */
@Controller
public class HelloController {

    @Autowired
    RabbitTemplate rabbitTemplate;
    //@ResponseBody
    //@GetMapping("/test/createOrder")
    //public String createOrder(){
    //    OrderEntity entity = new OrderEntity();
    //    entity.setOrderSn(UUID.randomUUID().toString());
    //    entity.setModifyTime(new Date());
    //    rabbitTemplate.convertAndSend("order-event-exchange","order.create.order",entity);
    //    return "ok";
    //}

    @GetMapping("/{page}.html")
    public String hello(@PathVariable("page") String page) {
        return page;
    }
}
