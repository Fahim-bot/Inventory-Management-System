package model;

public class Product {
    private final String id;
    private String name;
    private String category;
    private int quantity;
    private double purchasePrice;
    private double sellingPrice;

    public Product(String id, String name, String category, int quantity, double purchasePrice, double sellingPrice) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
    }

    public String getId()             { return id; }
    public String getName()           { return name; }
    public String getCategory()       { return category; }
    public int getQuantity()          { return quantity; }
    public double getPurchasePrice()  { return purchasePrice; }
    public double getSellingPrice()   { return sellingPrice; }

    public void updateDetails(String name, String category, double purchasePrice, double sellingPrice) {
        this.name = name;
        this.category = category;
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
    }

    public void addStock(int quantity)    { this.quantity += quantity; }

    public boolean reduceStock(int quantity) {
        if (quantity <= 0 || quantity > this.quantity) return false;
        this.quantity -= quantity;
        return true;
    }

    public boolean isLowStock()       { return quantity < 10; }
    public double profitPerItem()     { return sellingPrice - purchasePrice; }

    public String toFileLine() {
        return String.join("|",
                clean(id), clean(name), clean(category),
                String.valueOf(quantity),
                String.valueOf(purchasePrice),
                String.valueOf(sellingPrice));
    }

    public static Product fromFileLine(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length != 6)
            throw new IllegalArgumentException("Invalid product record: " + line);
        return new Product(parts[0], parts[1], parts[2],
                Integer.parseInt(parts[3]),
                Double.parseDouble(parts[4]),
                Double.parseDouble(parts[5]));
    }

    private static String clean(String v) { return v == null ? "" : v.replace("|", "/").trim(); }

    @Override
    public String toString() {
        return String.format("%-8s %-18s %-14s Qty:%4d  Buy:%8.2f  Sell:%8.2f",
                id, name, category, quantity, purchasePrice, sellingPrice);
    }
}
