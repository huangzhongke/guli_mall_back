package com.hzk.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hzk.common.utils.PageUtils;
import com.hzk.gulimall.member.entity.MemberEntity;
import com.hzk.gulimall.member.exception.PhoneExistException;
import com.hzk.gulimall.member.exception.UsernameExistException;
import com.hzk.gulimall.member.vo.MemberLoginVo;
import com.hzk.gulimall.member.vo.MemberRegistVo;
import com.hzk.gulimall.member.vo.SocialUser;

import java.util.Map;

/**
 * 会员
 *
 * @author kee
 * @email 1059308740@qq.com
 * @date 2022-08-15 09:46:13
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(MemberRegistVo vo);

    void checkPhoneUnique(String phone) throws PhoneExistException;

    void checkUsernameUnique(String username) throws UsernameExistException;


    MemberEntity login(MemberLoginVo vo);

    MemberEntity login(SocialUser vo);
}

