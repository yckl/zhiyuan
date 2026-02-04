package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.volunteer.dto.CommentDTO;
import com.volunteer.dto.CommentRequest;
import com.volunteer.entity.Comment;
import com.volunteer.entity.Experience;
import com.volunteer.entity.SysUser;
import com.volunteer.mapper.CommentMapper;
import com.volunteer.mapper.ExperienceMapper;
import com.volunteer.mapper.SysUserMapper;
import com.volunteer.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评论服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final SysUserMapper sysUserMapper;
    private final ExperienceMapper experienceMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Comment createComment(CommentRequest request, Long userId) {
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setTargetType(request.getTargetType());
        comment.setTargetId(request.getTargetId());
        comment.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        comment.setReplyToId(request.getReplyToId());
        comment.setReplyToUserId(request.getReplyToUserId());
        comment.setContent(request.getContent());
        comment.setLikeCount(0);
        comment.setStatus(Comment.STATUS_NORMAL);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        comment.setIsDeleted(0);

        commentMapper.insert(comment);

        // 更新心得评论数
        if (Comment.TARGET_EXPERIENCE.equals(request.getTargetType())) {
            Experience experience = experienceMapper.selectById(request.getTargetId());
            if (experience != null) {
                experience.setCommentCount(experience.getCommentCount() + 1);
                experienceMapper.updateById(experience);
            }
        }

        log.info("发表评论: id={}, targetType={}, targetId={}",
                comment.getId(), request.getTargetType(), request.getTargetId());

        return comment;
    }

    @Override
    public List<CommentDTO> getCommentList(String targetType, Long targetId) {
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getTargetType, targetType)
                .eq(Comment::getTargetId, targetId)
                .eq(Comment::getStatus, Comment.STATUS_NORMAL)
                .orderByAsc(Comment::getCreateTime);

        List<Comment> comments = commentMapper.selectList(queryWrapper);

        // 转换为DTO
        List<CommentDTO> dtoList = comments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // 构建树形结构
        return buildCommentTree(dtoList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long id, Long userId) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }

        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("只能删除自己的评论");
        }

        commentMapper.deleteById(id);

        // 更新心得评论数
        if (Comment.TARGET_EXPERIENCE.equals(comment.getTargetType())) {
            Experience experience = experienceMapper.selectById(comment.getTargetId());
            if (experience != null && experience.getCommentCount() > 0) {
                experience.setCommentCount(experience.getCommentCount() - 1);
                experienceMapper.updateById(experience);
            }
        }

        log.info("删除评论: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likeComment(Long id, Long userId) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }

        comment.setLikeCount(comment.getLikeCount() + 1);
        commentMapper.updateById(comment);

        log.info("点赞评论: id={}, userId={}", id, userId);
    }

    private CommentDTO convertToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        BeanUtils.copyProperties(comment, dto);

        // 查询用户信息
        SysUser user = sysUserMapper.selectById(comment.getUserId());
        if (user != null) {
            dto.setUsername(user.getUsername());
            dto.setUserAvatar(user.getAvatar());
        }

        // 查询回复用户信息
        if (comment.getReplyToUserId() != null) {
            SysUser replyToUser = sysUserMapper.selectById(comment.getReplyToUserId());
            if (replyToUser != null) {
                dto.setReplyToUsername(replyToUser.getUsername());
            }
        }

        return dto;
    }

    /**
     * 构建评论树形结构
     */
    private List<CommentDTO> buildCommentTree(List<CommentDTO> comments) {
        // 分组：顶级评论和子评论
        Map<Long, List<CommentDTO>> childrenMap = comments.stream()
                .filter(c -> c.getParentId() != null && c.getParentId() > 0)
                .collect(Collectors.groupingBy(CommentDTO::getParentId));

        // 设置子评论
        List<CommentDTO> rootComments = new ArrayList<>();
        for (CommentDTO comment : comments) {
            if (comment.getParentId() == null || comment.getParentId() == 0) {
                comment.setChildren(childrenMap.getOrDefault(comment.getId(), new ArrayList<>()));
                rootComments.add(comment);
            }
        }

        return rootComments;
    }
}
