package com.example.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.example.reggie.entity.Orders;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        Object originalObject = metaObject.getOriginalObject();
        if (originalObject instanceof Orders) {
            metaObject.setValue("orderTime", LocalDateTime.now());
            metaObject.setValue("checkoutTime", LocalDateTime.now());
            metaObject.setValue("userId", BaseContext.getUserCurrentId());
        } else {
            metaObject.setValue("createTime", LocalDateTime.now());
            metaObject.setValue("updateTime", LocalDateTime.now());
            metaObject.setValue("createUser", BaseContext.getEmployeeCurrentId());
            metaObject.setValue("updateUser", BaseContext.getEmployeeCurrentId());
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("updateUser", BaseContext.getEmployeeCurrentId());
    }
}
