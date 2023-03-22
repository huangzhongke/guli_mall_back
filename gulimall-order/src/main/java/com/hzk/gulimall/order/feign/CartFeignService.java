package com.hzk.gulimall.order.feign;

import com.hzk.gulimall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @author kee
 * @version 1.0
 * @date 2023/3/1 11:30
 */
@FeignClient("gulimall-cart")
public interface CartFeignService {
    @GetMapping("/currentUserCartItems")
    List<OrderItemVo> getCurrentUserCartItems();
}
