package com.hzk.gulimall.ware.feign;

import com.hzk.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author kee
 * @version 1.0
 * @date 2023/3/6 15:59
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {
    @RequestMapping("/member/memberreceiveaddress/info/{id}")
     R addrInfo(@PathVariable("id") Long id);
}
