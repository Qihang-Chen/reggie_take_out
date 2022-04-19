package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.reggie.common.R;
import com.example.reggie.entity.User;
import com.example.reggie.service.UserService;
import com.example.reggie.utils.CookieUtil;
import com.example.reggie.utils.SMSUtils;
import com.example.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user) {
        String phone = user.getPhone();
        if (StringUtils.isNotEmpty(phone)) {
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code={}", code);
            //SMSUtils.sendMessage("瑞吉外卖","",phone,code);
            //session.setAttribute(phone,code);
            redisTemplate.opsForValue().set(phone, code, 5, TimeUnit.MINUTES);
            return R.success("短信发送成功");
        }
        return R.error("短信发送失败");
    }

    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpServletRequest request, HttpServletResponse response) {
        String phone = (String) map.get("phone");
        //String code = (String) request.getSession().getAttribute(phone);
        String code = (String) redisTemplate.opsForValue().get(phone);
        if (code != null && code.equals(map.get("code"))) {
            LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper();
            lambdaQueryWrapper.eq(User::getPhone, phone);
            User user = userService.getOne(lambdaQueryWrapper);
            if (user == null) {
                user = new User();
                user.setPhone(phone);
                user.setName("用户" + code);
                userService.save(user);
            }
            redisTemplate.delete(phone);
            String ticket = UUID.randomUUID().toString();
            CookieUtil.setCookie(request, response, "userticket", ticket, 6 * 60 * 60);
            redisTemplate.opsForValue().set(ticket, user.getId(), 6, TimeUnit.HOURS);
            //request.getSession().setAttribute("user", user.getId());
            return R.success(user);
        }
        return R.error("登录失败");
    }

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request, HttpServletResponse response) {
        String ticket = CookieUtil.getCookieValue(request, "userticket");
        redisTemplate.delete(ticket);
        CookieUtil.deleteCookie(request, response, "userticket");
        return R.success("退出成功");
    }

}
