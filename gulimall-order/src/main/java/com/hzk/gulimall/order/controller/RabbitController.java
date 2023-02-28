package com.hzk.gulimall.order.controller;

import com.hzk.gulimall.order.entity.OrderEntity;
import com.hzk.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;

/**
 * @author kee
 * @version 1.0
 * @date 2023/2/24 15:05
 */
@Slf4j
@RestController
public class RabbitController {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @GetMapping("/sendMsg")
    public String sendMsg(@RequestParam(value = "num",defaultValue = "10") Integer num) {
        OrderReturnReasonEntity entity = new OrderReturnReasonEntity();
        if (num <= 0) {
            return "0";
        } else {
            for (int i = 0; i < num; i++) {
                if (i % 2 == 0) {
                    entity.setCreateTime(new Date());
                    entity.setName("哈哈哈" + i);
                    rabbitTemplate.convertAndSend("hello-java-exchange", "hello-java", entity);
                } else {
                    OrderEntity orderEntity = new OrderEntity();
                    orderEntity.setOrderSn(UUID.randomUUID().toString());
                    rabbitTemplate.convertAndSend("hello-java-exchange", "hello-java22", orderEntity);
                }

                log.info("信息发送成功:{}", i);
            }
            return "ok";
        }
    }
}
