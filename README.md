# Hostel Food Ordering & Billing System (Java + JDBC + MySQL)

A console-based mini project for a hostel mess/canteen where customers
(students) can:
1. View the food menu
2. Check the price of any item
3. Order food (add multiple items to a cart)
4. Generate a bill and pay it (CASH / CARD / UPI)

All data is stored in a MySQL database.

## Project Structure
```
HostelFoodOrderingSystem/
├── sql/
│   └── hostel_db.sql          # database schema + sample food menu
├── src/
│   ├── DBConnection.java      # JDBC connection helper
│   ├── Customer.java          # customer model
│   ├── FoodItem.java          # food item model
│   ├── CustomerService.java   # register / lookup customer
│   ├── FoodService.java       # view menu, check price
│   ├── OrderService.java      # place order, order summary
│   ├── BillService.java       # generate bill, pay bill
│   └── HostelFoodApp.java     # main() console menu
├── lib/                       # put mysql-connector-j-x.x.x.jar here
└── README.md
```

## Database Tables
- `customers`   – registered hostel customers
- `food_items`  – food menu with price, category, stock, status
- `orders`      – one row per order, with total amount & status
- `order_items` – line items (food + qty + subtotal) per order
- `bills`       – bill generated per order with payment status

## Setup Instructions

### 1. Create the database
Open MySQL and run:
```bash
mysql -u root -p < sql/hostel_db.sql
```
This creates the `hostel_db` database, all tables, and inserts a sample
food menu (Veg Thali, Chicken Biryani, Tea, Coffee, etc.).

### 2. Download the MySQL JDBC driver
Download `mysql-connector-j-8.x.x.jar` from
https://dev.mysql.com/downloads/connector/j/ and place it inside the
`lib/` folder.

### 3. Configure DB credentials
Edit `src/DBConnection.java` and set your MySQL username/password:
```java
private static final String USER     = "root";
private static final String PASSWORD = "your_password";
```

### 4. Compile
From the project root:
```bash
javac -cp "lib/mysql-connector-j-8.x.x.jar" -d out src/*.java
```

### 5. Run
```bash
java -cp "out:lib/mysql-connector-j-8.x.x.jar" HostelFoodApp
```
(On Windows use `;` instead of `:` in the classpath.)

## How It Works
1. On startup you enter your contact number. If you're new, you're
   asked to register (name + room number).
2. **View Food Menu** – lists all items with price, category, quantity
   in stock, and availability.
3. **Check Price** – enter a food ID to see just its price.
4. **Order Food** – add one or more items with quantities to a cart;
   the order and its line items are saved to the database and the
   total is calculated automatically.
5. **Generate & Pay Bill** – generates a bill for your last order,
   shows the amount due, and lets you pay via CASH/CARD/UPI. The
   order status and bill are updated to COMPLETED/PAID.

## Notes / Possible Extensions
- Add an admin role to manage the menu (add/update/delete food items).
- Add order history and past bills lookup per customer.
- Add discounts/coupons or a loyalty points system.
- Wrap in a GUI (JavaFX/Swing) or convert to a web app (Spring Boot).
