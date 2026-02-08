-- Add checkin_code column to activity table
ALTER TABLE activity ADD COLUMN checkin_code VARCHAR(20) COMMENT '签到码';
