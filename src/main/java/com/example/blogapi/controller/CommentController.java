package com.example.blogapi.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.blogapi.dto.request.CommentCreateRequest;
import com.example.blogapi.dto.request.CommentUpdateRequest;
import com.example.blogapi.dto.response.ApiResponse;
import com.example.blogapi.dto.response.CommentResponse;
import com.example.blogapi.service.CommentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
@RequiredArgsConstructor
@Tag(name = "Comment APIs", description = "API để quản lý bình luận")
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    @Operation(summary = "Tạo comment mới cho post")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @PathVariable Long postId,
            @RequestParam Long userId,
            @Valid @RequestBody CommentCreateRequest request) {

        CommentResponse comment = commentService.createComment(postId, userId, request);
        ApiResponse<CommentResponse> response = ApiResponse.created(comment);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Lấy tất cả comments của một post", description = "Hỗ trợ phân trang và sắp xếp. Default: page=0, size=10, sort=createdAt,desc")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getCommentsByPostId(
            @PathVariable Long postId,

            @Parameter(description = "Số trang", example = "0") @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Số items mỗi trang", example = "10") @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sắp xếp theo", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Hướng sắp xếp", example = "desc") @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CommentResponse> commentPage = commentService.getCommentsByPostId(postId, pageable);

        ApiResponse.PageMetadata pageMetadata = ApiResponse.PageMetadata.builder()
                .currentPage(commentPage.getNumber())
                .pageSize(commentPage.getSize())
                .totalElements(commentPage.getTotalElements())
                .totalPages(commentPage.getTotalPages())
                .hasNext(commentPage.hasNext())
                .hasPrevious(commentPage.hasPrevious())
                .build();

        ApiResponse<List<CommentResponse>> response = ApiResponse.<List<CommentResponse>>builder()
                .success(true)
                .message("Fetched comments successfully")
                .data(commentPage.getContent())
                .pageMetadata(pageMetadata)
                .timestamp(LocalDateTime.now())
                .statusCode(200)
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{commentId}")
    @Operation(summary = "Cập nhật comment")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest request) {

        CommentResponse comment = commentService.updateComment(postId, commentId, request);
        ApiResponse<CommentResponse> response = ApiResponse.success(comment, "Comment updated successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "Xóa comment")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId) {

        commentService.deleteComment(postId, commentId);
        ApiResponse<Void> response = ApiResponse.success(null, "Comment deleted successfully");
        return ResponseEntity.ok(response);
    }
}