package cc.mrbird.febs.cos.controller;


import cc.mrbird.febs.common.utils.R;
import cc.mrbird.febs.cos.entity.*;
import cc.mrbird.febs.cos.service.*;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author FanK
 */
@RestController
@RequestMapping("/cos/user-info")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserInfoController {

    private final IUserInfoService userInfoService;

    private final IMerchandiseOrdersService merchandiseOrdersService;

    private final IOfflineEventOrdersService offlineEventOrdersService;

    private final IOnlineEventLogsService onlineEventLogsService;

    private final IPostInfoService postInfoService;

    @Resource
    private Generation generation;

    @GetMapping("/detail/{userId}")
    public R userDetail(@PathVariable("userId") Integer userId) {
        return R.ok(userInfoService.getOne(Wrappers.<UserInfo>lambdaQuery().eq(UserInfo::getUserId, userId)));
    }

    /**
     * 查询用户画像
     *
     * @param userId
     * @return
     */
    @GetMapping("/query-user-portrait/{userId}")
    public R queryUserPortrait(Integer userId) {
        UserInfo userInfo = userInfoService.getById(userId);
        List<MerchandiseOrders> ordersList = merchandiseOrdersService.list(Wrappers.<MerchandiseOrders>lambdaQuery().eq(MerchandiseOrders::getUserId, userId));
        List<OfflineEventOrders> offlineEventOrdersList = offlineEventOrdersService.list(Wrappers.<OfflineEventOrders>lambdaQuery().eq(OfflineEventOrders::getUserId, userId));
        List<OnlineEventLogs> onlineEventLogsList = onlineEventLogsService.list(Wrappers.<OnlineEventLogs>lambdaQuery().eq(OnlineEventLogs::getUserId, userId));
        List<PostInfo> postInfoList = postInfoService.list(Wrappers.<PostInfo>lambdaQuery().eq(PostInfo::getUserId, userInfo.getUserId()));

        // 构建用户画像描述
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append("用户画像分析：");
        contentBuilder.append("用户基本信息：姓名").append(userInfo.getName()).append("，性别").append(userInfo.getSex()).append("，个人介绍").append(userInfo.getContent()).append("。");
        contentBuilder.append("购买行为：共购买商品订单").append(ordersList.size()).append("笔，显示用户的消费偏好。");
        contentBuilder.append("线下活动参与：参加线下活动订单").append(offlineEventOrdersList.size()).append("次，反映用户的社交活跃度。");
        contentBuilder.append("线上活动参与：参与线上活动").append(onlineEventLogsList.size()).append("次，体现用户的数字化参与程度。");
        contentBuilder.append("社区贡献：发布帖子").append(postInfoList.size()).append("篇，展示用户在社区的活跃度。");
        contentBuilder.append("请根据以上信息分析该用户的特征、兴趣爱好、消费习惯和社区参与度，并提出个性化的运营建议300字内。");

        Message userMessage = Message.builder()
                .role(Role.USER.getValue())
                .content(contentBuilder.toString())
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
                //启用互联网搜索，模型会将搜索结果作为文本生成过程中的参考信息，但模型会基于其内部逻辑"自行判断"是否使用互联网搜索结果。
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
        return R.ok(String.join("\n---\n", allContents));
    }

    /**
     * 分页查询用户信息
     *
     * @param page
     * @param userInfo
     * @return
     */
    @GetMapping("/page")
    public R page(Page page, UserInfo userInfo) {
        return R.ok();
    }

    @GetMapping("/list")
    public R list() {
        return R.ok(userInfoService.list());
    }

    /**
     * 新增用户信息
     *
     * @param userInfo
     * @return
     */
    @PostMapping
    public R save(UserInfo userInfo) {
        return R.ok(userInfoService.save(userInfo));
    }

    /**
     * 修改用户信息
     *
     * @param userInfo
     * @return
     */
    @PutMapping
    public R edit(UserInfo userInfo) {
        return R.ok(userInfoService.updateById(userInfo));
    }

    /**
     * 删除用户信息
     *
     * @param ids
     * @return
     */
    @DeleteMapping("/{ids}")
    public R deleteByIds(@PathVariable("ids") List<Integer> ids) {
        return R.ok(userInfoService.removeByIds(ids));
    }

}
