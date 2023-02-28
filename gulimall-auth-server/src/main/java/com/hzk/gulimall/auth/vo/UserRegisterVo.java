package com.hzk.gulimall.auth.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * @author kee
 * @version 1.0
 * @date 2022/12/8 17:01
 */
@Data
public class UserRegisterVo {

    @NotEmpty(message = "用户名不能为空")
    @Length(min = 6,max = 18,message = "用户名长度最小6位,最大18位")
    private String userName;

    @NotEmpty(message = "密码不能为空")
    @Length(min = 6,max = 18,message = "密码长度最小6位,最大18位")
    private String password;

    @NotEmpty(message = "手机号必须填写")
    @Pattern(regexp = "^[1]([3-9])\\d{9}$",message = "手机号格式不正确")
    private String phone;

    @NotEmpty(message = "验证码必须填写")
    private String code;
}
