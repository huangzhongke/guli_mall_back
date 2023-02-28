package com.hzk.gulimall.cart;

import com.hzk.gulimall.cart.vo.CartItemVo;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@SpringBootTest
class GulimallCartApplicationTests {

    @Test
    void contextLoads() {
        CartItemVo cartItemVo = new CartItemVo();
        cartItemVo.setPrice(new BigDecimal("6399"));
        cartItemVo.setCount(2);
        System.out.println(cartItemVo.getTotalPrice());
    }

}
