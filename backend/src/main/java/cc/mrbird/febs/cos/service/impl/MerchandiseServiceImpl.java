package cc.mrbird.febs.cos.service.impl;

import cc.mrbird.febs.cos.entity.Merchandise;
import cc.mrbird.febs.cos.dao.MerchandiseMapper;
import cc.mrbird.febs.cos.service.IMerchandiseService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

/**
 * @author FanK
 */
@Service
public class MerchandiseServiceImpl extends ServiceImpl<MerchandiseMapper, Merchandise> implements IMerchandiseService {

    /**
     * 根据分页和筛选条件获取周边商品信息
     *
     * @param page        分页对象
     * @param merchandise 周边商品信息
     * @return 结果
     */
    @Override
    public IPage<LinkedHashMap<String, Object>> queryPage(Page<Merchandise> page, Merchandise merchandise) {
        return baseMapper.queryPage(page, merchandise);
    }
}
