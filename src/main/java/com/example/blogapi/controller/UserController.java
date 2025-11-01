package com.example.blogapi.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.blogapi.dto.request.UserCreateRequest;
import com.example.blogapi.dto.response.ApiResponse;
import com.example.blogapi.dto.response.UserResponse;
import com.example.blogapi.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User APIs", description = "API để quản lý người dùng")
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Đăng ký user mới")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserResponse newUser = userService.createUser(request);
        ApiResponse<UserResponse> response = ApiResponse.created(newUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin user theo ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        ApiResponse<UserResponse> response = ApiResponse.success(user, "User retrieved successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách users với phân trang", description = "Hỗ trợ phân trang và sắp xếp. Default: page=0, size=10, sort=id, asc")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Số lượng items mỗi trang", example = "10") @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "Sắp xếp theo field", example = "username") @RequestParam(defaultValue = "id") String sortBy,

            @Parameter(description = "Hướng sắp xếpL asc hoặc desc", example = "asc") @RequestParam(defaultValue = "asc") String sortDir

    ) {

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserResponse> userPage = userService.getAllUsers(pageable);

        ApiResponse.PageMetadata pageMetadata = ApiResponse.PageMetadata.builder()
                .currentPage(userPage.getNumber())
                .pageSize(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .hasNext(userPage.hasNext())
                .hasPrevious(userPage.hasPrevious())
                .build();

        ApiResponse<List<UserResponse>> response = ApiResponse.<List<UserResponse>>builder()
                .success(true)
                .message("Fetched users successfully")
                .data(userPage.getContent())
                .pageMetadata(pageMetadata)
                .timestamp(LocalDateTime.now())
                .statusCode(200)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm users theo username")
    public ResponseEntity<ApiResponse<List<UserResponse>>> searchUsers(
            @Parameter(description = "Từ khóa tìm kiếm", example = "john", required = true) @RequestParam String keyword,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "username") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir

    ) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserResponse> userPage = userService.searchUsers(keyword, pageable);

        ApiResponse.PageMetadata pageMetadata = ApiResponse.PageMetadata.builder()
                .currentPage(userPage.getNumber())
                .pageSize(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .hasNext(userPage.hasNext())
                .hasPrevious(userPage.hasPrevious())
                .build();

        ApiResponse<List<UserResponse>> response = ApiResponse.<List<UserResponse>>builder()
                .success(true)
                .message(String.format("Found %d users matching '%s'", userPage.getTotalElements(), keyword))
                .data(userPage.getContent())
                .pageMetadata(pageMetadata)
                .timestamp(LocalDateTime.now())
                .statusCode(200)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/role/{role}")
    @Operation(summary = "Lấy users theo role")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(
            @Parameter(description = "Role của user", example = "ROLE_USER") @PathVariable String role,

            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("username").ascending());
        Page<UserResponse> userPage = userService.getUsersByRole(role, pageable);

        ApiResponse.PageMetadata pageMetadata = ApiResponse.PageMetadata.builder()
                .currentPage(userPage.getNumber())
                .pageSize(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .hasNext(userPage.hasNext())
                .hasPrevious(userPage.hasPrevious())
                .build();

        ApiResponse<List<UserResponse>> response = ApiResponse.<List<UserResponse>>builder()
                .success(true)
                .message(String.format("Fetched users with role '%s'", role))
                .data(userPage.getContent())
                .pageMetadata(pageMetadata)
                .timestamp(LocalDateTime.now())
                .statusCode(200)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        ApiResponse<Void> response = ApiResponse.success(null, "User deleted successfully");
        return ResponseEntity.ok(response);
    }

}
