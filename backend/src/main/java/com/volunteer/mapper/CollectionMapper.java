package com.volunteer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.entity.Collection;
import com.volunteer.vo.CollectionVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 收藏 Mapper 接口
 */
@Mapper
public interface CollectionMapper extends BaseMapper<Collection> {

        /**
         * 检查是否已收藏
         */
        @Select("SELECT COUNT(*) FROM collection WHERE user_id = #{userId} AND target_type = #{targetType} AND target_id = #{targetId} AND is_deleted = 0")
        int checkCollection(@Param("userId") Long userId, @Param("targetType") String targetType,
                        @Param("targetId") Long targetId);

        /**
         * 查找已存在的收藏记录（包括已删除的）
         */
        @Select("SELECT * FROM collection WHERE user_id = #{userId} AND target_type = #{targetType} AND target_id = #{targetId} LIMIT 1")
        Collection findExistingCollection(@Param("userId") Long userId, @Param("targetType") String targetType,
                        @Param("targetId") Long targetId);

        /**
         * 恢复已删除的收藏
         */
        @Update("UPDATE collection SET is_deleted = 0, update_time = NOW() WHERE user_id = #{userId} AND target_type = #{targetType} AND target_id = #{targetId} AND is_deleted = 1")
        int restoreCollection(@Param("userId") Long userId, @Param("targetType") String targetType,
                        @Param("targetId") Long targetId);

        /**
         * 删除收藏（逻辑删除）
         */
        @Update("UPDATE collection SET is_deleted = 1, update_time = NOW() WHERE user_id = #{userId} AND target_type = #{targetType} AND target_id = #{targetId} AND is_deleted = 0")
        int deleteCollection(@Param("userId") Long userId, @Param("targetType") String targetType,
                        @Param("targetId") Long targetId);

        /**
         * 获取用户收藏的活动列表（联表查询）
         */
        @Select("""
                            SELECT c.id, c.user_id, c.target_type, c.target_id, c.create_time,
                                   a.title, a.cover_image, a.summary, a.start_time, a.end_time, a.location, a.status
                            FROM collection c
                            LEFT JOIN activity a ON c.target_id = a.id AND a.is_deleted = 0
                            WHERE c.user_id = #{userId} AND c.target_type = 'ACTIVITY' AND c.is_deleted = 0
                            ORDER BY c.create_time DESC
                        """)
        IPage<CollectionVO> getActivityCollections(Page<CollectionVO> page, @Param("userId") Long userId);

        /**
         * 获取用户收藏的课程列表（联表查询）
         */
        @Select("""
                            SELECT c.id, c.user_id, c.target_type, c.target_id, c.create_time,
                                   co.title, co.cover_image, co.summary, co.instructor, co.duration, co.status
                            FROM collection c
                            LEFT JOIN course co ON c.target_id = co.id AND co.is_deleted = 0
                            WHERE c.user_id = #{userId} AND c.target_type = 'COURSE' AND c.is_deleted = 0
                            ORDER BY c.create_time DESC
                        """)
        IPage<CollectionVO> getCourseCollections(Page<CollectionVO> page, @Param("userId") Long userId);

        /**
         * 获取用户收藏的公告列表（联表查询）
         */
        @Select("""
                            SELECT c.id, c.user_id, c.target_type, c.target_id, c.create_time,
                                   n.title, n.content as summary, n.publish_time, n.status
                            FROM collection c
                            LEFT JOIN notice n ON c.target_id = n.id AND n.is_deleted = 0
                            WHERE c.user_id = #{userId} AND c.target_type = 'NOTICE' AND c.is_deleted = 0
                            ORDER BY c.create_time DESC
                        """)
        IPage<CollectionVO> getNoticeCollections(Page<CollectionVO> page, @Param("userId") Long userId);

        /**
         * 获取用户收藏的心得列表（联表查询）
         */
        @Select("""
                            SELECT c.id, c.user_id, c.target_type, c.target_id, c.create_time,
                                   e.title, e.content as summary, e.images, e.status
                            FROM collection c
                            LEFT JOIN experience e ON c.target_id = e.id AND e.is_deleted = 0
                            WHERE c.user_id = #{userId} AND c.target_type = 'EXPERIENCE' AND c.is_deleted = 0
                            ORDER BY c.create_time DESC
                        """)
        IPage<CollectionVO> getExperienceCollections(Page<CollectionVO> page, @Param("userId") Long userId);

        /**
         * 获取用户所有收藏ID列表
         */
        @Select("SELECT target_id FROM collection WHERE user_id = #{userId} AND target_type = #{targetType} AND is_deleted = 0")
        List<Long> getUserCollectionIds(@Param("userId") Long userId, @Param("targetType") String targetType);
}
