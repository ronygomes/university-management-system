CREATE SEQUENCE IF NOT EXISTS departments_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS students_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS student_educations_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS teacher_designations_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS teachers_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS courses_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS course_enrollments_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE IF NOT EXISTS course_schedules_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS departments (
    id BIGINT PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL UNIQUE,
    uuid CHAR(36) NOT NULL UNIQUE,
    version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS students (
    id BIGINT PRIMARY KEY,
    full_name VARCHAR(200) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    contact_number VARCHAR(14),
    address VARCHAR(1000),
    department_id BIGINT NOT NULL,
    registration_date TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    registration_number VARCHAR(20) NOT NULL UNIQUE,
    uuid CHAR(36) NOT NULL UNIQUE,
    version INTEGER DEFAULT 0,
    CONSTRAINT fk_students_department_id FOREIGN KEY (department_id) REFERENCES departments(id)
);

CREATE TABLE IF NOT EXISTS student_educations (
    id BIGINT PRIMARY KEY,
    idx INT NOT NULL,
    exam_type VARCHAR(10) NOT NULL CHECK (exam_type IN ('SSC', 'HSC', 'A_LEVEL', 'O_LEVEL')),
    grade VARCHAR(10) NOT NULL CHECK (grade IN ('A_PLUS', 'A', 'A_MINUS', 'B_PLUS', 'B', 'B_MINUS', 'C_PLUS', 'C', 'C_MINUS', 'F')),
    cgpa REAL NOT NULL,
    certificate_file_name VARCHAR(100) NOT NULL,
    certificate_path VARCHAR(150) NOT NULL UNIQUE,
    student_id BIGINT NOT NULL,
    CONSTRAINT fk_student_educations_student_id FOREIGN KEY (student_id) REFERENCES students(id)
);

CREATE TABLE IF NOT EXISTS teacher_designations (
    id BIGINT PRIMARY KEY,
    title VARCHAR(100) NOT NULL UNIQUE,
    version INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS teachers (
    id BIGINT PRIMARY KEY,
    full_name VARCHAR(200) NOT NULL,
    address VARCHAR(1000),
    email VARCHAR(100) NOT NULL UNIQUE,
    contact_number VARCHAR(14),
    assigned_credit real NOT NULL,
    designation_id BIGINT NOT NULL,
    department_id BIGINT NOT NULl,
    uuid CHAR(36) NOT NULL UNIQUE,
    version INTEGER DEFAULT 0,
    CONSTRAINT fk_teachers_designation_id FOREIGN KEY (designation_id) REFERENCES teacher_designations(id),
    CONSTRAINT fk_teachers_department_id FOREIGN KEY (department_id) REFERENCES departments(id)
);

CREATE TABLE IF NOT EXISTS courses (
    id BIGINT PRIMARY KEY,
    title VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL UNIQUE,
    credit REAL NOT NULL,
    description VARCHAR(2000),
    department_id BIGINT NOT NULL,
    semester VARCHAR(30) NOT NULL CHECK (semester IN ('FIRST_YEAR_FIRST', 'FIRST_YEAR_SECOND', 'SECOND_YEAR_FIRST', 'SECOND_YEAR_SECOND', 'THIRD_YEAR_FIRST', 'THIRD_YEAR_SECOND', 'FOURTH_YEAR_FIRST', 'FOURTH_YEAR_SECOND', 'FIFTH_YEAR_FIRST', 'FIFTH_YEAR_SECOND')),
    instructor_id BIGINT,
    uuid CHAR(36) NOT NULL UNIQUE,
    version INTEGER DEFAULT 0,
    CONSTRAINT fk_courses_department_id FOREIGN KEY (department_id) REFERENCES departments(id),
    CONSTRAINT fk_courses_instructor_id FOREIGN KEY (instructor_id) REFERENCES teachers(id)
);

CREATE TABLE IF NOT EXISTS course_enrollments (
    id BIGINT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    enrollment_date TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    status VARCHAR(10) NOT NULL CHECK (status IN ('PASSED', 'FAILED', 'ON_GOING')),
    grade VARCHAR(10) CHECK (grade IN ('A_PLUS', 'A', 'A_MINUS', 'B_PLUS', 'B', 'B_MINUS', 'C_PLUS', 'C', 'C_MINUS', 'F')),
    uuid CHAR(36) NOT NULL UNIQUE,
    version INTEGER DEFAULT 0,
    CONSTRAINT fk_course_enrollments_student_id FOREIGN KEY (student_id) REFERENCES students(id),
    CONSTRAINT fk_course_enrollments_course_id FOREIGN KEY (course_id) REFERENCES courses(id)
);

CREATE TABLE IF NOT EXISTS course_schedules (
    id BIGINT PRIMARY KEY,
    department_id BIGINT NOT NULL,
    semester VARCHAR(30) NOT NULL CHECK (semester IN ('FIRST_YEAR_FIRST', 'FIRST_YEAR_SECOND', 'SECOND_YEAR_FIRST', 'SECOND_YEAR_SECOND', 'THIRD_YEAR_FIRST', 'THIRD_YEAR_SECOND', 'FOURTH_YEAR_FIRST', 'FOURTH_YEAR_SECOND', 'FIFTH_YEAR_FIRST', 'FIFTH_YEAR_SECOND')),
    course_id BIGINT NOT NULL,
    building VARCHAR(30) NOT NULL CHECK (building IN ('BUILDING_1', 'BUILDING_2')),
    room_number VARCHAR(100) NOT NULL,
    day VARCHAR(20) NOT NULL CHECK (day IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY')),
    end_time TIMESTAMP(6) WITH TIME ZONE,
    start_time TIMESTAMP(6) WITH TIME ZONE,
    uuid CHAR(36) NOT NULL UNIQUE,
    version INTEGER DEFAULT 0,
    CONSTRAINT fk_course_schedules_department_id FOREIGN KEY (department_id) REFERENCES departments(id),
    CONSTRAINT fk_course_schedules_course_id FOREIGN KEY (course_id) REFERENCES courses(id)
);

CREATE TABLE IF NOT EXISTS registration_number_bounds (
    year smallint NOT NULL,
    department_id BIGINT NOT NULL,
    last_used_number smallint NOT NULL,
    PRIMARY KEY (year, department_id, last_used_number),
    CONSTRAINT fk_registration_number_bounds_department_id FOREIGN KEY (department_id) REFERENCES departments(id),
    CHECK(last_used_number BETWEEN 1 AND 9999)
);

INSERT INTO departments VALUES(NEXTVAL('departments_seq'), 'CE', 'Civil Engineering', gen_random_uuid(), 0);
INSERT INTO departments VALUES(NEXTVAL('departments_seq'), 'CSE', 'Computer Science & Engineering', gen_random_uuid(), 0);
INSERT INTO departments VALUES(NEXTVAL('departments_seq'), 'EEE', 'Electrical & Electronic Engineering', gen_random_uuid(), 0);
INSERT INTO departments VALUES(NEXTVAL('departments_seq'), 'MPE', 'Mechanical and Production Engineering', gen_random_uuid(), 0);
INSERT INTO departments VALUES(NEXTVAL('departments_seq'), 'TE', 'Textile Engineering', gen_random_uuid(), 0);
INSERT INTO departments VALUES(NEXTVAL('departments_seq'), 'A&S', 'Arts and Sciences', gen_random_uuid(), 0);
INSERT INTO departments VALUES(NEXTVAL('departments_seq'), 'ARCH', 'Architecture', gen_random_uuid(), 0);
INSERT INTO departments VALUES(NEXTVAL('departments_seq'), 'SoB', 'School of Business', gen_random_uuid(), 0);

INSERT INTO teacher_designations VALUES(NEXTVAL('teacher_designations_seq'), 'Lecturer', 0);
INSERT INTO teacher_designations VALUES(NEXTVAL('teacher_designations_seq'), 'Assistant Professor', 0);
INSERT INTO teacher_designations VALUES(NEXTVAL('teacher_designations_seq'), 'Associate Professor', 0);
INSERT INTO teacher_designations VALUES(NEXTVAL('teacher_designations_seq'), 'Professor', 0);
