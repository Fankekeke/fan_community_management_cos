package cc.mrbird.febs.cos.entity;

import java.time.LocalDateTime;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 线上活动访问记录表
 *
 * @author FanK
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class OnlineEventLogs implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 线上活动ID
     */
    private Integer eventId;

    /**
     * 进入时间
     */
    private String enterTime;

    /**
     * 离开时间
     */
    private String leaveTime;

    /**
     * 用户IP
     */
    private String ipAddress;

    /**
     * 线上活动标题
     */
    @TableField(exist = false)
    private String title;

    @TableField(exist = false)
    private String username;


}
