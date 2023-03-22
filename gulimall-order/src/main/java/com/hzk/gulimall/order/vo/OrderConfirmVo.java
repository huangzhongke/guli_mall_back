package com.hzk.gulimall.order.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author kee
 * @version 1.0
 * @date 2023/2/28 16:44
 */

public class OrderConfirmVo {

    @Getter
    @Setter
    private List<MemberAddressVo> address;
    @Getter
    @Setter
    private List<OrderItemVo> items;
    @Getter
    @Setter
    private Integer integration;//优惠卷信息
    @Getter
    @Setter
    private String orderToken;//防重令牌
    private BigDecimal total;//订单总额
    @Getter
    @Setter
    private Map<Long,Boolean> stocks;
    private Integer totalCount;

    public Integer getTotalCount() {
        Integer count = 0;
        if (items != null){
            for (OrderItemVo item : items) {
                count+= item.getCount();
            }
        }
        return count;
    }

    public BigDecimal getTotal() {
        BigDecimal sum = new BigDecimal("0");
        if (items != null){
            for (OrderItemVo item : items) {
                BigDecimal multiply = item.getPrice().multiply(new BigDecimal(item.getCount().toString()));
                sum = sum.add(multiply);
            }
        }
        return sum;
    }

    private BigDecimal payPrice;//应付价格

    public BigDecimal getPayPrice() {
        return getTotal();
    }


}
