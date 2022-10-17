package com.hzk.gulimall.product.feign;

import com.hzk.common.to.SkuReductionTo;
import com.hzk.common.to.SpuBoundTo;
import com.hzk.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author kee
 * @version 1.0
 * @date 2022/9/8 15:02
 */
@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);

    @PostMapping("/coupon/skufullreduction/saveReduction")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);


}
