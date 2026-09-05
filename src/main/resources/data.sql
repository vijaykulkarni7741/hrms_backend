-- ==============================================================================
-- HRMS SEED DATA SCRIPT FOR UNIT-WISE ROLES, 50 EMPLOYEES & MASTER TABLES
-- ==============================================================================

-- 1. Insert Countries
INSERT INTO master_countries (id, country_code, country_name, phone_code, status, is_active, created_date, updated_date)
VALUES 
(1, 'IND', 'India', '+91', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- 2. Insert States
INSERT INTO master_states (id, country_id, state_code, state_name, status, is_active, created_date, updated_date)
VALUES 
(1, 1, 'MH', 'Maharashtra', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 'KA', 'Karnataka', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 1, 'DL', 'Delhi', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- 3. Insert Cities
INSERT INTO master_cities (id, state_id, city_name, status, is_active, created_date, updated_date)
VALUES 
(1, 1, 'Mumbai', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 'Pune', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 2, 'Bengaluru', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 3, 'New Delhi', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 1, 'Nagpur', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- 4. Insert Organization
INSERT INTO organizations (id, organization_code, organization_name, organization_short_name, organization_type, registration_number, gst_number, pan_number, tan_number, website, email, mobile_number, status, is_active, created_date, updated_date, version)
VALUES 
(1, 'ORG001', 'WebicSoft India Pvt Ltd', 'WebicSoft', 'PRIVATE_LIMITED', 'REG12345678', '27AAACW1234F1Z1', 'AAACW1234F', 'MUMB12345F', 'https://webicsoft.com', 'info@webicsoft.com', '9876543210', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- 5. Insert Units
INSERT INTO units (id, organization_id, unit_code, unit_name, unit_type, status, is_active, created_date, updated_date, version)
VALUES 
(1, 1, 'UNIT-HQ', 'Headquarters Unit', 'HQ', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(2, 1, 'UNIT-DEV', 'Development Center Unit', 'BRANCH', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- 6. Insert Roles (Unit-Wise Role Master)
INSERT INTO master_roles (id, unit_id, role_code, role_name, description, status, is_active, created_date, updated_date)
VALUES 
-- Unit 1 (Headquarters Unit) Roles
(1, 1, 'ROLE_HQ_ADMIN', 'Headquarters Administrator', 'Full unit administrative access', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 'ROLE_HQ_HR', 'HQ HR Manager', 'Human resource management access for HQ', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 1, 'ROLE_DOCTOR', 'Medical Officer / Doctor', 'Healthcare and medical staff access', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 1, 'ROLE_STUDENT', 'Trainee / Student', 'Intern and student self-service access', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Unit 2 (Development Center Unit) Roles
(5, 2, 'ROLE_DEV_MANAGER', 'Engineering Manager', 'Software development manager access', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 2, 'ROLE_DEV_LEAD', 'Lead Developer', 'Technical lead and architecture access', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 2, 'ROLE_SR_DEVELOPER', 'Senior Developer', 'Senior software engineer access', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 2, 'ROLE_DEVELOPER', 'Software Developer', 'Standard software developer access', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- 7. Insert Departments
INSERT INTO departments (id, organization_id, unit_id, department_code, department_name, department_type, status, is_active, created_date, updated_date, version)
VALUES 
(1, 1, 1, 'DEPT-IT', 'Information Technology', 'TECHNICAL', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(2, 1, 1, 'DEPT-HR', 'Human Resources', 'ADMIN', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(3, 1, 1, 'DEPT-FIN', 'Finance & Accounting', 'SUPPORT', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(4, 1, 2, 'DEPT-OPS', 'Operations', 'CORE', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(5, 1, 2, 'DEPT-SLS', 'Sales & Marketing', 'BUSINESS', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- 8. Insert Grades
INSERT INTO grades (id, organization_id, grade_code, grade_name, grade_level, status, is_active, created_date, updated_date, version)
VALUES 
(1, 1, 'GRD-EXEC', 'Executive Level', 1, 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(2, 1, 'GRD-SENIOR', 'Senior Professional', 2, 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(3, 1, 'GRD-MID', 'Mid Professional', 3, 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(4, 1, 'GRD-JR', 'Junior Professional', 4, 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- 9. Insert Designations
INSERT INTO designations (id, organization_id, grade_id, designation_code, designation_name, designation_level, status, is_active, created_date, updated_date, version)
VALUES 
(1, 1, 1, 'DSG-SE', 'Software Engineer', 'L2', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(2, 1, 2, 'DSG-SSE', 'Senior Software Engineer', 'L3', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(3, 1, 2, 'DSG-HRM', 'HR Manager', 'L3', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(4, 1, 3, 'DSG-ACC', 'Accountant', 'L2', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(5, 1, 1, 'DSG-TL', 'Team Lead', 'L4', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (id) DO NOTHING;

-- 10. Insert Banks
INSERT INTO banks (id, bank_code, bank_name, short_name, ifsc_prefix, swift_code, status, is_active, created_date, updated_date)
VALUES 
(1, 'HDFC', 'HDFC Bank', 'HDFC', 'HDFC000', 'HDFCINBB', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'ICICI', 'ICICI Bank', 'ICICI', 'ICIC000', 'ICICINBB', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'SBI', 'State Bank of India', 'SBI', 'SBIN000', 'SBININBB', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'AXIS', 'Axis Bank', 'AXIS', 'UTIB000', 'AXISINBB', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- 11. Insert Shifts
INSERT INTO shifts (id, organization_id, unit_id, shift_code, shift_name, shift_type, start_time, end_time, working_hours, minimum_working_hours, night_shift, status, is_active, created_date, updated_date)
VALUES 
(1, 1, 1, 'SHF-DAY', 'Day General Shift', 'REGULAR', '09:00:00', '18:00:00', 8.00, 4.00, false, 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 2, 'SHF-NIGHT', 'Night Shift', 'ROTATIONAL', '21:00:00', '06:00:00', 8.00, 4.00, true, 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- ==============================================================================
-- 12. INSERT 50 EMPLOYEES WITH UNIT-WISE ROLES (Password for all employees: password123)
-- BCrypt encoded hash for password123: $2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm
-- ==============================================================================

INSERT INTO employees (
    employee_code, organization_id, unit_id, department_id, designation_id, grade_id, role_id,
    first_name, middle_name, last_name, email, official_email, password, mobile_number,
    gender, date_of_birth, joining_date, employment_status, employee_type, work_location, is_active, created_date, updated_date, version
) VALUES 
('EMP001', 1, 1, 1, 1, 1, 1, 'Aarav', 'Kumar', 'Sharma', 'aarav.sharma@example.com', 'aarav.sharma@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000001', 'MALE', '1995-05-12', '2022-01-10', 'PERMANENT', 'FULL_TIME', 'Mumbai', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP002', 1, 1, 1, 2, 2, 2, 'Vihaan', 'Singh', 'Verma', 'vihaan.verma@example.com', 'vihaan.verma@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000002', 'MALE', '1992-08-24', '2021-03-15', 'PERMANENT', 'FULL_TIME', 'Mumbai', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP003', 1, 1, 2, 3, 2, 3, 'Ananya', 'Rao', 'Deshmukh', 'ananya.deshmukh@example.com', 'ananya.deshmukh@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000003', 'FEMALE', '1994-03-19', '2020-07-01', 'PERMANENT', 'FULL_TIME', 'Mumbai', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP004', 1, 1, 3, 4, 3, 4, 'Diya', 'Nair', 'Patel', 'diya.patel@example.com', 'diya.patel@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000004', 'FEMALE', '1996-11-05', '2023-02-15', 'PERMANENT', 'FULL_TIME', 'Mumbai', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP005', 1, 1, 1, 5, 1, 2, 'Aditya', 'Prasad', 'Joshi', 'aditya.joshi@example.com', 'aditya.joshi@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000005', 'MALE', '1990-01-30', '2019-11-10', 'PERMANENT', 'FULL_TIME', 'Mumbai', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

('EMP006', 1, 1, 1, 1, 4, 3, 'Ishaan', 'Dev', 'Gupta', 'ishaan.gupta@example.com', 'ishaan.gupta@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000006', 'MALE', '1998-07-14', '2024-01-05', 'PERMANENT', 'FULL_TIME', 'Pune', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP007', 1, 1, 4, 1, 3, 4, 'Kabir', 'Chand', 'Mehta', 'kabir.mehta@example.com', 'kabir.mehta@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000007', 'MALE', '1997-09-21', '2022-09-01', 'PERMANENT', 'FULL_TIME', 'Pune', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP008', 1, 1, 5, 2, 2, 1, 'Neha', 'Kumari', 'Reddy', 'neha.reddy@example.com', 'neha.reddy@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000008', 'FEMALE', '1993-12-18', '2021-06-12', 'PERMANENT', 'FULL_TIME', 'Bengaluru', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP009', 1, 2, 1, 1, 4, 8, 'Rohan', 'Vikas', 'Kulkarni', 'rohan.kulkarni@example.com', 'rohan.kulkarni@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000009', 'MALE', '1999-02-28', '2024-03-01', 'PERMANENT', 'FULL_TIME', 'Bengaluru', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP010', 1, 2, 2, 3, 3, 5, 'Sanya', 'Shree', 'Agarwal', 'sanya.agarwal@example.com', 'sanya.agarwal@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000010', 'FEMALE', '1995-10-09', '2022-11-20', 'PERMANENT', 'FULL_TIME', 'Bengaluru', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

('EMP011', 1, 2, 3, 4, 3, 7, 'Arjun', 'Suresh', 'Nair', 'arjun.nair@example.com', 'arjun.nair@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000011', 'MALE', '1994-04-16', '2020-04-15', 'PERMANENT', 'FULL_TIME', 'New Delhi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP012', 1, 2, 1, 2, 2, 6, 'Pooja', 'Rani', 'Bhat', 'pooja.bhat@example.com', 'pooja.bhat@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000012', 'FEMALE', '1993-06-25', '2021-01-18', 'PERMANENT', 'FULL_TIME', 'New Delhi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP013', 1, 2, 4, 1, 4, 8, 'Karan', 'Raj', 'Malhotra', 'karan.malhotra@example.com', 'karan.malhotra@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000013', 'MALE', '1997-01-11', '2023-05-10', 'PERMANENT', 'FULL_TIME', 'New Delhi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP014', 1, 2, 5, 5, 1, 5, 'Priya', 'Mohan', 'Saxena', 'priya.saxena@example.com', 'priya.saxena@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000014', 'FEMALE', '1991-08-04', '2018-09-01', 'PERMANENT', 'FULL_TIME', 'Mumbai', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP015', 1, 1, 1, 1, 3, 2, 'Dev', 'Anand', 'Kapoor', 'dev.kapoor@example.com', 'dev.kapoor@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000015', 'MALE', '1996-03-08', '2022-08-15', 'PERMANENT', 'FULL_TIME', 'Mumbai', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

('EMP016', 1, 1, 2, 3, 2, 3, 'Kavya', 'Deepak', 'Mishra', 'kavya.mishra@example.com', 'kavya.mishra@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000016', 'FEMALE', '1994-09-17', '2021-10-05', 'PERMANENT', 'FULL_TIME', 'Pune', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP017', 1, 1, 3, 4, 4, 4, 'Yash', 'Gopal', 'Soni', 'yash.soni@example.com', 'yash.soni@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000017', 'MALE', '1998-11-22', '2023-11-01', 'PERMANENT', 'FULL_TIME', 'Pune', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP018', 1, 1, 4, 1, 3, 1, 'Sneha', 'Lata', 'Chaudhary', 'sneha.chaudhary@example.com', 'sneha.chaudhary@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000018', 'FEMALE', '1995-02-14', '2022-03-20', 'PERMANENT', 'FULL_TIME', 'Bengaluru', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP019', 1, 1, 5, 2, 2, 2, 'Varun', 'Nath', 'Tiwari', 'varun.tiwari@example.com', 'varun.tiwari@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000019', 'MALE', '1992-05-06', '2020-08-10', 'PERMANENT', 'FULL_TIME', 'Bengaluru', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP020', 1, 1, 1, 5, 1, 3, 'Meera', 'Ben', 'Shah', 'meera.shah@example.com', 'meera.shah@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000020', 'FEMALE', '1989-12-01', '2017-06-01', 'PERMANENT', 'FULL_TIME', 'Mumbai', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

('EMP021', 1, 2, 1, 1, 4, 8, 'Manish', 'Kumar', 'Pande', 'manish.pande@example.com', 'manish.pande@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000021', 'MALE', '1997-04-03', '2023-01-16', 'PERMANENT', 'FULL_TIME', 'Mumbai', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP022', 1, 2, 2, 3, 3, 5, 'Shweta', 'Kumari', 'Singh', 'shweta.singh@example.com', 'shweta.singh@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000022', 'FEMALE', '1996-06-20', '2022-06-01', 'PERMANENT', 'FULL_TIME', 'Pune', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP023', 1, 2, 3, 4, 3, 7, 'Vikas', 'Ram', 'Yadav', 'vikas.yadav@example.com', 'vikas.yadav@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000023', 'MALE', '1993-01-19', '2021-04-05', 'PERMANENT', 'FULL_TIME', 'Pune', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP024', 1, 2, 4, 1, 4, 8, 'Ankita', 'Roy', 'Dutta', 'ankita.dutta@example.com', 'ankita.dutta@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000024', 'FEMALE', '1998-08-31', '2024-02-12', 'PERMANENT', 'FULL_TIME', 'Bengaluru', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP025', 1, 2, 5, 2, 2, 6, 'Abhishek', 'Kumar', 'Tripathi', 'abhishek.tripathi@example.com', 'abhishek.tripathi@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000025', 'MALE', '1991-10-15', '2019-12-01', 'PERMANENT', 'FULL_TIME', 'Bengaluru', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

('EMP026', 1, 1, 1, 2, 2, 2, 'Riya', 'Sen', 'Chowdhury', 'riya.chowdhury@example.com', 'riya.chowdhury@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000026', 'FEMALE', '1994-07-27', '2021-09-15', 'PERMANENT', 'FULL_TIME', 'New Delhi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP027', 1, 1, 2, 3, 3, 3, 'Siddharth', 'Nath', 'Bose', 'siddharth.bose@example.com', 'siddharth.bose@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000027', 'MALE', '1995-12-04', '2022-04-10', 'PERMANENT', 'FULL_TIME', 'New Delhi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP028', 1, 1, 3, 4, 4, 4, 'Tarun', 'Deep', 'Goyal', 'tarun.goyal@example.com', 'tarun.goyal@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000028', 'MALE', '1999-03-22', '2024-04-01', 'PERMANENT', 'FULL_TIME', 'Mumbai', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP029', 1, 1, 4, 1, 3, 1, 'Tanvi', 'Shree', 'Bansal', 'tanvi.bansal@example.com', 'tanvi.bansal@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000029', 'FEMALE', '1996-05-18', '2023-03-01', 'PERMANENT', 'FULL_TIME', 'Mumbai', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP030', 1, 1, 5, 5, 1, 2, 'Rahul', 'Dev', 'Khanna', 'rahul.khanna@example.com', 'rahul.khanna@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000030', 'MALE', '1990-02-10', '2018-05-20', 'PERMANENT', 'FULL_TIME', 'Mumbai', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

('EMP031', 1, 2, 1, 1, 4, 8, 'Nikhil', 'Raj', 'Shetty', 'nikhil.shetty@example.com', 'nikhil.shetty@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000031', 'MALE', '1997-09-09', '2023-07-15', 'PERMANENT', 'FULL_TIME', 'Pune', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP032', 1, 2, 2, 3, 2, 5, 'Deepika', 'Kumari', 'Rao', 'deepika.rao@example.com', 'deepika.rao@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000032', 'FEMALE', '1993-11-30', '2020-11-01', 'PERMANENT', 'FULL_TIME', 'Pune', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP033', 1, 2, 3, 4, 3, 7, 'Kunal', 'Singh', 'Solanki', 'kunal.solanki@example.com', 'kunal.solanki@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000033', 'MALE', '1995-01-25', '2022-02-14', 'PERMANENT', 'FULL_TIME', 'Bengaluru', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP034', 1, 2, 4, 1, 3, 6, 'Divya', 'Prakash', 'Rana', 'divya.rana@example.com', 'divya.rana@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000034', 'FEMALE', '1996-08-12', '2022-10-01', 'PERMANENT', 'FULL_TIME', 'Bengaluru', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP035', 1, 2, 5, 2, 2, 8, 'Sameer', 'Ahmad', 'Khan', 'sameer.khan@example.com', 'sameer.khan@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000035', 'MALE', '1992-04-07', '2019-08-01', 'PERMANENT', 'FULL_TIME', 'New Delhi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

('EMP036', 1, 1, 1, 2, 2, 3, 'Aakash', 'Mohan', 'Bhardwaj', 'aakash.bhardwaj@example.com', 'aakash.bhardwaj@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000036', 'MALE', '1994-10-24', '2021-05-10', 'PERMANENT', 'FULL_TIME', 'New Delhi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP037', 1, 1, 2, 3, 3, 2, 'Meghna', 'Sen', 'Gupta', 'meghna.gupta@example.com', 'meghna.gupta@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000037', 'FEMALE', '1996-12-11', '2023-06-01', 'PERMANENT', 'FULL_TIME', 'Mumbai', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP038', 1, 1, 3, 4, 4, 4, 'Chirag', 'Kumar', 'Jain', 'chirag.jain@example.com', 'chirag.jain@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000038', 'MALE', '1998-06-05', '2024-01-15', 'PERMANENT', 'FULL_TIME', 'Mumbai', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP039', 1, 1, 4, 1, 3, 1, 'Payal', 'Kumari', 'Surve', 'payal.surve@example.com', 'payal.surve@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000039', 'FEMALE', '1995-03-29', '2022-07-20', 'PERMANENT', 'FULL_TIME', 'Pune', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP040', 1, 1, 5, 5, 1, 2, 'Gaurav', 'Nath', 'Kaushik', 'gaurav.kaushik@example.com', 'gaurav.kaushik@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000040', 'MALE', '1988-09-14', '2016-03-10', 'PERMANENT', 'FULL_TIME', 'Pune', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

('EMP041', 1, 2, 1, 1, 4, 8, 'Nisha', 'Rani', 'Varma', 'nisha.varma@example.com', 'nisha.varma@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000041', 'FEMALE', '1997-07-02', '2023-08-01', 'PERMANENT', 'FULL_TIME', 'Bengaluru', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP042', 1, 2, 2, 3, 2, 5, 'Harsh', 'Vardhan', 'Rathore', 'harsh.rathore@example.com', 'harsh.rathore@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000042', 'MALE', '1992-11-19', '2020-02-15', 'PERMANENT', 'FULL_TIME', 'Bengaluru', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP043', 1, 2, 3, 4, 3, 7, 'Pooja', 'Shree', 'Naik', 'pooja.naik@example.com', 'pooja.naik@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000043', 'FEMALE', '1995-04-10', '2021-12-01', 'PERMANENT', 'FULL_TIME', 'New Delhi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP044', 1, 2, 4, 1, 4, 8, 'Abhinav', 'Kumar', 'Garg', 'abhinav.garg@example.com', 'abhinav.garg@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000044', 'MALE', '1998-02-08', '2024-02-01', 'PERMANENT', 'FULL_TIME', 'New Delhi', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP045', 1, 2, 5, 2, 2, 6, 'Sonam', 'Ben', 'Patel', 'sonam.patel@example.com', 'sonam.patel@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000045', 'FEMALE', '1993-05-15', '2021-07-01', 'PERMANENT', 'FULL_TIME', 'Mumbai', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),

('EMP046', 1, 1, 1, 2, 2, 3, 'Alok', 'Nath', 'Tiwari', 'alok.tiwari@example.com', 'alok.tiwari@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000046', 'MALE', '1994-01-22', '2020-09-10', 'PERMANENT', 'FULL_TIME', 'Mumbai', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP047', 1, 1, 2, 3, 3, 2, 'Smriti', 'Kaur', 'Ahluwalia', 'smriti.ahluwalia@example.com', 'smriti.ahluwalia@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000047', 'FEMALE', '1996-09-05', '2023-04-15', 'PERMANENT', 'FULL_TIME', 'Pune', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP048', 1, 1, 3, 4, 4, 4, 'Prateek', 'Kumar', 'Dubey', 'prateek.dubey@example.com', 'prateek.dubey@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000048', 'MALE', '1999-06-18', '2024-05-02', 'PERMANENT', 'FULL_TIME', 'Pune', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP049', 1, 1, 4, 1, 3, 1, 'Tanya', 'Kumari', 'Singhal', 'tanya.singhal@example.com', 'tanya.singhal@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000049', 'FEMALE', '1995-08-12', '2022-05-20', 'PERMANENT', 'FULL_TIME', 'Bengaluru', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('EMP050', 1, 1, 5, 5, 1, 2, 'Vivek', 'Ram', 'Chaudhary', 'vivek.chaudhary@example.com', 'vivek.chaudhary@webicsoft.com', '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm', '9811000050', 'MALE', '1990-12-25', '2017-10-01', 'PERMANENT', 'FULL_TIME', 'Bengaluru', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (employee_code) DO UPDATE SET password = EXCLUDED.password, is_active = EXCLUDED.is_active;

UPDATE employees SET password = '$2a$10$BZViFWoCB7kxTuZvZH9pWOR5Gqb7bf0Lon.qxknpzgOjAGoW39WAm';

