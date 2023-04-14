package com.hzk.gulimall.product.feign;

import com.hzk.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author kee
 * @version 1.0
 * @date 2023/4/6 15:08
 */
@FeignClient("gulimall-seckill")
public interface SeckillFeignService {
    @GetMapping("/sku/seckill/{skuId}")
    R getSkuSeckillInfo(@PathVariable("skuId") Long skuId);
}
