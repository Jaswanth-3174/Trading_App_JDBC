package trading;

import java.util.HashMap;
import java.util.Map;

public class StockPortfolio {
    private Map<String, StockHolding> holdings;

    public StockPortfolio() {
        this.holdings = new HashMap<>();
    }
}