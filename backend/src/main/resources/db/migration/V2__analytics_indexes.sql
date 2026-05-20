-- V2__analytics_indexes.sql
-- Additional indexes for analytics query performance
-- NOTE: MySQL 8.0 does not support CREATE INDEX IF NOT EXISTS or
-- ALTER TABLE ADD COLUMN IF NOT EXISTS — use plain DDL inside Flyway.
-- Flyway runs each migration exactly once, so duplicate protection is unnecessary.

ALTER TABLE url_analytics
    ADD COLUMN browser VARCHAR(100) AFTER device_type;

CREATE INDEX idx_analytics_tenant_accessed
    ON url_analytics (tenant_id, accessed_at);

CREATE INDEX idx_analytics_country
    ON url_analytics (short_url_id, country, accessed_at);

CREATE INDEX idx_short_urls_active_tenant
    ON short_urls (tenant_id, is_active, created_at);
