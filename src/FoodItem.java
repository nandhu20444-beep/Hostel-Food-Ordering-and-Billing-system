/**
 * Simple POJO representing a row in the food_items table.
 */
public class FoodItem {
    private int foodId;
    private String foodName;
    private String category;
    private double price;
    private int availableQty;
    private String status;

    public FoodItem(int foodId, String foodName, String category, double price,
                     int availableQty, String status) {
        this.foodId = foodId;
        this.foodName = foodName;
        this.category = category;
        this.price = price;
        this.availableQty = availableQty;
        this.status = status;
    }

    public int getFoodId() { return foodId; }
    public String getFoodName() { return foodName; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public int getAvailableQty() { return availableQty; }
    public String getStatus() { return status; }

    @Override
    public String toString() {
        return String.format("%-4d %-22s %-12s Rs.%-8.2f Qty:%-4d %s",
                foodId, foodName, category, price, availableQty, status);
    }
}
