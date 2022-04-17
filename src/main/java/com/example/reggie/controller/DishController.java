package com.example.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.reggie.entity.Category;
import com.example.reggie.entity.DishDto;
import com.example.reggie.common.R;
import com.example.reggie.entity.Dish;
import com.example.reggie.entity.DishFlavor;
import com.example.reggie.service.CategoryService;
import com.example.reggie.service.DishFlavorService;
import com.example.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/page")
    public R<Page> showPage(int page, int pageSize, String name) {
        Page<Dish> pageInfo = new Page(page, pageSize);
        Page<DishDto> pageDtoInfo = new Page<>();
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.like(StringUtils.isNotEmpty(name), Dish::getName, name);
        queryWrapper.orderByDesc(Dish::getSort);
        dishService.page(pageInfo, queryWrapper);
        BeanUtils.copyProperties(pageInfo, pageDtoInfo, "records");
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> dtoRecords = new ArrayList<>();
        for (Dish record : records) {
            DishDto dtoRecord = new DishDto();
            BeanUtils.copyProperties(record, dtoRecord);
            Long categoryId = record.getCategoryId();
            Category category = categoryService.getById(categoryId);
            dtoRecord.setCategoryName(category.getName());
            dtoRecords.add(dtoRecord);
        }
        pageDtoInfo.setRecords(dtoRecords);
        return R.success(pageDtoInfo);
    }

    @GetMapping("/{id}")
    public R<DishDto> getById(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    @PostMapping
    @CacheEvict(value = "dishCache",key = "#dishDto.categoryId+'_1'")
    public R<String> save(@RequestBody DishDto dishDto) {
        dishService.saveWithFlavor(dishDto);
//        String key = "dish_"+dishDto.getCategoryId()+"_1";
//        redisTemplate.delete(key);
        return R.success("添加成功");
    }

    @PostMapping("/status/{status}")
    @CacheEvict(value = "dishCache",allEntries = true)
    public R<String> updateStatus(@PathVariable Integer status, Long[] ids) {
        List<Dish> dishes = dishService.listByIds(Arrays.asList(ids));
        for (Dish dish : dishes) {
            dish.setStatus(status);
        }
        dishService.updateBatchById(dishes);
//        Set keys = redisTemplate.keys("dish_*");
//        redisTemplate.delete(keys);
        return R.success("修改成功");
    }

    @PutMapping
    @CacheEvict(value = "dishCache",key = "#dishDto.categoryId+'_1'")
    public R<String> update(@RequestBody DishDto dishDto) {
        dishService.updateWithFlavor(dishDto);
//        String key = "dish_"+dishDto.getCategoryId()+"_1";
//        redisTemplate.delete(key);
        return R.success("修改成功");
    }

    @DeleteMapping
    @CacheEvict(value = "dishCache",allEntries = true)
    public R<String> delete(Long[] ids) {
        dishService.deleteWithFlavor(ids);
//        Set keys = redisTemplate.keys("dish_*");
//        redisTemplate.delete(keys);
        return R.success("删除成功");
    }

    @GetMapping("/list")
    @Cacheable(value = "dishCache",key = "#dish.categoryId+'_'+#dish.status")
    public R<List<DishDto>> list(Dish dish) {
//        List<DishDto> dtoList = null;
//        String key = "dish_"+dish.getCategoryId()+"_"+dish.getStatus();
//        dtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
//        if (dtoList != null){
//            return R.success(dtoList);
//        }
        List<DishDto> dtoList = new ArrayList<>();
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Dish::getCategoryId, dish.getCategoryId());
        lambdaQueryWrapper.orderByDesc(Dish::getSort);
        List<Dish> list = dishService.list(lambdaQueryWrapper);
        for (Dish d : list) {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(d,dishDto);
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapperWithFlavor = new LambdaQueryWrapper<>();
            lambdaQueryWrapperWithFlavor.eq(DishFlavor::getDishId,d.getId());
            List<DishFlavor> flavors = dishFlavorService.list(lambdaQueryWrapperWithFlavor);
            dishDto.setFlavors(flavors);
            dtoList.add(dishDto);
        }
//        redisTemplate.opsForValue().set(key,dtoList,1, TimeUnit.HOURS);
        return R.success(dtoList);
    }
}
