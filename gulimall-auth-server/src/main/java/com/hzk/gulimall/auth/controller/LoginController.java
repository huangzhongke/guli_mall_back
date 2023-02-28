package com.hzk.gulimall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.hzk.common.constant.AuthServerConstant;
import com.hzk.common.exception.BizCodeEnum;
import com.hzk.common.utils.R;
import com.hzk.common.vo.MemberResponseVo;
import com.hzk.gulimall.auth.feign.MemberFeignService;
import com.hzk.gulimall.auth.feign.ThirdPartFeignService;
import com.hzk.gulimall.auth.vo.UserLoginVo;
import com.hzk.gulimall.auth.vo.UserRegisterVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author kee
 * @version 1.0
 * @date 2022/12/7 10:47
 */
@Controller
public class LoginController {

    @Autowired
    ThirdPartFeignService thirdPartFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    MemberFeignService memberFeignService;

    @ResponseBody
    @GetMapping("/sms/sendCode")
    /**
     * 一个思路
     *  phone为 key,设置一个值为2 当作发送次数 设置这个key的过期时间
     *  这个号码没发一次减去1 当这个值为0时不能发送 10分钟内可以发送3此 如果已经为0在发送则 抛出频率过高异常
     */
    public R sendCode(@RequestParam("phone") String phone) {
        //int i = (int) (Math.random() * 9000 + 1000);//随机生成一个四位整数
        ValueOperations<String, String> ops = redisTemplate.opsForValue();

        //接口频率
        String redisCode = ops.get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (!StringUtils.isEmpty(redisCode)) {
            Long currentTime = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - currentTime < 60000) {
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
            }
        }


        //进来先判断是否有key 有key再判断是否为0
        //接口防刷
        String count = ops.get(AuthServerConstant.SMS_COUNT_CACHE_PREFIX + phone);
        if (!StringUtils.isEmpty(count)) {
            if (count.equals("0")) {
                return R.error(BizCodeEnum.SMS_COUNT_EXCEPTION.getCode(), BizCodeEnum.SMS_COUNT_EXCEPTION.getMsg());
            } else {
                String tempCount = ops.get(AuthServerConstant.SMS_COUNT_CACHE_PREFIX + phone);
                int i = Integer.parseInt(tempCount) - 1;
                ops.set(AuthServerConstant.SMS_COUNT_CACHE_PREFIX + phone, i + "");
            }
        } else {
            ops.set(AuthServerConstant.SMS_COUNT_CACHE_PREFIX + phone, "2", 10, TimeUnit.MINUTES);
        }

        int code = (int) ((Math.random() * 9 + 1) * 100000);
        String codeNum = String.valueOf(code);
        String redisStorage = codeNum + "_" + System.currentTimeMillis();

        ops.set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, redisStorage, 10, TimeUnit.MINUTES);
        thirdPartFeignService.sendCode(codeNum, phone);


        return R.ok();
    }

    @PostMapping("/regist")
    public String register(@Valid UserRegisterVo vo, BindingResult result, RedirectAttributes attributes, Model model) {
        if (result.hasErrors()) {
            //Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            Map<String, String> errors = new HashMap<>();
            for (FieldError fieldError : result.getFieldErrors()) {
                errors.put(fieldError.getField(), fieldError.getDefaultMessage());
            }
            //model.addAttribute("errors",errors);
            attributes.addFlashAttribute("errors", errors);

            //model.addAttribute("errors",errors);
            // 这个转发会造成405问题
            //return "forward:/reg.html";
            return "redirect:http://auth.gulimall.com/reg.html";
        }
        String code = vo.getCode();
        String cacheCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if (!StringUtils.isEmpty(cacheCode)) {
            if (code.equals(cacheCode.split("_")[0])) {
                redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
                //如果验证码正确 则调用远程服务注册
                R r = memberFeignService.regist(vo);
                if (r.getCode() == 0) {
                    //成功

                    return "redirect:http://auth.gulimall.com/login.html";
                } else {
                    Map<String, String> errors = new HashMap<>();
                    errors.put("msg", r.getData("msg",new TypeReference<String>() {
                    }));
                    attributes.addFlashAttribute("errors", errors);
                    return "redirect:http://auth.gulimall.com/reg.html";
                }
            } else {
                Map<String, String> errors = new HashMap<>();
                errors.put("code", "验证码不正确");
                attributes.addFlashAttribute("errors", errors);
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "验证码不正确");
            attributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }
    }


    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes, HttpSession session) {

        R r =  memberFeignService.login(vo);
        if (r.getCode() == 0){
            MemberResponseVo data = r.getData("data", new TypeReference<MemberResponseVo>() {});
            session.setAttribute(AuthServerConstant.LOGIN_USER,data);
            return "redirect:http://gulimall.com";
        }else {
            Map<String, String> errors = new HashMap<>();
            errors.put("msg",r.getData("msg",new TypeReference<String>(){}));
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/login.html";

        }

    }

    @GetMapping(value = "/login.html")
    public String loginPage(HttpSession session) {

        //从session先取出来用户的信息，判断用户是否已经登录过了
        Object attribute = session.getAttribute(AuthServerConstant.LOGIN_USER);
        //如果用户没登录那就跳转到登录页面
        if (attribute == null) {
            return "login";
        } else {
            return "redirect:http://gulimall.com";
        }

    }
}
