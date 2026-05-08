-- LWT Course Booking System Database Schema
-- Create database
CREATE DATABASE IF NOT EXISTS lwt DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE lwt;

-- User table (student / coach / admin)
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT 'Login username',
    password VARCHAR(128) NOT NULL COMMENT 'MD5 encrypted password',
    nickname VARCHAR(50) COMMENT 'Display name',
    phone VARCHAR(20) COMMENT 'Phone number',
    user_type VARCHAR(20) NOT NULL COMMENT 'student / coach / admin',
    remaining_hours INT DEFAULT 0 COMMENT 'Remaining course hours',
    status INT DEFAULT 1 COMMENT '1=active, 0=disabled',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT DEFAULT 0 COMMENT 'Logical delete: 0=active, 1=deleted'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User table';

-- Venue table
CREATE TABLE venue (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT 'Venue name, e.g. Court 1',
    location VARCHAR(100) COMMENT 'Location description',
    status INT DEFAULT 1 COMMENT '1=available, 0=unavailable',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Venue table';

-- Coach schedule (available time slots)
CREATE TABLE coach_schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    coach_id BIGINT NOT NULL COMMENT 'Coach user ID',
    venue_id BIGINT NOT NULL COMMENT 'Venue ID',
    schedule_date DATE NOT NULL COMMENT 'Schedule date',
    start_time TIME NOT NULL COMMENT 'Start time',
    end_time TIME NOT NULL COMMENT 'End time',
    status INT DEFAULT 1 COMMENT '1=available, 0=disabled',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_coach_date (coach_id, schedule_date),
    INDEX idx_venue_date (venue_id, schedule_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Coach available schedule';

-- Booking record
CREATE TABLE booking (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL COMMENT 'Student user ID',
    coach_id BIGINT NOT NULL COMMENT 'Coach user ID',
    venue_id BIGINT NOT NULL COMMENT 'Venue ID',
    schedule_date DATE NOT NULL COMMENT 'Booking date',
    start_time TIME COMMENT 'Class start time',
    end_time TIME COMMENT 'Class end time',
    status VARCHAR(20) DEFAULT 'pending' COMMENT 'pending / confirmed / completed / cancelled / rejected / no_confirm / leave / leave_pending',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    confirm_time DATETIME COMMENT 'Confirmation time',
    leave_reason VARCHAR(500) COMMENT 'Leave reason',
    leave_time DATETIME COMMENT 'Leave request time',
    INDEX idx_student_date (student_id, schedule_date),
    INDEX idx_coach_date (coach_id, schedule_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Booking record';

-- Recharge record
CREATE TABLE recharge_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT 'User ID',
    hours INT NOT NULL COMMENT 'Hours recharged',
    amount DECIMAL(10,2) DEFAULT 0.00 COMMENT 'Amount (manual record)',
    remark VARCHAR(200) COMMENT 'Remark',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Recharge record';

-- Insert sample data
INSERT INTO sys_user (username, password, nickname, phone, user_type, remaining_hours, status) VALUES
('admin', MD5('admin123'), 'Administrator', '13800000000', 'admin', 0, 1),
('coach1', MD5('coach123'), 'Coach Wang', '13800000001', 'coach', 0, 1),
('coach2', MD5('coach123'), 'Coach Li', '13800000002', 'coach', 0, 1),
('student1', MD5('student123'), 'Student Zhang', '13800000003', 'student', 10, 1),
('student2', MD5('student123'), 'Student Chen', '13800000004', 'student', 5, 1);

INSERT INTO venue (name, location, status) VALUES
('Court 1', 'Building A, Floor 1', 1),
('Court 2', 'Building A, Floor 1', 1),
('Court 3', 'Building A, Floor 2', 1),
('Court 4', 'Building B, Floor 1', 1),
('Court 5', 'Building B, Floor 2', 1);
