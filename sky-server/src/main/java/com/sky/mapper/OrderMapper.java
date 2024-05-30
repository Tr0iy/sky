package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    /**
     * 用户下单，把整好的order数据插进去
     *
     * @param orders
     */
    void insert(Orders orders);

    /**
     * 分页条件查询并按下单时间排序
     *
     * @param ordersPageQueryDTO
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据id查询订单
     *
     * @param id
     * @return
     */
    @Select("select * from orders where id=#{id}")
    Orders getById(Long id);

    /*********
     * 取消订单
     * @param orders
     */
    void update(Orders orders);

    /**
     * 各个状态的订单数量统计
     *
     * @param deliveryInProgress
     * @return
     */
    @Select("select count(id) from orders where status=#{status}")
    Integer countStatus(Integer deliveryInProgress);

    /**
     * 根据订单号查询订单
     *
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 用于替换微信支付更新数据库状态的问题
     */
    @Update("update orders set status=#{orderStatus},pay_status=#{orderPaidStatus},checkout_time=#{check_out_time} where id=#{id}")
    void updateStatus(Integer orderStatus, Integer orderPaidStatus, LocalDateTime check_out_time, Long id);

    /**
     * 根据订单状态和下单时间查询时间
     *
     * @param status
     * @param orderTime
     * @return
     */
    @Select("select * from orders where status=#{status} and order_time=#{orderTime}")
    List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime orderTime);

    /**
     * 根据动态条件来查询营业额
     *
     * @param map
     * @return
     */
    Double sumByMap(Map map);

    /**
     * 根据动态条件统计订单数量
     *
     * @param map
     * @return
     */
    Integer countByMap(Map map);

    /**
     * 指定时间区间的销量排名
     *
     * @param begin
     * @param end
     * @return
     */
    List<GoodsSalesDTO> getSalesTop10(LocalDateTime begin, LocalDateTime end);
}
