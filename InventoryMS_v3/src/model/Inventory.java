package model;

import java.time.LocalDateTime;
import java.util.*;

public class Inventory {
    private final Map<String, Product> products = new HashMap<>();
    private final List<SaleRecord> sales = new ArrayList<>();

    public void addProduct(Product product) {
        if (products.containsKey(product.getId()))
            throw new IllegalArgumentException("Product ID already exists.");
        products.put(product.getId(), product);
    }

    public boolean deleteProduct(String productId) {
        return products.remove(productId) != null;
    }

    public Product searchById(String productId) { return products.get(productId); }

    public List<Product> searchByName(String name) {
        String text = name.toLowerCase(Locale.ROOT);
        List<Product> result = new ArrayList<>();
        for (Product p : products.values())
            if (p.getName().toLowerCase(Locale.ROOT).contains(text)) result.add(p);
        return result;
    }

    public List<Product> searchByCategory(String category) {
        String text = category.toLowerCase(Locale.ROOT);
        List<Product> result = new ArrayList<>();
        for (Product p : products.values())
            if (p.getCategory().toLowerCase(Locale.ROOT).contains(text)) result.add(p);
        return result;
    }

    public void purchaseStock(String productId, int quantity) {
        Product p = requireProduct(productId);
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be greater than zero.");
        p.addStock(quantity);
    }

    public SaleRecord sellProduct(String productId, int quantity) {
        Product p = requireProduct(productId);
        if (!p.reduceStock(quantity))
            throw new IllegalArgumentException("Insufficient stock or invalid quantity.");
        double revenue = p.getSellingPrice() * quantity;
        double profit  = p.profitPerItem()   * quantity;
        SaleRecord sale = new SaleRecord(
                "S" + (sales.size() + 1001),
                p.getId(), p.getName(), quantity, revenue, profit, LocalDateTime.now());
        sales.add(sale);
        return sale;
    }

    public Product requireProduct(String productId) {
        Product p = products.get(productId);
        if (p == null) throw new IllegalArgumentException("Product not found: " + productId);
        return p;
    }

    public List<Product> lowStockProducts() {
        List<Product> result = new ArrayList<>();
        for (Product p : products.values()) if (p.isLowStock()) result.add(p);
        result.sort(Comparator.comparing(Product::getQuantity));
        return result;
    }

    public Collection<Product> allProducts() {
        List<Product> result = new ArrayList<>(products.values());
        result.sort(Comparator.comparing(Product::getId));
        return result;
    }

    public List<SaleRecord> allSales() { return sales; }

    public void loadProducts(List<Product> saved) {
        products.clear();
        for (Product p : saved) products.put(p.getId(), p);
    }

    public void loadSales(List<SaleRecord> saved) {
        sales.clear();
        sales.addAll(saved);
    }
}
