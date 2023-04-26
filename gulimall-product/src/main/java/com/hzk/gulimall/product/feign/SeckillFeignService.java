package com.hzk.gulimall.product.feign;

import com.hzk.common.utils.R;
import com.hzk.gulimall.product.feign.fallback.SeckillFeignServiceFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author kee
 * @version 1.0
 * @date 2023/4/6 15:08
 */
@FeignClient(value = "gulimall-seckill", fallback = SeckillFeignServiceFallback.class)
public interface SeckillFeignService {
    @GetMapping("/sku/seckill/{skuId}")
    R getSkuSeckillInfo(@PathVariable("skuId") Long skuId);
}
