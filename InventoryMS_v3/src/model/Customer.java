package model;

public class Customer {
    private final String id;
    private final String name;
    private final String phone;
    private final String address;

    public Customer(String id, String name, String phone, String address) {
        this.id = id; this.name = name; this.phone = phone; this.address = address;
    }

    public Customer(String id, String name, String phone) {
        this(id, name, phone, "");
    }

    public String getId()      { return id; }
    public String getName()    { return name; }
    public String getPhone()   { return phone; }
    public String getAddress() { return address; }

    public String toFilePart() {
        return clean(id) + "|" + clean(name) + "|" + clean(phone) + "|" + clean(address);
    }

    private static String clean(String v) { return v == null ? "" : v.replace("|", "/").trim(); }
}
