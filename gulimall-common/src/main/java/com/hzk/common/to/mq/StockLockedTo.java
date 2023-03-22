package com.hzk.common.to.mq;

import lombok.Data;

/**
 * @author kee
 * @version 1.0
 * @date 2023/3/17 16:24
 */
@Data
public class StockLockedTo {

    /** 库存工作单的id **/
    private Long id;

    /** 工作单详情的所有信息 **/
    private StockDetailTo detailTo;
}
