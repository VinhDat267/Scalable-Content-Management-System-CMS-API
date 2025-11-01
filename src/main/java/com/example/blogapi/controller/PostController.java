package com.example.blogapi.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.blogapi.config.OpenApiConfig;
import com.example.blogapi.dto.request.PostCreateRequest;
import com.example.blogapi.dto.response.ApiResponse;
import com.example.blogapi.dto.response.PostResponse;
import com.example.blogapi.dto.response.ApiResponse.ApiResponseBuilder;
import com.example.blogapi.dto.request.PostUpdateRequest;
import com.example.blogapi.service.PostService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Tag(name = "Post APIs", description = "API để quản lý các bài viết")
public class PostController {

    private final PostService postService;

    @PostMapping
    @Operation(summary = "Tạo bài viết mới")
    public ResponseEntity<ApiResponse<PostResponse>> createPost(@Valid @RequestBody PostCreateRequest request) {
        PostResponse createdPost = postService.createPost(request);
        ApiResponse<PostResponse> response = ApiResponse.created(createdPost);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách posts với phân trang và sắp xếp", description = "Hỗ trợ phân trang, sắp xếp theo nhiều field. Default: page=0, size=10, sort=createdAt,desc")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getAllPosts(
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Số lượng items mỗi trang", example = "10") @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sắp xếp theo field (có thể nhiều field cách nhau bởi dấu phẩy)", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Hướng sắp xếp: asc hoặc desc", example = "desc") @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<PostResponse> postPage = postService.getAllPosts(pageable);

        ApiResponse.PageMetadata pageMetaData = ApiResponse.PageMetadata.builder()
                .currentPage(postPage.getNumber())
                .pageSize(postPage.getSize())
                .totalElements(postPage.getTotalElements())
                .totalPages(postPage.getTotalPages())
                .hasNext(postPage.hasNext())
                .hasPrevious(postPage.hasPrevious())
                .build();

        ApiResponse<List<PostResponse>> response = ApiResponse.<List<PostResponse>>builder()
                .success(true)
                .message("Fetched posts successfully")
                .data(postPage.getContent())
                .pageMetadata(pageMetaData)
                .timestamp(LocalDateTime.now())
                .statusCode(200)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm posts theo keyword", description = "Tìm kiếm theo title và content của post")
    public ResponseEntity<ApiResponse<List<PostResponse>>> searchPosts(
            @Parameter(description = "Từ khóa tìm kiếm", example = "Spring Boot", required = true) @RequestParam String keyword,

            @Parameter(description = "Số trang", example = "0") @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Số items mỗi trang", example = "10") @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sắp xếp theo", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Hướng sắp xếp", example = "desc") @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<PostResponse> postPage = postService.searchPosts(keyword, pageable);

        ApiResponse.PageMetadata pageMetadata = ApiResponse.PageMetadata.builder()
                .currentPage(postPage.getNumber())
                .pageSize(postPage.getSize())
                .totalElements(postPage.getTotalElements())
                .totalPages(postPage.getTotalPages())
                .hasNext(postPage.hasNext())
                .hasPrevious(postPage.hasPrevious())
                .build();

        ApiResponse<List<PostResponse>> response = ApiResponse.<List<PostResponse>>builder()
                .success(true)
                .message(String.format("Found %d posts matching '%s'", postPage.getTotalElements(), keyword))
                .data(postPage.getContent())
                .pageMetadata(pageMetadata)
                .timestamp(LocalDateTime.now())
                .statusCode(200)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Lấy tất cả posts của một user")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getPostsByUserId(
            @Parameter(description = "ID của user", required = true) @PathVariable Long userId,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<PostResponse> postPage = postService.getPostsByUserId(userId, pageable);

        ApiResponse.PageMetadata pageMetadata = ApiResponse.PageMetadata.builder()
                .currentPage(postPage.getNumber())
                .pageSize(postPage.getSize())
                .totalElements(postPage.getTotalElements())
                .totalPages(postPage.getTotalPages())
                .hasNext(postPage.hasNext())
                .hasPrevious(postPage.hasPrevious())
                .build();

        ApiResponse<List<PostResponse>> response = ApiResponse.<List<PostResponse>>builder()
                .success(true)
                .message("Fetched user posts successfully")
                .data(postPage.getContent())
                .pageMetadata(pageMetadata)
                .timestamp(LocalDateTime.now())
                .statusCode(200)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recent")
    @Operation(summary = "Lấy các posts gần đây")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getRecentPosts(
            @Parameter(description = "Số ngày gần đây", example = "7") @RequestParam(defaultValue = "7") int days,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size

    ) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PostResponse> postPage = postService.getRecentPosts(days, pageable);

        ApiResponse.PageMetadata pageMetadata = ApiResponse.PageMetadata.builder()
                .currentPage(postPage.getNumber())
                .pageSize(postPage.getSize())
                .totalElements(postPage.getTotalElements())
                .totalPages(postPage.getTotalPages())
                .hasNext(postPage.hasNext())
                .hasPrevious(postPage.hasPrevious())
                .build();

        ApiResponse<List<PostResponse>> response = ApiResponse.<List<PostResponse>>builder()
                .success(true)
                .message(String.format("Fetched posts from last %d days", days))
                .data(postPage.getContent())
                .pageMetadata(pageMetadata)
                .timestamp(java.time.LocalDateTime.now())
                .statusCode(200)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết một post")
    public ResponseEntity<ApiResponse<PostResponse>> getPostById(@PathVariable Long id) {
        PostResponse post = postService.getPostById(id);
        ApiResponse<PostResponse> response = ApiResponse.success(post, "Post retrieved successfully");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật post")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(@PathVariable Long id,
            @Valid @RequestBody PostUpdateRequest request) {

        PostResponse post = postService.updatePost(id, request);
        ApiResponse<PostResponse> response = ApiResponse.success(post, "Post updated successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa post")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        ApiResponse<Void> response = ApiResponse.success(null, "Post deleted successfully");
        return ResponseEntity.ok(response);
    }

}
