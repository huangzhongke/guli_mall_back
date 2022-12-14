package com.hzk.gulimall.product.controller;

import com.hzk.common.utils.PageUtils;
import com.hzk.common.utils.R;
import com.hzk.common.valid.AddGroup;
import com.hzk.common.valid.UpdateGroup;
import com.hzk.common.valid.UpdateStatusGroup;
import com.hzk.gulimall.product.entity.BrandEntity;
import com.hzk.gulimall.product.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 品牌
 *
 * @author kee
 * @email 1059308740@qq.com
 * @date 2022-08-11 11:13:31
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    public R info(@PathVariable("brandId") Long brandId) {
        BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }


    @GetMapping("/infos")
    public R brandsInfos(@RequestParam("brandId") List<Long> brandId) {
        List<BrandEntity> brand = brandService.getBrandsByIds(brandId);

        return R.ok().put("brand", brand);
    }
    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@Validated(AddGroup.class) @RequestBody BrandEntity brand/*, BindingResult bindingResult*/) {

        //Map<String, String> map = new HashMap<>();
        //if (bindingResult.hasErrors()) {
        //    bindingResult.getFieldErrors().forEach((item) -> {
        //        map.put(item.getField(), item.getDefaultMessage());
        //    });
        //    return R.error(400, "提交的数据不合法").put("data", map);
        //} else {
        //    brandService.save(brand);
        //
        //    return R.ok();
        //}
        brandService.save(brand);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@Validated(UpdateGroup.class)@RequestBody BrandEntity brand) {
        brandService.updateDetail(brand);

        return R.ok();
    }
    /**
     * 修改状态
     */
    @RequestMapping("/update/status")
    public R updateStatus(@Validated(UpdateStatusGroup.class)@RequestBody BrandEntity brand) {
        brandService.updateById(brand);

        return R.ok();
    }
    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] brandIds) {
        brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
