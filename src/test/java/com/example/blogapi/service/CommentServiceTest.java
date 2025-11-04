package com.example.blogapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

	@Mock
	private CommentRepository commentRepository;

	@Mock
	private PostRepository postRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private CommentMapper commentMapper;

	@InjectMocks
	private CommentService commentService;

	// ============== TEST CASE 1: Create Comment - Happy Path ==============

	/**
	 * Test tạo comment thành công
	 * 
	 * 📚 QUAN TRỌNG:
	 * - CommentService.createComment(postId, userId, request)
	 * - postId và userId là parameters riêng, KHÔNG nằm trong request DTO
	 * - Request chỉ chứa body của comment
	 * 
	 * 🎯 GIẢI THÍCH:
	 * - Controller extract postId từ @PathVariable
	 * - Controller extract userId từ @RequestParam
	 * - Controller extract body từ @RequestBody
	 */
	@Test
	void createComment_whenValidRequest_shouldReturnCommentResponse() {
		// ========== ARRANGE ==========

		// 1. Tạo parameters riêng biệt (giống controller gọi service)
		Long postId = 1L;
		Long userId = 1L;

		// 2. Request CHỈ CÓ BODY (đúng với DTO thực tế)
		CommentCreateRequest request = new CommentCreateRequest();
		request.setBody("This is a great post!");

		// 3. Mock Post tồn tại
		Post mockPost = new Post();
		mockPost.setId(postId);
		mockPost.setTitle("Spring Boot Tutorial");
		when(postRepository.findById(postId)).thenReturn(Optional.of(mockPost));

		// 4. Mock User tồn tại
		User mockUser = new User();
		mockUser.setId(userId);
		mockUser.setUsername("john_doe");
		when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

		// 5. Mock Comment được save thành công
		Comment savedComment = new Comment();
		savedComment.setId(1L);
		savedComment.setBody("This is a great post!");
		savedComment.setCreatedAt(LocalDateTime.now());
		savedComment.setPost(mockPost);
		savedComment.setAuthor(mockUser);
		when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

		// 6. Mock mapper
		CommentResponse expectedResponse = new CommentResponse();
		expectedResponse.setId(1L);
		expectedResponse.setBody("This is a great post!");
		expectedResponse.setPostId(postId);
		expectedResponse.setAuthorUsername("john_doe");
		when(commentMapper.toCommentResponse(savedComment)).thenReturn(expectedResponse);

		// ========== ACT ==========
		// Gọi đúng signature: createComment(postId, userId, request)
		CommentResponse result = commentService.createComment(postId, userId, request);

		// ========== ASSERT ==========
		assertNotNull(result);
		assertEquals(1L, result.getId());
		assertEquals("This is a great post!", result.getBody());
		assertEquals(postId, result.getPostId());
		assertEquals("john_doe", result.getAuthorUsername());

		// Verify repositories được gọi theo đúng thứ tự
		verify(postRepository, times(1)).findById(postId);
		verify(userRepository, times(1)).findById(userId);
		verify(commentRepository, times(1)).save(any(Comment.class));
		verify(commentMapper, times(1)).toCommentResponse(savedComment);
	}

	// ============== TEST CASE 2: Create Comment - Post Not Found ==============

	@Test
	void createComment_whenPostNotFound_shouldThrowException() {
		// ========== ARRANGE ==========
		Long postId = 999L;
		Long userId = 1L;

		CommentCreateRequest request = new CommentCreateRequest();
		request.setBody("Comment on non-existent post");

		when(postRepository.findById(postId)).thenReturn(Optional.empty());

		// ========== ACT & ASSERT ==========
		ResourceNotFoundException exception = assertThrows(
				ResourceNotFoundException.class,
				() -> commentService.createComment(postId, userId, request));

		assertEquals("Không tìm thấy bài viết với ID: 999", exception.getMessage());

		verify(postRepository, times(1)).findById(postId);
		verify(userRepository, never()).findById(any());
		verify(commentRepository, never()).save(any());
	}

	// ============== TEST CASE 3: Create Comment - User Not Found ==============

	@Test
	void createComment_whenUserNotFound_shouldThrowException() {
		// ========== ARRANGE ==========
		Long postId = 1L;
		Long userId = 999L;

		CommentCreateRequest request = new CommentCreateRequest();
		request.setBody("Comment by non-existent user");

		Post mockPost = new Post();
		mockPost.setId(postId);
		when(postRepository.findById(postId)).thenReturn(Optional.of(mockPost));

		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		// ========== ACT & ASSERT ==========
		ResourceNotFoundException exception = assertThrows(
				ResourceNotFoundException.class,
				() -> commentService.createComment(postId, userId, request));

		assertEquals("Không tìm thấy người dùng với ID: 999", exception.getMessage());

		verify(postRepository, times(1)).findById(postId);
		verify(userRepository, times(1)).findById(userId);
		verify(commentRepository, never()).save(any());
	}

	// ============== TEST CASE 4: Update Comment - Success ==============

	@Test
	void updateComment_whenValidRequest_shouldReturnUpdatedComment() {
		// ========== ARRANGE ==========
		Long postId = 1L;
		Long commentId = 1L;

		CommentUpdateRequest request = new CommentUpdateRequest();
		request.setBody("Updated comment body");

		Post mockPost = new Post();
		mockPost.setId(postId);
		when(postRepository.existsById(postId)).thenReturn(true);

		Comment existingComment = new Comment();
		existingComment.setId(commentId);
		existingComment.setBody("Original comment");
		existingComment.setPost(mockPost);

		User mockUser = new User();
		mockUser.setUsername("john_doe");
		existingComment.setAuthor(mockUser);

		when(commentRepository.findById(commentId)).thenReturn(Optional.of(existingComment));

		Comment updatedComment = new Comment();
		updatedComment.setId(commentId);
		updatedComment.setBody("Updated comment body");
		updatedComment.setPost(mockPost);
		updatedComment.setAuthor(mockUser);
		when(commentRepository.save(existingComment)).thenReturn(updatedComment);

		CommentResponse expectedResponse = new CommentResponse();
		expectedResponse.setId(commentId);
		expectedResponse.setBody("Updated comment body");
		expectedResponse.setAuthorUsername("john_doe");
		when(commentMapper.toCommentResponse(updatedComment)).thenReturn(expectedResponse);

		// ========== ACT ==========
		CommentResponse result = commentService.updateComment(postId, commentId, request);

		// ========== ASSERT ==========
		assertNotNull(result);
		assertEquals("Updated comment body", result.getBody());

		verify(postRepository, times(1)).existsById(postId);
		verify(commentRepository, times(1)).findById(commentId);
		verify(commentRepository, times(1)).save(existingComment);
	}

	// ============== TEST CASE 5: Delete Comment - Success ==============

	@Test
	void deleteComment_whenCommentExists_shouldDeleteSuccessfully() {
		// ========== ARRANGE ==========
		Long postId = 1L;
		Long commentId = 1L;

		Comment mockComment = new Comment();
		mockComment.setId(commentId);

		Post mockPost = new Post();
		mockPost.setId(postId);
		mockComment.setPost(mockPost);

		when(commentRepository.findById(commentId)).thenReturn(Optional.of(mockComment));

		// ========== ACT ==========
		commentService.deleteComment(postId, commentId);

		// ========== ASSERT ==========
		verify(commentRepository, times(1)).findById(commentId);
		verify(commentRepository, times(1)).delete(mockComment);
	}

	// ============== TEST CASE 6: Delete Comment - Not Found ==============

	@Test
	void deleteComment_whenCommentNotFound_shouldThrowException() {
		// ========== ARRANGE ==========
		Long postId = 1L;
		Long commentId = 999L;

		when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

		// ========== ACT & ASSERT ==========
		ResourceNotFoundException exception = assertThrows(
				ResourceNotFoundException.class,
				() -> commentService.deleteComment(postId, commentId));

		assertEquals("Không tìm thấy bình luận với ID: 999", exception.getMessage());

		verify(commentRepository, times(1)).findById(commentId);
		verify(commentRepository, never()).delete(any());
	}
}