package com.hzk.gulimall.product;

import com.hzk.gulimall.product.dao.AttrGroupDao;
import com.hzk.gulimall.product.service.BrandService;
import com.hzk.gulimall.product.service.CategoryService;
import com.hzk.gulimall.product.vo.SkuItemVo;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

@SpringBootTest
class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    AttrGroupDao attrGroupDao;
    @Test
    void contextLoads() {
        List<SkuItemVo.SpuItemAttrGroupVo> attrGroupWithAttrsBySpuId = attrGroupDao.getAttrGroupWithAttrsBySpuId(225L, 5L);
        for (SkuItemVo.SpuItemAttrGroupVo spuItemAttrGroupVo : attrGroupWithAttrsBySpuId) {
            System.out.println(spuItemAttrGroupVo);
        }

    }

    @Test
    void testFindCatelogPath(){
        Long[] catelogPath = categoryService.findCatelogPath(225L);
        for (Long aLong : catelogPath) {
            System.out.println(aLong);
        }
    }


}
