CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    nickname VARCHAR(255) NOT NULL,
    provider VARCHAR(50) NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE stocks (
    id UUID PRIMARY KEY,
    market VARCHAR(50) NOT NULL,
    symbol VARCHAR(16) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE watchlist_items (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    stock_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_watchlist_user_stock UNIQUE (user_id, stock_id),
    CONSTRAINT fk_watchlist_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_watchlist_stock FOREIGN KEY (stock_id) REFERENCES stocks (id)
);

CREATE TABLE alert_rules (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    stock_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    target_price NUMERIC(19, 2),
    change_rate NUMERIC(8, 3),
    enabled BOOLEAN NOT NULL,
    repeat_policy VARCHAR(50) NOT NULL,
    last_triggered_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_alert_rule_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_alert_rule_stock FOREIGN KEY (stock_id) REFERENCES stocks (id)
);

CREATE TABLE price_snapshots (
    id UUID PRIMARY KEY,
    stock_id UUID NOT NULL,
    price NUMERIC(19, 2) NOT NULL,
    change_rate NUMERIC(8, 3) NOT NULL,
    captured_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_price_snapshot_stock FOREIGN KEY (stock_id) REFERENCES stocks (id)
);

CREATE TABLE notification_channels (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    destination VARCHAR(255) NOT NULL,
    verified BOOLEAN NOT NULL,
    enabled BOOLEAN NOT NULL,
    CONSTRAINT fk_notification_channel_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE alert_events (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    alert_rule_id UUID NOT NULL,
    stock_id UUID NOT NULL,
    trigger_price NUMERIC(19, 2) NOT NULL,
    trigger_change_rate NUMERIC(8, 3) NOT NULL,
    message VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    sent_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_alert_event_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_alert_event_rule FOREIGN KEY (alert_rule_id) REFERENCES alert_rules (id),
    CONSTRAINT fk_alert_event_stock FOREIGN KEY (stock_id) REFERENCES stocks (id)
);

CREATE INDEX idx_price_snapshots_stock_captured_at ON price_snapshots (stock_id, captured_at DESC);
CREATE INDEX idx_alert_rules_enabled ON alert_rules (enabled);
CREATE INDEX idx_alert_events_user_sent_at ON alert_events (user_id, sent_at DESC);
