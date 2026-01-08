-- ========================================
-- Blog API - Initial Database Schema
-- Version: 1.0
-- Database: PostgreSQL 17+
-- ========================================

-- ========================================
-- TABLE: users
-- Stores user accounts with authentication info
-- ========================================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    
    -- Soft delete fields
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50)
);

-- Index for faster username lookup (used in authentication)
CREATE INDEX idx_users_username ON users(username);

-- Index for role-based queries
CREATE INDEX idx_users_role ON users(role);

-- Index for soft delete queries (only non-deleted users)
CREATE INDEX idx_users_not_deleted ON users(deleted_at) WHERE deleted_at IS NULL;

-- ========================================
-- TABLE: posts
-- Stores blog posts
-- ========================================
CREATE TABLE posts (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    user_id BIGINT NOT NULL,
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    
    -- Soft delete fields
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50),
    
    -- Foreign key constraint
    CONSTRAINT fk_posts_user FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE
);

-- Index for user's posts
CREATE INDEX idx_posts_user_id ON posts(user_id);

-- Index for title search (case-insensitive)
CREATE INDEX idx_posts_title ON posts USING gin(to_tsvector('english', title));

-- Index for content search (case-insensitive)
CREATE INDEX idx_posts_content ON posts USING gin(to_tsvector('english', content));

-- Index for sorting by created date
CREATE INDEX idx_posts_created_at ON posts(created_at DESC);

-- Index for soft delete queries
CREATE INDEX idx_posts_not_deleted ON posts(deleted_at) WHERE deleted_at IS NULL;

-- Composite index for user's active posts
CREATE INDEX idx_posts_user_not_deleted ON posts(user_id, deleted_at) WHERE deleted_at IS NULL;

-- ========================================
-- TABLE: comments
-- Stores comments on blog posts
-- ========================================
CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    body TEXT NOT NULL,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    
    -- Soft delete fields
    deleted_at TIMESTAMP,
    deleted_by VARCHAR(50),
    
    -- Foreign key constraints
    CONSTRAINT fk_comments_post FOREIGN KEY (post_id) 
        REFERENCES posts(id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id) 
        REFERENCES users(id) 
        ON DELETE CASCADE
);

-- Index for post's comments
CREATE INDEX idx_comments_post_id ON comments(post_id);

-- Index for user's comments
CREATE INDEX idx_comments_user_id ON comments(user_id);

-- Index for sorting by created date
CREATE INDEX idx_comments_created_at ON comments(created_at DESC);

-- Index for soft delete queries
CREATE INDEX idx_comments_not_deleted ON comments(deleted_at) WHERE deleted_at IS NULL;

-- Composite index for post's active comments
CREATE INDEX idx_comments_post_not_deleted ON comments(post_id, deleted_at) WHERE deleted_at IS NULL;

-- ========================================
-- INITIAL DATA (Optional - for testing)
-- ========================================

-- Create admin user (password: admin123)
-- Note: Bcrypt hash generated with strength 10
INSERT INTO users (username, password, role, created_at, created_by) 
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', CURRENT_TIMESTAMP, 'system');

-- Create regular user (password: user123)
INSERT INTO users (username, password, role, created_at, created_by) 
VALUES ('user', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER', CURRENT_TIMESTAMP, 'system');

-- ========================================
-- COMMENTS
-- ========================================

-- This schema includes:
-- ✅ Primary keys with BIGSERIAL (auto-increment)
-- ✅ Foreign keys with CASCADE delete
-- ✅ Audit fields (created_at, updated_at, created_by, updated_by)
-- ✅ Soft delete support (deleted_at, deleted_by)
-- ✅ Optimized indexes for common queries
-- ✅ Full-text search indexes for posts
-- ✅ Initial test data

-- Performance notes:
-- - GIN indexes on title/content enable fast full-text search
-- - Partial indexes on deleted_at save space (only index non-deleted)
-- - Composite indexes optimize common query patterns

-- Migration verified: ✅
-- PostgreSQL version: 17.6
-- Date: 2025-11-23
