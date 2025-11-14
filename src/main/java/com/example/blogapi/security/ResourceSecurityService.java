package com.example.blogapi.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.blogapi.entity.Comment;
import com.example.blogapi.entity.Post;
import com.example.blogapi.exception.ResourceNotFoundException;
import com.example.blogapi.repository.CommentRepository;
import com.example.blogapi.repository.PostRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service("resourceSecurityService")
@RequiredArgsConstructor
@Slf4j
public class ResourceSecurityService {
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    /**
     * Check user hiện tại có phải là author của post không
     * 
     * @param postId ID của post cần check
     * @return true nếu user là author HOẶC là ADMIN
     */
    public boolean isPostAuthor(Long postId) {
        log.debug("Checking post ownership for postId: {}", postId);

        // 1. Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("No authenticated user found");
            return false;
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String currentUsername = userDetails.getUsername();

        // 2. Check if user is ADMIN (admin can do anything)
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            log.debug("User {} is ADMIN, access granted", currentUsername);
            return true;
        }

        // 3. Load post and check author
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy post với ID: " + postId));

        String postAuthor = post.getUser().getUsername();
        boolean isAuthor = postAuthor.equals(currentUsername);

        log.debug("Post author: {}, Current user: {}, Is author: {}", postAuthor, currentUsername, isAuthor);
        return isAuthor;
    }

    public boolean isCommentAuthor(Long commentId) {
        log.debug("Checking comment ownership for commentId: {}", commentId);

        // 1. Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("No authenticated user found");
            return false;
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String currentUsername = userDetails.getUsername();

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            log.debug("User {} is ADMIN, access granted", currentUsername);
            return true;
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy comment với ID: " + commentId));

        String commentAuthor = comment.getAuthor().getUsername();
        boolean isAuthor = commentAuthor.equals(currentUsername);

        log.debug("Comment author: {}, Current user: {}, Is author: {}",
                commentAuthor, currentUsername, isAuthor);
        return isAuthor;
    }

    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
}
