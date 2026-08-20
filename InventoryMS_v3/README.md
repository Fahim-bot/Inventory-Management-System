# Inventory Management System — v2 (Enhanced UI)

## Project Structure
```
InventoryManagementSystem/
├── src/
│   ├── model/
│   │   ├── Customer.java
│   │   ├── CustomerOrder.java
│   │   ├── Inventory.java
│   │   ├── OrderManager.java
│   │   ├── OrderStatus.java
│   │   ├── Product.java
│   │   └── SaleRecord.java
│   ├── service/
│   │   ├── FileStorage.java
│   │   ├── ReportService.java
│   │   └── SoundManager.java
│   ├── ui/
│   │   ├── CustomDialog.java
│   │   ├── DashboardPanel.java
│   │   ├── LoginFrame.java
│   │   ├── MainFrame.java
│   │   ├── OrderPanel.java
│   │   ├── ProductPanel.java
│   │   ├── ReportPanel.java
│   │   ├── SalesPanel.java
│   │   ├── Sidebar.java
│   │   ├── StatusBar.java
│   │   └── Theme.java
│   └── InventoryApp.java
├── data/
│   ├── orders.txt
│   ├── products.txt
│   └── sales.txt
├── lib/              ← place flatlaf-3.x.x.jar here (optional)
├── out/              ← compiled classes go here
├── run.bat           ← Windows launcher
├── run.sh            ← Linux / Mac launcher
└── README.md
```

## How to Run

### Windows (easiest)
Double-click `run.bat`

### Linux / Mac
```bash
chmod +x run.sh
./run.sh
```

### Manual
```bash
# Compile (without FlatLaf)
javac -d out src/*.java src/model/*.java src/service/*.java src/ui/*.java

# Run
java -cp out InventoryApp
```

## Optional: FlatLaf (better look)
1. Download `flatlaf-3.2.5.jar` from https://github.com/JFormDesigner/FlatLaf/releases
2. Place it inside the `lib/` folder
3. Run as normal — the app auto-detects it

Without the jar the app falls back to Nimbus (also clean).

## Login Credentials
| Role     | Password |
|----------|----------|
| Admin    | `1234`   |
| Employee | `0000`   |

## Features
- **Dark sidebar** navigation with emoji icons and hover effects
- **Gradient stat cards** on the Dashboard (Total Products, Revenue, Profit, etc.)
- **Live search / filter** on Products, Orders, and Sales tables
- **Color-coded order status** — PENDING (amber), DELIVERED (green), CANCELLED (red)
- **Custom modal dialogs** for Add / Edit / New Order
- **Role-based access** — Employees cannot delete products or view Reports
- **Sound effects** for actions (sale, restock, delete, save…)
- **Ctrl+S** saves all data
- **Status bar** with live clock and user role
- **Data persistence** — products, orders, and sales saved to `data/*.txt`
- **Demo seed data** auto-loaded on first run
