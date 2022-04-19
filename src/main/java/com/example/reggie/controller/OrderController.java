package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie.common.BaseContext;
import com.example.reggie.common.R;
import com.example.reggie.entity.OrderDetail;
import com.example.reggie.entity.Orders;
import com.example.reggie.entity.OrdersDto;
import com.example.reggie.service.OrderDetailService;
import com.example.reggie.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;

    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        ordersService.submit(orders);
        return R.success("提交订单成功");
    }

    @GetMapping("/userPage")
    public R<Page> userPage(int page, int pageSize) {
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        Page<OrdersDto> pageDtoInfo = new Page<>();
        LambdaQueryWrapper<Orders> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Orders::getUserId, BaseContext.getUserCurrentId());
        lambdaQueryWrapper.orderByDesc(Orders::getOrderTime);
        ordersService.page(pageInfo, lambdaQueryWrapper);
        BeanUtils.copyProperties(pageInfo, pageDtoInfo, "records");
        List<Orders> records = pageInfo.getRecords();
        List<OrdersDto> dtoRecords = new ArrayList<>();
        for (Orders record : records) {
            OrdersDto dtoRecord = new OrdersDto();
            BeanUtils.copyProperties(record, dtoRecord);
            LambdaQueryWrapper<OrderDetail> dtoLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dtoLambdaQueryWrapper.eq(OrderDetail::getOrderId, record.getId());
            dtoLambdaQueryWrapper.orderByDesc(OrderDetail::getAmount);
            List<OrderDetail> orderDetails = orderDetailService.list(dtoLambdaQueryWrapper);
            dtoRecord.setOrderDetails(orderDetails);
            dtoRecords.add(dtoRecord);
        }
        pageDtoInfo.setRecords(dtoRecords);
        return R.success(pageDtoInfo);
    }

    @GetMapping("/page")
    public R<Page> showPage(int page, int pageSize, Long id, String beginTime, String endTime) {
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        Page<OrdersDto> pageDtoInfo = new Page<>();
        LambdaQueryWrapper<Orders> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(id != null, Orders::getId, id);
        lambdaQueryWrapper.between(beginTime != null && endTime != null, Orders::getOrderTime, beginTime, endTime);
        ordersService.page(pageInfo, lambdaQueryWrapper);
        BeanUtils.copyProperties(pageInfo, pageDtoInfo, "records");
        List<Orders> records = pageInfo.getRecords();
        List<OrdersDto> dtoRecords = new ArrayList<>();
        for (Orders record : records) {
            OrdersDto dtoRecord = new OrdersDto();
            BeanUtils.copyProperties(record, dtoRecord);
            LambdaQueryWrapper<OrderDetail> lambdaQueryWrapperWithDetail = new LambdaQueryWrapper<>();
            lambdaQueryWrapperWithDetail.eq(OrderDetail::getOrderId, record.getId());
            List<OrderDetail> orderDetails = orderDetailService.list(lambdaQueryWrapperWithDetail);
            dtoRecord.setOrderDetails(orderDetails);
            dtoRecords.add(dtoRecord);
        }
        pageDtoInfo.setRecords(dtoRecords);
        return R.success(pageDtoInfo);
    }

    @PutMapping
    public R<String> updateStatus(@RequestBody Orders orders) {
        ordersService.updateById(orders);
        return R.success("修改成功");
    }

    @PostMapping("/again")
    public R<String> again(@RequestBody Orders orders) {
        ordersService.again(orders);
        return R.success("添加购物车成功");
    }
}
