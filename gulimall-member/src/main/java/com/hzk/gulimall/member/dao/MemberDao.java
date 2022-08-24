package com.hzk.gulimall.member.dao;

import com.hzk.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author kee
 * @email 1059308740@qq.com
 * @date 2022-08-15 09:46:13
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
