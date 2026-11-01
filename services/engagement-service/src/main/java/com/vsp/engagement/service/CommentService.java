package com.vsp.engagement.service;

import com.vsp.engagement.model.entity.Comment;
import com.vsp.engagement.repository.CommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public Comment createComment(Long videoId, Long userId, String content, Long parentId) {
        Comment comment = new Comment();
        comment.setVideoId(videoId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setParentId(parentId);
        comment.setLikeCount(0);
        comment.setDeleted(false);
        return commentRepository.save(comment);
    }

    public List<Comment> listForVideo(Long videoId) {
        return commentRepository.findByVideoIdOrderByCreatedAtAsc(videoId);
    }

    public List<Comment> listReplies(Long parentId) {
        return commentRepository.findByParentIdOrderByCreatedAtAsc(parentId);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + commentId));
        if (!comment.getUserId().equals(userId)) {
            throw new SecurityException("Not authorized to delete this comment");
        }
        comment.setDeleted(true);
        comment.setContent("[deleted]");
        commentRepository.save(comment);
    }
}
