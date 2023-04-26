package com.hzk.common.exception;

/**
 * @author kee
 * @version 1.0
 * @date 2022/8/29 16:35
 */
public enum BizCodeEnum {
    UNKONW_EXCEPTION(10000, "未知异常"),
    VALID_EXCEPTION(10001, "数据校验失败"),
    SMS_CODE_EXCEPTION(10002, "短信验证码频率太高"),
    SMS_COUNT_EXCEPTION(10003, "短信发送次数过多请十分钟后在进行尝试"),
    TOO_MANY_REQUEST(10004, "请求频率过快，请稍后"),
    PRODUCT_UP_EXCEPTION(11000,"商品上架失败"),
    USER_EXIST_EXCEPTION(15001,"用户存在"),
    PHONE_EXIST_EXCEPTION(15001,"手机号存在"),
    NO_STOCK_EXCEPTION(21000,"商品库存不足"),
    LOGINACCOUNT_PASSWORD_INVALID_EXCEPTION(15002,"账号或密码错误");

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
