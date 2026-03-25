-- users
INSERT INTO users (user_id, first_name, last_name, email, role, account_status)
VALUES
  ('S001','Alice', 'Tan', 'alice.tan@university.edu', 'STUDENT', 'ACTIVE'),
  ('S002','Bob', 'Lim', 'bob.lim@university.edu', 'STUDENT', 'ACTIVE'),
  ('S003','Carol', 'Ng', 'carol.ng@university.edu', 'STUDENT', 'ACTIVE'),
  ('S004','David', 'Oh', 'david.oh@university.edu', 'STUDENT', 'ACTIVE'),
  ('S005','Eve', 'Lau', 'eve.lau@university.edu', 'STUDENT', 'ACTIVE'),
  ('I001','Dr Sam', 'Wong', 'sam.wong@university.edu', 'INSTRUCTOR', 'ACTIVE'),
  ('ADM1','Diana', 'Lee', 'diana.lee@university.edu', 'ADMINISTRATOR', 'ACTIVE');

-- students
INSERT INTO students (user_id, program, current_level, current_semester, cgpa, recovery_status, recovery_plan_detail, advisor_email)
VALUES
  ('S001','Bachelor of Computer Science', 300,'SEM1 2024/2025', 3.7200,'NOT_STARTED', NULL,
   'sam.wong@university.edu'),
  ('S002','Bachelor of Information Technology',200,'SEM1 2024/2025',2.1500,'IN_PROGRESS',
   'Phase 1 (Wks 1-4): Complete missed assignments for CS201 and MATH201.\nPhase 2 (Wks 5-8): Supplementary assessment for CS202.',
   'sam.wong@university.edu'),
  ('S003','Bachelor of Computer Science', 100,'SEM1 2024/2025', 1.8500,'IN_PROGRESS',
   'Phase 1: Attend all remedial classes for CS101 and MATH101.\nPhase 2: Re-sit failed examinations.',
   'sam.wong@university.edu'),
  ('S004','Bachelor of Data Science', 300,'SEM2 2023/2024', 3.4000,'NOT_STARTED',  NULL, NULL),
  ('S005','Bachelor of Information Technology',200,'SEM1 2024/2025',2.9000,'NOT_STARTED',  NULL, NULL);

-- course_results
-- Alice – SEM1 2024/2025
INSERT INTO course_results (student_id,course_code,course_title,credit_hours,grade,grade_point,semester,academic_year) VALUES
  ('S001','CS301','Data Structures & Algorithms',3,'A', 4.0,'SEM1 2024/2025',2024),
  ('S001','CS302','Operating Systems', 3,'A-',3.7,'SEM1 2024/2025',2024),
  ('S001','CS303','Database Systems', 3,'B+',3.3,'SEM1 2024/2025',2024),
  ('S001','MATH301','Discrete Mathematics', 3,'A', 4.0,'SEM1 2024/2025',2024),
  ('S001','ENG301','Technical Communication', 2,'A-',3.7,'SEM1 2024/2025',2024);

-- Bob – SEM1 2024/2025
INSERT INTO course_results (student_id,course_code,course_title,credit_hours,grade,grade_point,semester,academic_year) VALUES
  ('S002','CS201','Data Structures', 3,'C+',2.3,'SEM1 2024/2025',2024),
  ('S002','CS202','Object-Oriented Programming', 3,'C', 2.0,'SEM1 2024/2025',2024),
  ('S002','MATH201','Calculus II', 3,'D', 1.0,'SEM1 2024/2025',2024),
  ('S002','ENG201','Professional Communication', 2,'B-',2.7,'SEM1 2024/2025',2024),
  ('S002','IT201','Web Technologies', 3,'C+',2.3,'SEM1 2024/2025',2024);

-- Carol – SEM1 2024/2025
INSERT INTO course_results (student_id,course_code,course_title,credit_hours,grade,grade_point,semester,academic_year) VALUES
  ('S003','CS101','Introduction to Programming', 3,'F', 0.0,'SEM1 2024/2025',2024),
  ('S003','MATH101','Pre-Calculus', 3,'F', 0.0,'SEM1 2024/2025',2024),
  ('S003','ENG101','English for Computing', 2,'D', 1.0,'SEM1 2024/2025',2024),
  ('S003','IT101','IT Fundamentals', 3,'C', 2.0,'SEM1 2024/2025',2024),
  ('S003','CS102','Computer Organisation', 3,'F', 0.0,'SEM1 2024/2025',2024);

-- David – SEM2 2023/2024
INSERT INTO course_results (student_id,course_code,course_title,credit_hours,grade,grade_point,semester,academic_year) VALUES
  ('S004','DS301','Machine Learning', 3,'A-',3.7,'SEM2 2023/2024',2024),
  ('S004','DS302','Data Visualisation', 3,'B+',3.3,'SEM2 2023/2024',2024),
  ('S004','STAT301','Statistical Methods', 3,'A', 4.0,'SEM2 2023/2024',2024),
  ('S004','CS303','Database Systems', 3,'B+',3.3,'SEM2 2023/2024',2024);

-- Eve – SEM1 2024/2025
INSERT INTO course_results (student_id,course_code,course_title,credit_hours,grade,grade_point,semester,academic_year) VALUES
  ('S005','IT201','Web Technologies', 3,'B', 3.0,'SEM1 2024/2025',2024),
  ('S005','IT202','Cloud Computing', 3,'B+',3.3,'SEM1 2024/2025',2024),
  ('S005','MATH201','Calculus II', 3,'C+',2.3,'SEM1 2024/2025',2024),
  ('S005','ENG201','Professional Communication', 2,'A-',3.7,'SEM1 2024/2025',2024);

-- student_milestones 
INSERT INTO student_milestones (student_id, milestone, due_date) VALUES
  ('S002','Submit CS201 Assignment 3','2024-10-30'),
  ('S002','Sit MATH201 supplementary exam','2024-11-05'),
  ('S002','Complete CS202 project report','2024-11-15'),
  ('S003','Attend all CS101 remedial classes','2024-11-01'),
  ('S003','Re-sit MATH101 examination','2024-11-20');

-- enrollment_log
INSERT INTO enrollment_log (student_id, decision, cgpa_at_check, failed_courses, new_level, new_semester, notes) VALUES
  ('S001','ENROLLED',  3.7200, 0, 300, 2, 'Auto-enrolled after SEM1 eligibility check.'),
  ('S002','INELIGIBLE',2.1500, 1, NULL,NULL,'CGPA meets threshold but recovery plan not completed.'),
  ('S003','INELIGIBLE',1.8500, 3, NULL,NULL,'CGPA below 2.0 and 3 failed courses.'),
  ('S004','ENROLLED',  3.4000, 0, 400, 1, 'Auto-enrolled after SEM2 eligibility check.'),
  ('S005','ENROLLED',  2.9000, 0, 300, 1, 'Auto-enrolled after SEM1 eligibility check.');

-- email_records (sample sent emails)
INSERT INTO email_records (id, recipient_email, email_type, subject, delivery_status) VALUES
  ('a1b2c3d4-0001-0000-0000-000000000001','alice.tan@university.edu','ACCOUNT_CREATED','[CRS] Welcome to the Course Recovery System','SENT'),
  ('a1b2c3d4-0002-0000-0000-000000000002','bob.lim@university.edu',  'ACCOUNT_CREATED','[CRS] Welcome to the Course Recovery System','SENT'),
  ('a1b2c3d4-0003-0000-0000-000000000003','carol.ng@university.edu', 'ACCOUNT_CREATED','[CRS] Welcome to the Course Recovery System','SENT'),
  ('a1b2c3d4-0004-0000-0000-000000000004','bob.lim@university.edu',  'RECOVERY_PLAN',  '[CRS] Your Course Recovery Plan Is Ready','SENT'),
  ('a1b2c3d4-0005-0000-0000-000000000005','carol.ng@university.edu', 'RECOVERY_PLAN',  '[CRS] Your Course Recovery Plan Is Ready','SENT'),
  ('a1b2c3d4-0006-0000-0000-000000000006','alice.tan@university.edu','PERFORMANCE_REPORT','[CRS] Your Academic Performance Report','SENT');