package cc.mrbird.febs.cos.controller;


import cc.mrbird.febs.common.utils.R;
import cc.mrbird.febs.cos.entity.OfflineEvents;
import cc.mrbird.febs.cos.service.IOfflineEventsService;
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
@RequestMapping("/cos/offline-events")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OfflineEventsController {

    private final IOfflineEventsService offlineEventsService;

    /**
     * 根据分页和筛选条件获取线下活动信息
     *
     * @param page          分页对象
     * @param offlineEvents 线下活动信息
     * @return 结果
     */
    @GetMapping("/page")
    public R page(Page<OfflineEvents> page, OfflineEvents offlineEvents) {
        return R.ok();
    }

    /**
     * 通过ID获取线下活动详细信息
     *
     * @param id 线下活动ID
     * @return 结果
     */
    @GetMapping("/{id}")
    public R detail(@PathVariable("id") Integer id) {
        return R.ok(offlineEventsService.getById(id));
    }

    /**
     * 列出所有线下活动信息
     *
     * @return 结果
     */
    @GetMapping("/list")
    public R list() {
        return R.ok(offlineEventsService.list());
    }

    /**
     * 新增线下活动信息
     *
     * @param offlineEvents 线下活动信息
     * @return 结果
     */
    @PostMapping
    public R save(OfflineEvents offlineEvents) {
        offlineEvents.setCreatedAt(DateUtil.formatDateTime(new Date()));
        return R.ok(offlineEventsService.save(offlineEvents));
    }

    /**
     * 修改线下活动信息
     *
     * @param offlineEvents 线下活动信息
     * @return 结果
     */
    @PutMapping
    public R edit(OfflineEvents offlineEvents) {
        return R.ok(offlineEventsService.updateById(offlineEvents));
    }

    /**
     * 删除线下活动信息
     *
     * @param ids ids
     * @return 线下活动信息
     */
    @DeleteMapping("/{ids}")
    public R deleteByIds(@PathVariable("ids") List<Integer> ids) {
        return R.ok(offlineEventsService.removeByIds(ids));
    }
}
