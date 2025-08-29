-- Create the base schedules table
CREATE TABLE schedules (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    country VARCHAR(3) NOT NULL DEFAULT 'US',
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Add unique constraint on name
ALTER TABLE schedules ADD CONSTRAINT schedules_name_unique UNIQUE (name);

-- Add check constraint for country code format
ALTER TABLE schedules ADD CONSTRAINT schedules_country_format CHECK (country ~ '^[A-Z]{2,3}$');

-- Create index for common queries
CREATE INDEX idx_schedules_active ON schedules (is_active);
CREATE INDEX idx_schedules_country ON schedules (country);

-- Add trigger to automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_schedules_updated_at BEFORE UPDATE ON schedules
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();