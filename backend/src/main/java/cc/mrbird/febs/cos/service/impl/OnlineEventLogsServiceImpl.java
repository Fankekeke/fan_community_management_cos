package cc.mrbird.febs.cos.service.impl;

import cc.mrbird.febs.cos.entity.OnlineEventLogs;
import cc.mrbird.febs.cos.dao.OnlineEventLogsMapper;
import cc.mrbird.febs.cos.service.IOnlineEventLogsService;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;

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

    /**
     * 获取线上活动访问信息占比
     *
     * @param eventId 活动ID
     * @return 结果
     */
    @Override
    public LinkedHashMap<String, Object> queryRateByEvent(Integer eventId) {
        List<OnlineEventLogs> list = this.list(Wrappers.<OnlineEventLogs>lambdaQuery()
                .eq(OnlineEventLogs::getEventId, eventId));

        if (list.isEmpty()) {
            return new LinkedHashMap<>();
        }
        // 统计不同类型的访问情况
        long totalVisits = list.size();
        long uniqueVisitors = list.stream()
                .map(OnlineEventLogs::getUserId)
                .distinct()
                .count();
        // 访问时段分析
        LinkedHashMap<String, Long> hourlyAccessDistribution = analyzeHourlyAccess(list);
        // 用户参与度 - 平均观看时长
        double avgWatchDuration = calculateAverageWatchDuration(list);
        // 流失率统计
        double retentionRate = calculateRetentionRate(list);

        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        result.put("totalVisits", totalVisits);                    // 总访问次数
        result.put("uniqueVisitors", uniqueVisitors);              // 独立访客数
        result.put("eventParticipationRate", String.format("%.2f%%",
                (double) uniqueVisitors / totalVisits * 100));     // 参与率
        result.put("hourlyAccessDistribution", hourlyAccessDistribution); // 不同时段访问分布
        result.put("avgWatchDuration", avgWatchDuration);          // 平均观看时长
        result.put("retentionRate", String.format("%.2f%%", retentionRate * 100)); // 留存率
        result.put("dropOffRate", String.format("%.2f%%", (1 - retentionRate) * 100)); // 流失率

        return result;
    }

    /**
     * 分析不同时段的访问分布
     *
     * @param logs 日志列表
     * @return 小时级别的访问分布
     */
    private LinkedHashMap<String, Long> analyzeHourlyAccess(List<OnlineEventLogs> logs) {
        LinkedHashMap<String, Long> hourlyDistribution = new LinkedHashMap<>();

        // 初始化24小时的数据
        for (int i = 0; i < 24; i++) {
            hourlyDistribution.put(String.format("%02d:00-%02d:59", i, i), 0L);
        }

        // 按小时统计访问量
        logs.stream()
                .filter(log -> StrUtil.isNotEmpty(log.getEnterTime())) // 过滤无效时间数据
                .collect(java.util.stream.Collectors.groupingBy(
                        log -> {
                            try {
                                return DateUtil.parseDateTime(log.getEnterTime()).getHours();
                            } catch (Exception e) {
                                // 如果解析失败，默认归入0时
                                return 0;
                            }
                        }))
                .forEach((hour, hourLogs) -> {
                    String hourRange = String.format("%02d:00-%02d:59", hour, hour);
                    hourlyDistribution.put(hourRange, (long) hourLogs.size());
                });

        return hourlyDistribution;
    }

    /**
     * 计算平均观看时长
     *
     * @param logs 日志列表
     * @return 平均观看时长（分钟）
     */
    private double calculateAverageWatchDuration(List<OnlineEventLogs> logs) {
        if (logs.isEmpty()) {
            return 0.0;
        }
        // 基于enterTime和leaveTime计算观看时长
        double totalDuration = logs.stream()
                .filter(log -> StrUtil.isNotEmpty(log.getEnterTime()) && StrUtil.isNotEmpty(log.getLeaveTime()))
                .mapToDouble(log -> {
                    try {
                        DateTime enterDateTime = DateUtil.parseDateTime(log.getEnterTime());
                        DateTime leaveDateTime = DateUtil.parseDateTime(log.getLeaveTime());
                        // 计算进入时间和离开时间之间的差值（分钟）
                        long diffInMillis = leaveDateTime.getTime() - enterDateTime.getTime();
                        long diffInMinutes = diffInMillis / (1000 * 60); // 转换为分钟
                        return Math.max(0, diffInMinutes); // 确保不会出现负数
                    } catch (Exception e) {
                        // 如果时间格式错误或解析失败，返回0
                        return 0.0;
                    }
                })
                .sum();
        long validRecordsCount = logs.stream()
                .filter(log -> StrUtil.isNotEmpty(log.getEnterTime()) && StrUtil.isNotEmpty(log.getLeaveTime()))
                .count();
        return validRecordsCount > 0 ? totalDuration / validRecordsCount : 0.0;
    }

    /**
     * 计算留存率（假设通过重复访问判断用户粘性）
     *
     * @param logs 日志列表
     * @return 留存率
     */
    private double calculateRetentionRate(List<OnlineEventLogs> logs) {
        if (logs.isEmpty()) {
            return 0.0;
        }
        // 计算重复访问用户数量
        long totalUsers = logs.stream()
                .map(OnlineEventLogs::getUserId)
                .distinct()
                .count();
        long returningUsers = logs.stream()
                .collect(java.util.stream.Collectors.groupingBy(OnlineEventLogs::getUserId))
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().size() > 1) // 多次访问的用户
                .count();
        return totalUsers > 0 ? (double) returningUsers / totalUsers : 0.0;
    }
}
