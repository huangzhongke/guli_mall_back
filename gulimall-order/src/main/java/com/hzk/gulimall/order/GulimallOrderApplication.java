package com.hzk.gulimall.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * CachingConnectionFactory
 * RabbitTemplate
 * RabbitMessagingTemplate
 * AmqpAdmin
 *
 *
 * 本地事务失效问题
 * 同一个对象内事务方法互调默认失效。原因是绕过了代理对象，本质上事务是通过代理对象来控制的
 * 解决：使用代理对象来调用事务方法
 * 1） 引入 spring-boot-starter-aop
 * 2) seata分布式事务 ,在每个微服务下面创建undo_log表
 */

//@EnableAspectJAutoProxy(exposeProxy = true)
@EnableTransactionManagement
@EnableFeignClients
@EnableRedisHttpSession
@EnableDiscoveryClient
@MapperScan(basePackages = "com.hzk.gulimall.order.dao")
@SpringBootApplication
@EnableRabbit
public class GulimallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class, args);
    }

}
