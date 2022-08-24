package com.hzk.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzk.common.utils.PageUtils;
import com.hzk.common.utils.Query;
import com.hzk.gulimall.product.dao.CategoryDao;
import com.hzk.gulimall.product.entity.CategoryEntity;
import com.hzk.gulimall.product.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {

        List<CategoryEntity> entities = baseMapper.selectList(null);
        //1 先查出所有父级分类
        //2 通过递归为每个分类获得它的子分类
        List<CategoryEntity> list = entities.stream().filter(categoryEntity -> (
                //只有父级分类的parentCid为0
                categoryEntity.getParentCid() == 0
        )).map(categoryEntity -> {
            //为每个categoryEntity 设置它的子分类
            categoryEntity.setChildren(getChildren(categoryEntity, entities));
            return categoryEntity;
        }).sorted((categoryEntity1, categoryEntity2) -> {
            return (categoryEntity1.getSort() == null ? 0 : categoryEntity1.getSort()) - (categoryEntity2.getSort() == null ? 0 : categoryEntity2.getSort());
        }).collect(Collectors.toList());
        return list;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO 1.检查当前删除的菜单，是否被别的地方引用
        baseMapper.deleteBatchIds(asList);
    }

    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> entities) {
       List<CategoryEntity> list =  entities.stream().filter(categoryEntity -> {
            return root.getCatId() == categoryEntity.getParentCid();
        }).map(categoryEntity -> {
            categoryEntity.setChildren(getChildren(categoryEntity,entities));
            return categoryEntity;
       }).sorted((categoryEntity1, categoryEntity2) -> {
           return (categoryEntity1.getSort() == null ? 0 : categoryEntity1.getSort()) - (categoryEntity2.getSort() == null ? 0 : categoryEntity2.getSort());
       }).collect(Collectors.toList());
        return list;
    }
}