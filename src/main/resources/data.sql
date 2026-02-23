-- DigiNest AI Receptionist - Development Seed Data
-- Run this SQL file to populate the database with sample data

-- Clean existing data (optional - uncomment if needed)
-- DELETE FROM bookings WHERE hotel_id = 1;
-- DELETE FROM room_types WHERE hotel_id = 1;
-- DELETE FROM users WHERE hotel_id = 1;
-- DELETE FROM usage_records WHERE hotel_id = 1;
-- DELETE FROM hotels WHERE id = 1;

-- 1. Insert Hotel
INSERT INTO hotels (id, name, address, phone, email, is_active, monthly_token_limit, created_at, updated_at)
VALUES (1, 'Grand Plaza Hotel', '123 Main Street, New York, NY 10001', '+1-555-0100', 'info@grandplaza.com', true, 100000, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- 2. Insert Users (Passwords are BCrypt encoded: 'SuperAdmin123!' and 'HotelAdmin123!')
INSERT INTO users (id, hotel_id, email, password, first_name, last_name, role, is_active, created_at, updated_at)
VALUES 
    (1, 1, 'superadmin@diginest.com', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqQwvYhZJhXHJj7tGKxO8W4F0JGKi', 'System', 'Administrator', 'SUPER_ADMIN', true, NOW(), NOW()),
    (2, 1, 'admin@grandplaza.com', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqQwvYhZJhXHJj7tGKxO8W4F0JGKi', 'John', 'Manager', 'HOTEL_ADMIN', true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Reset sequence for users table
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));

-- 3. Insert Room Types
INSERT INTO room_types (id, hotel_id, name, description, base_price, max_occupancy, total_rooms, is_active, created_at, updated_at)
VALUES 
    (1, 1, 'Standard', 'Comfortable room with city view', 150.00, 2, 50, true, NOW(), NOW()),
    (2, 1, 'Deluxe', 'Spacious room with king bed and premium amenities', 250.00, 3, 30, true, NOW(), NOW()),
    (3, 1, 'Suite', 'Luxury suite with separate living area', 450.00, 4, 10, true, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Reset sequence for room_types table
SELECT setval('room_types_id_seq', (SELECT MAX(id) FROM room_types));

-- 4. Insert Bookings
INSERT INTO bookings (id, hotel_id, guest_name, guest_email, guest_phone, check_in_date, check_out_date, room_number, total_amount, status, confirmed_at, created_at, updated_at)
VALUES 
    (1, 1, 'Alice Johnson', 'alice@email.com', '+1-555-0201', CURRENT_DATE + INTERVAL '1 day', CURRENT_DATE + INTERVAL '3 days', '101', 300.00, 'CONFIRMED', NOW(), NOW(), NOW()),
    (2, 1, 'Bob Smith', 'bob@email.com', '+1-555-0202', CURRENT_DATE + INTERVAL '5 days', CURRENT_DATE + INTERVAL '7 days', '205', 500.00, 'CONFIRMED', NOW(), NOW(), NOW()),
    (3, 1, 'Carol White', 'carol@email.com', '+1-555-0203', CURRENT_DATE + INTERVAL '10 days', CURRENT_DATE + INTERVAL '14 days', '301', 1800.00, 'CONFIRMED', NOW(), NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Reset sequence for bookings table
SELECT setval('bookings_id_seq', (SELECT MAX(id) FROM bookings));

-- Verify data insertion
SELECT 'Hotels: ' || COUNT(*) FROM hotels WHERE id = 1;
SELECT 'Users: ' || COUNT(*) FROM users WHERE hotel_id = 1;
SELECT 'Room Types: ' || COUNT(*) FROM room_types WHERE hotel_id = 1;
SELECT 'Bookings: ' || COUNT(*) FROM bookings WHERE hotel_id = 1;
