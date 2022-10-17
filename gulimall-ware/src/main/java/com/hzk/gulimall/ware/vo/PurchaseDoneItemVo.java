package com.hzk.gulimall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author kee
 * @version 1.0
 * @date 2022/9/21 15:01
 */
@Data
public class PurchaseDoneItemVo {
    @NotNull
    private Long itemId;
    private Integer status;
    private String reason;
}
