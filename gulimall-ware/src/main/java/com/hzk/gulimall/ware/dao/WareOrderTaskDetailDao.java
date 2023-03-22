package com.hzk.gulimall.ware.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hzk.gulimall.ware.entity.WareOrderTaskDetailEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存工作单
 * 
 * @author kee
 * @email 1059308740@qq.com
 * @date 2022-08-15 10:06:17
 */
@Mapper
public interface WareOrderTaskDetailDao extends BaseMapper<WareOrderTaskDetailEntity> {

    //void updateLockStatus(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("taskId") Long taskId);

}
