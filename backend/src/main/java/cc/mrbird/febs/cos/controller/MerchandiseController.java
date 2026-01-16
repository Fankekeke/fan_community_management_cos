package cc.mrbird.febs.cos.controller;


import cc.mrbird.febs.common.utils.R;
import cc.mrbird.febs.cos.entity.Merchandise;
import cc.mrbird.febs.cos.service.IMerchandiseService;
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
@RequestMapping("/cos/merchandise")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MerchandiseController {

    private final IMerchandiseService merchandiseService;

    /**
     * 根据分页和筛选条件获取周边商品信息
     *
     * @param page        分页对象
     * @param merchandise 周边商品信息
     * @return 结果
     */
    @GetMapping("/page")
    public R page(Page<Merchandise> page, Merchandise merchandise) {
        return R.ok();
    }

    /**
     * 通过ID获取周边商品详细信息
     *
     * @param id 周边商品ID
     * @return 结果
     */
    @GetMapping("/{id}")
    public R detail(@PathVariable("id") Integer id) {
        return R.ok(merchandiseService.getById(id));
    }

    /**
     * 列出所有周边商品信息
     *
     * @return 结果
     */
    @GetMapping("/list")
    public R list() {
        return R.ok(merchandiseService.list());
    }

    /**
     * 新增周边商品信息
     *
     * @param merchandise 周边商品信息
     * @return 结果
     */
    @PostMapping
    public R save(Merchandise merchandise) {
        merchandise.setCreatedAt(DateUtil.formatDateTime(new Date()));
        return R.ok(merchandiseService.save(merchandise));
    }

    /**
     * 修改周边商品信息
     *
     * @param merchandise 周边商品信息
     * @return 结果
     */
    @PutMapping
    public R edit(Merchandise merchandise) {
        return R.ok(merchandiseService.updateById(merchandise));
    }

    /**
     * 删除周边商品信息
     *
     * @param ids ids
     * @return 周边商品信息
     */
    @DeleteMapping("/{ids}")
    public R deleteByIds(@PathVariable("ids") List<Integer> ids) {
        return R.ok(merchandiseService.removeByIds(ids));
    }
}
