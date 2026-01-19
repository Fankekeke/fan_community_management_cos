package cc.mrbird.febs.cos.service;

import cc.mrbird.febs.cos.entity.OnlineEventLogs;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.LinkedHashMap;

/**
 * @author FanK
 */
public interface IOnlineEventLogsService extends IService<OnlineEventLogs> {

    /**
     * 根据分页和筛选条件获取线上活动访问信息
     *
     * @param page            分页对象
     * @param onlineEventLogs 线上活动访问信息
     * @return 结果
     */
    IPage<LinkedHashMap<String, Object>> queryPage(Page<OnlineEventLogs> page, OnlineEventLogs onlineEventLogs);
}
