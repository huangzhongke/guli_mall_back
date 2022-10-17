package com.hzk.gulimall.product.vo;

import lombok.Data;

/**
 * @author kee
 * @version 1.0
 * @date 2022/9/5 14:30
 */
@Data
public class AttrRespVo extends AttrVo {
    private String groupName;
    private String catelogName;
    private Long[] catelogPath;
}
