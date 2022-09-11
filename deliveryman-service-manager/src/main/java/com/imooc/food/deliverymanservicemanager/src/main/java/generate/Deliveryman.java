package generate;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * deliveryman
 * @author 
 */
@Data
public class Deliveryman implements Serializable {
    /**
     * 骑手id
     */
    private Integer id;

    /**
     * 名称
     */
    private String name;

    /**
     * 状态
     */
    private String status;

    /**
     * 时间
     */
    private Date date;

    private static final long serialVersionUID = 1L;
}