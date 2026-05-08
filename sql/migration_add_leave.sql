-- Migration: Add leave functionality to booking table
-- Date: 2026-05-07
-- Description: Add leave_reason and leave_time columns to support leave requests for confirmed bookings

USE lwt;

-- Add leave-related columns to booking table
ALTER TABLE booking
    ADD COLUMN leave_reason VARCHAR(500) COMMENT 'Leave reason (only for leave status)' AFTER confirm_time,
    ADD COLUMN leave_time DATETIME COMMENT 'Leave request time' AFTER leave_reason;

-- Update status comment to include 'leave' status
ALTER TABLE booking
    MODIFY COLUMN status VARCHAR(20) DEFAULT 'pending' COMMENT 'pending / confirmed / cancelled / leave';
