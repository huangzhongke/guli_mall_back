package com.hzk.gulimall.member.exception;

/**
 * @author kee
 * @version 1.0
 * @date 2022/12/12 14:13
 */
public class PhoneExistException extends RuntimeException {
    public PhoneExistException() {
        super("手机号已存在");
    }
}
