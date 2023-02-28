package com.hzk.gulimall.member.controller;

import com.hzk.common.exception.BizCodeEnum;
import com.hzk.common.utils.PageUtils;
import com.hzk.common.utils.R;
import com.hzk.gulimall.member.entity.MemberEntity;
import com.hzk.gulimall.member.exception.PhoneExistException;
import com.hzk.gulimall.member.exception.UsernameExistException;
import com.hzk.gulimall.member.service.MemberService;
import com.hzk.gulimall.member.vo.MemberLoginVo;
import com.hzk.gulimall.member.vo.MemberRegistVo;
import com.hzk.gulimall.member.vo.SocialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;



/**
 * 会员
 *
 * @author kee
 * @email 1059308740@qq.com
 * @date 2022-08-15 09:46:13
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @PostMapping("/oauth/login")
    public R oauthLogin(@RequestBody SocialUser vo){
        MemberEntity member = memberService.login(vo);
        if (member == null){
            return R.error(BizCodeEnum.LOGINACCOUNT_PASSWORD_INVALID_EXCEPTION.getCode(), BizCodeEnum.LOGINACCOUNT_PASSWORD_INVALID_EXCEPTION.getMsg());
        }else {
            return R.ok().setData(member);
        }
    }


    @PostMapping("/regist")
    public R regist(@RequestBody MemberRegistVo vo){
        try {
            memberService.regist(vo);
        } catch (PhoneExistException e) {
            return R.error(BizCodeEnum.PHONE_EXIST_EXCEPTION.getCode(),BizCodeEnum.PHONE_EXIST_EXCEPTION.getMsg());
        }catch (UsernameExistException e){
            return R.error(BizCodeEnum.USER_EXIST_EXCEPTION.getCode(),BizCodeEnum.USER_EXIST_EXCEPTION.getMsg());
        }
        return R.ok();
    }

    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo){
        MemberEntity member = memberService.login(vo);
        if (member == null){
            return R.error(BizCodeEnum.LOGINACCOUNT_PASSWORD_INVALID_EXCEPTION.getCode(), BizCodeEnum.LOGINACCOUNT_PASSWORD_INVALID_EXCEPTION.getMsg());
        }else {
            return R.ok().setData(member);
        }
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
