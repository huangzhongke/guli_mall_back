package com.hzk.gulimall.auth.controller;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.hzk.common.constant.AuthServerConstant;
import com.hzk.common.utils.R;
import com.hzk.gulimall.auth.feign.MemberFeignService;
import com.hzk.common.vo.MemberResponseVo;
import com.hzk.gulimall.auth.vo.SocialUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @author kee
 * @version 1.0
 * @date 2022/12/14 16:23
 */
@Slf4j
@Controller
public class OAuth2Controller {
    @Autowired
    MemberFeignService memberFeignService;

    @GetMapping("/oauth2.0/weibo/success")
    public String weibo(@RequestParam("code") String code, HttpSession session) throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("client_id", "77292125");
        map.put("client_secret", "3cbb9862304760bf8c7a441aeceefff8");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://auth.gulimall.com/oauth2.0/weibo/success");
        map.put("code", code);
        //String url = "https://api.weibo.com/oauth2/access_token";
        //HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "post", new HashMap<>(), map, new HashMap<>());
        //HttpResponse response = HttpUtils.post(url, null, map);

        String url = "https://api.weibo.com/oauth2/access_token?client_id=77292125&client_secret=3cbb9862304760bf8c7a441aeceefff8&grant_type=authorization_code&redirect_uri=http://auth.gulimall.com/oauth2.0/weibo/success&code=" + code;
        HttpPost post = new HttpPost(url);
        CloseableHttpResponse response = null;

        response = HttpClients.createDefault().execute(post);
        if (response.getStatusLine().getStatusCode() == 200) {
            String s = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSONObject.parseObject(s, SocialUser.class);
            R oauthLogin = memberFeignService.oauthLogin(socialUser);
            if (oauthLogin.getCode() == 0) {
                MemberResponseVo responseVo = oauthLogin.getData("data", new TypeReference<MemberResponseVo>() {
                });
                log.info("登陆成功，用户信息:{}", responseVo);
                session.setAttribute(AuthServerConstant.LOGIN_USER,responseVo);
                return "redirect:http://gulimall.com";
            } else {
                return "redirect:http://auth.gulimall.com/login.html";
            }
        } else {
            return "redirect:http://auth.gulimall.com/login.html";
        }

    }
}
