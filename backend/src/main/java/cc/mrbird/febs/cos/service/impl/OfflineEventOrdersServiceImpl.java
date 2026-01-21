package cc.mrbird.febs.cos.service.impl;

import cc.mrbird.febs.cos.entity.OfflineEventOrders;
import cc.mrbird.febs.cos.dao.OfflineEventOrdersMapper;
import cc.mrbird.febs.cos.service.IOfflineEventOrdersService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

/**
 * @author FanK
 */
@Service
public class OfflineEventOrdersServiceImpl extends ServiceImpl<OfflineEventOrdersMapper, OfflineEventOrders> implements IOfflineEventOrdersService {

    /**
     * 根据分页和筛选条件获取线下活动订单信息
     *
     * @param page               分页对象
     * @param offlineEventOrders 线下活动订单信息
     * @return 结果
     */
    @Override
    public IPage<LinkedHashMap<String, Object>> queryPage(Page<OfflineEventOrders> page, OfflineEventOrders offlineEventOrders) {
        return baseMapper.queryPage(page, offlineEventOrders);
    }

    /**
     * 查询订单详情
     *
     * @param orderCode 订单ID
     * @return 结果
     */
    @Override
    public LinkedHashMap<String, Object> queryOrderDetailByCode(String orderCode) {
        return baseMapper.queryOrderDetailByCode(orderCode);
    }
}
