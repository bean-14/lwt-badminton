-- Migration: Add leave functionality and time fields to booking table
-- Date: 2026-05-07
-- Description: Add start_time, end_time, leave_reason, leave_time columns

USE lwt;

-- Add time columns
ALTER TABLE booking
    ADD COLUMN start_time TIME COMMENT 'Class start time' AFTER schedule_date,
    ADD COLUMN end_time TIME COMMENT 'Class end time' AFTER start_time;

-- Add leave-related columns to booking table
ALTER TABLE booking
    ADD COLUMN leave_reason VARCHAR(500) COMMENT 'Leave reason' AFTER confirm_time,
    ADD COLUMN leave_time DATETIME COMMENT 'Leave request time' AFTER leave_reason;

-- Update status comment to include all statuses
ALTER TABLE booking
    MODIFY COLUMN status VARCHAR(20) DEFAULT 'pending' COMMENT 'pending / confirmed / completed / cancelled / rejected / no_confirm / leave / leave_pending';
