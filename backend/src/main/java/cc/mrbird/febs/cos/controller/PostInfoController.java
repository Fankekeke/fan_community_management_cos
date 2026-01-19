package cc.mrbird.febs.cos.controller;


import cc.mrbird.febs.common.service.CacheService;
import cc.mrbird.febs.common.utils.R;
import cc.mrbird.febs.cos.entity.*;
import cc.mrbird.febs.cos.service.*;
import cc.mrbird.febs.system.domain.User;
import cc.mrbird.febs.system.service.UserService;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.houbb.sensitive.word.core.SensitiveWordHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author FanK
 */
@RestController
@RequestMapping("/cos/post-info")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PostInfoController {

    private final IPostInfoService postInfoService;

    private final ISensitiveInfoService sensitiveInfoService;

    private final ICollectInfoService collectInfoService;

    private final IMessageInfoService messageInfoService;

    private final IFocusInfoService focusInfoService;

    private final UserService userService;

    private final CacheService cacheService;

    private final IUserInfoService userInfoService;

    @Resource
    private Generation generation;

    /**
     * 分页获取系统用户信息
     *
     * @param page
     * @param user
     * @return
     */
    @GetMapping("/user/page")
    public R userPage(Page page, User user) {
        return R.ok(postInfoService.selectUserPage(page, user));
    }

    /**
     * 获取用户详情
     *
     * @param userId 用户ID
     * @return 结果
     */
    @GetMapping("/user/detail")
    public R queryUserDetail(Integer userId) {
        return R.ok(postInfoService.queryUserDetail(userId));
    }

    /**
     * 用户状态更改
     *
     * @param flag
     * @param userId
     * @return
     */
    @PostMapping("/user/audit")
    public R userStatusAudit(Integer flag, Long userId) throws Exception {
        userService.update(Wrappers.<User>lambdaUpdate().set(User::getStatus, flag).eq(User::getUserId, userId));
        User user = userService.getById(userId);
        // 重新将用户信息，用户角色信息，用户权限信息 加载到 redis中
        cacheService.saveUser(user.getUsername());
        cacheService.saveRoles(user.getUsername());
        cacheService.savePermissions(user.getUsername());
        return R.ok(true);
    }

    /**
     * 获取关注信息
     *
     * @param userId
     * @param postId
     * @return
     */
    @GetMapping("/collcet")
    public R collectByUser(Long userId, Integer postId) {
        PostInfo postInfo = postInfoService.getById(postId);
        LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>() {
            {
                put("collect", collectInfoService.count(Wrappers.<CollectInfo>lambdaQuery().eq(CollectInfo::getUserId, userId).eq(CollectInfo::getPostId, postId).eq(CollectInfo::getDeleteFlag, 0)));
                put("focus", focusInfoService.count(Wrappers.<FocusInfo>lambdaQuery().eq(FocusInfo::getUserId, userId).eq(FocusInfo::getCollectUserId, postInfo.getUserId()).eq(FocusInfo::getDeleteFlag, 0)));
            }
        };
        return R.ok(result);
    }

    /**
     * 获取模块下的贴子
     *
     * @param tagId
     * @return
     */
    @GetMapping("/tag/{tagId}")
    public R getPostByTag(@PathVariable("tagId") Integer tagId) {
        return R.ok(postInfoService.getPostByTag(tagId));
    }

    /**
     * 获取模块下的贴子
     *
     * @param tagId
     * @return
     */
    @GetMapping("/tagByUser")
    public R getPostByTagUser(Integer tagId, Integer userId) {
        return R.ok(postInfoService.getPostByTagUser(tagId, userId));
    }

    /**
     * 获取贴子详细信息
     *
     * @param postId
     * @return
     */
    @GetMapping("/{postId}")
    public R postDetail(@PathVariable("postId") Integer postId) {
        return R.ok(postInfoService.postDetail(postId));
    }

    /**
     * 分页获取帖子信息
     *
     * @param page
     * @param postInfo
     * @return
     */
    @GetMapping("/page")
    public R page(Page page, PostInfo postInfo) {
        return R.ok(postInfoService.postInfoByPage(page, postInfo));
    }

    /**
     * 模糊查询帖子信息
     *
     * @return
     */
    @GetMapping("/list/{key}")
    public R list(@PathVariable("key") String key) {
        return R.ok(postInfoService.postByKey(key));
    }

    /**
     * 模糊查询帖子信息
     *
     * @return
     */
    @GetMapping("/listByUser")
    public R listByUser(String key, Integer userId) {
        return R.ok(postInfoService.listByUser(key, userId));
    }

    /**
     * 推荐贴子
     *
     * @param userId
     * @return
     */
    @GetMapping("/recommend/{userId}")
    public R recommend(@PathVariable("userId") Integer userId) {
        Integer tagIds = -1;
        List<Long> collectUserIdList = new ArrayList<>();
        // 查询收藏贴子类型
        List<CollectInfo> collectInfoList = collectInfoService.list(Wrappers.<CollectInfo>lambdaQuery().eq(CollectInfo::getUserId, userId).eq(CollectInfo::getDeleteFlag, 0));
        if (collectInfoList.size() != 0) {
            PostInfo postInfo = postInfoService.getById(collectInfoList.get(0).getPostId());
            tagIds = Integer.valueOf(postInfo.getTagIds().split(",")[0]);
        }
        // 查询关注人
        List<FocusInfo> focusInfoList = focusInfoService.list(Wrappers.<FocusInfo>lambdaQuery().eq(FocusInfo::getUserId, userId));
        for (FocusInfo focusInfo : focusInfoList) {
            collectUserIdList.add(focusInfo.getCollectUserId());
        }
        return R.ok(postInfoService.recommend(tagIds, collectUserIdList));
    }

    /**
     * 添加帖子信息
     *
     * @param postInfo
     * @return
     */
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public R save(PostInfo postInfo) {
        String check = this.contentCheck(postInfo.getContent());
        String key = "请根据以下数据生成一份文章内容审查报告：" + postInfo.getContent() + "，100字以内";
        String contentMessage = queryContent(key);
        postInfo.setAiRemark(contentMessage);
        if (SensitiveWordHelper.contains(postInfo.getContent())) {
            postInfo.setContent(SensitiveWordHelper.replace(postInfo.getContent()));
        }
        if (StrUtil.isNotEmpty(check)) {
            return R.error(500, check);
        } else {
            // 获取发帖人信息
            UserInfo user = userInfoService.getOne(Wrappers.<UserInfo>lambdaQuery().eq(UserInfo::getUserId, postInfo.getUserId()));
            String username = user != null ? user.getName() : "用户";
            // 消息通知
            List<FocusInfo> focusInfoList = focusInfoService.list(Wrappers.<FocusInfo>lambdaQuery().eq(FocusInfo::getCollectUserId, postInfo.getUserId()).eq(FocusInfo::getDeleteFlag, 0));
            List<MessageInfo> messageInfoList = new ArrayList<>();
            String message = "您关注的" + username + "发布了新贴子 《" + postInfo.getTitle() + "》，快去回复吧";
            for (FocusInfo focusInfo : focusInfoList) {
                messageInfoList.add(new MessageInfo(focusInfo.getUserId(), message, DateUtil.formatDateTime(new Date()), 0));
            }
            messageInfoService.saveBatch(messageInfoList);
            postInfo.setCreateDate(DateUtil.formatDateTime(new Date()));
            postInfo.setDeleteFlag(0);
            postInfo.setPageviews(0);
            return R.ok(postInfoService.save(postInfo));
        }
    }

    public String queryContent(String key) {
        Message userMessage = Message.builder()
                .role(Role.USER.getValue())
                .content(key)
                .build();
        GenerationParam param = GenerationParam.builder()
                //指定用于对话的通义千问模型名
                .model("qwen-plus")
                .messages(Arrays.asList(userMessage))
                //
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                //生成过程中核采样方法概率阈值，例如，取值为0.8时，仅保留概率加起来大于等于0.8的最可能token的最小集合作为候选集。
                // 取值范围为（0,1.0)，取值越大，生成的随机性越高；取值越低，生成的确定性越高。
                .topP(0.8)
                //阿里云控制台DASHSCOPE获取的api-key
                .apiKey("sk-ebb4821588054a66aa1951d7f239f77c")
                //启用互联网搜索，模型会将搜索结果作为文本生成过程中的参考信息，但模型会基于其内部逻辑“自行判断”是否使用互联网搜索结果。
                .enableSearch(true)
                .build();
        GenerationResult generationResult = null;
        try {
            generationResult = generation.call(param);
        } catch (NoApiKeyException | InputRequiredException e) {
            throw new RuntimeException(e);
        }
        List<String> allContents = generationResult.getOutput().getChoices().stream()
                .map(choice -> choice.getMessage().getContent())
                .collect(Collectors.toList());

        return String.join("\n---\n", allContents);
    }

    /**
     * 修改帖子信息
     *
     * @param postInfo
     * @return
     */
    @PutMapping
    public R edit(PostInfo postInfo) {
        String check = this.contentCheck(postInfo.getContent());
        if (StrUtil.isNotEmpty(check)) {
            return R.error(500, check);
        } else {
            return R.ok(postInfoService.updateById(postInfo));
        }
    }

    /**
     * 删除帖子信息
     *
     * @param ids
     * @return
     */
    @DeleteMapping("/{ids}")
    public R deleteByIds(@PathVariable("ids") List<Integer> ids) {
        return R.ok(postInfoService.update(Wrappers.<PostInfo>lambdaUpdate().set(PostInfo::getDeleteFlag, 1).in(PostInfo::getId, ids)));
    }

    /**
     * 规范检查
     *
     * @param content
     */
    public String contentCheck(String content) {
        String result = "";
        List<SensitiveInfo> sensitiveInfoList = sensitiveInfoService.list(Wrappers.<SensitiveInfo>lambdaQuery().eq(SensitiveInfo::getDeleteFlag, 0));
        for (SensitiveInfo item : sensitiveInfoList) {
            if (content.contains(item.getKeyName())) {
                result = item.getKeyName() + "使用不规范，请更改";
            }
        }
        return result;
    }
}
