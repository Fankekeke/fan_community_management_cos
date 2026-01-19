package cc.mrbird.febs.cos.controller;


import cc.mrbird.febs.common.utils.R;
import cc.mrbird.febs.cos.entity.MerchandiseOrders;
import cc.mrbird.febs.cos.service.IMerchandiseOrdersService;
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
@RequestMapping("/cos/merchandiseOrders-orders")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MerchandiseOrdersController {

    private final IMerchandiseOrdersService merchandiseOrdersService;

    /**
     * 根据分页和筛选条件获取用户订单信息
     *
     * @param page              分页对象
     * @param merchandiseOrders 用户订单信息
     * @return 结果
     */
    @GetMapping("/page")
    public R page(Page<MerchandiseOrders> page, MerchandiseOrders merchandiseOrders) {
        return R.ok(merchandiseOrdersService.queryPage(page, merchandiseOrders));
    }

    /**
     * 通过ID获取用户订单详细信息
     *
     * @param id 用户订单ID
     * @return 结果
     */
    @GetMapping("/{id}")
    public R detail(@PathVariable("id") Integer id) {
        return R.ok(merchandiseOrdersService.getById(id));
    }

    /**
     * 列出所有用户订单信息
     *
     * @return 结果
     */
    @GetMapping("/list")
    public R list() {
        return R.ok(merchandiseOrdersService.list());
    }

    /**
     * 新增用户订单信息
     *
     * @param merchandiseOrders 用户订单信息
     * @return 结果
     */
    @PostMapping
    public R save(MerchandiseOrders merchandiseOrders) {
        merchandiseOrders.setCreatedAt(DateUtil.formatDateTime(new Date()));
        return R.ok(merchandiseOrdersService.save(merchandiseOrders));
    }

    /**
     * 修改用户订单信息
     *
     * @param merchandiseOrders 用户订单信息
     * @return 结果
     */
    @PutMapping
    public R edit(MerchandiseOrders merchandiseOrders) {
        return R.ok(merchandiseOrdersService.updateById(merchandiseOrders));
    }

    /**
     * 删除用户订单信息
     *
     * @param ids ids
     * @return 用户订单信息
     */
    @DeleteMapping("/{ids}")
    public R deleteByIds(@PathVariable("ids") List<Integer> ids) {
        return R.ok(merchandiseOrdersService.removeByIds(ids));
    }
}
