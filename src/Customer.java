/**
 * Simple POJO representing a row in the customers table.
 */
public class Customer {
    private int customerId;
    private String name;
    private String roomNo;
    private String contact;

    public Customer(int customerId, String name, String roomNo, String contact) {
        this.customerId = customerId;
        this.name = name;
        this.roomNo = roomNo;
        this.contact = contact;
    }

    public int getCustomerId() { return customerId; }
    public String getName() { return name; }
    public String getRoomNo() { return roomNo; }
    public String getContact() { return contact; }

    @Override
    public String toString() {
        return "Customer{id=" + customerId + ", name=" + name +
                ", room=" + roomNo + ", contact=" + contact + "}";
    }
}
