package com.volunteer.service;

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
     * 获取评论列表（树形结构）
     */
    List<CommentDTO> getCommentList(String targetType, Long targetId);

    /**
     * 删除评论
     */
    void deleteComment(Long id, Long userId);

    /**
     * 点赞评论
     */
    void likeComment(Long id, Long userId);
}
