package com.hzk.common.exception;

/**
 * @author kee
 * @version 1.0
 * @date 2023/3/9 10:09
 */

public class NoStockException extends RuntimeException {
    private Long skuId;

    public NoStockException(Long skuId) {
        super("商品id:" + skuId + " 没有库存");
    }
    public NoStockException(String msg) {
        super(msg);
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }
}
