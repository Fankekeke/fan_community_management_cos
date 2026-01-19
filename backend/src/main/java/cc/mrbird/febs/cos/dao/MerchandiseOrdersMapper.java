package cc.mrbird.febs.cos.dao;

import cc.mrbird.febs.cos.entity.MerchandiseOrders;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedHashMap;

/**
 * @author FanK
 */
public interface MerchandiseOrdersMapper extends BaseMapper<MerchandiseOrders> {

    /**
     * 根据分页和筛选条件获取用户订单信息
     *
     * @param page              分页对象
     * @param merchandiseOrders 用户订单信息
     * @return 结果
     */
    IPage<LinkedHashMap<String, Object>> queryPage(Page<MerchandiseOrders> page, @Param("queryParam") MerchandiseOrders merchandiseOrders);
}
