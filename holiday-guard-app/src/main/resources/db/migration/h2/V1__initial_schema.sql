-- Holiday Guard initial schema
-- V1: Creates all tables from scratch.
-- Flyway manages schema going forward; Hibernate ddl-auto is set to 'validate'.

CREATE TABLE IF NOT EXISTS schedule (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255)    NOT NULL,
    description VARCHAR(1000),
    country     VARCHAR(2)      NOT NULL DEFAULT 'US',
    active      BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP       NOT NULL,
    updated_at  TIMESTAMP       NOT NULL,
    created_by  VARCHAR(255)    NOT NULL,
    updated_by  VARCHAR(255)    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_schedule_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS version (
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    schedule_id    BIGINT      NOT NULL,
    effective_from TIMESTAMP   NOT NULL,
    created_at     TIMESTAMP   NOT NULL,
    active         BOOLEAN     NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    CONSTRAINT fk_version_schedule FOREIGN KEY (schedule_id) REFERENCES schedule (id)
);

CREATE TABLE IF NOT EXISTS rule (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    schedule_id    BIGINT       NOT NULL,
    version_id     BIGINT       NOT NULL,
    rule_type      VARCHAR(64)  NOT NULL,
    rule_config    TEXT,
    effective_from DATE         NOT NULL,
    created_at     TIMESTAMP    NOT NULL,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT uq_rule_version UNIQUE (version_id),
    CONSTRAINT fk_rule_schedule FOREIGN KEY (schedule_id) REFERENCES schedule (id),
    CONSTRAINT fk_rule_version  FOREIGN KEY (version_id)  REFERENCES version (id)
);

CREATE TABLE IF NOT EXISTS deviation (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    schedule_id    BIGINT       NOT NULL,
    version_id     BIGINT       NOT NULL,
    deviation_date DATE         NOT NULL,
    action         VARCHAR(32)  NOT NULL,
    reason         VARCHAR(500) NOT NULL,
    created_by     VARCHAR(255),
    created_at     TIMESTAMP    NOT NULL,
    expires_at     DATE,
    PRIMARY KEY (id),
    CONSTRAINT fk_deviation_schedule FOREIGN KEY (schedule_id) REFERENCES schedule (id),
    CONSTRAINT fk_deviation_version  FOREIGN KEY (version_id)  REFERENCES version (id)
);

CREATE TABLE IF NOT EXISTS query_log (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    schedule_id       BIGINT       NOT NULL,
    version_id        BIGINT       NOT NULL,
    query_date        DATE         NOT NULL,
    should_run_result BOOLEAN      NOT NULL,
    reason            VARCHAR(500) NOT NULL,
    deviation_applied BOOLEAN      NOT NULL DEFAULT FALSE,
    queried_at        TIMESTAMP    NOT NULL,
    client_identifier VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT fk_query_log_schedule FOREIGN KEY (schedule_id) REFERENCES schedule (id),
    CONSTRAINT fk_query_log_version  FOREIGN KEY (version_id)  REFERENCES version (id)
);
