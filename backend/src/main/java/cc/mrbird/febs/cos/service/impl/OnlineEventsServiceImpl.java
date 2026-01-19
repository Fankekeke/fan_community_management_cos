package cc.mrbird.febs.cos.service.impl;

import cc.mrbird.febs.cos.entity.OnlineEvents;
import cc.mrbird.febs.cos.dao.OnlineEventsMapper;
import cc.mrbird.febs.cos.service.IOnlineEventsService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

/**
 * @author FanK
 */
@Service
public class OnlineEventsServiceImpl extends ServiceImpl<OnlineEventsMapper, OnlineEvents> implements IOnlineEventsService {

    /**
     * 根据分页和筛选条件获取线上活动信息
     *
     * @param page         分页对象
     * @param onlineEvents 线上活动信息
     * @return 结果
     */
    @Override
    public IPage<LinkedHashMap<String, Object>> queryPage(Page<OnlineEvents> page, OnlineEvents onlineEvents) {
        return baseMapper.queryPage(page, onlineEvents);
    }
}
