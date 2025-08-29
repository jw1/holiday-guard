-- Create schedule_versions table
CREATE TABLE schedule_versions (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    schedule_id UUID NOT NULL,
    effective_from TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT false,
    CONSTRAINT fk_schedule_versions_schedule FOREIGN KEY (schedule_id) REFERENCES schedules(id) ON DELETE CASCADE
);

-- Create schedule_rules table
CREATE TABLE schedule_rules (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    schedule_id UUID NOT NULL,
    version_id UUID NOT NULL,
    rule_type VARCHAR(50) NOT NULL,
    rule_config TEXT,
    effective_from DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT true,
    CONSTRAINT fk_schedule_rules_schedule FOREIGN KEY (schedule_id) REFERENCES schedules(id) ON DELETE CASCADE,
    CONSTRAINT fk_schedule_rules_version FOREIGN KEY (version_id) REFERENCES schedule_versions(id) ON DELETE CASCADE,
    CONSTRAINT schedule_rules_rule_type_check CHECK (rule_type IN ('WEEKDAYS_ONLY', 'CRON_EXPRESSION', 'CUSTOM_DATES', 'MONTHLY_PATTERN'))
);

-- Create schedule_overrides table
CREATE TABLE schedule_overrides (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    schedule_id UUID NOT NULL,
    version_id UUID NOT NULL,
    override_date DATE NOT NULL,
    action VARCHAR(20) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    created_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at DATE,
    CONSTRAINT fk_schedule_overrides_schedule FOREIGN KEY (schedule_id) REFERENCES schedules(id) ON DELETE CASCADE,
    CONSTRAINT fk_schedule_overrides_version FOREIGN KEY (version_id) REFERENCES schedule_versions(id) ON DELETE CASCADE,
    CONSTRAINT schedule_overrides_action_check CHECK (action IN ('SKIP', 'FORCE_RUN'))
);

-- Create schedule_materialized_calendar table
CREATE TABLE schedule_materialized_calendar (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    schedule_id UUID NOT NULL,
    version_id UUID NOT NULL,
    occurs_on DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    override_id UUID,
    CONSTRAINT fk_schedule_materialized_calendar_schedule FOREIGN KEY (schedule_id) REFERENCES schedules(id) ON DELETE CASCADE,
    CONSTRAINT fk_schedule_materialized_calendar_version FOREIGN KEY (version_id) REFERENCES schedule_versions(id) ON DELETE CASCADE,
    CONSTRAINT fk_schedule_materialized_calendar_override FOREIGN KEY (override_id) REFERENCES schedule_overrides(id) ON DELETE SET NULL,
    CONSTRAINT schedule_materialized_calendar_status_check CHECK (status IN ('SCHEDULED', 'OVERRIDDEN', 'COMPLETED'))
);

-- Create schedule_query_log table
CREATE TABLE schedule_query_log (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    schedule_id UUID NOT NULL,
    version_id UUID NOT NULL,
    query_date DATE NOT NULL,
    should_run_result BOOLEAN NOT NULL,
    reason VARCHAR(500) NOT NULL,
    override_applied BOOLEAN NOT NULL DEFAULT false,
    queried_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    client_identifier VARCHAR(100),
    CONSTRAINT fk_schedule_query_log_schedule FOREIGN KEY (schedule_id) REFERENCES schedules(id) ON DELETE CASCADE,
    CONSTRAINT fk_schedule_query_log_version FOREIGN KEY (version_id) REFERENCES schedule_versions(id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX idx_schedule_versions_schedule_id ON schedule_versions (schedule_id);
CREATE INDEX idx_schedule_versions_active ON schedule_versions (is_active);
CREATE UNIQUE INDEX idx_schedule_versions_active_unique ON schedule_versions (schedule_id) WHERE is_active = true;

CREATE INDEX idx_schedule_rules_schedule_version ON schedule_rules (schedule_id, version_id);
CREATE INDEX idx_schedule_rules_active ON schedule_rules (is_active);
CREATE INDEX idx_schedule_rules_effective_from ON schedule_rules (effective_from);

CREATE INDEX idx_schedule_overrides_schedule_version ON schedule_overrides (schedule_id, version_id);
CREATE INDEX idx_schedule_overrides_date ON schedule_overrides (override_date);
CREATE INDEX idx_schedule_overrides_expires_at ON schedule_overrides (expires_at);

CREATE INDEX idx_schedule_materialized_calendar_schedule_date ON schedule_materialized_calendar (schedule_id, occurs_on);
CREATE INDEX idx_schedule_materialized_calendar_version ON schedule_materialized_calendar (version_id);
CREATE INDEX idx_schedule_materialized_calendar_occurs_on ON schedule_materialized_calendar (occurs_on);
CREATE UNIQUE INDEX idx_schedule_materialized_calendar_unique ON schedule_materialized_calendar (schedule_id, version_id, occurs_on);

CREATE INDEX idx_schedule_query_log_schedule_date ON schedule_query_log (schedule_id, query_date);
CREATE INDEX idx_schedule_query_log_queried_at ON schedule_query_log (queried_at);
CREATE INDEX idx_schedule_query_log_client ON schedule_query_log (client_identifier);