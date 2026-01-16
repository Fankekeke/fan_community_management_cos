package cc.mrbird.febs.cos.service.impl;

import cc.mrbird.febs.cos.entity.Merchandise;
import cc.mrbird.febs.cos.dao.MerchandiseMapper;
import cc.mrbird.febs.cos.service.IMerchandiseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author FanK
 */
@Service
public class MerchandiseServiceImpl extends ServiceImpl<MerchandiseMapper, Merchandise> implements IMerchandiseService {

}
