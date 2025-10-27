package com.example.blogapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.blogapi.dto.response.PostResponse;
import com.example.blogapi.entity.Post;
import com.example.blogapi.exception.ResourceNotFoundException;
import com.example.blogapi.mapper.PostMapper;
import com.example.blogapi.repository.PostRepository;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {
    @Mock
    private PostRepository postRepository;

    @Mock
    private PostMapper postMapper;

    @InjectMocks
    private PostService postService;

    @Test
    void getPostById_whenPostExists_shouldReturnPostResponse() {
        // 1. ARRANGE (Sắp xếp kịch bản)
        Long postId = 1L;
        Post mockPost = new Post(); // Tạo dữ liệu giả
        mockPost.setId(postId);
        mockPost.setTitle("Test Title");

        PostResponse mockPostResponse = new PostResponse(); // Tạo kết quả mong đợi
        mockPostResponse.setId(postId);
        mockPostResponse.setTitle("Test Title");

        // "Dạy" cho mock repository: "Khi ai đó gọi findById với postId,
        // hãy trả về một Optional chứa đối tượng mockPost"
        when(postRepository.findById(postId)).thenReturn(Optional.of(mockPost));

        // "Dạy" cho mock mapper: "Khi ai đó gọi toPostResponse với mockPost,
        // hãy trả về đối tượng mockPostResponse"
        when(postMapper.toPostResponse(mockPost)).thenReturn(mockPostResponse);

        // 2. ACT (Hành động)
        // Gọi phương thức thật sự mà chúng ta muốn test
        PostResponse actualResponse = postService.getPostById(postId);

        // 3. ASSERT (Khẳng định kết quả)
        // Kiểm tra xem kết quả trả về có null không và có đúng như mong đợi không
        assertNotNull(actualResponse);
        assertEquals(postId, actualResponse.getId());
        assertEquals("Test Title", actualResponse.getTitle());

        // (Tùy chọn) Kiểm tra xem các mock có được gọi đúng số lần không
        verify(postRepository, times(1)).findById(postId);
        verify(postMapper, times(1)).toPostResponse(mockPost);
    }

    @Test
    void getPostById_whenPostNotFound_shouldThrowResourceNotFoundException() {
        // 1. ARRANGE (Sắp xếp kịch bản)
        Long postId = 99L;

        // "Dạy" cho mock repository: "Khi ai đó gọi findById với postId này,
        // hãy trả về một Optional rỗng"
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        // 2. ACT & 3. ASSERT (Hành động và Khẳng định trong cùng một bước)
        // Khẳng định rằng khi gọi postService.getPostById(postId),
        // một exception loại ResourceNotFoundException sẽ được ném ra.
        assertThrows(ResourceNotFoundException.class, () -> {
            postService.getPostById(postId);
        });

        // (Tùy chọn) Đảm bảo rằng mapper không bao giờ được gọi đến vì đã có lỗi xảy ra
        // trước đó
        verify(postMapper, never()).toPostResponse(any(Post.class));
    }
}