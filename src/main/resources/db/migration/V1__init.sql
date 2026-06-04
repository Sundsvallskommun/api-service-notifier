-- Consolidated baseline for the merged service (messaging + users + csvimport).
-- Pre-prod / disposable data: this is a single fresh baseline that reflects the FINAL schema state
-- of the three former services' migrations (notifier V1_0/V1_1/V1_2 + webapp-users V1/V2 + one
-- shedlock table). There is one Flyway owner and one shedlock table now.

-- Distributed lock table for @Dept44Scheduled (csvimport).
CREATE TABLE shedlock(
    name       VARCHAR(64)  NOT NULL,
    lock_until TIMESTAMP(3) NOT NULL,
    locked_at  TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    locked_by  VARCHAR(255) NOT NULL,
    PRIMARY KEY (name)
);

-- ---------------------------------------------------------------------------
-- messaging module: organization / employee / groups / messages
-- ---------------------------------------------------------------------------
CREATE TABLE organization(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id    VARCHAR(64) NOT NULL,
    parent_org_id VARCHAR(64),
    org_id        VARCHAR(64) NOT NULL,
    org_name      VARCHAR(255) NOT NULL,
    tree_level    INT NOT NULL,

    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_organization_org_id UNIQUE (org_id),
    CONSTRAINT fk_org_parent FOREIGN KEY (parent_org_id)
        REFERENCES organization(org_id)
        ON DELETE SET NULL
);

CREATE TABLE employee(
    employee_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    person_id       VARCHAR(36) NOT NULL,
    org_id          VARCHAR(64) NOT NULL,
    first_name      VARCHAR(100),
    last_name       VARCHAR(100),
    email           VARCHAR(255),
    work_mobile     VARCHAR(50),
    work_phone      VARCHAR(50),
    work_title      VARCHAR(100),
    active_employee BOOLEAN NOT NULL DEFAULT TRUE,
    manager_id      VARCHAR(36),
    manager_code    VARCHAR(10),

    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uq_pid_org_title UNIQUE (person_id, org_id, work_title),
    CONSTRAINT fk_employee_org FOREIGN KEY (org_id)
        REFERENCES organization(org_id)
        ON DELETE RESTRICT
);

CREATE TABLE user_group(
    group_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_name  VARCHAR(255) NOT NULL,
    description TEXT,
    creator_id  VARCHAR(255) NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE employee_user_group (
    employee_id BIGINT NOT NULL,
    group_id    BIGINT NOT NULL,
    PRIMARY KEY (employee_id, group_id),
    CONSTRAINT fk_eug_employee
        FOREIGN KEY (employee_id) REFERENCES employee(employee_id)
            ON DELETE CASCADE,
    CONSTRAINT fk_eug_group
        FOREIGN KEY (group_id) REFERENCES user_group(group_id)
            ON DELETE CASCADE
);

-- message: group_id was added in V1_0 then dropped in V1_2 — omitted here.
CREATE TABLE message(
    message_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(255) NOT NULL,
    content      TEXT NOT NULL,
    sender       VARCHAR(255) NOT NULL,
    message_type VARCHAR(20) NOT NULL,
    created_at   DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- message_recipient: synthetic id PK as introduced in V1_1 (replaced the composite PK).
CREATE TABLE message_recipient (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id      BIGINT NOT NULL,
    employee_id     BIGINT NOT NULL,
    org_id          VARCHAR(64) NOT NULL,
    work_title      VARCHAR(100),
    received_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delivery_status ENUM('DELIVERED', 'FAILED') NOT NULL DEFAULT 'DELIVERED',

    CONSTRAINT fk_recipient_message FOREIGN KEY (message_id)
        REFERENCES message(message_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_recipient_employee FOREIGN KEY (employee_id)
        REFERENCES employee(employee_id)
        ON DELETE RESTRICT
);

-- ---------------------------------------------------------------------------
-- users module: web-app accounts (formerly the separate user_management schema)
-- ---------------------------------------------------------------------------
CREATE TABLE users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    email_address   VARCHAR(255) NOT NULL UNIQUE,
    phone_number    VARCHAR(20),
    municipality_id VARCHAR(10),
    status          VARCHAR(20),
    password        VARCHAR(255),
    role            VARCHAR(20) NOT NULL DEFAULT 'USER'
);
