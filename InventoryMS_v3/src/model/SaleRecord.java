package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SaleRecord {
    private final String saleId;
    private final String productId;
    private final String productName;
    private final int quantity;
    private final double revenue;
    private final double profit;
    private final LocalDateTime soldAt;

    public SaleRecord(String saleId, String productId, String productName,
                      int quantity, double revenue, double profit, LocalDateTime soldAt) {
        this.saleId = saleId;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.revenue = revenue;
        this.profit = profit;
        this.soldAt = soldAt;
    }

    public String getSaleId()       { return saleId; }
    public String getProductId()    { return productId; }
    public String getProductName()  { return productName; }
    public int getQuantity()        { return quantity; }
    public double getRevenue()      { return revenue; }
    public double getProfit()       { return profit; }
    public LocalDateTime getSoldAt(){ return soldAt; }

    public String toFileLine() {
        return String.join("|",
                saleId, productId, clean(productName),
                String.valueOf(quantity),
                String.valueOf(revenue),
                String.valueOf(profit),
                soldAt.toString());
    }

    public static SaleRecord fromFileLine(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length != 7)
            throw new IllegalArgumentException("Invalid sale record: " + line);
        return new SaleRecord(parts[0], parts[1], parts[2],
                Integer.parseInt(parts[3]),
                Double.parseDouble(parts[4]),
                Double.parseDouble(parts[5]),
                LocalDateTime.parse(parts[6]));
    }

    private static String clean(String v) { return v == null ? "" : v.replace("|", "/").trim(); }

    @Override
    public String toString() {
        return String.format("%-8s %-18s Qty:%3d  Revenue:%9.2f  Profit:%9.2f  %s",
                saleId, productName, quantity, revenue, profit,
                soldAt.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
    }
}
