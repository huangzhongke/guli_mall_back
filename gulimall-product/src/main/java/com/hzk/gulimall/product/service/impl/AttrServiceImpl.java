package com.hzk.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzk.common.constant.ProductConstant;
import com.hzk.common.utils.PageUtils;
import com.hzk.common.utils.Query;
import com.hzk.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.hzk.gulimall.product.dao.AttrDao;
import com.hzk.gulimall.product.dao.AttrGroupDao;
import com.hzk.gulimall.product.dao.CategoryDao;
import com.hzk.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.hzk.gulimall.product.entity.AttrEntity;
import com.hzk.gulimall.product.entity.AttrGroupEntity;
import com.hzk.gulimall.product.entity.CategoryEntity;
import com.hzk.gulimall.product.service.AttrService;
import com.hzk.gulimall.product.service.CategoryService;
import com.hzk.gulimall.product.vo.AttrGroupVo;
import com.hzk.gulimall.product.vo.AttrRespVo;
import com.hzk.gulimall.product.vo.AttrVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {
    @Autowired
    AttrAttrgroupRelationDao relationDao;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    CategoryService categoryService;

    @Autowired
    AttrDao attrDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.save(attrEntity);

        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTY_TYPE_BASE.getCode() && attr.getAttrGroupId() != null) {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrId(attrEntity.getAttrId());
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationDao.insert(relationEntity);
        }

    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String type) {
        LambdaQueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().lambda().eq(AttrEntity::getAttrType, "base".equalsIgnoreCase(type) ? ProductConstant.AttrEnum.ATTY_TYPE_BASE.getCode() : ProductConstant.AttrEnum.ATTY_TYPE_SALE.getCode());

        if (null != params.get("key") && !StringUtils.isEmpty(params.get("key").toString())) {
            String key = params.get("key").toString();
            wrapper.and((obj) -> {
                obj.eq(AttrEntity::getAttrId, key).or().like(AttrEntity::getAttrName, key);
            });
        }
        IPage<AttrEntity> page = null;
        if (catelogId == 0) {
            page = this.page(
                    new Query<AttrEntity>().getPage(params),
                    wrapper
            );
        } else {
            wrapper.eq(AttrEntity::getCatelogId, catelogId);
            page = this.page(
                    new Query<AttrEntity>().getPage(params),
                    wrapper
            );

        }
        List<AttrEntity> records = page.getRecords();
        List<AttrRespVo> attrRespVoList = records.stream().map((attrEntity -> {
            AttrRespVo attrRespVo = new AttrRespVo();
            BeanUtils.copyProperties(attrEntity, attrRespVo);
            AttrAttrgroupRelationEntity relationEntity = relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().lambda().eq(AttrAttrgroupRelationEntity::getAttrId, attrEntity.getAttrId()));

            if ("base".equalsIgnoreCase(type)) {
                if (relationEntity != null && relationEntity.getAttrGroupId() != null) {
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectOne(new QueryWrapper<AttrGroupEntity>().lambda().eq(AttrGroupEntity::getAttrGroupId, relationEntity.getAttrGroupId()));
                    attrRespVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            if (categoryEntity != null) {
                attrRespVo.setCatelogName(categoryEntity.getName());
            }
            return attrRespVo;
        })).collect(Collectors.toList());

        PageUtils pageUtils = new PageUtils(page);
        pageUtils.setList(attrRespVoList);
        return pageUtils;
    }
    @Cacheable(value = "attr", key = "'attrinfo:'+#root.args[0]")
    @Override
    public AttrRespVo getAttrInfo(Long attrId) {
        AttrEntity attrEntity = this.getById(attrId);
        AttrRespVo attrRespVo = new AttrRespVo();
        BeanUtils.copyProperties(attrEntity, attrRespVo);
        AttrAttrgroupRelationEntity relationEntity = relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().lambda().eq(AttrAttrgroupRelationEntity::getAttrId, attrId));
        if (relationEntity != null) {
            attrRespVo.setAttrGroupId(relationEntity.getAttrGroupId());
        }
        Long[] catelogPath = categoryService.findCatelogPath(attrEntity.getCatelogId());
        attrRespVo.setCatelogPath(catelogPath);
        return attrRespVo;
    }

    @Transactional
    @Override
    public void updateAttr(AttrRespVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.updateById(attrEntity);
        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTY_TYPE_BASE.getCode()) {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            relationEntity.setAttrId(attr.getAttrId());

            Long count = relationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>().lambda().eq(AttrAttrgroupRelationEntity::getAttrId, attr.getAttrId()));
            if (count > 0) {

                relationDao.update(relationEntity, new UpdateWrapper<AttrAttrgroupRelationEntity>().lambda().eq(AttrAttrgroupRelationEntity::getAttrId, attr.getAttrId()));
            } else {
                relationDao.insert(relationEntity);
            }
        }

    }


    @Override
    public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId) {
        //????????????????????????????????????,?????????????????????????????????

        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        List<AttrGroupEntity> groupEntities = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().lambda().eq(AttrGroupEntity::getCatelogId, attrGroupEntity.getCatelogId()));
        List<Long> collect = groupEntities.stream().map((item) -> {
            return item.getAttrGroupId();
        }).collect(Collectors.toList());

        List<AttrAttrgroupRelationEntity> attrgroupRelationEntities = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().lambda().in(AttrAttrgroupRelationEntity::getAttrGroupId, collect));
        List<Long> attrIds = attrgroupRelationEntities.stream().map((item) -> {
            return item.getAttrId();
        }).collect(Collectors.toList());
        //??????????????????????????????
        LambdaQueryWrapper<AttrEntity> wrapper = new QueryWrapper<AttrEntity>().lambda().eq(AttrEntity::getCatelogId, attrGroupEntity.getCatelogId()).eq(AttrEntity::getAttrType, ProductConstant.AttrEnum.ATTY_TYPE_BASE.getCode());
        if (attrIds != null && attrIds.size() > 0) {
            wrapper.notIn(AttrEntity::getAttrId, attrIds);
        }
        if (params.get("key") != null && !StringUtils.isEmpty(params.get("key").toString())) {
            String key = params.get("key").toString();
            wrapper.and(item -> {
                item.eq(AttrEntity::getAttrId, key).or().like(AttrEntity::getAttrName, key);
            });
        }
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                wrapper);
        //??????????????????????????????????????????????????????????????????
        //?????????????????????????????????????????????????????????
        return new PageUtils(page);
    }

    @Override
    public void deleteRelation(AttrGroupVo[] vos) {
        // ??????????????????????????????????????????????????????
        //for (AttrGroupVo vo : vos) {
        //    relationDao.delete(new QueryWrapper<AttrAttrgroupRelationEntity>().lambda().eq(AttrAttrgroupRelationEntity::getAttrId,vo.getAttrId()).eq(AttrAttrgroupRelationEntity::getAttrGroupId,vo.getAttrGroupId()));
        //}
        List<AttrAttrgroupRelationEntity> relationList = Arrays.asList(vos).stream().map((item) -> {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item, relationEntity);
            return relationEntity;
        }).collect(Collectors.toList());
        relationDao.deleteBatchRelation(relationList);
    }

    /**
     * @param attrGroupId ??????id
     * @return ????????????
     */
    @Override
    public List<AttrEntity> getRelationAttr(Long attrGroupId) {
        List<AttrAttrgroupRelationEntity> relationEntities = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().lambda().eq(AttrAttrgroupRelationEntity::getAttrGroupId, attrGroupId));
        List<Long> attrIds = relationEntities.stream().map((obj) -> {
            return obj.getAttrId();
        }).collect(Collectors.toList());

        if (attrIds == null || attrIds.size() == 0) {
            return null;
        }
        List<AttrEntity> attrEntities = attrDao.selectBatchIds(attrIds);
        return attrEntities;
    }

    @Override
    public List<Long> selectSearchAttrIds(List<Long> attrIds) {

        return this.baseMapper.selectSearchAttrIds(attrIds);

    }

}