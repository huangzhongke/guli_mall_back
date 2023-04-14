package com.hzk.gulimall.seckill.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author kee
 * @version 1.0
 * @date 2023/3/31 9:56
 */
@Data
public class SeckillSessionsWithSkusVo {

    private Long id;
    private String name;
    private Date startTime;
    private Date endTime;
    private Integer status;
    private Date createTime;
    private List<SeckillSkuVo> relationSkus;
}
