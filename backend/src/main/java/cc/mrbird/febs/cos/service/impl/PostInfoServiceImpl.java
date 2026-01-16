package cc.mrbird.febs.cos.service.impl;

import cc.mrbird.febs.cos.entity.PostInfo;
import cc.mrbird.febs.cos.dao.PostInfoMapper;
import cc.mrbird.febs.cos.entity.UserInfo;
import cc.mrbird.febs.cos.service.IPostInfoService;
import cc.mrbird.febs.cos.service.IUserInfoService;
import cc.mrbird.febs.system.domain.User;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author FanK
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PostInfoServiceImpl extends ServiceImpl<PostInfoMapper, PostInfo> implements IPostInfoService {

    private final IUserInfoService userInfoService;

    /**
     * 分页获取系统用户信息
     * @param page
     * @param user
     * @return
     */
    @Override
    public IPage<LinkedHashMap<String, Object>> selectUserPage(Page page, User user) {
        return baseMapper.selectUserPage(page, user);
    }

    /**
     * 获取用户详情
     *
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    public LinkedHashMap<String, Object> queryUserDetail(Integer userId) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>() {
            {
                put("user", userInfoService.getOne(Wrappers.<UserInfo>lambdaQuery().eq(UserInfo::getUserId, userId)));
                put("post", baseMapper.queryPostByUser(userId));
            }
        };
        return result;
    }

    @Override
    public IPage<LinkedHashMap<String, Object>> postInfoByPage(Page page, PostInfo postInfo) {
        return baseMapper.postInfoByPage(page, postInfo);
    }

    @Override
    public List<LinkedHashMap<String, Object>> getPostByTag(Integer tagId) {
        return baseMapper.getPostByTag(tagId);
    }

    // 获取模块下的贴子
    @Override
    public List<LinkedHashMap<String, Object>> getPostByTagUser(Integer tagId, Integer userId) {
        return baseMapper.getPostByTagUser(tagId, userId);
    }

    @Override
    public LinkedHashMap<String, Object> postDetail(Integer postId) {
        return baseMapper.postDetail(postId);
    }

    @Override
    public List<LinkedHashMap<String, Object>> postByKey(String key) {
        return baseMapper.postByKey(key);
    }

    // 模糊查询帖子信息
    @Override
    public List<LinkedHashMap<String, Object>> listByUser(String key, Integer userId) {
        return baseMapper.listByUser(key, userId);
    }

    @Override
    public List<LinkedHashMap<String, Object>> recommend(Integer tagId, List<Long> collectUserIds) {
        return baseMapper.recommend(tagId, StringUtils.join(collectUserIds.toArray(), ","));
    }
}
