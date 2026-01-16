package cc.mrbird.febs.cos.service;

import cc.mrbird.febs.cos.entity.ChatRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author FanK
 */
public interface IChatRecordService extends IService<ChatRecord> {

    /**
     * 根据对应聊天方ID获取沟通联系人列表
     *
     * @param hotelId 对应聊天方ID
     * @return 联系人列表
     */
    List<LinkedHashMap<String, Object>> getContactsByHotelId(Integer hotelId);

    /**
     * 根据用户ID获取沟通联系人列表
     *
     * @param userId 用户ID
     * @return 联系人列表
     */
    List<LinkedHashMap<String, Object>> getContactsByUserId(Integer userId);

    /**
     * 根据用户ID和对应聊天方ID获取聊天记录
     *
     * @param userId  用户ID
     * @param hotelId 对应聊天方ID
     * @return 结果
     */
    List<LinkedHashMap<String, Object>> getListByUserAndHotel(Integer userId, Integer hotelId);
}
