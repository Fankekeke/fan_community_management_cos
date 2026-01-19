package cc.mrbird.febs.cos.service.impl;

import cc.mrbird.febs.cos.entity.OfflineEvents;
import cc.mrbird.febs.cos.dao.OfflineEventsMapper;
import cc.mrbird.febs.cos.service.IOfflineEventsService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

/**
 * @author FanK
 */
@Service
public class OfflineEventsServiceImpl extends ServiceImpl<OfflineEventsMapper, OfflineEvents> implements IOfflineEventsService {

    /**
     * 根据分页和筛选条件获取线下活动信息
     *
     * @param page          分页对象
     * @param offlineEvents 线下活动信息
     * @return 结果
     */
    @Override
    public IPage<LinkedHashMap<String, Object>> queryPage(Page<OfflineEvents> page, OfflineEvents offlineEvents) {
        return baseMapper.queryPage(page, offlineEvents);
    }
}
