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
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.blogapi.dto.request.UserCreateRequest;
import com.example.blogapi.dto.response.UserResponse;
import com.example.blogapi.entity.User;
import com.example.blogapi.exception.ResourceNotFoundException;
import com.example.blogapi.mapper.UserMapper;
import com.example.blogapi.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    // ============== TEST CASE 1: Create User - Happy Path ==============

    /**
     * Test tạo user thành công
     * 
     * Kịch bản: User nhập username/password hợp lệ -> Tạo thành công
     */
    @Test
    void createUser_whenValidRequest_shouldReturnUserResponse() {
        // ========== ARRANGE (Chuẩn bị dữ liệu test) ==========

        // 1. Tạo request giả lập từ client
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        // 2. Tạo User entity giả (sau khi save vào DB)
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("testuser");
        savedUser.setPassword("encoded_password");
        savedUser.setRole("ROLE_USER");

        // 3. Tạo response mong đợi
        UserResponse expectedResponse = new UserResponse();
        expectedResponse.setId(1L);
        expectedResponse.setUsername("testuser");
        expectedResponse.setRole("ROLE_USER");

        // 4. "Dạy các mock objects cách hoạt động"
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty()); // Username chưa tồn tại
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password"); // Password được mã hoá
        when(userRepository.save(any(User.class))).thenReturn(savedUser); // Save thành công
        when(userMapper.toUserResponse(savedUser)).thenReturn(expectedResponse); // Map sang response

        // ========== ACT (Gọi method cần test) ==========
        UserResponse result = userService.createUser(request);

        // ========== ASSERT (Kiểm tra kết quả) ==========
        assertNotNull(result, "Response không được null");
        assertEquals("testuser", result.getUsername(), "User phải khớp");
        assertEquals("ROLE_USER", result.getRole(), "Role mặc định phải là ROLE_USER");

        // Verify các mock được gọi đúng số lần
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).toUserResponse(savedUser);
    }

    // ============== TEST CASE 2: Create User - Error Case ==============

    /**
     * Test tạo user thất bại vì username đã tồn tại
     *
     * Kịch bản: User nhập username đã có trong DB → Throw exception
     */
    @Test
    void createdUser_whenUserExists_ShouldThrowException() {
        // ========== ARRANGE ==========
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("existinguser");
        request.setPassword("password123");

        // Giả lập username đã tồn tại trong DB
        User existingUser = new User();
        existingUser.setId(99L);
        existingUser.setUsername("existinguser");

        // Mock repository trả về user đã tồn tại
        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(existingUser));

        // ========== ACT & ASSERT ==========
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(request);
        }, "Phải throw IllegalArgumentException khi user đã tồn tại");

        assertEquals("User already exists", exception.getMessage(), "Message exception phải đúng");

        // ========== VERIFY ==========
        verify(userRepository, times(1)).findByUsername("existinguser");

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper, never()).toUserResponse(any());

    }

    // ============== TEST CASE 3: Get User by ID - Happy Path ==============

    /**
     * Test lấy user theo ID thành công
     * 
     * 📚 GIẢI THÍCH:
     * - Test method READ (read-only transaction)
     * - Verify mapper hoạt động đúng
     */
    @Test
    void getUserById_whenUserExists_shouldReturnUserResponse() {
        // ARRANGE
        Long userId = 1L;

        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setUsername("testuser");
        mockUser.setRole("ROLE_USER");

        UserResponse expectedResponse = new UserResponse();
        expectedResponse.setId(userId);
        expectedResponse.setUsername("testuser");
        expectedResponse.setRole("ROLE_USER");

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(userMapper.toUserResponse(mockUser)).thenReturn(expectedResponse);

        // ACT
        UserResponse result = userService.getUserById(userId);

        // ASSERT
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("testuser", result.getUsername());

        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, times(1)).toUserResponse(mockUser);

    }

    // ============== TEST CASE 4: Get User by ID - Not Found ==============

    /**
     * Test lấy user không tồn tại
     * 
     * 📚 GIẢI THÍCH:
     * - Test ResourceNotFoundException (custom exception)
     * - Verify message chứa ID trong thông báo lỗi
     */
    @Test
    void getUserById_whenUserNotFound_shouldThrowException() {
        // ARRANGE
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // ACT & ASSERT
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(userId);
        });

        assertEquals("Không tìm thấy người dùng với ID: " + userId, exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
        verify(userMapper, never()).toUserResponse(any());

    }

    // ============== TEST CASE 5: Delete User - Success ==============

    /**
     * Test xóa user thành công
     * 
     * 📚 GIẢI THÍCH:
     * - Method void không return gì → chỉ verify behavior
     * - Kiểm tra existsById() được gọi trước deleteById()
     */
    @Test
    void deleteUser_whenUserExists_shouldDeleteSuccessfully() {
        // ARRANGE
        Long userId = 1L;

        when(userRepository.existsById(userId)).thenReturn(true);

        // ACT
        userService.deleteUser(userId);

        // ASSERT
        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, times(1)).deleteById(userId);

    }

    // ============== TEST CASE 6: Delete User - Not Found ==============

    /**
     * Test xóa user không tồn tại
     */

    @Test
    void deleteUser_whenUserNotFound_shouldThrowException() {
        // ARRANGE
        Long userId = 999L;

        when(userRepository.existsById(userId)).thenReturn(false);

        // ACT & ASSERT
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.deleteUser(userId);
        });

        assertEquals("Không tìm thấy user với ID: " + userId, exception.getMessage());

        verify(userRepository, times(1)).existsById(userId);
        verify(userRepository, never()).deleteById(any());

    }

}
