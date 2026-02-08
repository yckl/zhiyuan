ALTER TABLE activity_registration ADD COLUMN rating INT COMMENT '评分(1-5)';
ALTER TABLE activity_registration ADD COLUMN rating_comment VARCHAR(500) COMMENT '评价评语';
