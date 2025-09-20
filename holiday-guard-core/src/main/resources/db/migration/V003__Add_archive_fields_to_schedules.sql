ALTER TABLE schedules ADD COLUMN archived_at TIMESTAMP;
ALTER TABLE schedules ADD COLUMN archived_by VARCHAR(255);
