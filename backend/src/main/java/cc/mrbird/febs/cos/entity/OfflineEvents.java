package cc.mrbird.febs.cos.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 线下活动表
 *
 * @author FanK
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class OfflineEvents implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 活动名称
     */
    private String title;

    /**
     * 活动地点
     */
    private String location;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 门票价格
     */
    private BigDecimal ticketPrice;

    /**
     * 总名额/票数
     */
    private Integer totalCapacity;

    /**
     * 剩余名额
     */
    private Integer remainingCapacity;

    /**
     * 活动日期
     */
    private String eventDate;

    /**
     * 活动详情介绍
     */
    private String description;

    private String createdAt;

    @TableField(exist = false)
    private String name;

}
