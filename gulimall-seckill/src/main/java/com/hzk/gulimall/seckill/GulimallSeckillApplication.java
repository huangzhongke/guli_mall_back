package com.hzk.gulimall.seckill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * 第一步导入依赖
 * <dependency>
 * <groupId>com.alibaba.cloud</groupId>
 * <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
 * </dependency>
 * 第二步 根据sentinel-core核心包的版本 下载对应的控制台版本
 * 第三步 配置 sentinel控制台环境
 * cloud:
 * sentinel:
 * transport:
 * port: 8719
 * dashboard: localhost:8333
 * 第四步启动控制台 java -Dserver.port=8333 -Dcsp.sentinel.dashboard.server=localhost:8333  -jar sentinel-dashboard-1.8.4.jar
 * 注：因为sentinel的配置是存在内存中的，所以一旦服务重启之后，之前设置的限流规则则会直接清空。
 * 导入actuator
 * <dependency>
 * <groupId>org.springframework.boot</groupId>
 * <artifactId>spring-boot-starter-actuator</artifactId>
 * </dependency>
 */
@EnableRedisHttpSession
@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class GulimallSeckillApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallSeckillApplication.class, args);
    }

}
