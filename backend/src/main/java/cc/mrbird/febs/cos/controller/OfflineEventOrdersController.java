package cc.mrbird.febs.cos.controller;


import cc.mrbird.febs.common.utils.R;
import cc.mrbird.febs.cos.entity.OfflineEventOrders;
import cc.mrbird.febs.cos.service.IOfflineEventOrdersService;
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
@RequestMapping("/cos/offline-event-orders")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OfflineEventOrdersController {

    private final IOfflineEventOrdersService offlineEventOrdersService;

    /**
     * 根据分页和筛选条件获取线下活动订单信息
     *
     * @param page               分页对象
     * @param offlineEventOrders 线下活动订单信息
     * @return 结果
     */
    @GetMapping("/page")
    public R page(Page<OfflineEventOrders> page, OfflineEventOrders offlineEventOrders) {
        return R.ok(offlineEventOrdersService.qeuryPage(page, offlineEventOrders));
    }

    /**
     * 通过ID获取线下活动订单详细信息
     *
     * @param id 线下活动订单ID
     * @return 结果
     */
    @GetMapping("/{id}")
    public R detail(@PathVariable("id") Integer id) {
        return R.ok(offlineEventOrdersService.getById(id));
    }

    /**
     * 列出所有线下活动订单信息
     *
     * @return 结果
     */
    @GetMapping("/list")
    public R list() {
        return R.ok(offlineEventOrdersService.list());
    }

    /**
     * 新增线下活动订单信息
     *
     * @param offlineEventOrders 线下活动订单信息
     * @return 结果
     */
    @PostMapping
    public R save(OfflineEventOrders offlineEventOrders) {
        offlineEventOrders.setCreatedAt(DateUtil.formatDateTime(new Date()));
        return R.ok(offlineEventOrdersService.save(offlineEventOrders));
    }

    /**
     * 修改线下活动订单信息
     *
     * @param offlineEventOrders 线下活动订单信息
     * @return 结果
     */
    @PutMapping
    public R edit(OfflineEventOrders offlineEventOrders) {
        return R.ok(offlineEventOrdersService.updateById(offlineEventOrders));
    }

    /**
     * 删除线下活动订单信息
     *
     * @param ids ids
     * @return 线下活动订单信息
     */
    @DeleteMapping("/{ids}")
    public R deleteByIds(@PathVariable("ids") List<Integer> ids) {
        return R.ok(offlineEventOrdersService.removeByIds(ids));
    }
}
