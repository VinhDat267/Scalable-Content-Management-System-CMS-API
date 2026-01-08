package com.example.blogapi.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.blogapi.entity.Post;
import com.example.blogapi.repository.PostRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service để cleanup data đã xóa (soft delete) sau một khoảng thời gian
 * 
 * 🎯 MỤC ĐÍCH:
 * - Giảm database size bằng cách xóa vĩnh viễn data đã soft delete quá lâu
 * - Tuân thủ data retention policy (GDPR, internal policies)
 * - Chạy tự động vào giờ thấp điểm
 * 
 * ⏰ LỊCH CHẠY:
 * - Mặc định: 2:00 AM mỗi ngày
 * - Có thể config qua application.yml
 * 
 * 📊 MONITORING:
 * - Log số lượng records đã xóa
 * - Log thời gian chạy
 * - Alert nếu có exception
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CleanupSchedulerService {

    private final PostRepository postRepository;

    @Value("${app.cleanup.retention-days:30}")
    private int retentionDays;

    @Value("${app.cleanup.enabled:true}")
    private boolean cleanupEnabled;

    /**
     * Cleanup posts đã bị soft delete quá 30 ngày
     * 
     * 🕐 CRON EXPRESSION: "0 0 2 * * ?"
     * - 0: giây thứ 0
     * - 0: phút thứ 0
     * - 2: giờ 2 AM
     * - *: mọi ngày trong tháng
     * - *: mọi tháng
     * - ?: mọi ngày trong tuần
     * 
     * VÍ DỤ KHÁC:
     * - "0 0 * * * ?" → Mỗi giờ
     * - "0 0 0 * * ?" → Mỗi ngày lúc midnight
     * - "0 0 2 * * SUN" → Mỗi Chủ nhật lúc 2 AM
     * - "0 * /30 * * * ?" → Mỗi 30 phút
     */
    @Scheduled(cron = "${app.cleanup.cron:0 0 2 * * ?}")
    @Transactional
    public void cleanupOldDeletedPosts() {
        log.info("========================================");
        log.info("🧹 Starting cleanup job for old deleted posts");
        log.info("========================================");

        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime threshold = LocalDateTime.now().minusDays(retentionDays);

        log.info("Threshold date: {} (posts deleted before this will be removed)", threshold);

        try {
            // Bước 1: Đếm số lượng posts cần xóa
            long countToDelete = postRepository.countByDeletedAtBefore(threshold);
            log.info("Found {} posts to cleanup (deleted more than {} days ago)",
                    countToDelete, retentionDays);

            if (countToDelete == 0) {
                log.info("No posts to cleanup. Job completed.");
                return;
            }

            // Bước 2: Lấy danh sách posts (để logging chi tiết - optional)
            List<Post> postsToDelete = postRepository.findByDeletedAtBefore(threshold);

            // Log chi tiết (có thể bỏ nếu quá nhiều)
            postsToDelete.forEach(post -> log.debug("Will delete Post ID: {} (deleted at: {}, deleted by: {})",
                    post.getId(),
                    post.getDeletedAt(),
                    post.getDeletedBy()));

            // Bước 3: Xóa vĩnh viễn
            postRepository.deleteAll(postsToDelete);

            // Hoặc dùng batch delete (nhanh hơn nhưng không trigger JPA lifecycle events)
            // int deletedCount = postRepository.deleteByDeletedAtBefore(threshold);

            LocalDateTime endTime = LocalDateTime.now();
            long durationSeconds = java.time.Duration.between(startTime, endTime).getSeconds();

            log.info("✅ Cleanup completed successfully!");
            log.info("   - Posts deleted: {}", countToDelete);
            log.info("   - Duration: {} seconds", durationSeconds);
            log.info("========================================");

        } catch (Exception e) {
            log.error("❌ Error during cleanup job: {}", e.getMessage(), e);
            // Có thể gửi alert email/Slack ở đây
            throw e; // Re-throw để Spring retry nếu có config
        }
    }

    /**
     * Manual cleanup method (dành cho admin trigger qua API)
     * 
     * @return Số lượng posts đã xóa
     */
    @Transactional
    public long manualCleanup() {
        log.warn("⚠️ Manual cleanup triggered by admin");
        LocalDateTime threshold = LocalDateTime.now().minusDays(retentionDays);

        long count = postRepository.countByDeletedAtBefore(threshold);
        List<Post> posts = postRepository.findByDeletedAtBefore(threshold);
        postRepository.deleteAll(posts);

        log.info("Manual cleanup completed: {} posts deleted", count);
        return count;
    }

    /**
     * Cleanup với custom retention days (dành cho testing)
     * 
     * @param days Số ngày retention
     * @return Số lượng posts đã xóa
     */
    @Transactional
    public long cleanupOlderThan(int days) {
        log.info("Custom cleanup triggered: retention = {} days", days);
        LocalDateTime threshold = LocalDateTime.now().minusDays(days);

        long count = postRepository.countByDeletedAtBefore(threshold);
        List<Post> posts = postRepository.findByDeletedAtBefore(threshold);
        postRepository.deleteAll(posts);

        log.info("Custom cleanup completed: {} posts deleted", count);
        return count;
    }

    /**
     * Lấy thống kê posts cần cleanup (không xóa)
     * Dành cho monitoring dashboard
     */
    @Transactional(readOnly = true)
    public CleanupStats getCleanupStats() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(retentionDays);
        long countToDelete = postRepository.countByDeletedAtBefore(threshold);

        return new CleanupStats(
                countToDelete,
                retentionDays,
                threshold);
    }

    /**
     * DTO cho cleanup statistics
     */
    public record CleanupStats(
            long postsToDelete,
            int retentionDays,
            LocalDateTime threshold) {
    }
}