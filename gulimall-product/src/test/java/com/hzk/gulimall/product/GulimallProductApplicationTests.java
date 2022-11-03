package com.hzk.gulimall.product;

import com.hzk.gulimall.product.service.BrandService;
import com.hzk.gulimall.product.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;
    @Test
    void contextLoads() {
        System.out.println(redissonClient);

    }

    @Test
    void testFindCatelogPath(){
        Long[] catelogPath = categoryService.findCatelogPath(225L);
        for (Long aLong : catelogPath) {
            System.out.println(aLong);
        }
    }


}
