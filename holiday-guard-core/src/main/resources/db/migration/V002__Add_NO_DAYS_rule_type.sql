-- V002: Add NO_DAYS rule type to the rule_type CHECK constraint

ALTER TABLE "rule" DROP CONSTRAINT rule_type_check;

ALTER TABLE "rule" ADD CONSTRAINT rule_type_check
    CHECK (rule_type IN ('WEEKDAYS_ONLY', 'US_FEDERAL_RESERVE_BUSINESS_DAYS', 'CRON_EXPRESSION', 'ALL_DAYS', 'NO_DAYS'));
