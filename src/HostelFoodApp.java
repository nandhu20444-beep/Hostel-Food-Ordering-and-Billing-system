import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


public class HostelFoodApp {

    private static final Scanner sc = new Scanner(System.in);
    private static final FoodService foodService = new FoodService();
    private static final CustomerService customerService = new CustomerService();
    private static final OrderService orderService = new OrderService();
    private static final BillService billService = new BillService();

    private static Customer currentCustomer = null;

    public static void main(String[] args) {
        System.out.println("=====================================");
        System.out.println(" HOSTEL FOOD ORDERING & BILLING SYSTEM");
        System.out.println("=====================================");

        loginOrRegister();

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Enter choice: ");

            switch (choice) {
                case 1 -> viewFoodMenu();
                case 2 -> checkPrice();
                case 3 -> orderFood();
                case 4 -> generateAndPayBill();
                case 5 -> {
                    System.out.println("Thank you for using the Hostel Food System. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice, try again.");
            }
        }

        DBConnection.closeConnection();
    }

    private static void printMenu() {
        System.out.println("\n---------------- MAIN MENU ----------------");
        System.out.println("1. View Food Menu");
        System.out.println("2. Check Price of an Item");
        System.out.println("3. Order Food");
        System.out.println("4. Generate & Pay Bill (last order)");
        System.out.println("5. Exit");
        System.out.println("--------------------------------------------");
    }

    private static void loginOrRegister() {
        System.out.print("Enter your contact number: ");
        String contact = sc.nextLine().trim();

        Customer existing = customerService.findByContact(contact);
        if (existing != null) {
            currentCustomer = existing;
            System.out.println("Welcome back, " + existing.getName() + "!");
        } else {
            System.out.println("New customer detected. Let's register you.");
            System.out.print("Name: ");
            String name = sc.nextLine().trim();
            System.out.print("Room No: ");
            String room = sc.nextLine().trim();

            currentCustomer = customerService.registerCustomer(name, room, contact);
            if (currentCustomer != null) {
                System.out.println("Registration successful. Welcome, " + name + "!");
            } else {
                System.out.println("Registration failed. Exiting.");
                System.exit(1);
            }
        }
    }

    private static void viewFoodMenu() {
        List<FoodItem> items = foodService.viewAllFood();
        System.out.println("\n============= FOOD MENU =============");
        System.out.printf("%-4s %-22s %-12s %-10s %-8s %s%n",
                "ID", "Name", "Category", "Price", "Qty", "Status");
        for (FoodItem item : items) {
            System.out.println(item);
        }
        System.out.println("======================================");
    }

    private static void checkPrice() {
        int id = readInt("Enter food ID to check price: ");
        double price = foodService.checkPrice(id);
        if (price >= 0) {
            System.out.printf("Price of item #%d: Rs.%.2f%n", id, price);
        } else {
            System.out.println("Food item not found.");
        }
    }

    private static void orderFood() {
        viewFoodMenu();
        Map<Integer, Integer> cart = new HashMap<>();

        boolean addingMore = true;
        while (addingMore) {
            int foodId = readInt("Enter food ID to add to cart: ");
            FoodItem item = foodService.getFoodById(foodId);
            if (item == null) {
                System.out.println("Invalid food ID.");
            } else {
                int qty = readInt("Enter quantity: ");
                cart.merge(foodId, qty, Integer::sum);
                System.out.println(item.getFoodName() + " x " + qty + " added to cart.");
            }

            System.out.print("Add another item? (y/n): ");
            addingMore = sc.nextLine().trim().equalsIgnoreCase("y");
        }

        int orderId = orderService.placeOrder(currentCustomer.getCustomerId(), cart);
        if (orderId != -1) {
            orderService.printOrderSummary(orderId);
            lastOrderId = orderId;
        }
    }

    private static int lastOrderId = -1;

    private static void generateAndPayBill() {
        if (lastOrderId == -1) {
            System.out.println("No recent order found. Please place an order first.");
            return;
        }

        int billId = billService.generateBill(lastOrderId, currentCustomer.getCustomerId());
        if (billId == -1) return;

        billService.printBill(billId);

        System.out.print("Proceed to payment? (y/n): ");
        if (sc.nextLine().trim().equalsIgnoreCase("y")) {
            System.out.print("Payment mode (CASH/CARD/UPI): ");
            String mode = sc.nextLine().trim().toUpperCase();
            if (!mode.equals("CASH") && !mode.equals("CARD") && !mode.equals("UPI")) {
                System.out.println("Invalid mode, defaulting to CASH.");
                mode = "CASH";
            }
            billService.payBill(billId, mode);
            billService.printBill(billId);
        } else {
            System.out.println("Payment postponed. Bill remains UNPAID.");
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
}
