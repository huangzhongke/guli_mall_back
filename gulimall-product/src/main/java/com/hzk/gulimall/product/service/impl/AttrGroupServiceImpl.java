package com.hzk.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzk.common.utils.PageUtils;
import com.hzk.common.utils.Query;
import com.hzk.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.hzk.gulimall.product.dao.AttrDao;
import com.hzk.gulimall.product.dao.AttrGroupDao;
import com.hzk.gulimall.product.entity.AttrEntity;
import com.hzk.gulimall.product.entity.AttrGroupEntity;
import com.hzk.gulimall.product.service.AttrGroupService;
import com.hzk.gulimall.product.service.AttrService;
import com.hzk.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.hzk.gulimall.product.vo.SkuItemVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    AttrAttrgroupRelationDao relationDao;

    @Autowired
    AttrDao attrDao;
    @Autowired
    AttrService attrService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {
        //如果Key 不为空 要么是根据id查要么是根据 组名查

        LambdaQueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>().lambda();
        //如果key不为空
        if (null != params.get("key") && !StringUtils.isEmpty(params.get("key").toString())) {
            String key = params.get("key").toString();
            wrapper.and((obj) -> {
                obj.eq(AttrGroupEntity::getAttrGroupId, key).or().like(AttrGroupEntity::getAttrGroupName, key);
            });

        }
        if (catelogId == 0) {
            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    wrapper
            );
            return new PageUtils(page);
        } else {
            wrapper.eq(AttrGroupEntity::getCatelogId, catelogId);
            IPage<AttrGroupEntity> page = this.page(
                    new Query<AttrGroupEntity>().getPage(params),
                    wrapper
            );
            return new PageUtils(page);
        }
    }

    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId) {
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().lambda().eq(AttrGroupEntity::getCatelogId, catelogId));
        List<AttrGroupWithAttrsVo> vos = attrGroupEntities.stream().map((item -> {
            AttrGroupWithAttrsVo attrGroupWithAttrsVo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(item, attrGroupWithAttrsVo);
            List<AttrEntity> attrs = attrService.getRelationAttr(item.getAttrGroupId());

            attrGroupWithAttrsVo.setAttrs(attrs);
            return attrGroupWithAttrsVo;
        })).collect(Collectors.toList());
        return vos;
    }

    @Override
    public List<SkuItemVo.SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long catelogId, Long spuId) {
        List<SkuItemVo.SpuItemAttrGroupVo> vos =  baseMapper.getAttrGroupWithAttrsBySpuId(catelogId,spuId);
        return vos;
    }


}