package com.hzk.gulimall.search.controller;

import com.hzk.common.es.SkuEsModel;
import com.hzk.common.exception.BizCodeEnum;
import com.hzk.common.utils.R;
import com.hzk.gulimall.search.service.ProductSavaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author kee
 * @version 1.0
 * @date 2022/9/30 15:39
 */
@Slf4j
@RequestMapping("search/save")
@RestController
public class ElasticSearchSavaController {


    @Autowired
    ProductSavaService productSavaService;

    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> models) {
        boolean b = false;
        try {
            b = productSavaService.productStatusUp(models);
        } catch (Exception e) {
            log.error("ElasticSearchSavaController 上架失败:{}", e);
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
        }
        if (b) {
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
        } else {
            return R.ok();
        }


    }
}
