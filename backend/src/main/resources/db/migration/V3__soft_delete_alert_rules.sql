ALTER TABLE alert_rules
    ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_alert_rules_enabled_deleted ON alert_rules (enabled, deleted);
