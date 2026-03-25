ALTER TABLE users
    ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255),
    ADD COLUMN IF NOT EXISTS password_reset_token VARCHAR(255),
    ADD COLUMN IF NOT EXISTS password_reset_expiry TIMESTAMP,
    ADD COLUMN IF NOT EXISTS failed_login_attempts TINYINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP;

-- user_sessions
CREATE TABLE IF NOT EXISTS user_sessions (
    session_id VARCHAR(64) NOT NULL,
    user_id VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    ip_address VARCHAR(45),
    user_agent VARCHAR(255),

    CONSTRAINT pk_user_sessions PRIMARY KEY (session_id),
    CONSTRAINT fk_us_user
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_us_user ON user_sessions(user_id);
CREATE INDEX idx_us_expires_at ON user_sessions(expires_at);

-- user_audit_log
CREATE TABLE IF NOT EXISTS user_audit_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id VARCHAR(20) NOT NULL,
    action VARCHAR(50) NOT NULL,
    performed_by VARCHAR(20),
    details TEXT,
    ip_address VARCHAR(45),
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_user_audit_log PRIMARY KEY (id),
    CONSTRAINT fk_ual_user
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_ual_user ON user_audit_log(user_id);
CREATE INDEX idx_ual_occurred_at ON user_audit_log(occurred_at);
CREATE INDEX idx_ual_action ON user_audit_log(action);

-- role_permissions (reference data)
CREATE TABLE IF NOT EXISTS role_permissions (
    role VARCHAR(20) NOT NULL,
    action VARCHAR(50) NOT NULL,

    CONSTRAINT pk_role_permissions PRIMARY KEY (role, action)
);

INSERT INTO role_permissions (role, action) VALUES
  -- ADMIN
  ('ADMIN','USER_CREATE'),('ADMIN','USER_UPDATE'),('ADMIN','USER_DEACTIVATE'),
  ('ADMIN','USER_DELETE'),('ADMIN','USER_VIEW_ALL'),('ADMIN','USER_VIEW_SELF'),
  ('ADMIN','ROLE_ASSIGN'),('ADMIN','RECOVERY_PLAN_CREATE'),
  ('ADMIN','RECOVERY_PLAN_UPDATE'),('ADMIN','RECOVERY_PLAN_VIEW'),
  ('ADMIN','REPORT_VIEW'),('ADMIN','REPORT_GENERATE'),('ADMIN','REPORT_EMAIL'),
  ('ADMIN','ELIGIBILITY_CHECK'),('ADMIN','ENROLMENT_APPROVE'),
  ('ADMIN','EMAIL_NOTIFICATION_SEND'),('ADMIN','SYSTEM_CONFIG'),
  -- INSTRUCTOR
  ('INSTRUCTOR','USER_VIEW_SELF'),('INSTRUCTOR','RECOVERY_PLAN_CREATE'),
  ('INSTRUCTOR','RECOVERY_PLAN_UPDATE'),('INSTRUCTOR','RECOVERY_PLAN_VIEW'),
  ('INSTRUCTOR','REPORT_VIEW'),('INSTRUCTOR','REPORT_GENERATE'),
  ('INSTRUCTOR','REPORT_EMAIL'),('INSTRUCTOR','ELIGIBILITY_CHECK'),
  ('INSTRUCTOR','EMAIL_NOTIFICATION_SEND'),
  -- STUDENT
  ('STUDENT','USER_VIEW_SELF'),('STUDENT','RECOVERY_PLAN_VIEW'),
  ('STUDENT','REPORT_VIEW');