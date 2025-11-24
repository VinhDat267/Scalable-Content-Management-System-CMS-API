package com.example.blogapi.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.blogapi.entity.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

  // ======== OVERRIDE DEFAULT METHODS TO EXCLUDE SOFT DELETED ========

  /**
   * Tìm post theo ID, loại trừ các post đã bị soft delete
   */
  @Query("SELECT p FROM Post p WHERE p.id = :id AND p.deletedAt IS NULL")
  Optional<Post> findById(@Param("id") Long id);

  /**
   * Lấy tất cả posts, loại trừ các post đã bị soft delete
   */
  @Query("SELECT p FROM Post p WHERE p.deletedAt IS NULL")
  Page<Post> findAll(Pageable pageable);

  /**
   * Kiểm tra post có tồn tại không (loại trừ đã xoá)
   */
  @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Post p WHERE p.id = :id AND p.deletedAT IS NULL")
  boolean existsById(@Param("id") Long id);

  // ================ CUSTOM QUERIRES ================

  /**
   * Tìm kiếm posts theo keyword trong title hoặc content
   * (loại trừ đã xoá)
   */
  @Query("SELECT p FROM Post p WHERE " +
      "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
      "LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
      "AND p.deletedAt IS NULL")
  Page<Post> searchPosts(@Param("keyword") String keyword, Pageable pageable);

  /**
   * Lấy posts của một user cụ thể (loại trừ đã xoá)
   */
  @Query("SELECT p FROM Post p WHERE p.user.id = :userId AND p.deletedAt IS NULL")
  Page<Post> findByUserId(@Param("userId") Long userId, Pageable pageable);

  /**
   * Lấy posts được tạo sau một thời điểm (loại trừ đã xoá)
   */
  @Query("SELECT p FROM Post p WHERE p.createdAt >= :fromDate AND p.deletedAt IS NULL ORDER BY p.createdAt DESC ")
  Page<Post> findRecentPosts(@Param("fromDate") LocalDateTime fromDate, Pageable pageable);

  // ========== ADMIN QUERIES (Bao gồm cả đã xóa) ==========

  /**
   * Lấy tất cả posts kể cả đã xoá (dành cho admin)
   */
  @Query("SELECT p FROM Post p")
  Page<Post> findAllIncludingDeleted(Pageable pageable);

  /**
   * Lấy chỉ các posts đã bị soft delete
   * 
   */
  @Query("SELECT p FROM Post p WHERE p.deletedAt IS NOT NULL")
  Page<Post> findDeletedPosts(Pageable pageable);

  /**
   * Tìm post theo ID kể cả đã xoá (để restore)
   */
  @Query("SELECT p FROM Post p WHERE p.id = :id")
  Optional<Post> findByIdIncludingDeleted(@Param("id") Long id);

}
