package com.example.thirdpart.controller;

import com.example.thirdpart.component.SmsComponent;
import com.hzk.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author kee
 * @version 1.0
 * @date 2022/12/8 10:15
 */
@RestController
@RequestMapping("/sms")
public class SmsSendController {
    @Autowired
    SmsComponent smsComponent;
    @PostMapping("/sendCode")
    public R sendCode(@RequestParam("code") String code, @RequestParam("mobile") String mobile){
        smsComponent.sendSmsCode(code,mobile);
        return  R.ok();
    }
}
