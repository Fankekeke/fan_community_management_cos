package cc.mrbird.febs.cos.dao;

import cc.mrbird.febs.cos.entity.OfflineEventOrders;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedHashMap;

/**
 * @author FanK
 */
public interface OfflineEventOrdersMapper extends BaseMapper<OfflineEventOrders> {

    /**
     * 根据分页和筛选条件获取线下活动订单信息
     *
     * @param page               分页对象
     * @param offlineEventOrders 线下活动订单信息
     * @return 结果
     */
    IPage<LinkedHashMap<String, Object>> queryPage(Page<OfflineEventOrders> page, @Param("queryParam") OfflineEventOrders offlineEventOrders);
}
