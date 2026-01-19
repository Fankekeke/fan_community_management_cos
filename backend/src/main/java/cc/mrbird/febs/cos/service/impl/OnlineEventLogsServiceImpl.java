package cc.mrbird.febs.cos.service.impl;

import cc.mrbird.febs.cos.entity.OnlineEventLogs;
import cc.mrbird.febs.cos.dao.OnlineEventLogsMapper;
import cc.mrbird.febs.cos.service.IOnlineEventLogsService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

/**
 * @author FanK
 */
@Service
public class OnlineEventLogsServiceImpl extends ServiceImpl<OnlineEventLogsMapper, OnlineEventLogs> implements IOnlineEventLogsService {


    /**
     * 根据分页和筛选条件获取线上活动访问信息
     *
     * @param page            分页对象
     * @param onlineEventLogs 线上活动访问信息
     * @return 结果
     */
    @Override
    public IPage<LinkedHashMap<String, Object>> queryPage(Page<OnlineEventLogs> page, OnlineEventLogs onlineEventLogs) {
        return baseMapper.queryPage(page, onlineEventLogs);
    }
}
