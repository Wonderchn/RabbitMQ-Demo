package com.imooc.food.restaurantservicemanager.po;

import com.imooc.food.restaurantservicemanager.enummeration.RestaurantStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class RestaurantPO {
    /**
     * 餐厅ID
     */
    private Integer id;
    /**
     * 名称
     */
    private String name;
    /**
     * 地址
     */
    private String address;
    /**
     * 餐厅状态
     */
    private RestaurantStatus status;
    /**
     * 日期
     */
    private Date date;
}
