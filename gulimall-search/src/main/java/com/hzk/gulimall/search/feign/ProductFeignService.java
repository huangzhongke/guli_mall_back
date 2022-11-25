package com.hzk.gulimall.search.feign;

import com.hzk.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author kee
 * @version 1.0
 * @date 2022/11/16 15:35
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {
    /**
     *
     * @param attrId 属性Id
     * @return 属性entity
     */
    @RequestMapping("/product/attr/info/{attrId}")
    R info(@PathVariable("attrId") Long attrId);

    /**
     *
     * @param brandId 品牌id集合
     * @return 品牌entityList
     */
    @GetMapping("/product/brand/infos")
    R brandsInfos(@RequestParam("brandId") List<Long> brandId);
}
