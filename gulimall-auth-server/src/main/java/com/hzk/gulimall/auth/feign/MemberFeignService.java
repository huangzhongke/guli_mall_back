package com.hzk.gulimall.auth.feign;

import com.hzk.common.utils.R;
import com.hzk.gulimall.auth.vo.SocialUser;
import com.hzk.gulimall.auth.vo.UserLoginVo;
import com.hzk.gulimall.auth.vo.UserRegisterVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author kee
 * @version 1.0
 * @date 2022/12/12 9:31
 */
@FeignClient("gulimall-member")
public interface MemberFeignService {
    @PostMapping("/member/member/regist")
    R regist(@RequestBody UserRegisterVo vo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo vo);

    @PostMapping("/member/member/oauth/login")
    R oauthLogin(@RequestBody SocialUser vo);
}
