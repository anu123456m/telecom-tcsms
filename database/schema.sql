-- =====================================================
-- TCSMS - Telecom Customer & Subscription Management System
-- Database schema + seed data (MySQL 8)
-- Run this whole script to recreate the database.
-- =====================================================

CREATE DATABASE IF NOT EXISTS tcsms_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE tcsms_db;

-- -----------------------------------------------------
-- BATCH 1: access and security
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS customer (
    customer_id        INT AUTO_INCREMENT PRIMARY KEY,
    customer_number    VARCHAR(15)  NOT NULL UNIQUE,
    first_name         VARCHAR(50)  NOT NULL,
    last_name          VARCHAR(50)  NOT NULL,
    date_of_birth      DATE         NOT NULL,
    email              VARCHAR(100) NOT NULL UNIQUE,
    mobile_number      VARCHAR(20)  NOT NULL UNIQUE,
    address            VARCHAR(255) NOT NULL,
    city               VARCHAR(60)  NOT NULL,
    country            VARCHAR(60)  NOT NULL,
    username           VARCHAR(50)  NOT NULL UNIQUE,
    password_hash      VARCHAR(255) NOT NULL,
    registration_date  DATE         NOT NULL DEFAULT (CURRENT_DATE),
    account_status     ENUM('ACTIVE','INACTIVE','SUSPENDED','LOCKED') NOT NULL DEFAULT 'ACTIVE',
    failed_attempts    TINYINT      NOT NULL DEFAULT 0,
    locked_until       DATETIME     NULL,
    last_login         DATETIME     NULL,
    created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_customer_city (city),
    INDEX idx_customer_status (account_status)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS administrator (
    admin_id         INT AUTO_INCREMENT PRIMARY KEY,
    username         VARCHAR(50)  NOT NULL UNIQUE,
    password_hash    VARCHAR(255) NOT NULL,
    full_name        VARCHAR(100) NOT NULL,
    email            VARCHAR(100) NOT NULL UNIQUE,
    role             ENUM('SUPER_ADMIN','BILLING_ADMIN','SUPPORT_ADMIN') NOT NULL DEFAULT 'SUPPORT_ADMIN',
    status           ENUM('ACTIVE','INACTIVE','LOCKED') NOT NULL DEFAULT 'ACTIVE',
    failed_attempts  TINYINT      NOT NULL DEFAULT 0,
    locked_until     DATETIME     NULL,
    last_login       DATETIME     NULL,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS login_history (
    login_id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_type           ENUM('CUSTOMER','ADMIN') NOT NULL,
    customer_id         INT          NULL,
    admin_id            INT          NULL,
    username_attempted  VARCHAR(50)  NOT NULL,
    login_time          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    logout_time         DATETIME     NULL,
    login_status        ENUM('SUCCESS','FAILED','LOCKED') NOT NULL,
    failure_reason      VARCHAR(100) NULL,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_login_customer FOREIGN KEY (customer_id) REFERENCES customer(customer_id),
    CONSTRAINT fk_login_admin    FOREIGN KEY (admin_id)    REFERENCES administrator(admin_id),
    INDEX idx_login_customer (customer_id, login_time),
    INDEX idx_login_admin (admin_id, login_time)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS password_reset_otp (
    otp_id       INT AUTO_INCREMENT PRIMARY KEY,
    customer_id  INT        NOT NULL,
    otp_code     CHAR(6)    NOT NULL,
    expires_at   DATETIME   NOT NULL,
    is_used      BOOLEAN    NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_otp_customer FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE CASCADE,
    INDEX idx_otp_customer (customer_id, is_used)
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- BATCH 2: plans, SIMs, subscriptions
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS telecom_plan (
    plan_id               INT AUTO_INCREMENT PRIMARY KEY,
    plan_code             VARCHAR(20)   NOT NULL UNIQUE,
    plan_name             VARCHAR(80)   NOT NULL,
    plan_type             ENUM('PREPAID','POSTPAID') NOT NULL,
    monthly_rental        DECIMAL(10,2) NOT NULL,
    data_allowance_gb     DECIMAL(8,2)  NOT NULL,
    voice_minutes         INT           NOT NULL COMMENT '-1 means unlimited',
    sms_allowance         INT           NOT NULL COMMENT '-1 means unlimited',
    validity_days         INT           NOT NULL DEFAULT 30,
    international_roaming BOOLEAN       NOT NULL DEFAULT FALSE,
    restricted_upgrade    BOOLEAN       NOT NULL DEFAULT FALSE,
    status                ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at            TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_plan_rental   CHECK (monthly_rental >= 0),
    CONSTRAINT chk_plan_data     CHECK (data_allowance_gb >= 0),
    CONSTRAINT chk_plan_validity CHECK (validity_days > 0),
    INDEX idx_plan_status (status),
    INDEX idx_plan_rental (monthly_rental)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sim_card (
    sim_id      INT AUTO_INCREMENT PRIMARY KEY,
    sim_number  VARCHAR(22) NOT NULL UNIQUE,
    sim_type    ENUM('PHYSICAL_SIM','ESIM') NOT NULL,
    sim_status  ENUM('AVAILABLE','ASSIGNED','BLOCKED','DEACTIVATED') NOT NULL DEFAULT 'AVAILABLE',
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_sim_status (sim_status)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS mobile_subscription (
    subscription_id      INT AUTO_INCREMENT PRIMARY KEY,
    subscription_number  VARCHAR(20) NOT NULL UNIQUE,
    customer_id          INT         NOT NULL,
    plan_id              INT         NOT NULL,
    sim_id               INT         NOT NULL UNIQUE,
    mobile_number        VARCHAR(20) NOT NULL UNIQUE,
    activation_date      DATE        NOT NULL,
    subscription_type    ENUM('PREPAID','POSTPAID') NOT NULL,
    status               ENUM('ACTIVE','INACTIVE','SUSPENDED','TERMINATED') NOT NULL DEFAULT 'ACTIVE',
    created_at           TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_sub_customer FOREIGN KEY (customer_id) REFERENCES customer(customer_id),
    CONSTRAINT fk_sub_plan     FOREIGN KEY (plan_id)     REFERENCES telecom_plan(plan_id),
    CONSTRAINT fk_sub_sim      FOREIGN KEY (sim_id)      REFERENCES sim_card(sim_id),
    INDEX idx_sub_customer_plan (customer_id, plan_id),
    INDEX idx_sub_status (status)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS subscription_history (
    history_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    subscription_id  INT          NOT NULL,
    old_plan_id      INT          NULL,
    new_plan_id      INT          NOT NULL,
    change_date      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    change_reason    VARCHAR(200) NULL,
    changed_by       VARCHAR(50)  NOT NULL,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_hist_sub     FOREIGN KEY (subscription_id) REFERENCES mobile_subscription(subscription_id),
    CONSTRAINT fk_hist_oldplan FOREIGN KEY (old_plan_id)     REFERENCES telecom_plan(plan_id),
    CONSTRAINT fk_hist_newplan FOREIGN KEY (new_plan_id)     REFERENCES telecom_plan(plan_id),
    INDEX idx_hist_sub_date (subscription_id, change_date)
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- BATCH 3: add-ons, usage, billing, payments
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS add_on (
    add_on_id       INT AUTO_INCREMENT PRIMARY KEY,
    add_on_code     VARCHAR(20)   NOT NULL UNIQUE,
    add_on_name     VARCHAR(80)   NOT NULL,
    description     VARCHAR(255)  NULL,
    monthly_charge  DECIMAL(10,2) NOT NULL,
    status          ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_addon_charge CHECK (monthly_charge >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS subscription_add_on (
    sub_addon_id     INT AUTO_INCREMENT PRIMARY KEY,
    subscription_id  INT       NOT NULL,
    add_on_id        INT       NOT NULL,
    activated_on     DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deactivated_on   DATETIME  NULL,
    status           ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_sa_sub   FOREIGN KEY (subscription_id) REFERENCES mobile_subscription(subscription_id),
    CONSTRAINT fk_sa_addon FOREIGN KEY (add_on_id)       REFERENCES add_on(add_on_id),
    CONSTRAINT uq_sub_addon UNIQUE (subscription_id, add_on_id),
    INDEX idx_sa_status (status)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS usage_record (
    usage_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    subscription_id  INT           NOT NULL,
    usage_date       DATE          NOT NULL,
    usage_type       ENUM('VOICE','SMS','DATA','ROAMING') NOT NULL,
    quantity         DECIMAL(12,2) NOT NULL,
    unit             ENUM('GB','MB','MINUTES','COUNT') NOT NULL,
    charge           DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    processed        BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_usage_sub FOREIGN KEY (subscription_id) REFERENCES mobile_subscription(subscription_id),
    CONSTRAINT chk_usage_qty    CHECK (quantity >= 0),
    CONSTRAINT chk_usage_charge CHECK (charge >= 0),
    INDEX idx_usage_sub_date (subscription_id, usage_date),
    INDEX idx_usage_type (usage_type),
    INDEX idx_usage_processed (processed)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS bill (
    bill_id          INT AUTO_INCREMENT PRIMARY KEY,
    bill_number      VARCHAR(30)   NOT NULL UNIQUE,
    subscription_id  INT           NOT NULL,
    billing_month    DATE          NOT NULL COMMENT 'first day of the billed month',
    plan_rental      DECIMAL(10,2) NOT NULL,
    usage_charges    DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    tax_amount       DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    discount         DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_amount     DECIMAL(10,2) NOT NULL,
    due_date         DATE          NOT NULL,
    bill_status      ENUM('GENERATED','PAID','OVERDUE','CANCELLED') NOT NULL DEFAULT 'GENERATED',
    created_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_bill_sub FOREIGN KEY (subscription_id) REFERENCES mobile_subscription(subscription_id),
    CONSTRAINT uq_bill_sub_month UNIQUE (subscription_id, billing_month),
    CONSTRAINT chk_bill_total CHECK (total_amount = plan_rental + usage_charges + tax_amount - discount),
    INDEX idx_bill_status_due (bill_status, due_date),
    INDEX idx_bill_month (billing_month)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS payment (
    payment_id             INT AUTO_INCREMENT PRIMARY KEY,
    transaction_reference  VARCHAR(40)   NOT NULL UNIQUE,
    bill_id                INT           NOT NULL,
    customer_id            INT           NOT NULL,
    amount                 DECIMAL(10,2) NOT NULL,
    payment_mode           ENUM('UPI','CARD','NET_BANKING','BANK_TRANSFER') NOT NULL,
    payment_date           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    payment_status         ENUM('SUCCESS','FAILED','PENDING') NOT NULL,
    created_at             TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_pay_bill     FOREIGN KEY (bill_id)     REFERENCES bill(bill_id),
    CONSTRAINT fk_pay_customer FOREIGN KEY (customer_id) REFERENCES customer(customer_id),
    CONSTRAINT chk_pay_amount  CHECK (amount > 0),
    INDEX idx_pay_bill (bill_id, payment_status),
    INDEX idx_pay_customer (customer_id, payment_date)
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- BATCH 4: complaints, notifications, audit
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS complaint (
    complaint_id      INT AUTO_INCREMENT PRIMARY KEY,
    complaint_number  VARCHAR(20)  NOT NULL UNIQUE,
    customer_id       INT          NOT NULL,
    subscription_id   INT          NULL,
    category          ENUM('BILLING','NETWORK','SIM','PLAN','PAYMENT','OTHER') NOT NULL,
    description       VARCHAR(1000) NOT NULL,
    priority          ENUM('LOW','MEDIUM','HIGH','CRITICAL') NOT NULL DEFAULT 'MEDIUM',
    created_date      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status            ENUM('OPEN','IN_PROGRESS','RESOLVED','CLOSED') NOT NULL DEFAULT 'OPEN',
    resolution        VARCHAR(1000) NULL,
    resolved_by       INT          NULL,
    resolved_date     DATETIME     NULL,
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_comp_customer FOREIGN KEY (customer_id)     REFERENCES customer(customer_id),
    CONSTRAINT fk_comp_sub      FOREIGN KEY (subscription_id) REFERENCES mobile_subscription(subscription_id),
    CONSTRAINT fk_comp_admin    FOREIGN KEY (resolved_by)     REFERENCES administrator(admin_id),
    INDEX idx_comp_customer (customer_id),
    INDEX idx_comp_status_priority (status, priority),
    INDEX idx_comp_category (category)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS notification (
    notification_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id        INT          NOT NULL,
    notification_type  ENUM('BILL','PAYMENT','USAGE_ALERT','ACCOUNT','COMPLAINT','GENERAL') NOT NULL,
    message            VARCHAR(500) NOT NULL,
    status             ENUM('PENDING','SENT','FAILED') NOT NULL DEFAULT 'PENDING',
    sent_at            DATETIME     NULL,
    created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_customer FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE CASCADE,
    INDEX idx_notif_customer (customer_id, status),
    INDEX idx_notif_status (status)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS audit_log (
    audit_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_type   ENUM('CUSTOMER','ADMIN','SYSTEM') NOT NULL,
    actor_id     INT          NULL,
    action       VARCHAR(50)  NOT NULL,
    entity_type  VARCHAR(50)  NOT NULL,
    entity_id    INT          NULL,
    details      VARCHAR(500) NULL,
    action_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_audit_entity (entity_type, entity_id),
    INDEX idx_audit_time (action_time)
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- SEED DATA (INSERT IGNORE = safe to re-run)
-- -----------------------------------------------------
INSERT IGNORE INTO telecom_plan
(plan_code, plan_name, plan_type, monthly_rental, data_allowance_gb, voice_minutes, sms_allowance, validity_days, international_roaming, restricted_upgrade, status)
VALUES
('PLAN-101', '5G Premium',   'POSTPAID',  999.00, 100.00,   -1, 3000, 30, TRUE,  FALSE, 'ACTIVE'),
('PLAN-102', '5G Standard',  'POSTPAID',  699.00,  50.00, 1500, 1500, 30, FALSE, FALSE, 'ACTIVE'),
('PLAN-103', 'Business Pro', 'POSTPAID', 1499.00, 200.00,   -1,   -1, 30, TRUE,  TRUE,  'ACTIVE'),
('PLAN-104', 'Basic',        'PREPAID',   399.00,  20.00,  500,  500, 28, FALSE, FALSE, 'ACTIVE'),
('PLAN-105', 'Prepaid Plus', 'PREPAID',   499.00,  30.00,   -1, 1000, 28, FALSE, FALSE, 'ACTIVE'),
('PLAN-106', 'Legacy Lite',  'PREPAID',   199.00,   5.00,  200,  100, 28, FALSE, FALSE, 'INACTIVE');

INSERT IGNORE INTO add_on (add_on_code, add_on_name, description, monthly_charge, status) VALUES
('ADDON-001', 'International Roaming Pack', 'Roaming calls and data abroad', 299.00, 'ACTIVE'),
('ADDON-002', 'Extra 10 GB Data',           'Additional 10 GB of data',       99.00, 'ACTIVE'),
('ADDON-003', 'Caller Tune',                'Custom caller tune',             30.00, 'ACTIVE'),
('ADDON-004', 'OTT Bundle',                 'Streaming apps bundle',         149.00, 'ACTIVE');

INSERT IGNORE INTO sim_card (sim_number, sim_type, sim_status) VALUES
('89911012000000000001', 'PHYSICAL_SIM', 'AVAILABLE'),
('89911012000000000002', 'PHYSICAL_SIM', 'AVAILABLE'),
('89911012000000000003', 'PHYSICAL_SIM', 'AVAILABLE'),
('89911012000000000004', 'PHYSICAL_SIM', 'AVAILABLE'),
('89911012000000000005', 'PHYSICAL_SIM', 'AVAILABLE'),
('89911012000000000006', 'ESIM',         'AVAILABLE'),
('89911012000000000007', 'ESIM',         'AVAILABLE'),
('89911012000000000008', 'ESIM',         'AVAILABLE'),
('89911012000000000009', 'ESIM',         'AVAILABLE'),
('89911012000000000010', 'ESIM',         'AVAILABLE');

-- Customers, administrators, subscriptions, usage and bills are created
-- from the Java application (passwords must be hashed by PasswordUtil).