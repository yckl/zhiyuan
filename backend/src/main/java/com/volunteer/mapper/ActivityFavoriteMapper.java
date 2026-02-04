package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.volunteer.entity.ActivityFavorite;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 活动收藏Mapper接口
 */
@Mapper
public interface ActivityFavoriteMapper extends BaseMapper<ActivityFavorite> {

    /**
     * 检查用户是否收藏了某活动
     */
    @Select("SELECT COUNT(*) FROM activity_favorite WHERE user_id = #{userId} AND activity_id = #{activityId}")
    int checkFavorite(Long userId, Long activityId);

    /**
     * 删除收藏记录
     */
    @Delete("DELETE FROM activity_favorite WHERE user_id = #{userId} AND activity_id = #{activityId}")
    int deleteFavorite(Long userId, Long activityId);

    /**
     * 获取用户收藏的活动ID列表
     */
    @Select("SELECT activity_id FROM activity_favorite WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<Long> getUserFavoriteActivityIds(Long userId);
}
