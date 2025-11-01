package com.example.blogapi.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.blogapi.entity.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Deprecated
    List<Comment> findByPostId(Long postId);

    // Lấy comments của một post với pagination
    Page<Comment> findByPostId(Long postId, Pageable pageable);

    // Lấy comments của một user cụ thể
    @Query("SELECT c FROM Comment c WHERE c.author.id = :userId")
    Page<Comment> findByAuthorId(@Param("userId") Long userId, Pageable pageable);

    // Tìm kiếm comments theo nội dung
    @Query("SELECT c FROM Comment c WHERE LOWER(c.body) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Comment> searchComments(@Param("keyword") String keyword, Pageable pageable);
}
