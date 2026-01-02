package market;

import account.*;
import trading.*;
import dbConnection.*;
import dao.*;

import java.sql.SQLException;
import java.util.*;

public class MarketPlace {

    private OrderDAO orderDAO;
    private TransactionDAO transactionDAO;
    private TradingAccountDAO tradingAccountDAO;
    private StockHoldingDAO stockHoldingDAO;
    private UserDAO userDAO;
    private StockDAO stockDAO;
    private DematAccountDAO dematAccountDAO;

    public MarketPlace() {
        this.orderDAO = new OrderDAO();
        this.transactionDAO = new TransactionDAO();
        this.tradingAccountDAO = new TradingAccountDAO();
        this.stockHoldingDAO = new StockHoldingDAO();
        this.userDAO = new UserDAO();
        this.stockDAO = new StockDAO();
        this.dematAccountDAO = new DematAccountDAO();
    }

    public Order placeBuyOrder(int userId, String stockName, int quantity, double price) throws SQLException, SQLException {
        Stock stock = stockDAO.findByName(stockName);
        if (stock == null) {
            System.out.println("Stock not found: " + stockName);
            return null;
        }

        double total = quantity * price;

        if (!tradingAccountDAO.reserveBalance(userId, total)) {
            TradingAccount ta = tradingAccountDAO.findByUserId(userId);
            double available = ta != null ? ta.getAvailableBalance() : 0;
            System.out.println("Insufficient balance!"); // Need Rs." + total + ", Available: Rs." + available);
            return null;
        }

        Order order = orderDAO.createOrder(userId, stockName, quantity, price, true);
        System.out.println("BUY order placed: #" + order.getOrderId());

        autoMatch(stock.getStockId());

        return orderDAO.findById(order.getOrderId());
    }

    public Order placeSellOrder(int userId, String stockName, int quantity, double price) throws SQLException {
        Stock stock = stockDAO.findByName(stockName);
        if (stock == null) {
            System.out.println("Stock not found: " + stockName);
            return null;
        }

        User user = userDAO.findById(userId);
        if (user == null) {
            System.out.println("User not found!");
            return null;
        }

        StockHolding holding = stockHoldingDAO.findByDematAndStock(user.getDematId(), stock.getStockId());
        int available = holding != null ? holding.getAvailableQuantity() : 0;

        if (!stockHoldingDAO.reserveStocks(user.getDematId(), stock.getStockId(), quantity)) {
            System.out.println("Insufficient stocks! Available: " + available);
            return null;
        }

        Order order = orderDAO.createOrder(userId, stockName, quantity, price, false);
        System.out.println("✓ SELL order placed: #" + order.getOrderId());

        autoMatch(stock.getStockId());

        return orderDAO.findById(order.getOrderId());
    }

    private void autoMatch(int stockId) throws SQLException {

        String stockName = StockDAO.getStockNameById(stockId);

        while (true) {

            List<Order> buyOrders = orderDAO.getBuyOrders(stockName);
            List<Order> sellOrders = orderDAO.getSellOrders(stockName);

            // 🚫 No possible match
            if (buyOrders.isEmpty() || sellOrders.isEmpty()) {
                break;
            }

            Order bestBuy = buyOrders.get(0);   // highest price
            Order bestSell = sellOrders.get(0); // lowest price

            // 🚫 Price mismatch
            if (bestBuy.getPrice() < bestSell.getPrice()) {
                break;
            }

            Order buy = null;
            Order sell = null;

        /* =========================
           USER CONFLICT HANDLING
           ========================= */
            if (bestBuy.getUserId() != bestSell.getUserId()) {
                buy = bestBuy;
                sell = bestSell;
            } else {
                // Try alternate sell
                for (Order s : sellOrders) {
                    if (s.getPrice() > bestBuy.getPrice()) break;
                    if (s.getUserId() != bestBuy.getUserId() && s.getQuantity() > 0) {
                        sell = s;
                        buy = bestBuy;
                        break;
                    }
                }

                // Try alternate buy
                if (sell == null) {
                    for (Order b : buyOrders) {
                        if (b.getPrice() < bestSell.getPrice()) break;
                        if (b.getUserId() != bestSell.getUserId() && b.getQuantity() > 0) {
                            buy = b;
                            sell = bestSell;
                            break;
                        }
                    }
                }
            }

            // 🚫 No valid match
            if (buy == null || sell == null) {
                break;
            }

            // 🚫 ZERO QUANTITY GUARD (CRITICAL)
            if (buy.getQuantity() <= 0 || sell.getQuantity() <= 0) {
                if (buy.getQuantity() <= 0) {
                    orderDAO.cancelOrder(buy.getOrderId());
                }
                if (sell.getQuantity() <= 0) {
                    orderDAO.cancelOrder(sell.getOrderId());
                }
                continue;
            }

            // ✅ Execute ONE safe trade
            boolean tradeDone = executeTrade(buy, sell);

            // 🚫 Trade failed → stop infinite loop
            if (!tradeDone) {
                break;
            }
        }
    }


//    private void autoMatch(int stockId) throws SQLException {
//        while (true) {
//            String stockName = StockDAO.getStockNameById(stockId);
//            List<Order> buyOrders = orderDAO.getBuyOrders(stockName);
//            List<Order> sellOrders = orderDAO.getSellOrders(stockName);
//
//            if (buyOrders.isEmpty() || sellOrders.isEmpty()) {
//                break;
//            }
//
//            Order bestBuy = buyOrders.get(0);   // sorted desc
//            Order bestSell = sellOrders.get(0); // sorted asc
//
//            if (bestBuy.getPrice() < bestSell.getPrice()) { // no match
//                break;
//            }
//
//            Order buy = null;
//            Order sell = null;
//
//            if (bestBuy.getUserId() != bestSell.getUserId()) { // match found, different user
//                buy = bestBuy;
//                sell = bestSell;
//            } else {
//                for (Order s : sellOrders) { // match found, but same user
//                    if (s.getPrice() > bestBuy.getPrice()) break;
//                    if (s.getUserId() != bestBuy.getUserId()) {
//                        sell = s;
//                        buy = bestBuy;
//                        break;
//                    }
//                }
//
//                if (sell == null) {
//                    for (Order b : buyOrders) {
//                        if (b.getPrice() < bestSell.getPrice()) break;
//                        if (b.getUserId() != bestSell.getUserId()) {
//                            buy = b;
//                            sell = bestSell;
//                            break;
//                        }
//                    }
//                }
//            }
//
//            if (buy == null || sell == null) {
//                break;
//            }
//
//            executeTrade(buy, sell); // to database
//        }
//    }

    private boolean executeTrade(Order buy, Order sell) throws SQLException {
        try {
            DatabaseConfig.beginTransaction();

            int quantity = Math.min(buy.getQuantity(), sell.getQuantity());
            double tradePrice = sell.getPrice();
            double total = Math.round(quantity * tradePrice * 100.0) / 100.0;

            User buyer = userDAO.findById(buy.getUserId());
            User seller = userDAO.findById(sell.getUserId());

            double buyerReserved = Math.round(quantity * buy.getPrice() * 100.0) / 100.0;
            double refund = Math.round((buyerReserved - total) * 100.0) / 100.0;

            // buyer
            tradingAccountDAO.debit(buy.getUserId(), total);
            if (refund > 0) {
                tradingAccountDAO.releaseReservedBalance(buy.getUserId(), refund);
            }

            // seller
            tradingAccountDAO.credit(sell.getUserId(), total);

            stockHoldingDAO.sellShares(seller.getDematId(), sell.getStockId(), quantity);
            stockHoldingDAO.addShares(buyer.getDematId(), buy.getStockId(), quantity);

            // Updating orders
            int newBuyQty = buy.getQuantity() - quantity;
            int newSellQty = sell.getQuantity() - quantity;
            orderDAO.updateQuantity(buy.getOrderId(), newBuyQty);
            orderDAO.updateQuantity(sell.getOrderId(), newSellQty);

            transactionDAO.createTransaction(buy.getUserId(), sell.getUserId(),
                    buy.getStockId(), quantity, tradePrice);

            DatabaseConfig.commit();

            printTradeDetails(buy.getStockName(), quantity, tradePrice, total, buyer, seller);
            return true;

        } catch (SQLException e) {
            DatabaseConfig.rollback();
            throw e;
        }
    }

    private void printTradeDetails(String stockName, int qty, double price, double total, User buyer, User seller) {
        System.out.println("\n+---------------------------------------+");
        System.out.println("+           ORDER MATCHED               +");
        System.out.println("+---------------------------------------+");
        System.out.printf("+ Stock      : %-25s +%n", stockName);
        System.out.printf("+ Quantity   : %-25d +%n", qty);
        System.out.printf("+ Price      : Rs.%-22.2f +%n", price);
        System.out.printf("+ Total      : Rs.%-22.2f +%n", total);
        System.out.printf("+ Buyer      : %d (%s)+%n", buyer.getUserId(), buyer.getUserName());
        System.out.printf("+ Seller     : %d (%s)+%n", seller.getUserId(), seller.getUserName());
        System.out.println("+---------------------------------------+\n");
    }

    public boolean modifyOrder(int userId, int orderId, int newQuantity, double newPrice) throws SQLException {
        try {
            DatabaseConfig.beginTransaction();

            Order order = orderDAO.findById(orderId);
            if (order == null) {
                System.out.println("Order not found!");
                DatabaseConfig.rollback();
                return false;
            }

            if (order.getUserId() != userId) {
                System.out.println("This is not your order!");
                DatabaseConfig.rollback();
                return false;
            }

//            if (!order.getStatus().equals("OPEN") && !order.getStatus().equals("PARTIAL")) {
//                System.out.println("Cannot modify " + order.getStatus() + " order!");
//                DatabaseConfig.rollback();
//                return false;
//            }

            User user = userDAO.findById(userId);

            if (order.isBuy()) {
                // Releasing old reserved balance
                double oldReserved = order.getQuantity() * order.getPrice();
                tradingAccountDAO.releaseReservedBalance(userId, oldReserved);

                // Reserving new balance
                double newReserved = newQuantity * newPrice;
                if (!tradingAccountDAO.reserveBalance(userId, newReserved)) {
                    // Restoring to old reservation
                    tradingAccountDAO.reserveBalance(userId, oldReserved);
                    System.out.println("Insufficient balance for modification!");
                    DatabaseConfig.rollback();
                    return false;
                }
            } else {
                // Releasing old reserved stocks
                stockHoldingDAO.releaseReservedStocks(user.getDematId(), order.getStockId(), order.getQuantity());

                // Reserving new stocks
                if (!stockHoldingDAO.reserveStocks(user.getDematId(), order.getStockId(), newQuantity)) {
                    // Restoring to old reservation
                    stockHoldingDAO.reserveStocks(user.getDematId(), order.getStockId(), order.getQuantity());
                    System.out.println("Insufficient stocks for modification!");
                    DatabaseConfig.rollback();
                    return false;
                }
            }

            // Updating order
            orderDAO.modifyOrder(orderId, newQuantity, newPrice);

            DatabaseConfig.commit();
            System.out.println("Order #" + orderId + " modified successfully!");

            autoMatch(order.getStockId());

            return true;

        } catch (SQLException e) {
            DatabaseConfig.rollback();
            throw e;
        }
    }

    public boolean cancelOrder(int userId, int orderId) throws SQLException {
        try {
            DatabaseConfig.beginTransaction();

            Order order = orderDAO.findById(orderId);

            if (order == null) {
                System.out.println("Order not found!");
                DatabaseConfig.rollback();
                return false;
            }

            if (order.getUserId() != userId) {
                System.out.println("This is not your order!");
                DatabaseConfig.rollback();
                return false;
            }

//            if (!order.getStatus().equals("OPEN") && !order.getStatus().equals("PARTIAL")) {
//                System.out.println("Cannot cancel " + order.getStatus() + " order!");
//                DatabaseConfig.rollback();
//                return false;
//            }

            User user = userDAO.findById(userId);

            if (order.isBuy()) {
                // Releasing reserved balance
                double reserved = order.getQuantity() * order.getPrice();
                tradingAccountDAO.releaseReservedBalance(userId, reserved);
            } else {
                // Releasing reserved stocks
                stockHoldingDAO.releaseReservedStocks(user.getDematId(), order.getStockId(), order.getQuantity());
            }

            orderDAO.cancelOrder(orderId);

            DatabaseConfig.commit();
            System.out.println("Order #" + orderId + " cancelled successfully!");
            return true;

        } catch (SQLException e) {
            DatabaseConfig.rollback();
            throw e;
        }
    }

    public void showOrderBook(String stockName) throws SQLException {
        Stock stock = stockDAO.findByName(stockName);
        if (stock == null) {
            System.out.println("Stock not found: " + stockName);
            return;
        }

        List<Order> buyOrders = orderDAO.getBuyOrders(stockName);
        List<Order> sellOrders = orderDAO.getSellOrders(stockName);

        System.out.println("\n+---------------------------------------------------------------+");
        System.out.println("+                    ORDER BOOK: " + stockName + "        +");
        System.out.println("+--------------------------------------------------------------  -+");

        // Buy orders
        System.out.println("+ BUY ORDERS                                                    +");
        System.out.println("+---------------------------------------------------------------+");
        if (buyOrders.isEmpty()) {
            System.out.println("+   No active buy orders                                        +");
        } else {
            System.out.printf("+ %-8s %-10s %-10s %-12s +%n",
                    "OrderID", "User", "Qty", "Price");
            System.out.println("+---------------------------------------------------------------+");
            for (Order o : buyOrders) {
                System.out.printf("║ %-8d %-10s %-10d Rs.%-9.2f  ║%n",
                        o.getOrderId(), UserDAO.findUsernameById(o.getUserId()), o.getQuantity(), o.getPrice());
            }
        }

        System.out.println("+---------------------------------------------------------------+");

        // Sell orders
        System.out.println("+                       SELL ORDERS                             +");
        System.out.println("+---------------------------------------------------------------+");
        if (sellOrders.isEmpty()) {
            System.out.println("+   No active sell orders                                       +");
        } else {
            System.out.printf("+ %-8s %-10s %-10s %-12s  +%n",
                    "OrderID", "User", "Qty", "Price");
            System.out.println("+---------------------------------------------------------------+");

            for (Order o : sellOrders) {
                System.out.printf("+ %-8d %-10s %-10d Rs.%-9.2f +%n",
                        o.getOrderId(), UserDAO.findUsernameById(o.getUserId()), o.getQuantity(), o.getPrice());
            }
        }

        System.out.println("+---------------------------------------------------------------+");
    }

    public void showUserOrders(int userId) throws SQLException {
        List<Order> orders = orderDAO.findByUserId(userId);

        System.out.println("\n+-----------------------------------------------------------------------+");
        System.out.println("+                           YOUR ORDERS                                 +");
        System.out.println("+-----------------------------------------------------------------------+");

        if (orders.isEmpty()) {
            System.out.println("+   No orders found                                                     +");
        } else {
            System.out.printf("+ %-6s %-6s %-8s %-6s %-12s +%n",
                    "ID", "Type", "Stock", "Qty","Price");
            System.out.println("+-----------------------------------------------------------------------+");
            for (Order o : orders) {
                System.out.printf("+ %-6d %-6s %-8s %-6d Rs.%-9.2f+%n",
                        o.getOrderId(),
                        o.isBuy() ? "BUY" : "SELL",
                        o.getStockName(),
                        o.getQuantity(),
                        o.getPrice()
                );
            }
        }
        System.out.println("+-----------------------------------------------------------------------+");
    }

    public void showTransactions(int userId) throws SQLException {
        List<Transaction> transactions = transactionDAO.findByUserId(userId);

        System.out.println("+-------------------------------------------------------------------------------+");
        System.out.println("+                            YOUR TRANSACTIONS                                  +");
        System.out.println("+-------------------------------------------------------------------------------+");

        if (transactions.isEmpty()) {
            System.out.println("+   No transactions found                                                    +");
        } else {
            System.out.printf("+ %-5s %-8s %-10s %-10s %-6s %-12s %-12s +%n",
                    "ID", "Stock", "Buyer", "Seller", "Qty", "Price", "Total");
            System.out.println("+-------------------------------------------------------------------------------+");
            for (Transaction t : transactions) {
                System.out.printf("+ %-5d %-8s %-10s %-10s %-6d Rs.%-9.2f Rs.%-9.2f +%n",
                        t.getTransactionId(),
                        t.getStockName(),
                        t.getUserName(t.getBuyerId()),
                        t.getUserName(t.getSellerId()),
                        t.getQuantity(),
                        t.getPrice(),
                        t.getTotal());
            }
        }
        System.out.println("+-------------------------------------------------------------------------------+");
    }

    public void showAllTransactions() throws SQLException {
        List<Transaction> transactions = transactionDAO.findAll();

        System.out.println("\n+-------------------------------------------------------------------------------+");
        System.out.println("+                            ALL TRANSACTIONS                                   ║");
        System.out.println("+-------------------------------------------------------------------------------+");

        if (transactions.isEmpty()) {
            System.out.println("+   No transactions found                                                       +");
        } else {
            System.out.printf("+ %-5s %-8s %-10s %-10s %-6s %-12s %-12s +%n",
                    "ID", "Stock", "Buyer", "Seller", "Qty", "Price", "Total");
            System.out.println("+-------------------------------------------------------------------------------+");
            for (Transaction t : transactions) {
                System.out.printf("+ %-5d %-8s %-10s %-10s %-6d Rs.%-9.2f Rs.%-9.2f +%n",
                        t.getTransactionId(),
                        t.getStockName(),
                        t.getUserName(t.getBuyerId()),
                        t.getUserName(t.getSellerId()),
                        t.getQuantity(),
                        t.getPrice(),
                        t.getTotal());
            }
        }
        System.out.println("+-------------------------------------------------------------------------------+");
    }

    public void showBalance(int userId) throws SQLException {
        TradingAccount ta = tradingAccountDAO.findByUserId(userId);

        if (ta == null) {
            System.out.println("Trading account not found!");
            return;
        }

        System.out.println("\n+---------------------------------------+");
        System.out.println("+          TRADING ACCOUNT              +");
        System.out.println("+---------------------------------------+");
        System.out.printf("+ Available Balance : Rs.%-14.2f +%n", ta.getAvailableBalance());
        System.out.printf("+ Reserved Balance  : Rs.%-14.2f +%n", ta.getReservedBalance());
        System.out.printf("+ Total Balance     : Rs.%-14.2f +%n", ta.getBalance());
        System.out.println("+---------------------------------------+");
    }

    public void showPortfolio(int userId) throws SQLException {
        User user = userDAO.findById(userId);
        if (user == null) {
            System.out.println("User not found!");
            return;
        }

        List<StockHolding> holdings = stockHoldingDAO.findByDematId(user.getDematId());

        System.out.println("\n+-----------------------------------------------------------+");
        System.out.println("+                    STOCK PORTFOLIO                        +");
        System.out.println("+-----------------------------------------------------------+");

        if (holdings.isEmpty()) {
            System.out.println("+   No stock holdings                                       +");
        } else {
            System.out.printf("+ %-10s %-12s %-12s %-12s +%n",
                    "Stock", "Total", "Reserved", "Available");
            System.out.println("+-----------------------------------------------------------+");
            for (StockHolding h : holdings) {
                System.out.printf("║ %-10s %-12d %-12d %-12d ║%n",
                        StockDAO.getStockNameById(h.getStockId()),
                        h.getTotalQuantity(),
                        h.getReservedQuantity(),
                        h.getAvailableQuantity());
            }
        }
        System.out.println("+-----------------------------------------------------------+");
    }

    public boolean addBalance(int userId, double amount) throws SQLException {
        if (amount <= 0) {
            System.out.println("Amount must be positive!");
            return false;
        }
        boolean success = tradingAccountDAO.credit(userId, amount);
        if (success) {
            System.out.println("Rs." + amount + " added to your account.");
        } else {
            System.out.println("Failed to add balance!");
        }
        return success;
    }

    // for using in main
    public StockDAO getStockDAO() { return stockDAO; }
    public UserDAO getUserDAO() { return userDAO; }
    public DematAccountDAO getDematAccountDAO() { return dematAccountDAO; }
    public TradingAccountDAO getTradingAccountDAO() { return tradingAccountDAO; }
    public StockHoldingDAO getStockHoldingDAO() { return stockHoldingDAO; }
    public OrderDAO getOrderDAO() { return orderDAO; }
    public TransactionDAO getTransactionDAO() { return transactionDAO; }
}