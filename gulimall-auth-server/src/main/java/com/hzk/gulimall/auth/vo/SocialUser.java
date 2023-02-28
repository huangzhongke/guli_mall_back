package com.hzk.gulimall.auth.vo;

import lombok.Data;

/**
 * @author kee
 * @version 1.0
 * @date 2022/12/15 14:14
 */
@Data
public class SocialUser {
    private String access_token;

    private String remind_in;

    private long expires_in;

    private String uid;

    private String isRealName;
}
