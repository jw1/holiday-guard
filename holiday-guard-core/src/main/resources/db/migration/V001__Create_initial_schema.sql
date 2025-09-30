-- V001: Create initial schema for all tables

-- Create schedule table
CREATE TABLE schedule (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255),
    country VARCHAR(255) NOT NULL DEFAULT 'US',
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL
);

-- Create version table
CREATE TABLE "version" (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    schedule_id UUID NOT NULL,
    effective_from TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT false,
    CONSTRAINT fk_version_schedule FOREIGN KEY (schedule_id) REFERENCES schedule(id) ON DELETE CASCADE
);

-- Create rule table
CREATE TABLE "rule" (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    schedule_id UUID NOT NULL,
    version_id UUID NOT NULL UNIQUE,
    rule_type VARCHAR(50) NOT NULL,
    rule_config TEXT,
    effective_from DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT true,
    CONSTRAINT fk_rule_schedule FOREIGN KEY (schedule_id) REFERENCES schedule(id) ON DELETE CASCADE,
    CONSTRAINT fk_rule_version FOREIGN KEY (version_id) REFERENCES "version"(id) ON DELETE CASCADE,
    CONSTRAINT rule_type_check CHECK (rule_type IN ('WEEKDAYS_ONLY', 'US_FEDERAL_RESERVE_BUSINESS_DAYS', 'CRON_EXPRESSION', 'ALL_DAYS'))
);

-- Create deviation table
CREATE TABLE "deviation" (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    schedule_id UUID NOT NULL,
    version_id UUID NOT NULL,
    override_date DATE NOT NULL,
    action VARCHAR(20) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    created_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at DATE,
    CONSTRAINT fk_deviation_schedule FOREIGN KEY (schedule_id) REFERENCES schedule(id) ON DELETE CASCADE,
    CONSTRAINT fk_deviation_version FOREIGN KEY (version_id) REFERENCES "version"(id) ON DELETE CASCADE,
    CONSTRAINT deviation_action_check CHECK (action IN ('SKIP', 'FORCE_RUN'))
);

-- Create query_log table
CREATE TABLE query_log (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    schedule_id UUID NOT NULL,
    version_id UUID NOT NULL,
    query_date DATE NOT NULL,
    should_run_result BOOLEAN NOT NULL,
    reason VARCHAR(500) NOT NULL,
    deviation_applied BOOLEAN NOT NULL DEFAULT false,
    queried_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    client_identifier VARCHAR(100),
    CONSTRAINT fk_query_log_schedule FOREIGN KEY (schedule_id) REFERENCES schedule(id) ON DELETE CASCADE,
    CONSTRAINT fk_query_log_version FOREIGN KEY (version_id) REFERENCES "version"(id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX idx_version_schedule_id ON "version" (schedule_id);
CREATE INDEX idx_version_active ON "version" (active);
CREATE UNIQUE INDEX idx_version_active_unique ON "version" (schedule_id) WHERE active = true;

CREATE INDEX idx_rule_schedule_version ON "rule" (schedule_id, version_id);
CREATE INDEX idx_rule_active ON "rule" (active);
CREATE INDEX idx_rule_effective_from ON "rule" (effective_from);

CREATE INDEX idx_deviation_schedule_version ON "deviation" (schedule_id, version_id);
CREATE INDEX idx_deviation_date ON "deviation" (override_date);
CREATE INDEX idx_deviation_expires_at ON "deviation" (expires_at);

CREATE INDEX idx_query_log_schedule_date ON query_log (schedule_id, query_date);
CREATE INDEX idx_query_log_queried_at ON query_log (queried_at);
CREATE INDEX idx_query_log_client ON query_log (client_identifier);