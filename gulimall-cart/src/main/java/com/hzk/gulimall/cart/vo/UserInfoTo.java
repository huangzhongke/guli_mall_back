package com.hzk.gulimall.cart.vo;

import lombok.Data;

/**
 * @author kee
 * @version 1.0
 * @date 2023/2/2 16:17
 */
@Data
public class UserInfoTo {
    private Long userId;
    private String userKey;
    private boolean tempUser = false;
}
