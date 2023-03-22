package com.hzk.gulimall.order.feign;

import com.hzk.gulimall.order.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @author kee
 * @version 1.0
 * @date 2023/3/1 9:54
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {

    @GetMapping("/member/memberreceiveaddress/{memberId}/address")
   List<MemberAddressVo> getAddress(@PathVariable("memberId") Long id);
}
