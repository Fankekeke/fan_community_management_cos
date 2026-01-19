package cc.mrbird.febs.cos.dao;

import cc.mrbird.febs.cos.entity.OfflineEvents;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedHashMap;

/**
 * @author FanK
 */
public interface OfflineEventsMapper extends BaseMapper<OfflineEvents> {

    /**
     * 根据分页和筛选条件获取线下活动信息
     *
     * @param page          分页对象
     * @param offlineEvents 线下活动信息
     * @return 结果
     */
    IPage<LinkedHashMap<String, Object>> queryPage(Page<OfflineEvents> page, @Param("queryParam") OfflineEvents offlineEvents);
}
