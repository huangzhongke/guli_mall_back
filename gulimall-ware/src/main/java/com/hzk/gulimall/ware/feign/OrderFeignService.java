package com.hzk.gulimall.ware.feign;

import com.hzk.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author kee
 * @version 1.0
 * @date 2023/3/20 15:30
 */
@FeignClient("gulimall-order")
public interface OrderFeignService {
    @GetMapping("/order/order/status/{orderSn}")
    R getOrderStatus(@PathVariable("orderSn") String orderSn);
}
