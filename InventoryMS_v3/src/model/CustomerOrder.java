package model;

import java.time.LocalDate;

public class CustomerOrder {
    private final String orderId;
    private final Customer customer;
    private final String productId;
    private final String productName;
    private final int quantity;
    private final LocalDate deadline;
    private OrderStatus status;
    private String assignedTo;

    public CustomerOrder(String orderId, Customer customer, String productId, String productName,
                         int quantity, LocalDate deadline, OrderStatus status, String assignedTo) {
        this.orderId = orderId; this.customer = customer;
        this.productId = productId; this.productName = productName;
        this.quantity = quantity; this.deadline = deadline;
        this.status = status; this.assignedTo = assignedTo == null ? "" : assignedTo;
    }

    public CustomerOrder(String orderId, Customer customer, String productId, String productName,
                         int quantity, LocalDate deadline, OrderStatus status) {
        this(orderId, customer, productId, productName, quantity, deadline, status, "");
    }

    public String getOrderId()     { return orderId; }
    public Customer getCustomer()  { return customer; }
    public String getProductId()   { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity()       { return quantity; }
    public LocalDate getDeadline() { return deadline; }
    public OrderStatus getStatus() { return status; }
    public String getAssignedTo()  { return assignedTo; }
    public void setStatus(OrderStatus s)    { this.status = s; }
    public void setAssignedTo(String name)  { this.assignedTo = name == null ? "" : name; }
    public boolean isPending()  { return status == OrderStatus.PENDING; }
    public boolean isOverdue()  { return isPending() && deadline.isBefore(LocalDate.now()); }

    public String toFileLine() {
        return String.join("|",
                clean(orderId),
                customer.toFilePart(),   // 4 fields: id|name|phone|address
                clean(productId), clean(productName),
                String.valueOf(quantity),
                deadline.toString(),
                status.name(),
                clean(assignedTo));
    }

    public static CustomerOrder fromFileLine(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length == 9) {
            Customer c = new Customer(p[1], p[2], p[3], "");
            return new CustomerOrder(p[0], c, p[4], p[5],
                    Integer.parseInt(p[6]), LocalDate.parse(p[7]), OrderStatus.valueOf(p[8]), "");
        } else if (p.length >= 11) {
            Customer c = new Customer(p[1], p[2], p[3], p[4]);
            return new CustomerOrder(p[0], c, p[5], p[6],
                    Integer.parseInt(p[7]), LocalDate.parse(p[8]), OrderStatus.valueOf(p[9]),
                    p.length > 10 ? p[10] : "");
        } else {
            throw new IllegalArgumentException("Invalid order record (" + p.length + " fields): " + line);
        }
    }

    private static String clean(String v) { return v == null ? "" : v.replace("|", "/").trim(); }
}
