package com.example.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.example.reggie.common.BaseContext;
import com.example.reggie.common.R;
import com.example.reggie.utils.CookieUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
public class LoginCheckFilter implements Filter {

    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String requestURI = request.getRequestURI();
        log.info("拦截请求:" + requestURI);
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login",
                "/user/logout"
        };
        if (check(requestURI, urls)) {
            filterChain.doFilter(request, response);
            return;
        }
        String employeeticket = CookieUtil.getCookieValue(request, "employeeticket");
        if (employeeticket != null && StringUtils.isNotEmpty(employeeticket)) {
            Long employeeId = (Long) redisTemplate.opsForValue().get(employeeticket);
            if (employeeId != null) {
                String[] employeeCanNotVisit = new String[]{
                        "/shoppingCart/*",
                        "/order/submit",
                        "/order/userPage",
                        "/order/again",
                        "/addressBook/*",
                        "/addressBook"
                };
                if (checkNotIn(requestURI, employeeCanNotVisit)) {
                    BaseContext.setEmployeeCurrentId(employeeId);
                    filterChain.doFilter(request, response);
                    return;
                }
            }
        }
        String userticket = CookieUtil.getCookieValue(request, "userticket");
        if (userticket != null && StringUtils.isNotEmpty(userticket)) {
            Long userId = (Long) redisTemplate.opsForValue().get(userticket);
            if (userId != null) {
                String[] userUrls = new String[]{
                        "/category/list",
                        "/addressBook/*",
                        "/addressBook",
                        "/dish/list",
                        "/order/submit",
                        "/order/userPage",
                        "/order/again",
                        "/setmeal/list",
                        "/shoppingCart/*",
                };
                if (check(requestURI, userUrls)) {
                    BaseContext.setUserCurrentId(userId);
                    filterChain.doFilter(request, response);
                    return;
                }
            }
        }
//        Long employeeId = (Long) redisTemplate.opsForValue().get(employeeticket);
//        if (check(requestURI, urls)) {
//            filterChain.doFilter(request, response);
//            return;
//        }
//        if (request.getSession().getAttribute("employee") != null) {
////            BaseContext.setCurrentId((Long) request.getSession().getAttribute("employee"));
//            String ticket = CookieUtil.getCookieValue(request, "ticket");
//            Long id = (Long) redisTemplate.opsForValue().get(ticket);
//            BaseContext.setCurrentId(id);
//            filterChain.doFilter(request, response);
//            return;
//        }
//        if (request.getSession().getAttribute("user") != null) {
////            BaseContext.setCurrentId((Long) request.getSession().getAttribute("user"));
//            String ticket = CookieUtil.getCookieValue(request, "ticket");
//            Long id = (Long) redisTemplate.opsForValue().get(ticket);
//            BaseContext.setCurrentId(id);
//            filterChain.doFilter(request, response);
//            return;
//        }
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
    }

    private boolean check(String uri, String[] urls) {
        for (String s : urls) {
            if (PATH_MATCHER.match(s, uri)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkNotIn(String uri, String[] urls) {
        for (String s : urls) {
            if (PATH_MATCHER.match(s, uri)) {
                return false;
            }
        }
        return true;
    }
//
//    private boolean canVisit(String uri, String[] urls){
//
//    }
}
