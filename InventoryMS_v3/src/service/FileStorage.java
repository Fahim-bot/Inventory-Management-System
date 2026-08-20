package service;

import model.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class FileStorage {
    private final Path dataDirectory;
    private final Path productsFile;
    private final Path ordersFile;
    private final Path salesFile;

    public FileStorage(String dataDirectory) {
        this.dataDirectory = Paths.get(dataDirectory);
        this.productsFile  = this.dataDirectory.resolve("products.txt");
        this.ordersFile    = this.dataDirectory.resolve("orders.txt");
        this.salesFile     = this.dataDirectory.resolve("sales.txt");
    }

    public void ensureFiles() {
        try {
            Files.createDirectories(dataDirectory);
            createIfMissing(productsFile);
            createIfMissing(ordersFile);
            createIfMissing(salesFile);
        } catch (IOException e) {
            throw new RuntimeException("Unable to prepare data files: " + e.getMessage(), e);
        }
    }

    public List<Product> loadProducts() {
        List<Product> list = new ArrayList<>();
        for (String line : readLines(productsFile))
            if (!line.trim().isEmpty()) list.add(Product.fromFileLine(line));
        return list;
    }

    public List<CustomerOrder> loadOrders() {
        List<CustomerOrder> list = new ArrayList<>();
        for (String line : readLines(ordersFile))
            if (!line.trim().isEmpty()) list.add(CustomerOrder.fromFileLine(line));
        return list;
    }

    public List<SaleRecord> loadSales() {
        List<SaleRecord> list = new ArrayList<>();
        for (String line : readLines(salesFile))
            if (!line.trim().isEmpty()) list.add(SaleRecord.fromFileLine(line));
        return list;
    }

    public void saveAll(Inventory inventory, OrderManager orderManager) {
        List<String> products = new ArrayList<>();
        for (Product p : inventory.allProducts()) products.add(p.toFileLine());

        List<String> orders = new ArrayList<>();
        for (CustomerOrder o : orderManager.allOrders()) orders.add(o.toFileLine());

        List<String> sales = new ArrayList<>();
        for (SaleRecord s : inventory.allSales()) sales.add(s.toFileLine());

        writeLines(productsFile, products);
        writeLines(ordersFile, orders);
        writeLines(salesFile, sales);
    }

    private List<String> readLines(Path path) {
        try { return Files.readAllLines(path); }
        catch (IOException e) { throw new RuntimeException("Cannot read " + path + ": " + e.getMessage(), e); }
    }

    private void writeLines(Path path, List<String> lines) {
        try { Files.write(path, lines); }
        catch (IOException e) { throw new RuntimeException("Cannot write " + path + ": " + e.getMessage(), e); }
    }

    private void createIfMissing(Path path) throws IOException {
        if (!Files.exists(path)) Files.createFile(path);
    }
}
