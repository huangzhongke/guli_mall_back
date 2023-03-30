package com.hzk.gulimall.search.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author kee
 * @version 1.0
 * @date 2023/3/22 11:02
 */
@FeignClient("gulimall-cart")
public interface CartFeignService {
    @ResponseBody
    @GetMapping("/cart/count")
    String getCartItemsCount();
}
