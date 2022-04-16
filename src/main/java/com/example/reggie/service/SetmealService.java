package com.example.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.reggie.entity.Setmeal;
import com.example.reggie.entity.SetmealDto;

public interface SetmealService extends IService<Setmeal> {
    public void saveWithDish(SetmealDto setmealDto);

    public SetmealDto getByIdWithDishes(Long id);

    public void updateWithDish(SetmealDto setmealDto);

    public void deleteWithDish(Long[] ids);
}
