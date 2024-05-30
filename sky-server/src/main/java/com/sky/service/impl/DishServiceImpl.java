package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 新增菜品和相应口味
     *
     * @param dishDTO
     */
    @Transactional//开启事务注解，两个表操作只能一起成功一起失败
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        //向菜品插入1条数据
        dishMapper.insert(dish);
        //获取insert语句生成的主键值
        Long dishId = dish.getId();
        //向口味表插入n条数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            dishFlavorMapper.insertBatch(flavors);
        }

    }


    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());//页码，一页的存储量
        Page<Dish> page = dishMapper.pageQuery(dishPageQueryDTO);//根据page对象获取到所有数据
        return new PageResult(page.getTotal(), page.getResult());//page.getTotal()是根据page获得的总记录数，page.getResult()是一页的数据，把他们封装为pageResult
    }

    /**
     * 菜品批量删除
     *
     * @param ids
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {
        //判断当前菜品是否可以删除，在售或在套餐不能删除
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            //是否在售
            if (dish.getStatus() == StatusConstant.ENABLE) {
                //当前菜品为在售 ，不能删除，抛异常
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //是否在套餐
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds != null && setmealIds.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        //for( Long id : ids){
        //这里出现过一个错误，在做的过程中，应该在方法中填id，不是ids，一个个来删，遍历每一个
        // dishMapper.deleteById(id);
        //删除口味
        // dishFlavorMapper.deleteByDishId(id);}

        dishMapper.deleteByIds(ids);
        dishFlavorMapper.deleteByDishIds(ids);
    }

    /**
     * 根据id来查询菜品和口味数据
     *
     * @param id
     * @return
     */

    public DishVO getByIdWithFlavor(Long id) {
        //根据id查询菜品数据
        Dish dish = dishMapper.getById(id);
        //根据菜品id查询口味数据
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);

        //将查询到的数据封装到vo
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }

    /**
     * 修改菜品和对应口味数据
     *
     * @param dishDTO
     */
    public void updtateWiithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        //先修改菜品表
        dishMapper.update(dish);
        //再根据菜品id删除口味数据
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        //获得口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        //重新插入口味数据
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
            //向口味表插入n条数据
            dishFlavorMapper.insertBatch(flavors);
        }
    }


    /**
     * 条件查询菜品和口味
     *
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d, dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }

    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    public List<Dish> list(Long categoryId) {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return dishMapper.list(dish);
    }

    /**
     * 菜品的起售和停售
     *
     * @param status
     * @param id
     */
    @Transactional
    public void startOrStop(Integer status, Long id) {

        List<Long> dishId = new ArrayList<Long>();
        dishId.add(id);

        //菜品停售，则包含菜品的套餐同时停售
        if (status == StatusConstant.DISABLE) {
            List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(dishId);
            if (setmealIds != null && setmealIds.size() > 0) {
                setmealIds.forEach(Id -> {
                    Setmeal setmeal = Setmeal.builder()
                            .id(Id)
                            .status(status)
                            .build();
                    setmealMapper.update(setmeal);
                });
            }
        }

        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.update(dish);
    }

}