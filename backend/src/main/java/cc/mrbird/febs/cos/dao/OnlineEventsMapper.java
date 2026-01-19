package cc.mrbird.febs.cos.dao;

import cc.mrbird.febs.cos.entity.OnlineEvents;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedHashMap;

/**
 * @author FanK
 */
public interface OnlineEventsMapper extends BaseMapper<OnlineEvents> {

    /**
     * 根据分页和筛选条件获取线上活动信息
     *
     * @param page         分页对象
     * @param onlineEvents 线上活动信息
     * @return 结果
     */
    IPage<LinkedHashMap<String, Object>> queryPage(Page<OnlineEvents> page, @Param("queryParam") OnlineEvents onlineEvents);
}
