package com.example.blogapi.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.blogapi.dto.request.PostCreateRequest;
import com.example.blogapi.dto.response.PostResponse;
import com.example.blogapi.dto.request.PostUpdateRequest;
import com.example.blogapi.entity.Post;
import com.example.blogapi.entity.User;
import com.example.blogapi.exception.ResourceNotFoundException;
import com.example.blogapi.mapper.PostMapper;
import com.example.blogapi.repository.PostRepository;
import com.example.blogapi.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostMapper postMapper;

    @Transactional
    public PostResponse createPost(PostCreateRequest request) {
        log.info("Creating post for user ID {}", request.getUserId());

        User author = userRepository.findById(request.getUserId()).orElseThrow(
                () -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + request.getUserId()));

        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());

        author.addPost(post);
        Post savePosted = postRepository.save(post);

        log.info("Post created successfully with ID: {}", savePosted.getId());

        return postMapper.toPostResponse(savePosted);

    }

    /**
     * Lấy tất cả posts với phân trang và sắp xếp
     * 
     * @param pageable chứa thông tin về page, size, sort
     * @return Page<PostResponse>
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> getAllPosts(Pageable pageable) {
        log.info("Fetching posts - Page: {}, Size: {}, Sort: {}",
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort());

        Page<Post> postPage = postRepository.findAll(pageable);
        Page<PostResponse> responsePage = postPage.map(postMapper::toPostResponse);

        log.info("Fetched {} posts out of {} total",
                responsePage.getNumberOfElements(),
                responsePage.getTotalElements());

        return responsePage;

    }

    /**
     * Method cũ không dùng pagination (deprecated)
     * Giữ lại để backward compatibility
     */
    @Deprecated
    @Transactional(readOnly = true)
    public List<PostResponse> getAllPosts() {
        log.warn("⚠️ Using deprecated getAllPosts() without pagination! Consider migrating to paginated version.");
        return postRepository.findAll().stream()
                .map(postMapper::toPostResponse)
                .collect(Collectors.toList());
    }

    /**
     * Tìm kiếm posts theo keyword
     * 
     * @param keyword  từ khoá tìm kiếm
     * @param pageable thông tin phân trang
     * @return Page<PostResponse>
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> searchPosts(String keyword, Pageable pageable) {
        log.info("Searching posts with keyword: '{}' - Page: {}, Size: {}",
                keyword,
                pageable.getPageNumber(),
                pageable.getPageSize());

        Page<Post> postPage = postRepository.searchPosts(keyword, pageable);
        log.info("Found {} posts matching keyword '{}'", postPage.getTotalElements(), keyword);
        return postPage.map(postMapper::toPostResponse);
    }

    /**
     * Lấy posts của một user cụ thể
     * 
     * @param userId   ID của user
     * @param pageable thông tin phân trang
     * @return Page<PostResponse>
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> getPostsByUserId(Long userId, Pageable pageable) {

        log.info("Fetching posts for user ID: {} - Page: {}, Size: {}",
                userId,
                pageable.getPageNumber(),
                pageable.getPageSize());

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Không tìm thấy user với ID: " + userId);
        }

        Page<Post> postPage = postRepository.findByUserId(userId, pageable);

        return postPage.map(postMapper::toPostResponse);

    }

    /**
     * Lấy các posts gần đây (trong N ngày)
     * 
     * @param days     số ngày
     * @param pageable thông tin phân trang
     * @return Page<PostResponse>
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> getRecentPosts(int days, Pageable pageable) {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(days);
        log.info("Fetching posts from last {} days - Page: {}, Size: {}",
                days,
                pageable.getPageNumber(),
                pageable.getPageSize());
        Page<Post> postPage = postRepository.findRecentPosts(fromDate, pageable);

        return postPage.map(postMapper::toPostResponse);
    }

    @Transactional(readOnly = true)
    public PostResponse getPostById(Long id) {
        log.info("Fetching post with ID: {}", id);
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy post với ID: " + id));

        return postMapper.toPostResponse(post);
    }

    @Transactional
    public PostResponse updatePost(Long id, PostUpdateRequest request) {
        log.info("Updating post with ID: {}", id);
        Post existingPost = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy post với ID: " + id));
        existingPost.setTitle(request.getTitle());
        existingPost.setContent(request.getContent());

        Post updatedPost = postRepository.save(existingPost);

        return postMapper.toPostResponse(updatedPost);
    }

    @Transactional
    public void deletePost(Long id) {
        log.warn("Deleting post with ID: {}", id);
        if (!postRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy post với ID: " + id);
        }

        postRepository.deleteById(id);

        log.info("Post with ID: {} deleted successfully", id);
    }

}
