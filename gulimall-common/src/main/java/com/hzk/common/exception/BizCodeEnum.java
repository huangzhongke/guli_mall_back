package com.hzk.common.exception;

/**
 * @author kee
 * @version 1.0
 * @date 2022/8/29 16:35
 */
public enum BizCodeEnum {
    UNKONW_EXCEPTION(10000, "未知异常"),
    VALID_EXCEPTION(10001, "数据校验失败"),
    PRODUCT_UP_EXCEPTION(11000,"商品上架失败");

    private int code;
    private String msg;

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    BizCodeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}
