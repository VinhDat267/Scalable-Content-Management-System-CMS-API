package com.example.blogapi.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.blogapi.dto.request.UserCreateRequest;
import com.example.blogapi.dto.response.UserResponse;
import com.example.blogapi.entity.User;
import com.example.blogapi.exception.ResourceNotFoundException;
import com.example.blogapi.mapper.UserMapper;
import com.example.blogapi.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        log.info("Creating user with username: {}", request.getUsername());

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("User already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        User savedUser = userRepository.save(user);

        log.info("User created successfully with ID: {}", savedUser.getId());
        return userMapper.toUserResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.info("Fetching user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng với ID: " + id));

        return userMapper.toUserResponse(user);
    }

    /**
     * Lấy tất cả users với phân trang và sắp xếp
     * 
     * @param pageable chứa thông tin về page, size, sort
     * @return Page<UserResponse>
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        log.info("Fetching users - Page: {}, Size: {}, Sort: {}",
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort());

        Page<User> userPage = userRepository.findAll(pageable);
        Page<UserResponse> responsePage = userPage.map(userMapper::toUserResponse);

        log.info("Fetched {} users out of {} total", responsePage.getNumberOfElements(),
                responsePage.getTotalElements());

        return responsePage;
    }

    /**
     * Method cũ không dùng pagination (deprecated)
     * Giữ lại để backward compatibility
     */
    @Deprecated
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.warn("⚠️ Using deprecated getAllUsers() without pagination! Consider migrating to paginated version.");
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(String keyword, Pageable pageable) {
        log.info("Searching users with keyword: '{}' - Page: {}, Size: {}",
                keyword,
                pageable.getPageNumber(),
                pageable.getPageSize());

        Page<User> userPage = userRepository.searchByUsername(keyword, pageable);
        log.info("Found {} users matching keyword '{}'", userPage.getTotalElements(), keyword);
        return userPage.map(userMapper::toUserResponse);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getUsersByRole(String role, Pageable pageable) {

        log.info("Fetching users with role: {} - Page: {}, Size: {}",
                role,
                pageable.getPageNumber(),
                pageable.getPageSize());

        Page<User> userPage = userRepository.findByRole(role, pageable);
        return userPage.map(userMapper::toUserResponse);
    }

    @Transactional
    public void deleteUser(Long id) {
        log.warn("Deleting user with ID: {}", id);

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy user với ID: " + id);
        }

        userRepository.deleteById(id);
        log.info("User with ID: {} deleted successfully", id);
    }

}
