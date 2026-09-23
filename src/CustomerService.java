import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Handles customer lookup and registration.
 */
public class CustomerService {

    public Customer findByContact(String contact) {
        String sql = "SELECT * FROM customers WHERE contact = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, contact);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Customer(
                            rs.getInt("customer_id"),
                            rs.getString("name"),
                            rs.getString("room_no"),
                            rs.getString("contact"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error looking up customer: " + e.getMessage());
        }
        return null;
    }

    public Customer registerCustomer(String name, String roomNo, String contact) {
        String sql = "INSERT INTO customers (name, room_no, contact) VALUES (?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setString(2, roomNo);
            ps.setString(3, contact);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int newId = keys.getInt(1);
                    return new Customer(newId, name, roomNo, contact);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error registering customer: " + e.getMessage());
        }
        return null;
    }
}
