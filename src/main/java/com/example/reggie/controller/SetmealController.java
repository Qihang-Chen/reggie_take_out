package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie.common.R;
import com.example.reggie.entity.Setmeal;
import com.example.reggie.entity.SetmealDish;
import com.example.reggie.entity.SetmealDto;
import com.example.reggie.service.CategoryService;
import com.example.reggie.service.SetmealDishService;
import com.example.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/page")
    public R<Page> showPage(int page, int pageSize, String name) {
        Page<Setmeal> pageInfo = new Page<>(page,pageSize);
        Page<SetmealDto> pageDtoInfo = new Page<>();
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(StringUtils.isNotEmpty(name), Setmeal::getName, name);
        lambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(pageInfo, lambdaQueryWrapper);
        BeanUtils.copyProperties(pageInfo, pageDtoInfo, "records");
        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> dtoRecords = new ArrayList<>();
        LambdaQueryWrapper<SetmealDish> dtoLambdaQueryWrapper;
        for (Setmeal record : records) {
            SetmealDto dtoRecord = new SetmealDto();
            BeanUtils.copyProperties(record, dtoRecord);
            dtoLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dtoLambdaQueryWrapper.eq(SetmealDish::getSetmealId, dtoRecord.getId());
            List<SetmealDish> setmealDishes = setmealDishService.list(dtoLambdaQueryWrapper);
            dtoRecord.setSetmealDishes(setmealDishes);
            dtoRecord.setCategoryName(categoryService.getById(dtoRecord.getCategoryId()).getName());
            dtoRecords.add(dtoRecord);
        }
        pageDtoInfo.setRecords(dtoRecords);
        return R.success(pageDtoInfo);
    }

    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        setmealService.saveWithDish(setmealDto);
        return R.success("添加成功");
    }

    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable Long id){
        SetmealDto setmealDto = setmealService.getByIdWithDishes(id);
        return R.success(setmealDto);
    }

    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        setmealService.updateWithDish(setmealDto);
        return R.success("修改成功");
    }

    @PostMapping("status/{status}")
    public R<String> updateStatus(@PathVariable Integer status, Long[] ids){
        List<Setmeal> setmeals = setmealService.listByIds(Arrays.asList(ids));
        for (Setmeal setmeal : setmeals) {
            setmeal.setStatus(status);
        }
        setmealService.updateBatchById(setmeals);
        return R.success("修改成功");
    }

    @DeleteMapping
    public R<String> delete(Long[] ids){
        setmealService.deleteWithDish(ids);
        return R.success("删除成功");
    }

    @GetMapping("/list")
    public R<List<SetmealDto>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Setmeal::getCategoryId,setmeal.getCategoryId());
        lambdaQueryWrapper.eq(Setmeal::getStatus,setmeal.getStatus());
        List<Setmeal> list = setmealService.list(lambdaQueryWrapper);
        List<SetmealDto> dtoList = new ArrayList<>();
        for (Setmeal s : list) {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(s,setmealDto);
            LambdaQueryWrapper<SetmealDish> lambdaQueryWrapperWithDish = new LambdaQueryWrapper<>();
            lambdaQueryWrapperWithDish.eq(SetmealDish::getSetmealId,s.getId());
            List<SetmealDish> setmealDishes = setmealDishService.list(lambdaQueryWrapperWithDish);
            setmealDto.setSetmealDishes(setmealDishes);
            dtoList.add(setmealDto);
        }
        return R.success(dtoList);
    }
}
