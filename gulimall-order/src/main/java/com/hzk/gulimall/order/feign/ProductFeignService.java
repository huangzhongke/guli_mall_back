package com.hzk.gulimall.order.feign;

import com.hzk.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author kee
 * @version 1.0
 * @date 2023/3/8 13:51
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {
    @GetMapping("/product/spuinfo/skuId/{id}")
    R getSpuBySkuId(@PathVariable("id") Long skuId);
}
