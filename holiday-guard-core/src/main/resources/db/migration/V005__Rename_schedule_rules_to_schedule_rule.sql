ALTER TABLE schedule_rules RENAME TO schedule_rule;
ALTER TABLE schedule_rule ADD CONSTRAINT uk_schedule_rule_version_id UNIQUE (version_id);
