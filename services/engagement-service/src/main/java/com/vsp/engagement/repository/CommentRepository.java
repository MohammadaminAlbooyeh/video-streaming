package com.vsp.engagement.repository;

import com.vsp.engagement.model.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByVideoIdOrderByCreatedAtAsc(Long videoId);

    List<Comment> findByParentIdOrderByCreatedAtAsc(Long parentId);

    long countByVideoId(Long videoId);
}
