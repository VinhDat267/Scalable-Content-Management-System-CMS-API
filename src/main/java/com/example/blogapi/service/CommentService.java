package com.example.blogapi.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.blogapi.dto.request.CommentCreateRequest;
import com.example.blogapi.dto.request.CommentUpdateRequest;
import com.example.blogapi.dto.response.CommentResponse;
import com.example.blogapi.entity.Comment;
import com.example.blogapi.entity.Post;
import com.example.blogapi.entity.User;
import com.example.blogapi.exception.ResourceNotFoundException;
import com.example.blogapi.mapper.CommentMapper;
import com.example.blogapi.repository.CommentRepository;
import com.example.blogapi.repository.PostRepository;
import com.example.blogapi.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentMapper commentMapper;

    @Transactional
    public CommentResponse createComment(Long postId, Long userId, CommentCreateRequest request) {
        log.info("Creating comment for post ID {} by user ID {}", postId, userId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bài viết với ID: " + postId));

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + userId));

        Comment comment = new Comment();
        comment.setBody(request.getBody());

        comment.setPost(post);
        author.addComment(comment);

        Comment savedComment = commentRepository.save(comment);

        log.info("Comment created successfully with ID: {}", savedComment.getId());
        return commentMapper.toCommentResponse(savedComment);
    }

    /**
     * Lấy comments của một post với phân trang
     * 
     * @param postId   ID của post
     * @param pageable thông tin phân trang
     * @return Page<CommentResponse>
     */
    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsByPostId(Long postId, Pageable pageable) {
        log.info("Fetching comments for post ID: {} - Page: {}, Size: {}",
                postId,
                pageable.getPageNumber(),
                pageable.getPageSize());

        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Không tìm thấy post với ID: " + postId);
        }

        Page<Comment> commentPage = commentRepository.findByPostId(postId, pageable);
        log.info("Found {} comments for post ID: {}", commentPage.getTotalElements(), postId);

        return commentPage.map(commentMapper::toCommentResponse);
    }

    /**
     * Method cũ không dùng pagination (deprecated)
     * Giữ lại để backward compatibility
     */
    @Deprecated
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPostId(Long postId) {
        log.warn("⚠️ Using deprecated getCommentsByPostId() without pagination!");

        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Không tìm thấy post với ID: " + postId);
        }

        List<Comment> comments = commentRepository.findByPostId(postId);
        return comments.stream()
                .map(commentMapper::toCommentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy comments của một user cụ thể
     * 
     * @param userId   ID của user
     * @param pageable thông tin phân trang
     * @return Page<CommentResponse>
     */
    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsByUserId(Long userId, Pageable pageable) {
        log.info("Fetching comments for user ID: {} - Page: {}, Size: {}",
                userId,
                pageable.getPageNumber(),
                pageable.getPageSize());

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Không tìm thấy user với ID: " + userId);
        }

        Page<Comment> commentPage = commentRepository.findByAuthorId(userId, pageable);
        return commentPage.map(commentMapper::toCommentResponse);
    }

    /**
     * Tìm kiếm comments theo keyword
     * 
     * @param keyword  từ khóa tìm kiếm
     * @param pageable thông tin phân trang
     * @return Page<CommentResponse>
     */
    @Transactional(readOnly = true)
    public Page<CommentResponse> searchComments(String keyword, Pageable pageable) {
        log.info("Searching comments with keyword: '{}' - Page: {}, Size: {}",
                keyword,
                pageable.getPageNumber(),
                pageable.getPageSize());

        Page<Comment> commentPage = commentRepository.searchComments(keyword, pageable);
        log.info("Found {} comments matching keyword '{}'", commentPage.getTotalElements(), keyword);

        return commentPage.map(commentMapper::toCommentResponse);
    }

    @Transactional
    public CommentResponse updateComment(Long postId, Long commentId, CommentUpdateRequest request) {
        log.info("Updating comment ID {} for post ID {}", commentId, postId);

        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Không tìm thấy post với ID: " + postId);
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy comment với ID " + commentId));

        if (!comment.getPost().getId().equals(postId)) {
            throw new IllegalArgumentException("Bình luận không thuộc về bài viết này");
        }

        comment.setBody(request.getBody());
        Comment updatedComment = commentRepository.save(comment);

        log.info("Comment updated successfully with ID: {}", updatedComment.getId());
        return commentMapper.toCommentResponse(updatedComment);
    }

    @Transactional
    public void deleteComment(Long postId, Long commentId) {
        log.warn("Deleting comment ID {} from post ID {}", commentId, postId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bình luận với ID: " + commentId));

        if (!comment.getPost().getId().equals(postId)) {
            throw new IllegalArgumentException("Bình luận không thuộc về bài viết này.");
        }

        commentRepository.delete(comment);
        log.info("Comment with ID {} deleted successfully", commentId);
    }
}