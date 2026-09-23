import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

 class BillService {

    /**
     * Generates a bill for the given order (if one doesn't already exist)
     * and returns the bill_id.
     */
    public int generateBill(int orderId, int customerId) {
        String checkSql = "SELECT bill_id FROM bills WHERE order_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(checkSql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("bill_id"); // already generated
                }
            }
        } catch (SQLException e) {
            System.out.println("Error checking existing bill: " + e.getMessage());
        }

        double total = getOrderTotal(orderId);
        if (total < 0) {
            System.out.println("Cannot generate bill: order not found.");
            return -1;
        }

        String insertSql = "INSERT INTO bills (order_id, customer_id, total_amount, payment_status) " +
                            "VALUES (?, ?, ?, 'UNPAID')";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, orderId);
            ps.setInt(2, customerId);
            ps.setDouble(3, total);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int billId = keys.getInt(1);
                    System.out.printf("Bill #%d generated. Amount due: Rs.%.2f%n", billId, total);
                    return billId;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error generating bill: " + e.getMessage());
        }
        return -1;
    }

    public boolean payBill(int billId, String paymentMode) {
        String sql = "UPDATE bills SET payment_mode = ?, payment_status = 'PAID', bill_date = NOW() " +
                     "WHERE bill_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, paymentMode);
            ps.setInt(2, billId);
            int rows = ps.executeUpdate();

            if (rows > 0) {
                // Mark the related order as completed
                markOrderCompleted(billId);
                System.out.println("Payment successful via " + paymentMode + ". Thank you!");
                return true;
            } else {
                System.out.println("Bill not found.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Error processing payment: " + e.getMessage());
            return false;
        }
    }

    public void printBill(int billId) {
        String sql = "SELECT b.*, c.name, c.room_no FROM bills b " +
                     "JOIN customers c ON b.customer_id = c.customer_id " +
                     "WHERE b.bill_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, billId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("\n========= HOSTEL FOOD BILL =========");
                    System.out.println("Bill No   : " + rs.getInt("bill_id"));
                    System.out.println("Order No  : " + rs.getInt("order_id"));
                    System.out.println("Customer  : " + rs.getString("name") + " (Room " + rs.getString("room_no") + ")");
                    System.out.printf("Amount    : Rs.%.2f%n", rs.getDouble("total_amount"));
                    System.out.println("Status    : " + rs.getString("payment_status"));
                    System.out.println("=====================================\n");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error printing bill: " + e.getMessage());
        }
    }

    private double getOrderTotal(int orderId) {
        String sql = "SELECT total_amount FROM orders WHERE order_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total_amount");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching order total: " + e.getMessage());
        }
        return -1;
    }

    private void markOrderCompleted(int billId) {
        String sql = "UPDATE orders SET order_status = 'COMPLETED' WHERE order_id = " +
                     "(SELECT order_id FROM (SELECT order_id FROM bills WHERE bill_id = ?) AS tmp)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, billId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating order status: " + e.getMessage());
        }
    }
}
