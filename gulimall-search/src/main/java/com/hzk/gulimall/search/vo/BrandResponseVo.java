package com.hzk.gulimall.search.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author kee
 * @version 1.0
 * @date 2022/11/17 11:18
 */
@Data
public class BrandResponseVo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 品牌id
     */
    private Long brandId;
    /**
     * 品牌名
     */

    private String name;

}
