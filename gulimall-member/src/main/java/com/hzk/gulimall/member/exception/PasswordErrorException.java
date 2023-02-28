package com.hzk.gulimall.member.exception;

/**
 * @author kee
 * @version 1.0
 * @date 2022/12/13 9:58
 */
public class PasswordErrorException extends RuntimeException {
    public PasswordErrorException() {
        super("密码错误");
    }
}
