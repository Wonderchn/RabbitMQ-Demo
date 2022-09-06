package com.imooc.food.restaurantservicemanager.po;

import com.imooc.food.restaurantservicemanager.enummeration.ProductStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@ToString
public class ProductPO {
    /**
     * 产品id
     */
    private Integer id;
    /**
     * 名称
     */
    private String name;
    /**
     * 单价
     */
    private BigDecimal price;
    /**
     * 餐厅ID
     */
    private Integer restaurantId;
    /**
     * 产品状态
     */
    private ProductStatus status;
    /**
     * 日期
     */
    private Date date;
}
