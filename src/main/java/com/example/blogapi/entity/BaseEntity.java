package com.example.blogapi.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

/**
 * Base entity với Auditing và Soft Delete support
 * 
 * 🎯 CHỨC NĂNG:
 * - Auto auditing (createdAt, updatedAt, createdBy, updatedBy)
 * - Soft delete support (deletedAt, deletedBy)
 * 
 * 📝 SỬ DỤNG:
 * - entity.softDelete() → Đánh dấu đã xóa
 * - entity.restore() → Khôi phục
 * - entity.isDeleted() → Kiểm tra trạng thái
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseEntity {
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private String deletedBy;

    /**
     * Đánh dấu entity đã bị xoá (soft delete)
     * deleteBy sẽ tự động được set từ Security Context
     */
    public void softDelete(String deletedBy) {
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }

    /**
     * Khôi phục Entity đã bị soft delete
     */
    public void restore() {
        this.deletedAt = null;
        this.deletedBy = null;
    }

    /**
     * Kiểm tra entity đã bị xoá chưa
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

}
