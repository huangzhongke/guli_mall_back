package com.hzk.gulimall.auth.feign;

import com.hzk.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author kee
 * @version 1.0
 * @date 2022/12/8 11:23
 */
@FeignClient("gulimall-third-part")
public interface ThirdPartFeignService {
    /**
     *  给第三方服务发送验证码
     * @param code 生成的验证码
     * @param mobile 手机号
     * @return
     */
    @PostMapping("/sms/sendCode")
    public R sendCode(@RequestParam("code") String code, @RequestParam("mobile") String mobile);
}
