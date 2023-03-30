package com.hzk.gulimall.order.interceptor;

import com.hzk.common.constant.AuthServerConstant;
import com.hzk.common.vo.MemberResponseVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author kee
 * @version 1.0
 * @date 2023/2/28 15:23
 */
@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberResponseVo > threadLocal = new ThreadLocal<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        boolean match = antPathMatcher.match("/order/order/status/**", request.getRequestURI());
        boolean match1 = antPathMatcher.match("/payed/notify", request.getRequestURI());
        if (match || match1){
            return true;
        }
        HttpSession session = request.getSession();
        MemberResponseVo user = (MemberResponseVo)session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (user != null){
            threadLocal.set(user);
            return  true;
        }else {
            request.getSession().setAttribute("msg","请先登录");
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return  false;
        }
    }
}
