-- V1__initial_schema.sql
-- SwiftLinkAI initial database schema

CREATE TABLE IF NOT EXISTS users (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id       VARCHAR(64) NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255),
    role            VARCHAR(50) NOT NULL DEFAULT 'USER',
    is_active       TINYINT(1) NOT NULL DEFAULT 1,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_users_tenant_id (tenant_id),
    INDEX idx_users_tenant_email (tenant_id, email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS short_urls (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id       VARCHAR(64)  NOT NULL,
    alias           VARCHAR(255) NOT NULL UNIQUE,
    original_url    TEXT         NOT NULL,
    generated_tags  JSON,
    category        VARCHAR(255),
    seo_title       VARCHAR(500),
    seo_description TEXT,
    qr_code_url     VARCHAR(512),
    created_by      BIGINT,
    expires_at      TIMESTAMP    NULL,
    click_count     BIGINT       NOT NULL DEFAULT 0,
    is_active       TINYINT(1)   NOT NULL DEFAULT 1,
    created_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_short_urls_tenant (tenant_id),
    INDEX idx_short_urls_tenant_alias (tenant_id, alias),
    INDEX idx_short_urls_created_by (created_by),
    INDEX idx_short_urls_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS url_analytics (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id     VARCHAR(64) NOT NULL,
    short_url_id  BIGINT,
    ip_address    VARCHAR(255),
    user_agent    TEXT,
    referer       TEXT,
    country       VARCHAR(255),
    city          VARCHAR(255),
    device_type   VARCHAR(50),
    accessed_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (short_url_id) REFERENCES short_urls(id) ON DELETE SET NULL,
    INDEX idx_analytics_short_url (short_url_id),
    INDEX idx_analytics_tenant (tenant_id),
    INDEX idx_analytics_accessed_at (accessed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
