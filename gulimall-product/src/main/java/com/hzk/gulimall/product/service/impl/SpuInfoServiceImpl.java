package com.hzk.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzk.common.constant.ProductConstant;
import com.hzk.common.to.es.SkuEsModel;
import com.hzk.common.to.SkuHasStockVo;
import com.hzk.common.to.SkuReductionTo;
import com.hzk.common.to.SpuBoundTo;
import com.hzk.common.utils.PageUtils;
import com.hzk.common.utils.Query;
import com.hzk.common.utils.R;
import com.hzk.gulimall.product.dao.SpuInfoDao;
import com.hzk.gulimall.product.entity.*;
import com.hzk.gulimall.product.feign.CouponFeignService;
import com.hzk.gulimall.product.feign.SearchFeignService;
import com.hzk.gulimall.product.feign.WareFeignService;
import com.hzk.gulimall.product.service.*;
import com.hzk.gulimall.product.vo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {
    @Autowired
    SpuInfoDescService spuInfoDescService;
    @Autowired
    SpuImagesService spuImagesService;
    @Autowired
    ProductAttrValueService productAttrValueService;
    @Autowired
    AttrService attrService;
    @Autowired
    SkuInfoService skuInfoService;
    @Autowired
    SkuImagesService skuImagesService;
    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    CouponFeignService couponFeignService;
    @Autowired
    BrandService brandService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    WareFeignService wareFeignService;
    @Autowired
    SearchFeignService searchFeignService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * TODO 等待高级篇的优化，应对发生错误的解决方案
     *
     * @param vo 发布的商品信息
     */
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {
        // 1. 保存spu基本信息  pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfoEntity);
        // 2. 保存spu的描述图片 pms_spu_info_desc
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        String decriptStr = StringUtils.join(decript, ",");
        spuInfoDescEntity.setDecript(decriptStr);
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescService.save(spuInfoDescEntity);
        // 3. 保存spu的图片集 pms_spu_images
        spuImagesService.saveImages(spuInfoEntity.getId(), vo.getImages());
        // 4. 保存spu的规格参数，pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> productAttrValueEntityList = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            AttrEntity attrEntity = attrService.getById(attr.getAttrId());
            productAttrValueEntity.setAttrId(attr.getAttrId());
            productAttrValueEntity.setAttrName(attrEntity.getAttrName());
            productAttrValueEntity.setAttrValue(attr.getAttrValues());
            productAttrValueEntity.setQuickShow(attr.getShowDesc());
            productAttrValueEntity.setSpuId(spuInfoEntity.getId());
            return productAttrValueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveProductAttr(productAttrValueEntityList);
        // 5  保存spu的积分信息 gulimall_sms -》 sms_spu_bounds
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        Bounds bounds = vo.getBounds();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(spuInfoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if (r.getCode() != 0) {
            log.error("远程保存SpuBound失败");
        }
        // 6. 保存当前spu对应的所有sku信息
        List<Skus> skus = vo.getSkus();
        if (skus != null && skus.size() > 0) {
            for (Skus sku : skus) {
                String defaultImage = "";
                for (Images image : sku.getImages()) {
                    if (image.getDefaultImg() == 1) {
                        defaultImage = image.getImgUrl();
                    }
                }
                //private String skuName;
                //private BigDecimal price;
                //private String skuTitle;
                //private String skuSubtitle;
                // 6.1） sku基本信息 pms_sku_info
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku, skuInfoEntity);
                skuInfoEntity.setBrandId(vo.getBrandId());
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setCatalogId(vo.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSkuDefaultImg(defaultImage);
                skuInfoService.save(skuInfoEntity);
                // 6.2） sku图片信息 pms_sku_images
                List<SkuImagesEntity> skuImagesEntityList = sku.getImages().stream().map(image -> {
                            SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                            skuImagesEntity.setSkuId(skuInfoEntity.getSkuId());
                            skuImagesEntity.setImgUrl(image.getImgUrl());
                            skuImagesEntity.setDefaultImg(image.getDefaultImg());
                            return skuImagesEntity;
                        }).filter(entity -> {
                            //过滤非空的Url图片路径
                            return !StringUtils.isEmpty(entity.getImgUrl());
                        })
                        .collect(Collectors.toList());
                skuImagesService.saveBatch(skuImagesEntityList);
                //TODO 不需要保存没有路径的图片
                Long skuId = skuInfoEntity.getSkuId();
                // 6.3） sku销售属性信息 pms_sku_sale_attr_value
                List<Attr> attrs = sku.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntityList = attrs.stream().map(attr -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuId);
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntityList);
                // 6.4） sku 优惠 满减信息 gulimall_sms -》  sms_sku_full_reduction(满多少减多少 ), sms_sku_ladder(满多少件打折)
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                skuReductionTo.setSkuId(skuId);
                BeanUtils.copyProperties(sku, skuReductionTo);
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(BigDecimal.ZERO) == 1) {
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r1.getCode() != 0) {
                        log.error("远程保存SkuReduction失败");
                    }
                }

            }

        }


    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.save(spuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        //  key: '华为',//检索关键字
        //   catelogId: 6,//三级分类id
        //   brandId: 1,//品牌id
        //   status: 0,//商品状态
        LambdaQueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<SpuInfoEntity>().lambda();


        if (params.get("key") != null && !StringUtils.isEmpty(params.get("key").toString())) {
            String key = params.get("key").toString();
            wrapper.and((w) -> {
                w.eq(SpuInfoEntity::getId, key).eq(SpuInfoEntity::getSpuName, key);
            });
        }
        if (null != params.get("status")) {
            String status = params.get("status").toString();
            if (!StringUtils.isEmpty(status)) {
                wrapper.eq(SpuInfoEntity::getPublishStatus, status);
            }
        }
        if (null != params.get("catelogId")) {
            String catelogId = params.get("catelogId").toString();
            if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
                wrapper.eq(SpuInfoEntity::getCatalogId, catelogId);
            }
        }
        if (null != params.get("brandId")) {
            String brandId = params.get("brandId").toString();
            if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
                wrapper.eq(SpuInfoEntity::getBrandId, brandId);
            }
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper

        );

        return new PageUtils(page);
    }

    @Override
    public void up(Long spuId) {
        List<SkuInfoEntity> skus = skuInfoService.getSkuBySpuId(spuId);
        List<Long> skuIdList = skus.stream().map(skusInfo -> {
            return skusInfo.getSkuId();
        }).collect(Collectors.toList());
        // TODO 查出所有可被检索的属性
        List<ProductAttrValueEntity> productAttrValueEntities = productAttrValueService.baseAttrlistforspu(spuId);
        List<Long> attrIds = productAttrValueEntities.stream().map(item -> {
            return item.getAttrId();
        }).collect(Collectors.toList());
        List<Long> searchAttrIds = attrService.selectSearchAttrIds(attrIds);
        Set<Long> set = new HashSet<>(searchAttrIds);

        List<SkuEsModel.Attrs> attrsList = productAttrValueEntities.stream().filter(item -> {
            return set.contains(item.getAttrId());
        }).map(item -> {
            SkuEsModel.Attrs attrs1 = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(item, attrs1);
            return attrs1;
        }).collect(Collectors.toList());

        Map<Long, Boolean> stockMap = null;
        try {
            R r = wareFeignService.getSkuHasStock(skuIdList);
            stockMap = r.getData(new TypeReference<List<SkuHasStockVo>>(){}).stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, item -> item.getHasStock()));
        } catch (Exception e) {
           log.error("库存远程调用异常：{}",e);
        }

        // 封装需要保存的数据
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuEsModel> skuEsModelList = skus.stream().map(sku -> {
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, skuEsModel);
            /**
             *     private BigDecimal skuPrice;
             *     private String skuImg;
             *     private Boolean hasStock;
             *     private Long hotScore;
             *     private String brandName;
             *     private String brandImg;
             *     private String catalogName;
             *     private List<Attrs> attrs;
             *          private Long attrId;
             *          private String attrName;
             *          private String attrValue;
             */
            skuEsModel.setSkuPrice(sku.getPrice());
            skuEsModel.setSkuImg(sku.getSkuDefaultImg());

            skuEsModel.setHotScore(0L);

            if(finalStockMap == null){
                skuEsModel.setHasStock(true);
            }else {
                skuEsModel.setHasStock(finalStockMap.get(sku.getSkuId()));
            }


            Long brandId = sku.getBrandId();
            Long catalogId = sku.getCatalogId();
            CategoryEntity category = categoryService.getById(catalogId);
            BrandEntity brand = brandService.getById(brandId);
            skuEsModel.setBrandName(brand.getName());
            skuEsModel.setBrandImg(brand.getLogo());
            skuEsModel.setCatalogName(category.getName());
            //设置检索属性
            skuEsModel.setAttrs(attrsList);


            return skuEsModel;
        }).collect(Collectors.toList());

        R r = searchFeignService.productStatusUp(skuEsModelList);
        if (r.getCode() == 0){
            // 吧上架状态修改
            baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.UP_SPU.getCode());
        }else {
            //调用失败
            //TODO 重试机制？
            //Feign调用流程
            /**
             *  1.构造请求参数 将对象转为Json
             *  2. 发送请求执行（执行成功会解码响应数据）
             *   executeAndDecodes(template)
             *  3.执行请求会有重试机制 默认关闭
             *
             */
        }

    }

    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long skuId) {
        SkuInfoEntity skuInfo = skuInfoService.getById(skuId);
        SpuInfoEntity spuInfo = getById(skuInfo.getSpuId());

        return spuInfo;
    }


}