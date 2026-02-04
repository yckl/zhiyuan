package com.volunteer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.dto.ExperienceDTO;
import com.volunteer.dto.ExperienceRequest;
import com.volunteer.entity.Experience;

/**
 * 心得服务接口
 */
public interface ExperienceService {

    /**
     * 发布心得
     */
    Experience createExperience(ExperienceRequest request, Long volunteerId);

    /**
     * 更新心得
     */
    void updateExperience(ExperienceRequest request, Long volunteerId);

    /**
     * 获取心得详情
     */
    ExperienceDTO getExperienceDetail(Long id);

    /**
     * 分页查询心得
     */
    Page<ExperienceDTO> pageExperiences(Integer page, Integer size, Long activityId, Long volunteerId, Integer status);

    /**
     * 分页查询我的心得 (根据userId)
     */
    Page<ExperienceDTO> pageMyExperiences(Integer page, Integer size, Long userId);

    /**
     * 删除心得
     */
    void deleteExperience(Long id, Long volunteerId);

    /**
     * 增加浏览次数
     */
    void incrementViewCount(Long id);

    /**
     * 点赞/取消点赞心得
     * 
     * @return true-已点赞, false-已取消
     */
    boolean toggleLike(Long experienceId, Long userId);

    /**
     * 检查是否已点赞
     */
    boolean checkLiked(Long experienceId, Long userId);
}
