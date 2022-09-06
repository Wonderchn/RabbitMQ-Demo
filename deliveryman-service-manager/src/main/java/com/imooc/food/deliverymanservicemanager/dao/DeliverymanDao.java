package com.imooc.food.deliverymanservicemanager.dao;

import com.imooc.food.deliverymanservicemanager.enummeration.DeliverymanStatus;
import com.imooc.food.deliverymanservicemanager.po.DeliverymanPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface DeliverymanDao {

    @Select("SELECT id,name,status,date FROM deliveryman WHERE id = #{id}")
    DeliverymanPO selectDeliveryman(Integer id);

    @Select("SELECT id,name,status,date FROM deliveryman WHERE status = #{status}")
    List<DeliverymanPO> selectAvaliableDeliveryman(DeliverymanStatus status);
}
