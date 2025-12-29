package service;

import config.DatabaseConfig;
import dao.*;
import model.*;

import java.sql.SQLException;
import java.util.List;

public class MarketPlace {
    
    private OrderDAO orderDAO;
    private TransactionDAO transactionDAO;
    private TradingAccountDAO tradingAccountDAO;
    private StockHoldingDAO stockHoldingDAO;
    private UserDAO userDAO;
    private StockDAO stockDAO;
    
    public MarketPlace() {
        this.orderDAO = new OrderDAO();
        this.transactionDAO = new TransactionDAO();
        this.tradingAccountDAO = new TradingAccountDAO();
        this.stockHoldingDAO = new StockHoldingDAO();
        this.userDAO = new UserDAO();
        this.stockDAO = new StockDAO();
    }
    
    /**
     * Place a BUY order
     */
    public Order placeBuyOrder(int userId, String stockName, int quantity, double price) throws SQLException {
        // Get stock
        Stock stock = stockDAO.findByName(stockName);
        if (stock == null) {
            System.out.println("Stock not found: " + stockName);
            return null;
        }
        
        double totalCost = quantity * price;
        
        // Reserve balance
        if (!tradingAccountDAO.reserveBalance(userId, totalCost)) {
            System.out.println("Insufficient balance! Need Rs." + totalCost);
            return null;
        }
        
        // Create order
        Order order = orderDAO.create(userId, stock.getStockId(), quantity, price, true);
        System.out.println("BUY order placed: #" + order.getOrderId());
        
        // Try to match
        autoMatch(stock.getStockId());
        
        return orderDAO.findById(order.getOrderId());
    }
    
    /**
     * Place a SELL order
     */
    public Order placeSellOrder(int userId, String stockName, int quantity, double price) throws SQLException {
        // Get stock
        Stock stock = stockDAO.findByName(stockName);
        if (stock == null) {
            System.out.println("Stock not found: " + stockName);
            return null;
        }
        
        // Get user's demat
        User user = userDAO.findById(userId);
        if (user == null) {
            System.out.println("User not found!");
            return null;
        }
        
        // Reserve stocks
        if (!stockHoldingDAO.reserveStocks(user.getDematId(), stock.getStockId(), quantity)) {
            System.out.println("Insufficient stocks! Check your holdings.");
            return null;
        }
        
        // Create order
        Order order = orderDAO.create(userId, stock.getStockId(), quantity, price, false);
        System.out.println("SELL order placed: #" + order.getOrderId());
        
        // Try to match
        autoMatch(stock.getStockId());
        
        return orderDAO.findById(order.getOrderId());
    }
    
    /**
     * Auto-match orders for a given stock
     */
    private void autoMatch(int stockId) throws SQLException {
        while (true) {
            // Get best buy and sell orders
            List<Order> buyOrders = orderDAO.getActiveBuyOrders(stockId);
            List<Order> sellOrders = orderDAO.getActiveSellOrders(stockId);
            
            if (buyOrders.isEmpty() || sellOrders.isEmpty()) {
                break;
            }
            
            Order bestBuy = buyOrders.get(0);
            Order bestSell = sellOrders.get(0);
            
            // Check if prices cross
            if (bestBuy.getPrice() < bestSell.getPrice()) {
                break;
            }
            
            Order buy = null;
            Order sell = null;
            
            // Check for same user conflict
            if (bestBuy.getUserId() != bestSell.getUserId()) {
                buy = bestBuy;
                sell = bestSell;
            } else {
                // Find alternative sell
                for (Order s : sellOrders) {
                    if (s.getPrice() > bestBuy.getPrice()) break;
                    if (s.getUserId() != bestBuy.getUserId()) {
                        sell = s;
                        buy = bestBuy;
                        break;
                    }
                }
                
                // Find alternative buy
                if (sell == null) {
                    for (Order b : buyOrders) {
                        if (b.getPrice() < bestSell.getPrice()) break;
                        if (b.getUserId() != bestSell.getUserId()) {
                            buy = b;
                            sell = bestSell;
                            break;
                        }
                    }
                }
            }
            
            if (buy == null || sell == null) {
                break;
            }
            
            // Execute trade
            executeTrade(buy, sell);
        }
    }
    
    /**
     * Execute a trade between buy and sell orders
     */
    private void executeTrade(Order buy, Order sell) throws SQLException {
        try {
            DatabaseConfig.beginTransaction();
            
            int quantity = Math.min(buy.getQuantity(), sell.getQuantity());
            double tradePrice = sell.getPrice();
            double total = quantity * tradePrice;
            
            // Get users and demat IDs
            User buyer = userDAO.findById(buy.getUserId());
            User seller = userDAO.findById(sell.getUserId());
            
            // Calculate refund for buyer (if bought at lower price)
            double buyerReserved = quantity * buy.getPrice();
            double refund = buyerReserved - total;
            
            // 1. Debit buyer (from reserved)
            tradingAccountDAO.debit(buy.getUserId(), total);
            
            // 2. Refund excess to buyer
            if (refund > 0) {
                tradingAccountDAO.releaseReservedBalance(buy.getUserId(), refund);
            }
            
            // 3. Credit seller
            tradingAccountDAO.credit(sell.getUserId(), total);
            
            // 4. Transfer stocks
            stockHol// filepath: src/service/MarketPlace.java
package service;

import config.DatabaseConfig;
import dao.*;
import model.*;

import java.sql.SQLException;
import java.util.List;

public class MarketPlace {
    
    private OrderDAO orderDAO;
    private TransactionDAO transactionDAO;
    private TradingAccountDAO tradingAccountDAO;
    private StockHoldingDAO stockHoldingDAO;
    private UserDAO userDAO;
    private StockDAO stockDAO;
    
    public MarketPlace() {
        this.orderDAO = new OrderDAO();
        this.transactionDAO = new TransactionDAO();
        this.tradingAccountDAO = new TradingAccountDAO();
        this.stockHoldingDAO = new StockHoldingDAO();
        this.userDAO = new UserDAO();
        this.stockDAO = new StockDAO();
    }
    
    /**
     * Place a BUY order
     */
    public Order placeBuyOrder(int userId, String stockName, int quantity, double price) throws SQLException {
        // Get stock
        Stock stock = stockDAO.findByName(stockName);
        if (stock == null) {
            System.out.println("Stock not found: " + stockName);
            return null;
        }
        
        double totalCost = quantity * price;
        
        // Reserve balance
        if (!tradingAccountDAO.reserveBalance(userId, totalCost)) {
            System.out.println("Insufficient balance! Need Rs." + totalCost);
            return null;
        }
        
        // Create order
        Order order = orderDAO.create(userId, stock.getStockId(), quantity, price, true);
        System.out.println("BUY order placed: #" + order.getOrderId());
        
        // Try to match
        autoMatch(stock.getStockId());
        
        return orderDAO.findById(order.getOrderId());
    }
    
    /**
     * Place a SELL order
     */
    public Order placeSellOrder(int userId, String stockName, int quantity, double price) throws SQLException {
        // Get stock
        Stock stock = stockDAO.findByName(stockName);
        if (stock == null) {
            System.out.println("Stock not found: " + stockName);
            return null;
        }
        
        // Get user's demat
        User user = userDAO.findById(userId);
        if (user == null) {
            System.out.println("User not found!");
            return null;
        }
        
        // Reserve stocks
        if (!stockHoldingDAO.reserveStocks(user.getDematId(), stock.getStockId(), quantity)) {
            System.out.println("Insufficient stocks! Check your holdings.");
            return null;
        }
        
        // Create order
        Order order = orderDAO.create(userId, stock.getStockId(), quantity, price, false);
        System.out.println("SELL order placed: #" + order.getOrderId());
        
        // Try to match
        autoMatch(stock.getStockId());
        
        return orderDAO.findById(order.getOrderId());
    }
    
    /**
     * Auto-match orders for a given stock
     */
    private void autoMatch(int stockId) throws SQLException {
        while (true) {
            // Get best buy and sell orders
            List<Order> buyOrders = orderDAO.getActiveBuyOrders(stockId);
            List<Order> sellOrders = orderDAO.getActiveSellOrders(stockId);
            
            if (buyOrders.isEmpty() || sellOrders.isEmpty()) {
                break;
            }
            
            Order bestBuy = buyOrders.get(0);
            Order bestSell = sellOrders.get(0);
            
            // Check if prices cross
            if (bestBuy.getPrice() < bestSell.getPrice()) {
                break;
            }
            
            Order buy = null;
            Order sell = null;
            
            // Check for same user conflict
            if (bestBuy.getUserId() != bestSell.getUserId()) {
                buy = bestBuy;
                sell = bestSell;
            } else {
                // Find alternative sell
                for (Order s : sellOrders) {
                    if (s.getPrice() > bestBuy.getPrice()) break;
                    if (s.getUserId() != bestBuy.getUserId()) {
                        sell = s;
                        buy = bestBuy;
                        break;
                    }
                }
                
                // Find alternative buy
                if (sell == null) {
                    for (Order b : buyOrders) {
                        if (b.getPrice() < bestSell.getPrice()) break;
                        if (b.getUserId() != bestSell.getUserId()) {
                            buy = b;
                            sell = bestSell;
                            break;
                        }
                    }
                }
            }
            
            if (buy == null || sell == null) {
                break;
            }
            
            // Execute trade
            executeTrade(buy, sell);
        }
    }
    
    /**
     * Execute a trade between buy and sell orders
     */
    private void executeTrade(Order buy, Order sell) throws SQLException {
        try {
            DatabaseConfig.beginTransaction();
            
            int quantity = Math.min(buy.getQuantity(), sell.getQuantity());
            double tradePrice = sell.getPrice();
            double total = quantity * tradePrice;
            
            // Get users and demat IDs
            User buyer = userDAO.findById(buy.getUserId());
            User seller = userDAO.findById(sell.getUserId());
            
            // Calculate refund for buyer (if bought at lower price)
            double buyerReserved = quantity * buy.getPrice();
            double refund = buyerReserved - total;
            
            // 1. Debit buyer (from reserved)
            tradingAccountDAO.debit(buy.getUserId(), total);
            
            // 2. Refund excess to buyer
            if (refund > 0) {
                tradingAccountDAO.releaseReservedBalance(buy.getUserId(), refund);
            }
            
            // 3. Credit seller
            tradingAccountDAO.credit(sell.getUserId(), total);
            
            // 4. Transfer stocks
            stockHol