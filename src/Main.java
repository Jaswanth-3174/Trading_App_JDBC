import dbConnection.*;
import dao.*;
import trading.*;
import market.*;
import util.*;
import account.*;

import java.sql.SQLException;
import java.util.List;

public class Main {

    private static MarketPlace marketPlace;
    private static User currentUser = null;

    public static void main(String[] args) {
        try {
            // Initialize
            DatabaseConfig.getConnection();
            marketPlace = new MarketPlace();

            System.out.println("\n+-----------------------------------------------------------+");
            System.out.println("+         WELCOME TO TRADING CONSOLE APPLICATION            +");
            System.out.println("+-----------------------------------------------------------+");

            // Main loop
            while (true) {
                if (currentUser == null) {
                    showLoginMenu();
                } else {
                    showMainMenu();
                }
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseConfig.closeConnection();
        }
    }


    private static void showLoginMenu() throws SQLException {
        System.out.println("\n+---------------------------------------+");
        System.out.println("+            LOGIN MENU                 +");
        System.out.println("+---------------------------------------+");
        System.out.println("+  1. Login                             +");
        System.out.println("+  2. Register New User                 +");
        System.out.println("+  3. Reset Database                    +");
        System.out.println("+  0. Exit                              +");
        System.out.println("+---------------------------------------+");

        int choice = InputHandler.getInteger("Enter choice: ");
        switch (choice) {
            case 1 -> login();
            case 2 -> register();
            case 3 -> resetDatabase();
            case 0 -> {
                System.out.println("Exiting!");
                System.exit(0);
            }
        }
    }

    private static void login() throws SQLException {
        System.out.println("\n--- LOGIN ---");
        int userId = InputHandler.getInteger("Enter User ID: ");
        String password = InputHandler.getString("Enter Password: ");
        if (marketPlace.getUserDAO().authenticateUser(userId, password)) {
            currentUser = marketPlace.getUserDAO().findById(userId);
            System.out.println("\nWelcome, " + currentUser.getUserName() + "!");
        } else {
            System.out.println("\nInvalid credentials!");
        }
    }

    private static void register() throws SQLException {
        System.out.println("\n--- REGISTER NEW USER ---");

        // Demat Account
        String pan = InputHandler.getString("Enter PAN Number: ").toUpperCase();
        if (!Validator.validatePanNumber(pan)) {
            System.out.println("Invalid PAN format!");
            return;
        }
        DematAccount demat = marketPlace.getDematAccountDAO().findByPanNumber(pan);
        if (demat != null) {
            // check for active user
            System.out.println("Demat account found for PAN: " + pan);
            String dematPassword = InputHandler.getString("Enter Demat Password: ");
            if (!marketPlace.getDematAccountDAO().authenticate(pan, dematPassword)) {
                System.out.println("Invalid demat password!");
                return;
            }
            if (marketPlace.getUserDAO().isActiveUserLinkedWithDematId(demat.getDematAccountId())) {
                System.out.println("Active user exists with this demat account!");
                return;
            }
        } else {
            // Create new demat
            String dematPassword = InputHandler.getString("Create Demat Password: ");
            if (!Validator.validatePassword(dematPassword)) {
                System.out.println("Password does not meet requirements!");
                return;
            }
            demat = marketPlace.getDematAccountDAO().createDematAccount(pan, dematPassword);
            System.out.println("Demat account created!");
        }

        // User Account
        String username = InputHandler.getString("Enter Username: ");
        if (!Validator.validateUserName(username)) {
            System.out.println("Invalid username!"); // min 3 chars
            return;
        }

        String userPassword = InputHandler.getString("Create User Password: ");
        if (!Validator.validatePassword(userPassword)) {
            System.out.println("Password does not meet requirements!");
            return;
        }

        User user = marketPlace.getUserDAO().createUser(username, userPassword, demat.getDematAccountId(), false);
        double initialBalance = 1000 + Math.random() * 4000;
        marketPlace.getTradingAccountDAO().createTradingAccount(user.getUserId(), initialBalance);
        System.out.println("\n+---------------------------------------+");
        System.out.println("+        REGISTRATION SUCCESSFUL        +");
        System.out.println("+---------------------------------------+");
        System.out.printf("+  User ID       : %-20d +%n", user.getUserId());
        System.out.printf("+  Username      : %-20s +%n", user.getUserName());
        System.out.printf("+  Initial Balance: Rs.%-16.2f +%n", initialBalance);
        System.out.println("+---------------------------------------+");

    }

    private static void resetDatabase() throws SQLException {
        if (InputHandler.getYesNo("Sure you want to reset the database? Y to confirm")) {
            DatabaseConfig.resetDatabase();
        }
    }

    private static void showMainMenu() throws SQLException {
        System.out.println("\n+-----------------------------------------------------------+");
        System.out.printf("+  MAIN MENU                    User: %s %n", currentUser.getUserName());
        System.out.println("+-----------------------------------------------------------+");
        System.out.println("+  1. Place BUY Order                                       +");
        System.out.println("+  2. Place SELL Order                                      +");
        System.out.println("+  3. View My Orders                                        +");
        System.out.println("+  4. Modify Order                                          +");
        System.out.println("+  5. Cancel Order                                          +");
        System.out.println("+  6. View Order Book                                       +");
        System.out.println("+  7. View My Transactions                                  +");
        System.out.println("+  8. View All Transactions                                 +");
        System.out.println("+  9. View Balance                                          +");
        System.out.println("+ 10. Add Balance                                           +");
        System.out.println("+ 11. View Portfolio                                        +");
        System.out.println("+ 12. View Available Stocks                                 +");
        System.out.println("+  0. Logout                                                +");
        System.out.println("+-----------------------------------------------------------+");

        int choice = InputHandler.getInteger("Enter choice: ");
        switch (choice) {
            case 1 -> placeBuyOrder();
            case 2 -> placeSellOrder();
            case 3 -> marketPlace.showUserOrders(currentUser.getUserId());
            case 4 -> modifyOrder();
            case 5 -> cancelOrder();
            case 6 -> viewOrderBook();
            case 7 -> marketPlace.showTransactions(currentUser.getUserId());
            case 8 -> marketPlace.showAllTransactions();
            case 9 -> marketPlace.showBalance(currentUser.getUserId());
            case 10 -> addBalance();
            case 11 -> marketPlace.showPortfolio(currentUser.getUserId());
            case 12 -> showAvailableStocks();
            case 0 -> logout();
        }
    }

    private static void placeBuyOrder() throws SQLException {
        System.out.println("\n--- PLACE BUY ORDER ---");
        showAvailableStocks();

        String stockName = InputHandler.getString("Enter Stock Name: ").toUpperCase();
        int quantity = InputHandler.getPositiveInteger("Enter Quantity: ");
        double price = InputHandler.getPositiveDouble("Enter Price per share: Rs.");
        marketPlace.placeBuyOrder(currentUser.getUserId(), stockName, quantity, price);
    }

    private static void placeSellOrder() throws SQLException {
        System.out.println("\n--- PLACE SELL ORDER ---");
        marketPlace.showPortfolio(currentUser.getUserId());

        String stockName = InputHandler.getString("Enter Stock Name: ").toUpperCase();
        int quantity = InputHandler.getPositiveInteger("Enter Quantity: ");
        double price = InputHandler.getPositiveDouble("Enter Price per share: Rs.");
        marketPlace.placeSellOrder(currentUser.getUserId(), stockName, quantity, price);
    }

    private static void modifyOrder() throws SQLException {
        System.out.println("\n--- MODIFY ORDER ---");
        marketPlace.showUserOrders(currentUser.getUserId());

        int orderId = InputHandler.getPositiveInteger("Enter Order ID to modify: ");
        int newQuantity = InputHandler.getPositiveInteger("Enter New Quantity: ");
        double newPrice = InputHandler.getPositiveDouble("Enter New Price: Rs.");
        marketPlace.modifyOrder(currentUser.getUserId(), orderId, newQuantity, newPrice);
    }

    private static void cancelOrder() throws SQLException {
        System.out.println("\n--- CANCEL ORDER ---");
        marketPlace.showUserOrders(currentUser.getUserId());

        int orderId = InputHandler.getPositiveInteger("Enter Order ID to cancel: ");
        if (InputHandler.getYesNo("Sure you want to cancel order #" + orderId + "?")) {
            marketPlace.cancelOrder(currentUser.getUserId(), orderId);
        }
    }

    private static void viewOrderBook() throws SQLException {
        System.out.println("\n--- VIEW ORDER BOOK ---");
        showAvailableStocks();

        String stockName = InputHandler.getString("Enter Stock Name: ").toUpperCase();
        marketPlace.showOrderBook(stockName);
    }

    private static void addBalance() throws SQLException {
        System.out.println("\n--- ADD BALANCE ---");
        marketPlace.showBalance(currentUser.getUserId());

        double amount = InputHandler.getPositiveDouble("Enter amount to add: Rs.");
        marketPlace.addBalance(currentUser.getUserId(), amount);
        marketPlace.showBalance(currentUser.getUserId());
    }

    private static void showAvailableStocks() throws SQLException {
        List<Stock> stocks = marketPlace.getStockDAO().listAllStocks();
        System.out.println("\n+---------------------------------------+");
        System.out.println("+         AVAILABLE STOCKS              +");
        System.out.println("+---------------------------------------+");
        for (Stock stock : stocks) {
            System.out.printf("+   • %-33s +%n", stock.getStockName());
        }

        System.out.println("+---------------------------------------+");
    }

    private static void logout() {
        System.out.println("\nLogged out successfully. Exiting, " + currentUser.getUserName() + "!");
        currentUser = null;
    }
}