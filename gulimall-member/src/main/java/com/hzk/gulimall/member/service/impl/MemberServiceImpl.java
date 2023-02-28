package com.hzk.gulimall.member.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzk.common.utils.HttpUtils;
import com.hzk.common.utils.PageUtils;
import com.hzk.common.utils.Query;
import com.hzk.gulimall.member.dao.MemberDao;
import com.hzk.gulimall.member.entity.MemberEntity;
import com.hzk.gulimall.member.entity.MemberLevelEntity;
import com.hzk.gulimall.member.exception.PhoneExistException;
import com.hzk.gulimall.member.exception.UsernameExistException;
import com.hzk.gulimall.member.service.MemberLevelService;
import com.hzk.gulimall.member.service.MemberService;
import com.hzk.gulimall.member.vo.MemberLoginVo;
import com.hzk.gulimall.member.vo.MemberRegistVo;
import com.hzk.gulimall.member.vo.SocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelService memberLevelService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(MemberRegistVo vo) {
        MemberEntity memberEntity = new MemberEntity();
        MemberLevelEntity memberLevelEntity = memberLevelService.getOne(new QueryWrapper<MemberLevelEntity>().lambda().eq(MemberLevelEntity::getDefaultStatus, 1));
        memberEntity.setLevelId(memberLevelEntity.getId());

        checkPhoneUnique(vo.getPhone());
        checkUsernameUnique(vo.getUserName());

        memberEntity.setMobile(vo.getPhone());
        memberEntity.setUsername(vo.getUserName());
        memberEntity.setNickname(vo.getUserName());

        /**
         * 密码加密
         */
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(vo.getPassword());
        memberEntity.setPassword(encode);
        baseMapper.insert(memberEntity);
    }

    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException {
        Long count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().lambda().eq(MemberEntity::getMobile, phone));
        if (count > 0) {
            //说明数据库已经存在该手机号
            throw new PhoneExistException();
        }
    }

    @Override
    public void checkUsernameUnique(String username) throws UsernameExistException {

        Long count = baseMapper.selectCount(new QueryWrapper<MemberEntity>().lambda().eq(MemberEntity::getUsername, username));
        if (count > 0) {
            throw new UsernameExistException();
        }

    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {
        String loginacct = vo.getLoginacct();
        MemberEntity member = baseMapper.selectOne(new QueryWrapper<MemberEntity>().lambda().eq(MemberEntity::getUsername, loginacct).or().eq(MemberEntity::getMobile, loginacct));
        if (member == null) {
            return null;
        }
        String encodePassword = member.getPassword();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        boolean matches = passwordEncoder.matches(vo.getPassword(), encodePassword);
        if (matches) {
            return member;
        } else {
            return null;
        }
    }

    @Override
    public MemberEntity login(SocialUser vo) {
        String uid = vo.getUid();
        MemberEntity member = baseMapper.selectOne(new QueryWrapper<MemberEntity>().lambda().eq(MemberEntity::getSocialUid, uid));
        if (member != null) {
            //说明已经注册
            MemberEntity updateMember = new MemberEntity();
            updateMember.setAccessToken(vo.getAccess_token());
            updateMember.setId(member.getId());
            updateMember.setExpiresIn(vo.getExpires_in() + "");
            baseMapper.updateById(updateMember);

            member.setAccessToken(vo.getAccess_token());
            member.setExpiresIn(vo.getExpires_in() + "");
            return member;
        } else {
            MemberEntity register = new MemberEntity();
            try {
                Map<String, String> map = new HashMap<>();
                map.put("access_token", vo.getAccess_token());
                map.put("uid", vo.getUid());
                HttpResponse response = null;
                response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "get", new HashMap<>(), map);
                if (response.getStatusLine().getStatusCode() == 200) {
                    String result = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObject = JSONObject.parseObject(result);
                    String name = jsonObject.getString("name");
                    String gender = jsonObject.getString("gender");

                    register.setNickname(name);
                    register.setGender("m".equals(gender) ? 1 : 0);
                }

            } catch (Exception e) {
            }
            register.setSocialUid(vo.getUid());
            register.setAccessToken(vo.getAccess_token());
            register.setExpiresIn(vo.getExpires_in() + "");
            baseMapper.insert(register);
            return register;
        }
    }
}