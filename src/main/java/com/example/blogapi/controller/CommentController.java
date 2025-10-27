package com.example.blogapi.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.blogapi.dto.request.CommentCreateRequest;
import com.example.blogapi.dto.response.ApiResponse;
import com.example.blogapi.dto.response.CommentResponse;
import com.example.blogapi.dto.request.CommentUpdateRequest;
import com.example.blogapi.service.CommentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest request) {
        Long currentUserId = 1L;
        CommentResponse createdComment = commentService.createComment(postId, currentUserId, request);
        ApiResponse<CommentResponse> response = ApiResponse.created(createdComment);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getCommentsByPostId(@PathVariable Long postId) {
        List<CommentResponse> comments = commentService.getCommentsByPostId(postId);
        ApiResponse<List<CommentResponse>> response = ApiResponse.success(comments,
                "Fetched all comments successfully");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable Long commentId,
            @PathVariable Long postId,
            @Valid @RequestBody CommentUpdateRequest request) {
        CommentResponse comment = commentService.updateComment(postId, commentId, request);
        ApiResponse<CommentResponse> response = ApiResponse.success(comment, "Updated comment successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long commentId,
            @PathVariable Long postId) {
        commentService.deleteComment(postId, commentId);
        ApiResponse<Void> response = ApiResponse.success(null, "Deleted comment successfully");
        return ResponseEntity.ok(response);

    }

}
