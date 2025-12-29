package DbConnection;

import java.sql.*;

public class DatabaseConfig {
        static String url = "jdbc:mysql://localhost:3306/trading_app";
        static String username = "jaswanth";
        static String password = "root";
        static String query = "select * from users";
        static Connection con = null;

        public static Connection getConnection(){
            try{
                con = DriverManager.getConnection(url, username, password);
                System.out.println("Connection established ... \n");
            }catch (Exception e){
                e.printStackTrace();
            }
            return con;
        }

        public static void closeConnection(){
            try{
                if(con != null){
                    con.close();
                    System.out.println("Connection closed ... \n");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
}
