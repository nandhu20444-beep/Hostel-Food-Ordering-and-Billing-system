import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

/**
 * Handles creation of orders and order line items,
 * and calculates order totals.
 */
public class OrderService {

    private final FoodService foodService = new FoodService();

    /**
     * Places an order for the given customer.
     * @param customerId id of the customer placing the order
     * @param cart       map of foodId -> quantity
     * @return the newly created order_id, or -1 on failure
     */
    public int placeOrder(int customerId, Map<Integer, Integer> cart) {
        if (cart == null || cart.isEmpty()) {
            System.out.println("Cart is empty. Nothing to order.");
            return -1;
        }

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            // 1. Create the order shell with total 0 (updated later)
            int orderId;
            String orderSql = "INSERT INTO orders (customer_id, total_amount, order_status) VALUES (?, 0, 'PENDING')";
            try (PreparedStatement ps = con.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, customerId);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    orderId = keys.getInt(1);
                }
            }

            // 2. Insert each order line item and accumulate total
            double total = 0;
            String itemSql = "INSERT INTO order_items (order_id, food_id, quantity, price, subtotal) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(itemSql)) {
                for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
                    int foodId = entry.getKey();
                    int qty = entry.getValue();

                    FoodItem item = foodService.getFoodById(foodId);
                    if (item == null) {
                        System.out.println("Skipping unknown food id " + foodId);
                        continue;
                    }
                    double subtotal = item.getPrice() * qty;
                    total += subtotal;

                    ps.setInt(1, orderId);
                    ps.setInt(2, foodId);
                    ps.setInt(3, qty);
                    ps.setDouble(4, item.getPrice());
                    ps.setDouble(5, subtotal);
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // 3. Update order total
            String updateSql = "UPDATE orders SET total_amount = ? WHERE order_id = ?";
            try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                ps.setDouble(1, total);
                ps.setInt(2, orderId);
                ps.executeUpdate();
            }

            con.commit();
            System.out.printf("Order #%d placed successfully. Total: Rs.%.2f%n", orderId, total);
            return orderId;

        } catch (SQLException e) {
            System.out.println("Error placing order: " + e.getMessage());
            try {
                if (con != null) con.rollback();
            } catch (SQLException ex) {
                System.out.println("Rollback failed: " + ex.getMessage());
            }
            return -1;
        } finally {
            try {
                if (con != null) con.setAutoCommit(true);
            } catch (SQLException ignored) { }
        }
    }

    public void printOrderSummary(int orderId) {
        String sql = "SELECT f.food_name, oi.quantity, oi.price, oi.subtotal " +
                     "FROM order_items oi JOIN food_items f ON oi.food_id = f.food_id " +
                     "WHERE oi.order_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("\n--- Order #" + orderId + " Summary ---");
                System.out.printf("%-25s %-5s %-10s %-10s%n", "Item", "Qty", "Price", "Subtotal");
                while (rs.next()) {
                    System.out.printf("%-25s %-5d Rs.%-7.2f Rs.%-7.2f%n",
                            rs.getString("food_name"),
                            rs.getInt("quantity"),
                            rs.getDouble("price"),
                            rs.getDouble("subtotal"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error printing order summary: " + e.getMessage());
        }
    }
}
