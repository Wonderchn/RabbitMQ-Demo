package com.imooc.food.deliverymanservicemanager;

import com.imooc.food.deliverymanservicemanager.Deliveryman;

public interface DeliverymanDao {
    int deleteByPrimaryKey(Integer id);

    int insert(Deliveryman record);

    int insertSelective(Deliveryman record);

    Deliveryman selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Deliveryman record);

    int updateByPrimaryKey(Deliveryman record);
}