package com.hzk.gulimall.member.exception;

/**
 * @author kee
 * @version 1.0
 * @date 2022/12/12 14:14
 */
public class UsernameExistException extends RuntimeException{
    public UsernameExistException() {
        super("用户名已存在");
    }
}
