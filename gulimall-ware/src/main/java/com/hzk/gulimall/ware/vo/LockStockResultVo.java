package com.hzk.gulimall.ware.vo;

import lombok.Data;

/**
 * @author kee
 * @version 1.0
 * @date 2023/3/8 17:14
 */
@Data
public class LockStockResultVo {

    private Long skuId;
    private Integer num;
    private Boolean locked;
}
