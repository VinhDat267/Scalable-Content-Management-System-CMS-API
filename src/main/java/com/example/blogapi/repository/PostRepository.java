package com.example.blogapi.repository;

import com.example.blogapi.entity.Post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findAll(Pageable pageable);

    // Tìm kiếm posts theo keyword trong title hoặc content
    @Query("SELECT p FROM Post p WHERE " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Post> searchPosts(@Param("keyword") String keyword, Pageable pageable);

    // Lấy posts của một user cụ thể
    Page<Post> findByUserId(Long userId, Pageable pageable);

    // Lấy posts được tạo sau một thời điểm
    @Query("SELECT p FROM Post p WHERE p.createdAt >= :fromDate ORDER BY p.createdAt DESC")
    Page<Post> findRecentPosts(@Param("fromDate") java.time.LocalDateTime fromDate, Pageable pageable);
}
