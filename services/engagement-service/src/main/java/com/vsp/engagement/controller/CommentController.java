package com.vsp.engagement.controller;

import com.vsp.engagement.model.entity.Comment;
import com.vsp.engagement.service.CommentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public Comment createComment(@RequestBody CreateCommentRequest request) {
        return commentService.createComment(request.videoId(), request.userId(), request.content(), request.parentId());
    }

    @GetMapping
    public List<Comment> listForVideo(@RequestParam Long videoId) {
        return commentService.listForVideo(videoId);
    }

    @GetMapping("/{parentId}/replies")
    public List<Comment> listReplies(@PathVariable Long parentId) {
        return commentService.listReplies(parentId);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId, @RequestParam Long userId) {
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }

    public record CreateCommentRequest(@Valid @NotBlank @Size(max = 5000) String content,
                                       Long videoId,
                                       Long userId,
                                       Long parentId) {
    }
}
