package com.hzk.gulimall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author kee
 * @version 1.0
 * @date 2023/3/7 9:40
 */
@Data
public class FareVo {
    private MemberAddressVo address;
    private BigDecimal fare;
}
