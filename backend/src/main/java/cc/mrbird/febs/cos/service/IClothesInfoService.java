package cc.mrbird.febs.cos.service;

import cc.mrbird.febs.cos.entity.ClothesInfo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.LinkedHashMap;

/**
 * @author FanK
 */
public interface IClothesInfoService extends IService<ClothesInfo> {

    /**
     * 分页查询官方动态信息
     *
     * @param page
     * @param clothesInfo
     * @return
     */
    IPage<LinkedHashMap<String, Object>> selectClothesPage(Page page, ClothesInfo clothesInfo);

}
