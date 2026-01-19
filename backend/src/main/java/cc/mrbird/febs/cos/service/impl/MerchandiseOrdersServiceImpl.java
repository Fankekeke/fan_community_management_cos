package cc.mrbird.febs.cos.service.impl;

import cc.mrbird.febs.cos.entity.MerchandiseOrders;
import cc.mrbird.febs.cos.dao.MerchandiseOrdersMapper;
import cc.mrbird.febs.cos.service.IMerchandiseOrdersService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

/**
 * @author FanK
 */
@Service
public class MerchandiseOrdersServiceImpl extends ServiceImpl<MerchandiseOrdersMapper, MerchandiseOrders> implements IMerchandiseOrdersService {

    /**
     * 根据分页和筛选条件获取用户订单信息
     *
     * @param page              分页对象
     * @param merchandiseOrders 用户订单信息
     * @return 结果
     */
    @Override
    public IPage<LinkedHashMap<String, Object>> queryPage(Page<MerchandiseOrders> page, MerchandiseOrders merchandiseOrders) {
        return baseMapper.queryPage(page, merchandiseOrders);
    }
}
