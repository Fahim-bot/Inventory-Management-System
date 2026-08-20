package model;

import java.util.*;

public class OrderManager {
    private final Queue<CustomerOrder> deliveryQueue = new LinkedList<>();

    public void addOrder(CustomerOrder order)   { deliveryQueue.add(order); }

    public List<CustomerOrder> allOrders()      { return new ArrayList<>(deliveryQueue); }

    public List<CustomerOrder> pendingOrders() {
        List<CustomerOrder> result = new ArrayList<>();
        for (CustomerOrder o : deliveryQueue) if (o.isPending()) result.add(o);
        return result;
    }

    public CustomerOrder findOrder(String orderId) {
        for (CustomerOrder o : deliveryQueue)
            if (o.getOrderId().equalsIgnoreCase(orderId)) return o;
        return null;
    }

    public boolean deleteOrder(String orderId) {
        List<CustomerOrder> list = new ArrayList<>(deliveryQueue);
        boolean removed = list.removeIf(o -> o.getOrderId().equalsIgnoreCase(orderId));
        if (removed) loadOrders(list);
        return removed;
    }

    public boolean replaceOrder(String orderId, CustomerOrder updated) {
        List<CustomerOrder> list = new ArrayList<>(deliveryQueue);
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getOrderId().equalsIgnoreCase(orderId)) {
                list.set(i, updated);
                loadOrders(list);
                return true;
            }
        }
        return false;
    }

    public void loadOrders(List<CustomerOrder> orders) {
        deliveryQueue.clear();
        deliveryQueue.addAll(orders);
    }

    public int nextOrderNumber() { return deliveryQueue.size() + 1001; }
}
