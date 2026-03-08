package com.volunteer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.volunteer.dto.CommentDTO;
import com.volunteer.dto.CommentRequest;
import com.volunteer.entity.Comment;
import com.volunteer.entity.Experience;
import com.volunteer.entity.SysMessage;
import com.volunteer.entity.SysUser;
import com.volunteer.entity.Volunteer;
import com.volunteer.mapper.CommentMapper;
import com.volunteer.mapper.ExperienceMapper;
import com.volunteer.mapper.SysUserMapper;
import com.volunteer.mapper.VolunteerMapper;
import com.volunteer.mapper.ActivityMapper;
import com.volunteer.service.CommentService;
import com.volunteer.service.SysMessageService;
import com.volunteer.util.SensitiveWordFilter;
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
    private final VolunteerMapper volunteerMapper;
    private final ActivityMapper activityMapper;
    private final SysMessageService sysMessageService;
    private final SensitiveWordFilter sensitiveWordFilter;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Comment createComment(CommentRequest request, Long userId) {
        // 敏感词检测
        if (sensitiveWordFilter.containsSensitiveWord(request.getContent())) {
            var hitWords = sensitiveWordFilter.getSensitiveWords(request.getContent());
            log.warn("评论包含敏感词: userId={}, words={}", userId, hitWords);
            throw new RuntimeException("评论包含敏感词汇，请修改后重试");
        }

        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setTargetType(request.getTargetType());
        comment.setTargetId(request.getTargetId());
        comment.setParentId(request.getParentId() != null ? request.getParentId() : 0L);
        comment.setReplyToId(request.getReplyToId());
        comment.setReplyToUserId(request.getReplyToUserId());
        comment.setContent(request.getContent());
        comment.setRating(request.getRating());
        comment.setLikeCount(0);
        comment.setStatus(Comment.STATUS_NORMAL);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        comment.setIsDeleted(0);

        commentMapper.insert(comment);

        // 更新心得评论数 & 发送互动通知
        if (Comment.TARGET_EXPERIENCE.equals(request.getTargetType())) {
            Experience experience = experienceMapper.selectById(request.getTargetId());
            if (experience != null) {
                experience.setCommentCount(experience.getCommentCount() + 1);
                experienceMapper.updateById(experience);

                // 发送评论通知给心得作者（排除自己评论自己）
                Volunteer author = volunteerMapper.selectById(experience.getVolunteerId());
                if (author != null && author.getUserId() != null && !author.getUserId().equals(userId)) {
                    String contentPreview = request.getContent();
                    if (contentPreview != null && contentPreview.length() > 20) {
                        contentPreview = contentPreview.substring(0, 20) + "...";
                    }
                    sysMessageService.sendMessage(
                            author.getUserId(),
                            userId,
                            "收到新评论",
                            "有人评论了您的心得：" + contentPreview,
                            SysMessage.TYPE_INTERACTION);
                    log.info("发送评论通知: authorUserId={}, experienceId={}", author.getUserId(), experience.getId());
                }
            }
        }

        log.info("发表评论: id={}, targetType={}, targetId={}",
                comment.getId(), request.getTargetType(), request.getTargetId());

        return comment;
    }

    @Override
    public Page<CommentDTO> getCommentList(String targetType, Long targetId, Integer page, Integer size) {
        // 1. 分页查询根评论 (parentId = 0)
        Page<Comment> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Comment> rootQuery = new LambdaQueryWrapper<>();
        rootQuery.eq(Comment::getTargetType, targetType)
                .eq(Comment::getTargetId, targetId)
                .eq(Comment::getParentId, 0L)
                .eq(Comment::getStatus, Comment.STATUS_NORMAL)
                .orderByDesc(Comment::getCreateTime);

        Page<Comment> rootPage = commentMapper.selectPage(pageParam, rootQuery);
        List<Comment> rootComments = rootPage.getRecords();

        if (rootComments.isEmpty()) {
            return new Page<CommentDTO>(page, size).setTotal(0);
        }

        // 2. 提取根评论IDs，查询所有子评论
        List<Long> rootIds = rootComments.stream().map(Comment::getId).collect(Collectors.toList());
        LambdaQueryWrapper<Comment> childQuery = new LambdaQueryWrapper<>();
        childQuery.eq(Comment::getTargetType, targetType)
                .eq(Comment::getTargetId, targetId)
                .eq(Comment::getStatus, Comment.STATUS_NORMAL)
                .in(Comment::getParentId, rootIds)
                .orderByAsc(Comment::getCreateTime);

        List<Comment> childComments = commentMapper.selectList(childQuery);

        // 3. 将所有涉及的评论合并并转换为 DTO
        List<Comment> allComments = new ArrayList<>(rootComments);
        allComments.addAll(childComments);

        List<CommentDTO> dtoList = allComments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // 4. 构建树形结构并设置到分页对象返回
        List<CommentDTO> rootDtoList = buildCommentTree(dtoList);

        // 按照创建时间降序重排根评论（满足常见评论区最新在上）
        rootDtoList.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));

        Page<CommentDTO> resultPage = new Page<>(page, size);
        resultPage.setTotal(rootPage.getTotal());
        resultPage.setRecords(rootDtoList);

        return resultPage;
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

    @Override
    public Double getAverageRating(String targetType, Long targetId) {
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getTargetType, targetType)
                .eq(Comment::getTargetId, targetId)
                .eq(Comment::getStatus, Comment.STATUS_NORMAL)
                .eq(Comment::getParentId, 0L)
                .isNotNull(Comment::getRating);

        List<Comment> ratedComments = commentMapper.selectList(queryWrapper);
        if (ratedComments.isEmpty()) {
            return 0.0;
        }

        double sum = ratedComments.stream()
                .mapToInt(Comment::getRating)
                .sum();
        return Math.round(sum / ratedComments.size() * 10.0) / 10.0;
    }

    @Override
    public Long getCommentCount(String targetType, Long targetId) {
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getTargetType, targetType)
                .eq(Comment::getTargetId, targetId)
                .eq(Comment::getStatus, Comment.STATUS_NORMAL);
        return commentMapper.selectCount(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateMockComments() {
        List<com.volunteer.entity.Activity> activities = activityMapper.selectList(null);
        List<SysUser> users = sysUserMapper.selectList(null);

        if (activities.isEmpty() || users.isEmpty()) {
            log.warn("Cannot generate mock comments: missing activities or users.");
            return;
        }

        java.util.Random random = new java.util.Random();

        for (com.volunteer.entity.Activity act : activities) {
            // Generate 3 to 10 root comments
            int rootCount = 3 + random.nextInt(8);
            for (int i = 0; i < rootCount; i++) {
                SysUser rootUser = users.get(random.nextInt(users.size()));

                Comment rootComment = new Comment();
                rootComment.setUserId(rootUser.getId());
                rootComment.setTargetType(Comment.TARGET_ACTIVITY);
                rootComment.setTargetId(act.getId());
                rootComment.setParentId(0L);
                rootComment.setReplyToId(null);
                rootComment.setReplyToUserId(null);

                String[] rootPhrases = { "这个活动太棒了！", "已经报名啦，期待期待~", "有没有一起组队的伙伴？", "非常有意义的志愿活动。", "感谢组织方提供这么好的机会！" };
                rootComment.setContent(
                        rootPhrases[random.nextInt(rootPhrases.length)] + " [系统生成 " + random.nextInt(1000) + "]");
                rootComment.setRating(3 + random.nextInt(3)); // 3 to 5
                rootComment.setLikeCount(random.nextInt(100));
                rootComment.setStatus(Comment.STATUS_NORMAL);
                rootComment.setCreateTime(
                        LocalDateTime.now().minusDays(random.nextInt(30)).minusHours(random.nextInt(24)));
                rootComment.setUpdateTime(rootComment.getCreateTime());
                rootComment.setIsDeleted(0);

                commentMapper.insert(rootComment);

                // Spawn 1 to 5 sub-replies mimicking nested interactions
                int replyCount = 1 + random.nextInt(5);
                Long currentParentId = rootComment.getId();
                Long lastReplyId = rootComment.getId();
                Long lastReplyUserId = rootComment.getUserId();
                LocalDateTime lastTime = rootComment.getCreateTime();

                for (int j = 0; j < replyCount; j++) {
                    SysUser replyUser = users.get(random.nextInt(users.size()));

                    Comment reply = new Comment();
                    reply.setUserId(replyUser.getId());
                    reply.setTargetType(Comment.TARGET_ACTIVITY);
                    reply.setTargetId(act.getId());
                    reply.setParentId(currentParentId);

                    if (j == 0 || random.nextBoolean()) {
                        // Reply to the root directly
                        reply.setReplyToId(currentParentId);
                        reply.setReplyToUserId(rootComment.getUserId());
                    } else {
                        // Reply to the previous reply (deep nesting)
                        reply.setReplyToId(lastReplyId);
                        reply.setReplyToUserId(lastReplyUserId);
                    }

                    String[] replyPhrases = { "确实如此！", "我也觉得", "带我一个可以吗？", "顶上去！", "这周末去哪集合？" };
                    reply.setContent(replyPhrases[random.nextInt(replyPhrases.length)] + " @"
                            + sysUserMapper.selectById(reply.getReplyToUserId()).getUsername());
                    reply.setRating(0);
                    reply.setLikeCount(random.nextInt(20));
                    reply.setStatus(Comment.STATUS_NORMAL);
                    lastTime = lastTime.plusMinutes(random.nextInt(100) + 1);
                    reply.setCreateTime(lastTime);
                    reply.setUpdateTime(reply.getCreateTime());
                    reply.setIsDeleted(0);

                    commentMapper.insert(reply);

                    lastReplyId = reply.getId();
                    lastReplyUserId = reply.getUserId();
                }
            }
        }
        log.info("Successfully injected deep mock comments to all activities.");
    }

    @SuppressWarnings("null")
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
