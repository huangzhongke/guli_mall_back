package com.hzk.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author kee
 * @version 1.0
 * @date 2022/9/14 10:50
 */
@Data
public class MergeVo {
    /**
     * {"purchaseId":1,"items":[1,2]}
     */
    private Long purchaseId;
    private List<Long> items;
}
