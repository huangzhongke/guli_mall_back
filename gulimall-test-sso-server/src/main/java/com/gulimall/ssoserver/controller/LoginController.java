package com.gulimall.ssoserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * @author kee
 * @version 1.0
 * @date 2022/12/21 15:06
 */
@Controller
public class LoginController {
    @Autowired
    StringRedisTemplate redisTemplate;
    @ResponseBody
    @GetMapping("/userInfo")
    public String userInfo(@RequestParam("token") String token){
        return redisTemplate.opsForValue().get(token);
    }

    @GetMapping("/login.html")
    public String login(@RequestParam("redirect_url") String url,
                        Model model,
                        @CookieValue(value = "sso_token",required = false) String ssoToken) {
        if (!StringUtils.isEmpty(ssoToken)){
            return "redirect:" + url + "?token=" + ssoToken;
        }
        model.addAttribute("url", url);
        return "login";
    }

    @PostMapping("/doLogin")
    public String doLogin(@RequestParam("username") String username,
                          @RequestParam("password") String password,
                          @RequestParam("url") String url,
                          HttpServletResponse response) {
        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
            String uuid = UUID.randomUUID().toString().replace("-", "");
            redisTemplate.opsForValue().set(uuid, username);
            Cookie sso_token = new Cookie("sso_token", uuid);
            response.addCookie(sso_token);
            return "redirect:" + url + "?token=" + uuid;
        } else {
            return "";
        }

    }
}
