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
import org.springframework.beans.factory.annotation.Autowired;
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

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> list = new ArrayList<>();
        getParentId(catelogId, list);
        Collections.reverse(list);
        return list.toArray(new Long[list.size()]);
    }

    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        if (!StringUtils.isEmpty(category.getName())) {
            categoryBrandRelationDao.updateCategory(category.getCatId(), category.getName());
        }
    }

    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        List<CategoryEntity> list = baseMapper.selectList(new QueryWrapper<CategoryEntity>().lambda().eq(CategoryEntity::getParentCid, 0));

        return list;
    }

    public List<CategoryEntity> getListByParentCid(List<CategoryEntity> entityList, Long parentCid) {
        return entityList.stream().filter(item -> item.getParentCid() == parentCid).collect(Collectors.toList());
    }


    /**
     * TODO 在压力测试下会出现Socket closed 或者是 堆外内存溢出 OutOfDirectMemoryError
     * 加上了redis缓存技术减少与DB的交互，大大提高性能
     *
     * @return
     */
    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        /**
         *  1 . 缓存穿透 给空结果也加上缓存
         *  2 . 缓存击穿 加锁
         *  3 。缓存雪崩 给缓存设置过期时间（最好是随机，如果同时百万请求，刚好缓存失效，会一下子涌入数据库，造成雪崩。）
         */
        String catelogJSON = stringRedisTemplate.opsForValue().get("catelogJSON");
        if (StringUtils.isEmpty(catelogJSON)) {
            System.out.println("缓存未命中，查询数据库...");
            Map<String, List<Catelog2Vo>> catalogJsonFromDB = getCatalogJsonFromDBWithRedisLock();

            return catalogJsonFromDB;
        }
        System.out.println("缓存命中");
        Map<String, List<Catelog2Vo>> result = JSONObject.parseObject(catelogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });
        return result;
    }

    /**
     * 本地锁
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
                //先查缓存 如果有数据直接返回
            }
            System.out.println("查询了数据库...");
            return getDataFromDb();
        }

    }

    /**
     * Redis分布式锁
     *
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDBWithRedisLock() {
        String uuid = UUID.randomUUID().toString();
        // 加锁要原子性操作
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 50, TimeUnit.SECONDS);
        if (lock) {
            System.out.println("获取分布式锁成功");
            Map<String, List<Catelog2Vo>> catelogJson = null;
            // 加锁成功
            try {
                catelogJson = getDataFromDb();
            } finally {
                //做完操作需要释放锁，释放的过程也是一个原子操作
                String script =  "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

                Long lock1 = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);

            }

            return catelogJson;

        } else {
            System.out.println("获取分布式锁失败");
            //加锁失败 进行重试
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getCatalogJsonFromDBWithRedisLock();
        }


    }

    @NotNull
    private Map<String, List<Catelog2Vo>> getDataFromDb() {
        //得到锁以后，我们应该再去缓存中确定一次，如果没有才需要继续查询
        String catalogJson = stringRedisTemplate.opsForValue().get("catelogJSON");
        if (!StringUtils.isEmpty(catalogJson)) {
            //缓存不为空直接返回
            Map<String, List<Catelog2Vo>> result = JSONObject.parseObject(catalogJson, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });

            return result;
        }

        System.out.println("查询了数据库");
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