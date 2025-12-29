package DAO;

import DbConnection.DatabaseConfig;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StockDAO {
    public List<String> getStocks(){
        List<String> stocks = new ArrayList<>();
        String query = "SELECT * FROM stocks order by stock_id";
        try(Connection con = DatabaseConfig.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(query);
        ){
            while (rs.next()){
                stocks.add(rs.getString(2));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stocks;
    }
}
