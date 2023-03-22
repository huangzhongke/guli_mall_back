package com.hzk.gulimall.ware;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@SpringBootTest
class GulimallWareApplicationTests {

    @Test
    void contextLoads() {
        String str = "1234567";
        char c = str.charAt(str.length() - 1);
        System.out.println(String.valueOf(c));
        System.out.println(c);
        System.out.println(new BigDecimal(String.valueOf(c)));
    }

}
