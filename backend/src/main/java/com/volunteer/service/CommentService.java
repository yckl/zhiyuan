package com.volunteer.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.dto.CommentDTO;
import com.volunteer.dto.CommentRequest;
import com.volunteer.entity.Comment;

import java.util.List;

/**
 * 评论服务接口
 */
public interface CommentService {

    /**
     * 发表评论
     */
    Comment createComment(CommentRequest request, Long userId);

    /**
     * 获取评论列表（树形结构，分页）
     */
    Page<CommentDTO> getCommentList(String targetType, Long targetId, Integer page, Integer size);

    /**
     * 删除评论
     */
    void deleteComment(Long id, Long userId);

    /**
     * 点赞评论
     */
    void likeComment(Long id, Long userId);

    /**
     * 获取目标对象的平均评分
     */
    Double getAverageRating(String targetType, Long targetId);

    /**
     * 获取目标对象的绝对真实评论总数（包含嵌套子评论）
     */
    Long getCommentCount(String targetType, Long targetId);

    /**
     * 系统级隐藏功能：自动为随机活动生成海量嵌套的随机评论
     */
    void generateMockComments();
}
