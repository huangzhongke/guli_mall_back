package com.hzk.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hzk.common.utils.PageUtils;
import com.hzk.gulimall.ware.entity.WareInfoEntity;
import com.hzk.gulimall.ware.vo.FareVo;

import java.util.Map;

/**
 * 仓库信息
 *
 * @author kee
 * @email 1059308740@qq.com
 * @date 2022-08-15 10:06:17
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    FareVo getFare(Long addrId);


}

