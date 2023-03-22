package com.hzk.gulimall.order.feign;

import com.hzk.common.utils.R;
import com.hzk.gulimall.order.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author kee
 * @version 1.0
 * @date 2023/3/3 16:14
 */
@FeignClient("gulimall-ware")
public interface WmsFeignService {
    @PostMapping("/ware/waresku/hasstock")
    R getSkuHasStock(@RequestBody List<Long> skuIds);

    @GetMapping("/ware/wareinfo/fare")
    R getFare(@RequestParam("addrId") Long addrId);

    @PostMapping("/ware/waresku/lock/order")
    R orderStockLock(@RequestBody WareSkuLockVo vo);
}
