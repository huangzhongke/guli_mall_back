package com.hzk.gulimall.seckill.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author kee
 * @version 1.0
 * @date 2023/4/3 9:29
 */
@Configuration
public class RedissonConfig {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redisson(){
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.56.10:6379");
        RedissonClient redisson = Redisson.create(config);
        return  redisson;
    }

    //@Bean
    //public JedisCluster jedisCluster(){
    //    HostAndPort hostAndPort = new HostAndPort("192.168.56.10", 6379);
    //    JedisCluster jedisCluster = new JedisCluster(hostAndPort);
    //    return jedisCluster;
    //}
}
