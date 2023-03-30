package com.hzk.gulimall.member.feign;

import com.hzk.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @author kee
 * @version 1.0
 * @date 2023/3/27 9:52
 */
@FeignClient("gulimall-order")
public interface OrderFeignService {
    @PostMapping("/order/order/listWithItem")
     R listWithItem(@RequestBody Map<String, Object> params);
}
