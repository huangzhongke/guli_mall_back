package com.hzk.gulimall.seckill.feign;

import com.hzk.common.utils.R;
import com.hzk.gulimall.seckill.vo.SeckillSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author kee
 * @version 1.0
 * @date 2023/4/14 11:09
 */
@FeignClient("gulimall-ware")
public interface WmsFeignService {
    @PostMapping("/hasstock")
    R getSkuHasStock(@RequestBody List<Long> skuIds);

    @PostMapping("/lock/seckill")
     R seckillSkuLock(@RequestBody SeckillSkuLockVo vo);
}
