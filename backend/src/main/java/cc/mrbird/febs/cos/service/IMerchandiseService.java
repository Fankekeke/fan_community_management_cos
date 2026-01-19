package cc.mrbird.febs.cos.service;

import cc.mrbird.febs.cos.entity.Merchandise;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedHashMap;

/**
 * @author FanK
 */
public interface IMerchandiseService extends IService<Merchandise> {

    /**
     * 根据分页和筛选条件获取周边商品信息
     *
     * @param page        分页对象
     * @param merchandise 周边商品信息
     * @return 结果
     */
    IPage<LinkedHashMap<String, Object>> queryPage(Page<Merchandise> page, Merchandise merchandise);
}
