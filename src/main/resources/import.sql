-- This file allow to write SQL commands that will be emitted in test and dev.
-- The commands are commented as their support depends of the database
-- insert into myentity (id, field) values(1, 'field-1');
-- insert into myentity (id, field) values(2, 'field-2');
-- insert into myentity (id, field) values(3, 'field-3');
-- alter sequence myentity_seq restart with 4;

-- Insert sample users
INSERT INTO users (id, username, email, firstName, lastName, active, created_at, updated_at) VALUES
(1, 'john.doe', 'john.doe@example.com', 'John', 'Doe', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'jane.smith', 'jane.smith@example.com', 'Jane', 'Smith', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'admin.user', 'admin@example.com', 'Admin', 'User', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert user roles
INSERT INTO user_roles (user_id, role) VALUES
(1, 'USER'),
(2, 'USER'),
(2, 'MANAGER'),
(3, 'USER'),
(3, 'ADMIN');

-- Set sequence to continue from the inserted data
ALTER SEQUENCE users_seq RESTART WITH 4;
