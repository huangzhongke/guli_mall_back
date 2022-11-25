package com.hzk.gulimall.product.vo;

import com.hzk.gulimall.product.entity.SkuImagesEntity;
import com.hzk.gulimall.product.entity.SkuInfoEntity;
import com.hzk.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

/**
 * @author kee
 * @version 1.0
 * @date 2022/11/21 15:15
 */
@Data
public class SkuItemVo {
    private SkuInfoEntity info;

    private List<SkuImagesEntity> images;
    private List<SkuItemSaleAttrVo> saleAttr;
    private SpuInfoDescEntity desc;
    private List<SpuItemAttrGroupVo> groupAttrs;
    private boolean hasStock = true;
    @Data
    public static class SkuItemSaleAttrVo {
        private Long attrId;
        private String attrName;
        private List<AttrValueWithSkuIdVo> attrValues;
    }

    @Data
    public static class SpuItemAttrGroupVo{
        private String groupName;
        private List<Attr> attrs;
    }

}
