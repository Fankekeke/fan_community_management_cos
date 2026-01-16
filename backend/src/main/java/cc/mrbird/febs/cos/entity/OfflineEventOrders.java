package cc.mrbird.febs.cos.entity;

import java.time.LocalDateTime;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 线下活动订单表
 *
 * @author FanK
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class OfflineEventOrders implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 订单编号
     */
    private String orderSn;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 线下活动ID
     */
    private Integer eventId;

    /**
     * 核销二维码内容/原始码
     */
    private String checkCode;

    /**
     * 是否已核销: 0否, 1是
     */
    private String isChecked;

    /**
     * 核销时间
     */
    private String checkedAt;

    /**
     * 0待支付, 1已支付, 2已退款
     */
    private Integer paymentStatus;

    private String createdAt;


}
