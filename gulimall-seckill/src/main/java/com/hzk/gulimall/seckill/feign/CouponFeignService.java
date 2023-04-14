package com.hzk.gulimall.seckill.feign;

import com.hzk.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author kee
 * @version 1.0
 * @date 2023/3/30 17:10
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {
    @GetMapping("/coupon/seckillsession/latest3DaySession")
     R getLatest3DaySession();
}
