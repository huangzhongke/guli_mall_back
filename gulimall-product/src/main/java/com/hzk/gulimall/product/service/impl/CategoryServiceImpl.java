package com.hzk.gulimall.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzk.common.utils.PageUtils;
import com.hzk.common.utils.Query;
import com.hzk.gulimall.product.dao.CategoryBrandRelationDao;
import com.hzk.gulimall.product.dao.CategoryDao;
import com.hzk.gulimall.product.entity.CategoryEntity;
import com.hzk.gulimall.product.service.CategoryService;
import com.hzk.gulimall.product.vo.Catelog2Vo;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationDao categoryBrandRelationDao;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redisson;

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
        //1 ???????????????????????????
        //2 ????????????????????????????????????????????????
        List<CategoryEntity> list = entities.stream().filter(categoryEntity -> (
                //?????????????????????parentCid???0
                categoryEntity.getParentCid() == 0
        )).map(categoryEntity -> {
            //?????????categoryEntity ?????????????????????
            categoryEntity.setChildren(getChildren(categoryEntity, entities));
            return categoryEntity;
        }).sorted((categoryEntity1, categoryEntity2) -> {
            return (categoryEntity1.getSort() == null ? 0 : categoryEntity1.getSort()) - (categoryEntity2.getSort() == null ? 0 : categoryEntity2.getSort());
        }).collect(Collectors.toList());
        return list;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO 1.?????????????????????????????????????????????????????????
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> list = new ArrayList<>();
        getParentId(catelogId, list);
        Collections.reverse(list);
        return list.toArray(new Long[list.size()]);
    }

    /**
     * ?????????????????????????????????
     * @CacheEvict ????????????
     * @param category
     *  @CacheEvict ?????????????????????????????????catelog:xxx??????????????????
     *  @CacheEvict(value = "catelog",allEntries = true) ????????????catelog??????????????????
     */
    //@CacheEvict(value = "catelog" ,key = "'getLevel1Categorys'")
    //@Caching(evict = {@CacheEvict(value = "catelog" ,key = "'getLevel1Categorys'"),
    //                 @CacheEvict(value = "catelog" ,key = "'getCatalogJson'"), ??????????????????
    //})

    @CacheEvict(value = "catelog",allEntries = true)
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        if (!StringUtils.isEmpty(category.getName())) {
            categoryBrandRelationDao.updateCategory(category.getCatId(), category.getName());
        }
    }

    @Cacheable(value = {"catelog"},key = "#root.methodName", sync = true)
    @Override
    public List<CategoryEntity> getLevel1Categorys() {

        List<CategoryEntity> list = baseMapper.selectList(new QueryWrapper<CategoryEntity>().lambda().eq(CategoryEntity::getParentCid, 0));
        //return  null;
        return list;
    }

    public List<CategoryEntity> getListByParentCid(List<CategoryEntity> entityList, Long parentCid) {
        return entityList.stream().filter(item -> item.getParentCid() == parentCid).collect(Collectors.toList());
    }


    /**
     * TODO ???????????????????????????Socket closed ????????? ?????????????????? OutOfDirectMemoryError
     * ?????????redis?????????????????????DB??????????????????????????????
     *
     * @return
     */
    //@Override
    public Map<String, List<Catelog2Vo>> getCatalogJson2() {
        /**
         *  1 . ???????????? ???????????????????????????
         *  2 . ???????????? ??????
         *  3 ??????????????? ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
         */
        String catelogJSON = stringRedisTemplate.opsForValue().get("catelogJSON");
        if (StringUtils.isEmpty(catelogJSON)) {
            System.out.println("?????????????????????????????????...");
            Map<String, List<Catelog2Vo>> catalogJsonFromDB = getCatalogJsonFromDBWithRedissonLock();

            return catalogJsonFromDB;
        }
        System.out.println("????????????");
        Map<String, List<Catelog2Vo>> result = JSONObject.parseObject(catelogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });
        return result;
    }


    @Cacheable(value = "catelog", key = "#root.methodName",sync = true)
    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        List<CategoryEntity> categoryEntityList = baseMapper.selectList(null);
        List<CategoryEntity> level1Categorys = getListByParentCid(categoryEntityList, 0L);
        Map<String, List<Catelog2Vo>> list = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            List<CategoryEntity> categoryEntities = getListByParentCid(categoryEntityList, v.getCatId());
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());

                    List<CategoryEntity> l3List = getListByParentCid(categoryEntityList, l2.getCatId());
                    if (l3List != null) {
                        List<Catelog2Vo.Category3Vo> category3Vos = l3List.stream().map(l3 -> {
                            Catelog2Vo.Category3Vo category3Vo = new Catelog2Vo.Category3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return category3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(category3Vos);
                    }

                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        return list;
    }

    /**
     * ?????????
     *
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDBWithLocalLock() {
        synchronized (this) {
            ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
            String catelogJSON = ops.get("catelogJSON");
            if (!StringUtils.isEmpty(catelogJSON)) {
                Map<String, List<Catelog2Vo>> result = JSONObject.parseObject(catelogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
                });
                return result;
                //???????????? ???????????????????????????
            }
            System.out.println("??????????????????...");
            return getDataFromDb();
        }

    }

    /**
     * Redis????????????
     *
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDBWithRedisLock() {
        String uuid = UUID.randomUUID().toString();
        // ????????????????????????
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 50, TimeUnit.SECONDS);
        if (lock) {
            System.out.println("????????????????????????");
            Map<String, List<Catelog2Vo>> catelogJson = null;
            // ????????????
            try {
                catelogJson = getDataFromDb();
            } finally {
                //?????????????????????????????????????????????????????????????????????
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

                Long lock1 = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);

            }

            return catelogJson;

        } else {
            System.out.println("????????????????????????");
            //???????????? ????????????
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatalogJsonFromDBWithRedisLock();
        }


    }


    /**
     * Redisson ????????????
     *
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDBWithRedissonLock() {
        RLock lock = redisson.getLock("CatelogJson-lock");
        // ??????
        lock.lock();
        Map<String, List<Catelog2Vo>> catelogJson = null;
        try {
            catelogJson = getDataFromDb();
        } finally {
            lock.unlock();
        }

        return catelogJson;

    }

    @NotNull
    private Map<String, List<Catelog2Vo>> getDataFromDb() {
        //?????????????????????????????????????????????????????????????????????????????????????????????
        String catalogJson = stringRedisTemplate.opsForValue().get("catelogJSON");
        if (!StringUtils.isEmpty(catalogJson)) {
            //???????????????????????????
            Map<String, List<Catelog2Vo>> result = JSONObject.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });

            return result;
        }

        System.out.println("??????????????????");
        List<CategoryEntity> categoryEntityList = baseMapper.selectList(null);
        List<CategoryEntity> level1Categorys = getListByParentCid(categoryEntityList, 0L);
        Map<String, List<Catelog2Vo>> list = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            List<CategoryEntity> categoryEntities = getListByParentCid(categoryEntityList, v.getCatId());
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());

                    List<CategoryEntity> l3List = getListByParentCid(categoryEntityList, l2.getCatId());
                    if (l3List != null) {
                        List<Catelog2Vo.Category3Vo> category3Vos = l3List.stream().map(l3 -> {
                            Catelog2Vo.Category3Vo category3Vo = new Catelog2Vo.Category3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return category3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(category3Vos);
                    }

                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));
        stringRedisTemplate.opsForValue().set("catelogJSON", JSONObject.toJSONString(list), 1, TimeUnit.DAYS);
        return list;
    }

    private void getParentId(Long catelogId, List<Long> list) {
        list.add(catelogId);
        CategoryEntity categoryEntity = this.getById(catelogId);
        if (categoryEntity.getParentCid() != 0) {
            getParentId(categoryEntity.getParentCid(), list);
        }
    }

    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> entities) {
        List<CategoryEntity> list = entities.stream().filter(categoryEntity -> {
            return root.getCatId() == categoryEntity.getParentCid();
        }).map(categoryEntity -> {
            categoryEntity.setChildren(getChildren(categoryEntity, entities));
            return categoryEntity;
        }).sorted((categoryEntity1, categoryEntity2) -> {
            return (categoryEntity1.getSort() == null ? 0 : categoryEntity1.getSort()) - (categoryEntity2.getSort() == null ? 0 : categoryEntity2.getSort());
        }).collect(Collectors.toList());
        return list;
    }
}