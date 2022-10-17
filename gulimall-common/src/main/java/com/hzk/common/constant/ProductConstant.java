package com.hzk.common.constant;

/**
 * @author kee
 * @version 1.0
 * @date 2022/9/5 16:43
 */
public class ProductConstant {

    public enum AttrEnum {
        ATTY_TYPE_BASE(1, "基本属性"), ATTY_TYPE_SALE(0, "销售属性");
        private int code;
        private String msg;

        AttrEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }

    public enum StatusEnum {
        NEW_SPU(0, "新建"), UP_SPU(1, "商品上架"),
        DOWN_SPU(1, "商品下架");
        private int code;
        private String msg;

        StatusEnum (int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }
}
