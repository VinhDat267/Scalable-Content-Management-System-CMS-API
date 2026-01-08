package com.example.blogapi.controller;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.blogapi.dto.response.ApiResponse;
import com.example.blogapi.service.CleanupSchedulerService;
import com.example.blogapi.service.CleanupSchedulerService.CleanupStats;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin APIs", description = "API dành cho quản trị viên")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final CleanupSchedulerService cleanupSchedulerService;

    /**
     * Trigger manual cleanup
     */
    @PostMapping("/cleanup/posts")
    @Operation(summary = "Manual cleanup posts đã xoá", description = "Xoá vĩnh viễn posts đã soft delete quá retention period")
    public ResponseEntity<ApiResponse<Long>> triggerCleanup() {
        Long deletedCount = cleanupSchedulerService.manualCleanup();

        ApiResponse<Long> response = ApiResponse.<Long>builder()
                .success(true)
                .message("Cleanup completed successfully")
                .data(deletedCount)
                .timestamp(LocalDateTime.now())
                .statusCode(200)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Cleanup với custom retention days
     */
    @PostMapping("/cleanup/posts/custom")
    @Operation(summary = "Cleanup với custom retention", description = "Xóa posts đã soft delete quá X ngày")
    public ResponseEntity<ApiResponse<Long>> customCleanup(
            @RequestParam(defaultValue = "30") int days) {
        Long deletedCount = cleanupSchedulerService.cleanupOlderThan(days);

        ApiResponse<Long> response = ApiResponse.<Long>builder()
                .success(true)
                .message("Custom cleanup completed: " + days + " days retention")
                .data(deletedCount)
                .timestamp(LocalDateTime.now())
                .statusCode(200)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Lấy cleanup statistics
     */
    @GetMapping("/cleanup/stats")
    public ResponseEntity<ApiResponse<CleanupStats>> getCleanupStats() {
        CleanupStats stats = cleanupSchedulerService.getCleanupStats();
        ApiResponse<CleanupStats> response = ApiResponse.<CleanupStats>builder()
                .success(true)
                .message("Cleanup statistics retrieved")
                .data(stats)
                .timestamp(LocalDateTime.now())
                .statusCode(200)
                .build();

        return ResponseEntity.ok(response);
    }

}
