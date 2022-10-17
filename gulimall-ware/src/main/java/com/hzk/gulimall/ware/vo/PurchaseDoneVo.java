package com.hzk.gulimall.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author kee
 * @version 1.0
 * @date 2022/9/21 15:01
 */
@Data
public class PurchaseDoneVo {
    @NotNull
    private Long id;
    private List<PurchaseDoneItemVo> items;
}
