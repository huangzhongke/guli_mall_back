package com.hzk.gulimall.order.dao;

import com.hzk.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author kee
 * @email 1059308740@qq.com
 * @date 2022-08-15 09:32:02
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
