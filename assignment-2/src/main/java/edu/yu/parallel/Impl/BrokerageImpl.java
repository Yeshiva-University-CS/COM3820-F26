package edu.yu.parallel.Impl;

import java.util.List;
import java.util.Map;

import edu.yu.parallel.Brokerage;
import edu.yu.parallel.BrokerageSnapshot;
import edu.yu.parallel.Portfolio;
import edu.yu.parallel.PriceSource;
import edu.yu.parallel.Stock;

final public class BrokerageImpl implements Brokerage {

    /**
     * Constructor
     * 
     * @param stocks      list of Stock objects; each stock's share count is the
     *                    brokerage's starting inventory of that stock
     * @param priceSource the current price of each stock, which may change at
     *                    any moment
     */
    public BrokerageImpl(List<Stock> stocks, PriceSource priceSource) {
        throw new UnsupportedOperationException("Implement this");
    }

    @Override
    public Portfolio registerClient(String clientId, double initialBalance) {
        throw new UnsupportedOperationException("Implement this");
    }

    @Override
    public Portfolio buyShares(String clientId, String symbol, int shares) {
        throw new UnsupportedOperationException("Implement this");
    }

    @Override
    public Portfolio sellShares(String clientId, String symbol, int shares) {
        throw new UnsupportedOperationException("Implement this");
    }

    @Override
    public Portfolio swap(String clientId, String sellSymbol, int sellShares, String buySymbol, int buyShares) {
        throw new UnsupportedOperationException("Implement this");
    }

    @Override
    public Portfolio transfer(String fromClientId, String toClientId, String symbol, int shares) {
        throw new UnsupportedOperationException("Implement this");
    }

    @Override
    public Portfolio getClientPortfolio(String clientId) {
        throw new UnsupportedOperationException("Implement this");
    }

    @Override
    public double getMarketValue(String clientId) {
        throw new UnsupportedOperationException("Implement this");
    }

    @Override
    public Map<String, Double> getStockPrices() {
        throw new UnsupportedOperationException("Implement this");
    }

    @Override
    public BrokerageSnapshot snapshot() {
        throw new UnsupportedOperationException("Implement this");
    }
}
