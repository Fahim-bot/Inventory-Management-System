package service;

import model.Inventory;
import model.OrderManager;
import model.Product;
import model.SaleRecord;

import java.util.*;

public class ReportService {
    private final Inventory inventory;
    private final OrderManager orderManager;

    public ReportService(Inventory inventory, OrderManager orderManager) {
        this.inventory    = inventory;
        this.orderManager = orderManager;
    }

    public double totalRevenue() {
        double total = 0;
        for (SaleRecord s : inventory.allSales()) total += s.getRevenue();
        return total;
    }

    public double totalProfit() {
        double total = 0;
        for (SaleRecord s : inventory.allSales()) total += s.getProfit();
        return total;
    }

    public double stockValue() {
        double total = 0;
        for (Product p : inventory.allProducts()) total += p.getPurchasePrice() * p.getQuantity();
        return total;
    }

    public List<Product> lowStockProducts() {
        return inventory.lowStockProducts();
    }

    /** Returns products sorted by units sold descending. */
    public List<Map.Entry<String, Integer>> topSellingProducts(int limit) {
        Map<String, Integer> sold = new LinkedHashMap<>();
        Map<String, String>  names = new HashMap<>();
        for (SaleRecord s : inventory.allSales()) {
            sold.merge(s.getProductId(), s.getQuantity(), Integer::sum);
            names.put(s.getProductId(), s.getProductName());
        }
        List<Map.Entry<String, Integer>> list = new ArrayList<>(sold.entrySet());
        list.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        return list.subList(0, Math.min(limit, list.size()));
    }
}
