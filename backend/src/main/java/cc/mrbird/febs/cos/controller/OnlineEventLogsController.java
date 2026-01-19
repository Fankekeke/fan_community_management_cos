package cc.mrbird.febs.cos.controller;


import cc.mrbird.febs.common.utils.R;
import cc.mrbird.febs.cos.entity.OnlineEventLogs;
import cc.mrbird.febs.cos.service.IOnlineEventLogsService;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * @author FanK
 */
@RestController
@RequestMapping("/cos/online-event-logs")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OnlineEventLogsController {

    private final IOnlineEventLogsService onlineEventLogsService;

    /**
     * 根据分页和筛选条件获取线上活动访问信息
     *
     * @param page            分页对象
     * @param onlineEventLogs 线上活动访问信息
     * @return 结果
     */
    @GetMapping("/page")
    public R page(Page<OnlineEventLogs> page, OnlineEventLogs onlineEventLogs) {
        return R.ok(onlineEventLogsService.queryPage(page, onlineEventLogs));
    }

    /**
     * 通过ID获取线上活动访问详细信息
     *
     * @param id 线上活动访问ID
     * @return 结果
     */
    @GetMapping("/{id}")
    public R detail(@PathVariable("id") Integer id) {
        return R.ok(onlineEventLogsService.getById(id));
    }

    /**
     * 列出所有线上活动访问信息
     *
     * @return 结果
     */
    @GetMapping("/list")
    public R list() {
        return R.ok(onlineEventLogsService.list());
    }

    /**
     * 新增线上活动访问信息
     *
     * @param onlineEventLogs 线上活动访问信息
     * @return 结果
     */
    @PostMapping
    public R save(OnlineEventLogs onlineEventLogs) {
        onlineEventLogs.setEnterTime(DateUtil.formatDateTime(new Date()));
        return R.ok(onlineEventLogsService.save(onlineEventLogs));
    }

    /**
     * 修改线上活动访问信息
     *
     * @param onlineEventLogs 线上活动访问信息
     * @return 结果
     */
    @PutMapping
    public R edit(OnlineEventLogs onlineEventLogs) {
        return R.ok(onlineEventLogsService.updateById(onlineEventLogs));
    }

    /**
     * 删除线上活动访问信息
     *
     * @param ids ids
     * @return 线上活动访问信息
     */
    @DeleteMapping("/{ids}")
    public R deleteByIds(@PathVariable("ids") List<Integer> ids) {
        return R.ok(onlineEventLogsService.removeByIds(ids));
    }
}
