import DAO.DematAccountDAO;
import DAO.StockDAO;
import DAO.TradingAccountDAO;
import DbConnection.DatabaseConfig;
import trading.*;
import util.*;
import account.*;
import market.*;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
public class Main {

    static Validator validator;
    static HashMap<Integer, User> users;
    static HashMap<Integer, DematAccount> demats;
    static HashMap<Integer, TradingAccount> tradings;

    static ArrayList<Order> buyOrders, sellOrders;
    static HashMap<Integer, Order> ordersById;

    static ArrayList<Transaction> transactions;

    static ArrayList<String> stockSymbols;

    static MarketPlace marketPlace;

    static Scanner sc;
    static InputHandler inputHandler;

    static User promoter1;

    static {
        validator = new Validator();
        users = new HashMap<>();
        demats = new HashMap<>();
        tradings = new HashMap<>();
        buyOrders = new ArrayList<>();
        sellOrders = new ArrayList<>();
        ordersById = new HashMap<>();
        transactions = new ArrayList<>();
        sc = new Scanner(System.in);
        inputHandler = new InputHandler(sc);

        stockSymbols = new ArrayList<>();
        stockSymbols.add("TCS");
        stockSymbols.add("NIFTY");
        stockSymbols.add("SBI");
        stockSymbols.add("INFY");

        marketPlace = new MarketPlace();
        marketPlace.setReferences(tradings, demats, users, transactions, ordersById);

        // Promoter 1
        promoter1 = new User("Ram", "Ab.11111", "AWSD12J", true);
        DematAccount dematAccount1 = getOrCreateDematByPAN(promoter1.getPanNumber(), "Ab.11111");
        promoter1.setDematAccount(dematAccount1);
        users.put(promoter1.getUserId(), promoter1);
        TradingAccount tradingAccount1 = new TradingAccount(promoter1.getUserId());
        tradings.put(tradingAccount1.getTradingAccountId(), tradingAccount1);
        promoter1.setTradingAccount(tradingAccount1);
        dematAccount1.addShares("TCS", 1000);
        dematAccount1.addShares("NIFTY", 1500);
        dematAccount1.addShares("SBI", 2000);
        dematAccount1.addShares("INFY", 1200);

        DematAccount p1Demat = promoter1.getDematAccount();
        int p1SellQty = 300;
        double p1SellPrice = 1500.5;
        if (p1Demat.reserveStocks("TCS", p1SellQty)) {
            Order p1SellOrder = new Order(promoter1, "TCS", p1SellQty, p1SellPrice, false);
            marketPlace.addSellOrder(p1SellOrder);
            System.out.println("Promoter1 SELL placed: " + p1SellOrder.getOrderId());
        }
    }

    public static void main(String[] args) {
        System.out.println("---------- WELCOME TO TRADING ----------");
//        DatabaseConfig db = new DatabaseConfig();
//        db.getConnection();
//        db.closeConnection();
        StockDAO s = new StockDAO();
        List<String> list = s.getStocks();
        System.out.println(list);

        DematAccountDAO d = new DematAccountDAO();

//        DematAccount dematAccount = d.findByPanNumber("RAMS12R");
//        System.out.println(dematAccount.getDemandAccountId() + " " + dematAccount.getPanNumber());

//          System.out.println(d.authenticate("KJKWk1L", "Jaswanth.1"));

//        dematAccount = d.findByPanNumber("KJKWk1L");
//        System.out.println(dematAccount.getDemandAccountId() + " " + dematAccount.getPanNumber());
        TradingAccountDAO t = new TradingAccountDAO();

//        TradingAccount tradingAccount = t.findByUserId(1);
//        System.out.println(tradingAccount.getTradingAccountId() + " " + tradingAccount.getUserId() + " " + tradingAccount.getBalance());
//        System.out.println();

//        while (true) {
//            mainMenu();
//            int choice = inputHandler.getMenuChoice();
//            switch (choice) {
//                case 1:
//                    register();
//                    break;
//                case 2:
//                    login();
//                    break;
//                case 3:
//                    System.out.println("Exited!");
//                    return;
//                default:
//                    System.out.println("Invalid choice.");
//            }
//        }
    }

    static void mainMenu(){
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.println("3, Exit\n");
        System.out.print("Enter your choice : ");
    }

    static void printUserMenu(User user) {
        System.out.println("\n--- USER MENU (" + user.getUserName() + ") ---");
        System.out.println("1.  View Portfolio");
        System.out.println("2.  Place BUY Order");
        System.out.println("3.  Place SELL Order");
        System.out.println("4.  View My Orders");
        System.out.println("5.  Modify Order");
        System.out.println("6.  View All Order Book");
        System.out.println("7.  View My Transactions");
        System.out.println("8.  View All Transactions");
        System.out.println("9.  Add money from Savings account");
        System.out.println("10. Delete Account");
        System.out.println("11. Logout");
        System.out.print("Enter choice: ");
    }

    static void register() {
        String name = inputHandler.getString("Enter username: ");
        if(!validator.validateUserName(name)){
            return;
        }
        
        String pass = inputHandler.getString("Enter password: ");
        if(!validator.validatePassword(pass)){
            return;
        }

        String confirmPass = inputHandler.getString("Enter confirm password : ");
        if(!pass.equals(confirmPass)){
            System.out.println("Passwords don't match! Register again");
            return;
        }
        
        String pan = inputHandler.getString("Enter PAN number: ");

        if (isPanLinkedToActiveUser(pan)) {
            System.out.println("This PAN is already linked to an active account. Cannot register.");
            return;
        }

        DematAccount existingDemat = getDematByPAN(pan);
        String dematPass = null;
        
        if (existingDemat != null) {
            System.out.println("This PAN has an existing Demat Account.");
            if (!existingDemat.authenticateWithPrompt(inputHandler, "Enter your Demat password to link: ")) {
                System.out.println("Authentication failed! Wrong Demat password.");
                return;
            }
            System.out.println("Authentication successful! Linking to existing Demat Account...");
        } else {
            System.out.println("Creating new Demat Account for PAN: " + pan);
            dematPass = inputHandler.getString("Create Demat password: ");
            if(!validator.validatePassword(dematPass)){
                return;
            }
            String confirmDematPass = inputHandler.getString("Confirm Demat password: ");
            if (!dematPass.equals(confirmDematPass)) {
                System.out.println("Demat passwords don't match! Register again");
                return;
            }
        }

        User user = new User(name, pass, pan, false);
        users.put(user.getUserId(), user);

        DematAccount demat = getOrCreateDematByPAN(pan, dematPass);
        user.setDematAccount(demat);

        TradingAccount trade = new TradingAccount(user.getUserId());
        tradings.put(trade.getTradingAccountId(), trade);
        user.setTradingAccount(trade);

        System.out.println("Registered! User ID: " + user.getUserId());
        if (demat.hasHoldings()) {
            System.out.println("Your previous holdings have been restored!");
        }
    }

    static void login() {
        int id = inputHandler.getInteger("Enter User ID: ");

        User user = users.get(id);
        if (user == null) {
            System.out.println("User not found.");
            return;
        }
        if (user.isDeleted()) {
            System.out.println("User ID has been deleted.");
            return;
        }

        if (!user.login(inputHandler)) {
            System.out.println("Wrong password.");
            return;
        }

        System.out.println("Login successful! Welcome " + user.getUserName());
        userMenu(user);
    }

    static void userMenu(User user) {
        while (true) {
            printUserMenu(user);
            int choice = inputHandler.getMenuChoice();

            switch (choice) {
                case 1:
                    viewPortfolio(user);
                    break;
                case 2:
                    placeBuyOrder(user);
                    break;
                case 3:
                    placeSellOrder(user);
                    break;
                case 4:
                    viewMyOrders(user);
                    break;
                case 5:
                    modifyOrder(user);
                    break;
                case 6:
                    viewOrderBook();
                    break;
                case 7:
                    viewMyTransactions(user);
                    break;
                case 8:
                    viewAllTransactions();
                    break;
                case 9:
                    addMoney(user);
                    break;
                case 10:
                    if (deleteAccount(user)) {
                        return;
                    }
                    break;
                case 11:
                    System.out.println("Logged out.");
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    static void viewPortfolio(User user) {
        System.out.println("\n--- PORTFOLIO ---");

        DematAccount demat = user.getDematAccount();
        if (demat != null) {
            demat.getHoldings();
        } else {
            System.out.println("No Demat account.");
        }

        TradingAccount ta = user.getTradingAccount();
        if (ta != null) {
            ta.showBalances();
        }
    }

    static void placeBuyOrder(User user) {
        System.out.println("\nAvailable stocks: " + stockSymbols);
        String stock = inputHandler.getString("Enter stock name: ").toUpperCase();

        if (!stockSymbols.contains(stock)) {
            System.out.println("Invalid stock.");
            return;
        }

        int qty = inputHandler.getPositiveInteger("Enter quantity: ");
        double price = inputHandler.getPositiveDouble("Enter price: ");

        double total = qty * price;
        TradingAccount trade = user.getTradingAccount();

        if (!trade.reserveBalance(total)) {
            System.out.println("Insufficient balance.");
            return;
        }

        Order order = new Order(user, stock, qty, price, true);
        marketPlace.addBuyOrder(order);

        if (order.getStatus().equals("FILLED")) {
            System.out.println("\nOrder #" + order.getOrderId() + " completely filled!");
        } else if (order.getStatus().equals("PARTIAL")) {
            System.out.println("\nOrder #" + order.getOrderId() + " partially filled. Remaining: " + order.getQuantity() + " shares waiting.");
        } else {
            System.out.println("\nBUY order #" + order.getOrderId() + " placed. Waiting for matching sell order.");
        }
    }

    static void placeSellOrder(User user) {
        DematAccount demat = user.getDematAccount();
        if (demat == null) {
            System.out.println("No Demat account.");
            return;
        }

        demat.getHoldings();
        String stock = inputHandler.getString("Enter stock name: ").toUpperCase();

        int qty = inputHandler.getPositiveInteger("Enter quantity: ");
        double price = inputHandler.getPositiveDouble("Enter price (per 1 stock): ");

        if (!demat.reserveStocks(stock, qty)) {
            System.out.println("Cannot reserve stocks.");
            return;
        }

        Order order = new Order(user, stock, qty, price, false);
        marketPlace.addSellOrder(order);

        if (order.getStatus().equals("FILLED")) {
            System.out.println("\nOrder #" + order.getOrderId() + " completely filled!");
        } else if (order.getStatus().equals("PARTIAL")) {
            System.out.println("\nOrder #" + order.getOrderId() + " partially filled. Remaining: " + order.getQuantity() + " shares waiting.");
        } else {
            System.out.println("\nSELL order #" + order.getOrderId() + " placed. Waiting for matching buy order.");
        }
    }

    static void viewMyOrders(User user) {
        System.out.println("\n--- MY ACTIVE ORDERS (OPEN & PARTIAL) ---");
        boolean found = false;

        System.out.println("+----------+--------+----------+----------+----------+----------+------------+--------------+------------+");
        System.out.printf("| %-8s | %-6s | %-8s | %-8s | %-8s | %-8s | %-10s | %-12s | %-10s |%n",
                "Order ID", "Type", "Stock", "Original", "Filled", "Remaining", "Price", "Total", "Status");
        System.out.println("+----------+--------+----------+----------+----------+----------+------------+--------------+------------+");
        
        for (Order order : ordersById.values()) {
            // shows only OPEN and PARTIAL orders (not FILLED or CANCELLED)
            if (order.getUser() == user && 
                (order.getStatus().equals("OPEN") || order.getStatus().equals("PARTIAL"))) {
                order.printRow(user);
                found = true;
            }
        }

        System.out.println("+----------+--------+----------+----------+----------+----------+------------+--------------+------------+");
        
        if (!found) {
            System.out.println("No active orders.");
        }
    }

    static void modifyOrder(User user) {
        viewMyOrders(user);

        boolean hasActiveOrders = false;
        for (Order order : ordersById.values()) {
            if (order.getUser() == user && 
                (order.getStatus().equals("OPEN") || order.getStatus().equals("PARTIAL"))) {
                hasActiveOrders = true;
                break;
            }
        }
        
        if (!hasActiveOrders) {
            System.out.println("You have no orders to modify.");
            return;
        }
        
        int orderId = inputHandler.getInteger("Enter Order ID to modify: ");
        
        Order order = marketPlace.getOrderById(orderId);
        if (order == null) {
            System.out.println("Order #" + orderId + " not found.");
            return;
        }
        
        // checking ownership
        if (order.getUser() != user) {
            System.out.println("You can only modify your own orders.");
            return;
        }
        
        // current order details
        System.out.println("\n--- Current Order Details ---");
        String type = order.isBuy() ? "BUY" : "SELL";
        System.out.println("Order ID    : " + order.getOrderId());
        System.out.println("Type        : " + type);
        System.out.println("Stock       : " + order.getStockName());
        System.out.println("Original Qty: " + order.getOriginalQuantity());
        System.out.println("Filled Qty  : " + order.getFilledQuantity());
        System.out.println("Remaining   : " + order.getQuantity());
        System.out.println("Price       : Rs." + order.getPrice());
        System.out.println("Status      : " + order.getStatus());

        System.out.println("\n(Enter new values or 0 to keep current)");
        
        int newQuantity = inputHandler.getInteger("Enter new total quantity (current: " + order.getOriginalQuantity() + "): ");
        if (newQuantity == 0) {
            newQuantity = order.getOriginalQuantity();
        }
        
        double newPrice = inputHandler.getDouble("Enter new price (current: " + order.getPrice() + "): ");
        if (newPrice == 0) {
            newPrice = order.getPrice();
        }
        
        // Confirm ?
        System.out.println("\nNew quantity: " + newQuantity + ", New price: Rs." + newPrice);
        String confirm = inputHandler.getString("Confirm modification? (yes/no): ");
        
        if (confirm.equalsIgnoreCase("yes") || confirm.equalsIgnoreCase("y")) {
            marketPlace.modifyOrder(user, orderId, newQuantity, newPrice);
        } else {
            System.out.println("Modification cancelled.");
        }
    }

//    static void viewOrders() {
//        System.out.println("\n--- ALL ORDERS ---"); // open and partial
//        boolean found = false;
//
//        System.out.println("+----------+--------+----------+----------+----------+----------+------------+--------------+------------+");
//        System.out.printf("| %-8s | %-6s | %-8s | %-8s | %-8s | %-8s | %-10s | %-12s | %-10s |%n",
//                "Order ID", "Type", "Stock", "Original", "Filled", "Remaining", "Price", "Total", "Status");
//        System.out.println("+----------+--------+----------+----------+----------+----------+------------+--------------+------------+");
//
//        for (Order order : ordersById.values()) {
//            if (order.getStatus().equals("OPEN") || order.getStatus().equals("PARTIAL")) {
//                order.printRow();
//                found = true;
//            }
//        }
//
//        System.out.println("+----------+--------+----------+----------+----------+----------+------------+--------------+------------+");
//
//        if (!found) {
//            System.out.println("No active orders in the system.");
//        }
//    }

    static void viewOrderBook() {
        System.out.println("\n--- ORDER BOOKS ---");
        for (String stock : stockSymbols) {
            marketPlace.printBook(stock);
        }
    }

    static void viewMyTransactions(User user) {
        System.out.println("\n--- MY TRANSACTIONS ---");
        boolean found = false;

        System.out.println("+----------+--------+----------+-------+------------+--------------+---------------------+------------+");
        System.out.printf("| %-8s | %-6s | %-8s | %-5s | %-10s | %-12s | %-19s | %-10s |%n",
                "Trans ID", "Type", "Stock", "Qty", "Price", "Total", "Counterparty", "Time");
        System.out.println("+----------+--------+----------+-------+------------+--------------+---------------------+------------+");

        for (Transaction t : transactions) {
            if (t.getBuyer() == user || t.getSeller() == user) {
                t.printRow(user);
                found = true;
            }
        }

        System.out.println("+----------+--------+----------+-------+------------+--------------+---------------------+------------+");

        if (!found) {
            System.out.println("No transactions yet.");
        }
    }

    static void viewTransactions() {
        System.out.println("\n--- ALL TRANSACTIONS ---");
        if (transactions.isEmpty()) {
            System.out.println("No transactions yet.");
        } else {
            System.out.println("+----------+----------+-------+------------+--------------+--------------+--------------+------------+");
            System.out.printf("| %-8s | %-8s | %-5s | %-10s | %-12s | %-12s | %-12s | %-10s |%n",
                    "Trans ID", "Stock", "Qty", "Price", "Total", "Buyer", "Seller", "Time");
            System.out.println("+----------+----------+-------+------------+--------------+--------------+--------------+------------+");
            for (Transaction t : transactions) {
                t.printRow();
            }
            System.out.println("+----------+----------+-------+------------+--------------+--------------+--------------+------------+");
        }
    }

    static void viewAllTransactions() {
        viewTransactions();
    }

    static void addMoney(User user){
        double amount = inputHandler.getDouble("Enter the amount to add : ");
        if(amount <= 0){
            System.out.println("Enter the amount from that 0.0");
            return;
        }
        TradingAccount temp1 = user.getTradingAccount();
        System.out.println("Added " + amount + " Successfully!");
        temp1.credit(amount);
    }

    static DematAccount getDematByPAN(String pan) {
        for (DematAccount d : demats.values()) {
            if (d.getPanNumber().equals(pan)) {
                return d;
            }
        }
        return null;
    }

    static DematAccount getOrCreateDematByPAN(String pan, String password) {
        for (DematAccount d : demats.values()) {
            if (d.getPanNumber().equals(pan)) {
                System.out.println("Linked existing Demat for PAN: " + pan);
                return d;
            }
        }
        DematAccount newDemat = new DematAccount(pan, password);
        demats.put(newDemat.getDemandAccountId(), newDemat);
        System.out.println("Created new Demat for PAN: " + pan);
        return newDemat;
    }

    static boolean isPanLinkedToActiveUser(String pan) {
        for (User u : users.values()) {
            if (u.getPanNumber().equals(pan) && !u.isDeleted()) {
                return true;
            }
        }
        return false;
    }

    // deletes only trading account, not the demat account
    static boolean deleteAccount(User user) {
        System.out.println("\n--- DELETE ACCOUNT ---");

        TradingAccount trade = user.getTradingAccount();
        if (trade != null) {
            System.out.println("\nYour Account with the savings money will also be deleted :");
            System.out.printf("Trading Balance  : Rs.%-16.2f \n", trade.getBalance());
            System.out.printf("Current Balance  : Rs.%-16.2f \n", trade.getCurrentBalance());
            System.out.printf("Reserved Balance : Rs.%-16.2f \n", trade.getReservedBalance());
            System.out.println("\nYour Demat Account and stock holdings will be PRESERVED.");
            System.out.println("------------------------------------------------------------");
        }
        if (!user.login(inputHandler)) {
            System.out.println("Wrong password. Account deletion unsuccessful.");
            return false;
        }

        cancelUserOrders(user);

        TradingAccount tradeToRemove = user.getTradingAccount();
        if (tradeToRemove != null) {
            tradings.remove(tradeToRemove.getTradingAccountId());
        }

        user.setDeleted(true);

        System.out.println("\nAccount deleted successfully!");
        return true;
    }

    static void cancelUserOrders(User user) {
        for (Order order : ordersById.values()) {
            if (order.getUser() == user && !order.getStatus().equals("FILLED")) {
                if (order.isBuy()) {
                    TradingAccount ta = order.getTradingAccount();
                    if (ta != null) {
                        ta.releaseReservedBalance(order.getQuantity() * order.getPrice());
                    }
                } else {
                    DematAccount da = order.getDematAccount();
                    if (da != null) {
                        da.releaseReservedStocks(order.getStockName(), order.getQuantity());
                    }
                }
                order.setStatus("CANCELLED");
                marketPlace.removeOrder(order);
            }
        }
    }
}