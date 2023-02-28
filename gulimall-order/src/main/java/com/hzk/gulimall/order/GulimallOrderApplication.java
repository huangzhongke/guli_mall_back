package com.hzk.gulimall.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * CachingConnectionFactory
 * RabbitTemplate
 * RabbitMessagingTemplate
 * AmqpAdmin
 */
@EnableDiscoveryClient
@MapperScan(basePackages = "com.hzk.gulimall.order.dao")
@SpringBootApplication
@EnableRabbit
public class GulimallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class, args);
    }

}
