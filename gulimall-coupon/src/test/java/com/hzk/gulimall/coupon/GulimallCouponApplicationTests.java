package com.hzk.gulimall.coupon;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class GulimallCouponApplicationTests {

    @Test
    void contextLoads() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "jack");
        map.forEach((key, value) -> {
            System.out.println(key + ":" + value);
        });
    }

}
