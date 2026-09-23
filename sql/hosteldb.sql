-- =========================================================
-- Hostel Food Ordering & Billing System - Database Script
-- =========================================================

DROP DATABASE IF EXISTS hostel_db;
CREATE DATABASE hostel_db;
USE hostel_db;

-- ---------------------------------------------------------
-- Table: customers
-- Stores hostel students/customers who order food
-- ---------------------------------------------------------
CREATE TABLE customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    room_no     VARCHAR(10)  NOT NULL,
    contact     VARCHAR(15)  NOT NULL UNIQUE
);

-- ---------------------------------------------------------
-- Table: food_items
-- Menu of food items available in the hostel mess/canteen
-- ---------------------------------------------------------
CREATE TABLE food_items (
    food_id       INT AUTO_INCREMENT PRIMARY KEY,
    food_name     VARCHAR(100) NOT NULL,
    category      VARCHAR(50)  NOT NULL,
    price         DECIMAL(8,2) NOT NULL,
    available_qty INT DEFAULT 100,
    status        ENUM('AVAILABLE','UNAVAILABLE') DEFAULT 'AVAILABLE'
);

-- ---------------------------------------------------------
-- Table: orders
-- One row per order placed by a customer
-- ---------------------------------------------------------
CREATE TABLE orders (
    order_id     INT AUTO_INCREMENT PRIMARY KEY,
    customer_id  INT NOT NULL,
    order_date   DATETIME DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2) DEFAULT 0,
    order_status ENUM('PENDING','COMPLETED','CANCELLED') DEFAULT 'PENDING',
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

-- ---------------------------------------------------------
-- Table: order_items
-- Line items (food + quantity) belonging to an order
-- ---------------------------------------------------------
CREATE TABLE order_items (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id      INT NOT NULL,
    food_id       INT NOT NULL,
    quantity      INT NOT NULL,
    price         DECIMAL(8,2) NOT NULL,
    subtotal      DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (food_id)  REFERENCES food_items(food_id)
);

-- ---------------------------------------------------------
-- Table: bills
-- Payment/bill record generated against an order
-- ---------------------------------------------------------
CREATE TABLE bills (
    bill_id        INT AUTO_INCREMENT PRIMARY KEY,
    order_id       INT NOT NULL UNIQUE,
    customer_id    INT NOT NULL,
    total_amount   DECIMAL(10,2) NOT NULL,
    payment_mode   ENUM('CASH','CARD','UPI') DEFAULT NULL,
    payment_status ENUM('PAID','UNPAID') DEFAULT 'UNPAID',
    bill_date      DATETIME DEFAULT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

-- ---------------------------------------------------------
-- Sample food menu data
-- ---------------------------------------------------------
INSERT INTO food_items (food_name, category, price, available_qty) VALUES
('Veg Thali',        'Meals',     60.00, 50),
('Chicken Biryani',   'Meals',     90.00, 40),
('Paneer Butter Masala','Curry',   70.00, 30),
('Chapati (2 pcs)',   'Bread',     15.00, 100),
('Masala Dosa',       'Snacks',    40.00, 40),
('Idli Sambhar',      'Snacks',    30.00, 40),
('Tea',                'Beverages', 10.00, 200),
('Coffee',             'Beverages', 15.00, 200),
('Cold Drink (500ml)', 'Beverages', 35.00, 60),
('Gulab Jamun (2 pcs)','Dessert',   25.00, 50);

-- ---------------------------------------------------------
-- Sample customer (optional, for quick testing)
-- ---------------------------------------------------------
INSERT INTO customers (name, room_no, contact) VALUES
('Test Student', 'A-101', '9999999999');
