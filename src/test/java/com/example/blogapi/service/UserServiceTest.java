package com.example.blogapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    // ============== TEST CASE 7: Get All Users with Pagination ==============
    @Test
    void getAllUsers_withPagination_shouldReturnPageOfUsers() {
        // ========== ARRANGE ==========

        // 1. Tạo Pageable object (giống trong Controller)
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());

        // 2. Tạo danh sách mock users (giả lập data từ DB)
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setRole("ROLE_USER");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setRole("ROLE_ADMIN");

        User user3 = new User();
        user3.setId(3L);
        user3.setUsername("user3");
        user3.setRole("ROLE_USER");

        List<User> userList = Arrays.asList(user1, user2, user3);

        // 3. Tạo Page<User> với PageImpl
        // PageImpl(content, pageable, total)
        Page<User> userPage = new PageImpl<>(
                userList, // Danh sách items trong trang này
                pageable, // Thông tin phân trang
                3 // Tổng số records trong DB
        );

        // 4. Tạo mock UserResponse cho từng User
        UserResponse response1 = new UserResponse();
        response1.setId(1L);
        response1.setUsername("user1");
        response1.setRole("ROLE_USER");

        UserResponse response2 = new UserResponse();
        response2.setId(2L);
        response2.setUsername("user2");
        response2.setRole("ROLE_ADMIN");

        UserResponse response3 = new UserResponse();
        response3.setId(3L);
        response3.setUsername("user3");
        response3.setRole("ROLE_USER");

        // 5. "Dạy" các mock objects hoạt động
        when(userRepository.findAll(pageable)).thenReturn(userPage);

        when(userMapper.toUserResponse(user1)).thenReturn(response1);
        when(userMapper.toUserResponse(user2)).thenReturn(response2);
        when(userMapper.toUserResponse(user3)).thenReturn(response3);

        // ========== ACT ==========
        Page<UserResponse> result = userService.getAllUsers(pageable);

        // ========== ASSERT ==========

        // 1. Verify kết quả không null
        assertNotNull(result, "Page result không được null");

        // 2. Verify số lượng item trong trang
        assertEquals(3, result.getContent().size(), "Page phải chứa 3 users");

        // 3. Verify pagination metadata
        assertEquals(3, result.getTotalElements(), "Tổng số users phải là 3");
        assertEquals(1, result.getTotalPages(), "Phải có 1 trang (vì 3 users < pageSize 10)");
        assertEquals(0, result.getNumber(), "Đang ở trang 0");
        assertEquals(10, result.getSize(), "Page size phải là 10");

        // 4. Verify content trong page
        List<UserResponse> content = result.getContent();
        assertEquals("user1", content.get(0).getUsername());
        assertEquals("user2", content.get(1).getUsername());
        assertEquals("user3", content.get(2).getUsername());

        // 5. Verify các mock được gọi đúng số lần
        verify(userRepository, times(1)).findAll(pageable);

        // Mapper được gọi 3 lần (1 lần cho mỗi user)
        verify(userMapper, times(1)).toUserResponse(user1);
        verify(userMapper, times(1)).toUserResponse(user2);
        verify(userMapper, times(1)).toUserResponse(user3);
    }

    // ======= TEST CASE 8: Get All Users with Pagination - Empty Page======
    @Test
    void getAllUsers_whenNoUsers_shouldReturnEmptyPage() {
        // ========== ARRANGE ==========
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());

        Page<User> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);

        when(userRepository.findAll(pageable)).thenReturn(emptyPage);

        // ========== ACT ==========
        Page<UserResponse> result = userService.getAllUsers(pageable);

        // ========== ASSERT ==========
        assertNotNull(result, "Result không được null dù Page rỗng");
        assertEquals(true, result.getContent().isEmpty());
        assertEquals(0, result.getContent().size());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
        assertEquals(0, result.getNumber());

        verify(userRepository, times(1)).findAll(pageable);
        verify(userMapper, never()).toUserResponse(any());

    }

    static User createMockUser(Long userId, String username) {
        User user = new User();
        user.setId(userId);
        user.setUsername(username);
        return user;
    }

    // ============== TEST CASE 9: Get All Users - Multiple Pages ==============
    @Test
    void getAllUsers_withMultiplePages_shouldReturnCorrectMetadata() {

        // ARRANGE
        Pageable pageable = PageRequest.of(1, 10, Sort.by("id").ascending());

        List<User> userList = Arrays.asList(
                createMockUser(10L, "user10"),
                createMockUser(11L, "user11"),
                createMockUser(12L, "user12"),
                createMockUser(13L, "user13"),
                createMockUser(14L, "user14"),
                createMockUser(15L, "user15"),
                createMockUser(16L, "user16"),
                createMockUser(17L, "user17"),
                createMockUser(18L, "user18"),
                createMockUser(19L, "user19"));

        Page<User> userPage = new PageImpl<>(
                userList,
                pageable,
                25);

        for (User user : userList) {
            UserResponse response = new UserResponse();
            response.setId(user.getId());
            response.setUsername(user.getUsername());
            response.setRole(user.getRole());
            when(userMapper.toUserResponse(user)).thenReturn(response);
        }

        when(userRepository.findAll(pageable)).thenReturn(userPage);

        // ACT
        Page<UserResponse> result = userService.getAllUsers(pageable);

        // ASSERT
        assertNotNull(result, "Page Result không được null");
        assertEquals(10, result.getContent().size(), "Page phải chứa 10 users");

        // Verify Pagination
        assertEquals(25, result.getTotalElements(), "Tổng số users phải là 25");
        assertEquals(3, result.getTotalPages(), "Tổng số trang phải là 3");
        assertEquals(1, result.getNumber(), "Trang hiện phải là 1");
        assertEquals(10, result.getSize(), "Tổng số user tối đa của trang hiện tại là 10");
        assertEquals(true, result.hasNext());
        assertEquals(true, result.hasPrevious());

        assertEquals("user19", result.getContent().get(9).getUsername());
        assertEquals("user10", result.getContent().get(0).getUsername());

        verify(userRepository, times(1)).findAll(pageable);
        verify(userMapper, times(10)).toUserResponse(any(User.class));
    }

}
