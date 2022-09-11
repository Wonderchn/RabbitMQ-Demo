package generate;

import generate.Deliveryman;

public interface DeliverymanDao {
    int deleteByPrimaryKey(Integer id);

    int insert(Deliveryman record);

    int insertSelective(Deliveryman record);

    Deliveryman selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Deliveryman record);

    int updateByPrimaryKey(Deliveryman record);
}