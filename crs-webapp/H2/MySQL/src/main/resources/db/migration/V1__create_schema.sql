-- users
CREATE TABLE IF NOT EXISTS users (
    user_id VARCHAR(20) NOT NULL,
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL,
    email VARCHAR(160) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL DEFAULT 'STUDENT',
    account_status VARCHAR(30) NOT NULL DEFAULT 'PENDING_VERIFICATION',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_users PRIMARY KEY (user_id),
    CONSTRAINT chk_users_role
        CHECK (role IN ('STUDENT','INSTRUCTOR','ADMINISTRATOR')),
    CONSTRAINT chk_users_status
        CHECK (account_status IN ('ACTIVE','INACTIVE','SUSPENDED','PENDING_VERIFICATION'))
);

-- students
CREATE TABLE IF NOT EXISTS students (
    user_id VARCHAR(20) NOT NULL,
    program VARCHAR(120) NOT NULL,
    current_level SMALLINT NOT NULL DEFAULT 100,
    current_semester VARCHAR(30) NOT NULL,
    cgpa DECIMAL(4,4) NOT NULL DEFAULT 0.0000,
    recovery_status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED',
    recovery_plan_detail TEXT,
    advisor_email VARCHAR(160),

    CONSTRAINT pk_students PRIMARY KEY (user_id),
    CONSTRAINT fk_students_users
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT chk_students_level
        CHECK (current_level IN (100,200,300,400,500,600,700,800,900)),
    CONSTRAINT chk_students_cgpa
        CHECK (cgpa >= 0.0 AND cgpa <= 4.0),
    CONSTRAINT chk_students_recovery
        CHECK (recovery_status IN ('NOT_STARTED','IN_PROGRESS','MILESTONE_DUE','COMPLETED','WITHDRAWN'))
);

-- course_results
CREATE TABLE IF NOT EXISTS course_results (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_id VARCHAR(20) NOT NULL,
    course_code VARCHAR(15) NOT NULL,
    course_title VARCHAR(120) NOT NULL,
    credit_hours TINYINT NOT NULL,
    grade VARCHAR(4) NOT NULL,
    grade_point DECIMAL(3,1) NOT NULL,
    semester VARCHAR(30) NOT NULL,
    academic_year SMALLINT NOT NULL,
    recorded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_course_results PRIMARY KEY (id),
    CONSTRAINT fk_cr_student
        FOREIGN KEY (student_id) REFERENCES students(user_id) ON DELETE CASCADE,
    CONSTRAINT chk_cr_credit_hours
        CHECK (credit_hours > 0),
    CONSTRAINT chk_cr_grade_point
        CHECK (grade_point >= 0.0 AND grade_point <= 4.0),
    CONSTRAINT uq_cr_student_course_semester
        UNIQUE (student_id, course_code, semester)
);

CREATE INDEX idx_cr_student_semester course_results(student_id, semester);
CREATE INDEX idx_cr_student_year ON course_results(student_id, academic_year);

-- academic_reports
CREATE TABLE IF NOT EXISTS academic_reports (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_id VARCHAR(20) NOT NULL,
    report_type VARCHAR(10) NOT NULL DEFAULT 'SEMESTER',
    period VARCHAR(30) NOT NULL,
    cgpa DECIMAL(4,4)  NOT NULL,
    total_credit_hours SMALLINT NOT NULL,
    total_grade_points DECIMAL(6,2)  NOT NULL,
    generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_academic_reports PRIMARY KEY (id),
    CONSTRAINT fk_ar_student
        FOREIGN KEY (student_id) REFERENCES students(user_id) ON DELETE CASCADE,
    CONSTRAINT chk_ar_type
        CHECK (report_type IN ('SEMESTER','YEARLY'))
);

CREATE INDEX idx_ar_student_period ON academic_reports(student_id, period);

-- enrollment_log
CREATE TABLE IF NOT EXISTS enrollment_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_id VARCHAR(20) NOT NULL,
    decision VARCHAR(12) NOT NULL,
    cgpa_at_check DECIMAL(4,4),
    failed_courses TINYINT,
    new_level SMALLINT,
    new_semester TINYINT,
    decided_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,

    CONSTRAINT pk_enrollment_log PRIMARY KEY (id),
    CONSTRAINT fk_el_student
        FOREIGN KEY (student_id) REFERENCES students(user_id) ON DELETE CASCADE,
    CONSTRAINT chk_el_decision
        CHECK (decision IN ('ENROLLED','INELIGIBLE','PENDING'))
);

CREATE INDEX idx_el_student ON enrollment_log(student_id);

-- email_records
CREATE TABLE IF NOT EXISTS email_records (
    id VARCHAR(36) NOT NULL,
    recipient_email VARCHAR(160) NOT NULL,
    email_type VARCHAR(30) NOT NULL,
    subject VARCHAR(250) NOT NULL,
    delivery_status VARCHAR(10) NOT NULL DEFAULT 'PENDING',
    failure_reason TEXT,
    sent_atTIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_email_records PRIMARY KEY (id),
    CONSTRAINT chk_er_status
        CHECK (delivery_status IN ('SENT','FAILED','PENDING'))
);

CREATE INDEX idx_er_recipient ON email_records(recipient_email);
CREATE INDEX idx_er_type      ON email_records(email_type);
CREATE INDEX idx_er_sent_at   ON email_records(sent_at);

-- scheduled_notifications
CREATE TABLE IF NOT EXISTS scheduled_notifications (
    id VARCHAR(36) NOT NULL, 
    recipient_email VARCHAR(160) NOT NULL,
    email_type VARCHAR(30) NOT NULL,
    subject VARCHAR(250) NOT NULL,
    body TEXT NOT NULL,
    scheduled_for TIMESTAMP NOT NULL,
    status VARCHAR(12) NOT NULL DEFAULT 'PENDING',
    created_by VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_scheduled_notifications PRIMARY KEY (id),
    CONSTRAINT chk_sn_status
        CHECK (status IN ('PENDING','SENT','CANCELLED'))
);

CREATE INDEX idx_sn_status_scheduled ON scheduled_notifications(status, scheduled_for);

-- student_milestones
CREATE TABLE IF NOT EXISTS student_milestones (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_id VARCHAR(20) NOT NULL,
    milestone VARCHAR(250) NOT NULL,
    due_date DATE,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_student_milestones PRIMARY KEY (id),
    CONSTRAINT fk_sm_student
        FOREIGN KEY (student_id) REFERENCES students(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_sm_student ON student_milestones(student_id);