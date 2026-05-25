-- ================================================================
-- Auth Service Database Schema
-- Auth Service Database Schema Migration Script
-- Version: 1.0.0
-- Database: MySQL 8.0
-- ================================================================

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS auth_service
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE auth_service;

-- ================================================================
-- 1. Users Table
-- ================================================================
CREATE TABLE IF NOT EXISTS adm_users (
    id CHAR(36) PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL COMMENT 'BCrypt encoded password',
    fullname VARCHAR(200),
    type INT NOT NULL DEFAULT 1 COMMENT '0: ADMIN, 1: CUSTOMER',
    status INT NOT NULL DEFAULT 1 COMMENT '1: ACTIVE, 2: LOCKED, 3: DELETED',
    mobile VARCHAR(20),
    address VARCHAR(500),
    email VARCHAR(100),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_mobile (mobile),
    INDEX idx_type (type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- 2. Authorities Table (Permissions)
-- ================================================================
CREATE TABLE IF NOT EXISTS adm_authorities (
    id CHAR(36) PRIMARY KEY,
    authority VARCHAR(100) NOT NULL UNIQUE COMMENT 'Permission code (e.g., USER_READ)',
    fid VARCHAR(100) COMMENT 'Function ID that this authority belongs to',
    description VARCHAR(500),
    order_id INT DEFAULT 0,
    auth_key VARCHAR(100),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_authority (authority),
    INDEX idx_fid (fid),
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- 3. Groups Table
-- ================================================================
CREATE TABLE IF NOT EXISTS adm_group (
    id CHAR(36) PRIMARY KEY,
    group_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    type INT COMMENT 'Group type',
    status INT NOT NULL DEFAULT 1 COMMENT '1: ACTIVE, 0: INACTIVE',
    authority VARCHAR(100),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_group_name (group_name),
    INDEX idx_type (type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- 4. Group-Authorities Many-to-Many Table
-- ================================================================
CREATE TABLE IF NOT EXISTS adm_group_authorities (
    id CHAR(36) PRIMARY KEY,
    group_id CHAR(36) NOT NULL,
    authority_id CHAR(36) NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES adm_group(id) ON DELETE CASCADE,
    FOREIGN KEY (authority_id) REFERENCES adm_authorities(id) ON DELETE CASCADE,
    UNIQUE KEY unique_group_authority (group_id, authority_id),
    INDEX idx_group_id (group_id),
    INDEX idx_authority_id (authority_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- 5. Group-Users Many-to-Many Table
-- ================================================================
CREATE TABLE IF NOT EXISTS adm_group_users (
    id CHAR(36) PRIMARY KEY,
    group_id CHAR(36) NOT NULL,
    user_id CHAR(36) NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES adm_group(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES adm_users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_group_user (group_id, user_id),
    INDEX idx_group_id (group_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- 6. Parameters Table
-- ================================================================
CREATE TABLE IF NOT EXISTS adm_parameter (
    id CHAR(36) PRIMARY KEY,
    param_key VARCHAR(100) NOT NULL UNIQUE COMMENT 'Parameter key',
    param_value VARCHAR(1000) COMMENT 'Parameter value',
    param_name VARCHAR(200) COMMENT 'Parameter display name',
    description VARCHAR(500),
    status INT NOT NULL DEFAULT 1 COMMENT '1: ACTIVE, 0: INACTIVE',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_param_key (param_key),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- Insert Default Data
-- ================================================================

-- Insert default admin user (username: admin, password: admin123)
-- Password is BCrypt encoded for "admin123"
INSERT INTO adm_users (id, username, password, fullname, type, status) VALUES
(UUID(), 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'System Administrator', 0, 1)
ON DUPLICATE KEY UPDATE username=username;

-- Insert default authorities
INSERT INTO adm_authorities (id, authority, fid, description, order_id) VALUES
(UUID(), 'USER_READ', 'USER', 'Read user information', 1),
(UUID(), 'USER_WRITE', 'USER', 'Create and update users', 2),
(UUID(), 'USER_DELETE', 'USER', 'Delete users', 3),
(UUID(), 'GROUP_READ', 'GROUP', 'Read group information', 4),
(UUID(), 'GROUP_WRITE', 'GROUP', 'Create and update groups', 5),
(UUID(), 'GROUP_DELETE', 'GROUP', 'Delete groups', 6),
(UUID(), 'AUTHORITY_READ', 'AUTHORITY', 'Read authority/permission information', 7),
(UUID(), 'AUTHORITY_WRITE', 'AUTHORITY', 'Create and update authorities', 8),
(UUID(), 'PARAMETER_READ', 'PARAMETER', 'Read system parameters', 9),
(UUID(), 'PARAMETER_WRITE', 'PARAMETER', 'Create and update parameters', 10)
ON DUPLICATE KEY UPDATE authority=authority;

-- Insert default admin group
INSERT INTO adm_group (id, group_name, description, type, status, authority) VALUES
(UUID(), 'ADMIN_GROUP', 'Administrator group with full permissions', 0, 1, 'ALL')
ON DUPLICATE KEY UPDATE group_name=group_name;

-- Insert default parameters
INSERT INTO adm_parameter (id, param_key, param_value, param_name, description, status) VALUES
(UUID(), 'jwt.access-token-expiration', '900000', 'JWT Access Token Expiration', 'Access token expiration time in milliseconds (15 minutes)', 1),
(UUID(), 'jwt.refresh-token-expiration', '604800000', 'JWT Refresh Token Expiration', 'Refresh token expiration time in milliseconds (7 days)', 1),
(UUID(), 'password.min-length', '6', 'Minimum Password Length', 'Minimum password length requirement', 1),
(UUID(), 'password.max-length', '50', 'Maximum Password Length', 'Maximum password length requirement', 1),
(UUID(), 'session.timeout', '1800', 'Session Timeout', 'Session timeout in seconds (30 minutes)', 1)
ON DUPLICATE KEY UPDATE param_key=param_key;
