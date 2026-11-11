package edu.yu.parallel;

import java.util.Map;

/**
 * A source of current stock prices. Prices may change at any moment, including
 * in the middle of a trade.
 *
 * {@link StockPrices} is the production implementation. Tests supply their own
 * implementations (for example, frozen or rapidly changing prices), so the
 * brokerage must depend only on this interface.
 */
public interface PriceSource {

    /**
     * Get the current price of a stock
     *
     * @param symbol the stock's symbol
     * @return the stock's current price
     * @throws IllegalArgumentException if the symbol is invalid
     */
    double getStockPrice(String symbol);

    /**
     * Get the current prices of all stocks
     *
     * @return an unmodifiable map of stock symbols to prices
     */
    Map<String, Double> getStockPrices();
}
