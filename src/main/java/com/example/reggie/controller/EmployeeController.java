package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie.common.R;
import com.example.reggie.entity.Employee;
import com.example.reggie.service.EmployeeService;
import com.example.reggie.utils.CookieUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, HttpServletResponse response, @RequestBody Employee employee) {
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        log.info(password);
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);
        if (emp == null) {
            return R.error("用户不存在");
        }
        log.info(emp.getPassword());
        if (!emp.getPassword().equals(password)) {
            return R.error("密码错误");
        }
        if (emp.getStatus() == 0) {
            return R.error("没有权限");
        }
//        request.getSession().setAttribute("employee", emp.getId());
        String ticket = UUID.randomUUID().toString();
        CookieUtil.setCookie(request, response, "employeeticket", ticket);
        redisTemplate.opsForValue().set(ticket, emp.getId(), 6, TimeUnit.HOURS);
        return R.success(emp);
    }

    @PostMapping("/logout")
    public R<String> loginout(HttpServletRequest request, HttpServletResponse response) {
//        request.getSession().removeAttribute("employee");
        String ticket = CookieUtil.getCookieValue(request, "employeeticket");
        redisTemplate.delete(ticket);
        CookieUtil.deleteCookie(request, response, "employeeticket");
        return R.success("退出成功");
    }

    @PostMapping
    public R<String> save(@RequestBody Employee employee) {
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        employeeService.save(employee);
        return R.success("创建成功");
    }

    @GetMapping("/page")
    public R<Page> showPage(int page, int pageSize, String name) {
        Page<Employee> pageInfo = new Page(page, pageSize);
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        employeeService.page(pageInfo, queryWrapper);
        log.info(String.valueOf(pageInfo.getTotal()));
        return R.success(pageInfo);
    }

    @PutMapping
    public R<String> update(@RequestBody Employee employee) {
        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }

    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id) {
        Employee employee = employeeService.getById(id);
        return R.success(employee);
    }


}
