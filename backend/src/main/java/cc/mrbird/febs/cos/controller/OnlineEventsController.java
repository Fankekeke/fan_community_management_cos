package cc.mrbird.febs.cos.controller;


import cc.mrbird.febs.common.utils.R;
import cc.mrbird.febs.cos.entity.OnlineEvents;
import cc.mrbird.febs.cos.service.IOnlineEventsService;
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
@RequestMapping("/cos/online-events")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OnlineEventsController {

    private final IOnlineEventsService onlineEventsService;

    /**
     * 根据分页和筛选条件获取线上活动信息
     *
     * @param page         分页对象
     * @param onlineEvents 线上活动信息
     * @return 结果
     */
    @GetMapping("/page")
    public R page(Page<OnlineEvents> page, OnlineEvents onlineEvents) {
        return R.ok(onlineEventsService.queryPage(page, onlineEvents));
    }

    /**
     * 通过ID获取线上活动详细信息
     *
     * @param id 线上活动ID
     * @return 结果
     */
    @GetMapping("/{id}")
    public R detail(@PathVariable("id") Integer id) {
        return R.ok(onlineEventsService.getById(id));
    }

    /**
     * 列出所有线上活动信息
     *
     * @return 结果
     */
    @GetMapping("/list")
    public R list() {
        return R.ok(onlineEventsService.list());
    }

    /**
     * 新增线上活动信息
     *
     * @param onlineEvents 线上活动信息
     * @return 结果
     */
    @PostMapping
    public R save(OnlineEvents onlineEvents) {
        onlineEvents.setCreatedAt(DateUtil.formatDateTime(new Date()));
        return R.ok(onlineEventsService.save(onlineEvents));
    }

    /**
     * 修改线上活动信息
     *
     * @param onlineEvents 线上活动信息
     * @return 结果
     */
    @PutMapping
    public R edit(OnlineEvents onlineEvents) {
        return R.ok(onlineEventsService.updateById(onlineEvents));
    }

    /**
     * 删除线上活动信息
     *
     * @param ids ids
     * @return 线上活动信息
     */
    @DeleteMapping("/{ids}")
    public R deleteByIds(@PathVariable("ids") List<Integer> ids) {
        return R.ok(onlineEventsService.removeByIds(ids));
    }
}
