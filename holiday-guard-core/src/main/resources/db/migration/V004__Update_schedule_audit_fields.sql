ALTER TABLE schedules DROP COLUMN archived_by;
ALTER TABLE schedules ADD COLUMN created_by VARCHAR(255);
ALTER TABLE schedules ADD COLUMN updated_by VARCHAR(255);
