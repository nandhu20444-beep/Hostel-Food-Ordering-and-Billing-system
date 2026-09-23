import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all operations related to the food menu:
 * viewing items, checking price/availability by id.
 */
public class FoodService {

    public List<FoodItem> viewAllFood() {
        List<FoodItem> list = new ArrayList<>();
        String sql = "SELECT * FROM food_items ORDER BY category, food_name";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching food menu: " + e.getMessage());
        }
        return list;
    }

    public FoodItem getFoodById(int foodId) {
        String sql = "SELECT * FROM food_items WHERE food_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, foodId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching food item: " + e.getMessage());
        }
        return null;
    }

    public double checkPrice(int foodId) {
        FoodItem item = getFoodById(foodId);
        return (item != null) ? item.getPrice() : -1;
    }

    private FoodItem mapRow(ResultSet rs) throws SQLException {
        return new FoodItem(
                rs.getInt("food_id"),
                rs.getString("food_name"),
                rs.getString("category"),
                rs.getDouble("price"),
                rs.getInt("available_qty"),
                rs.getString("status")
        );
    }
}
