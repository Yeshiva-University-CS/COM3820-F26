package edu.yu.parallel;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A price source the tests control. Prices can be set directly, or changed
 * continuously by a background "ticker" thread doing a random walk.
 */
final class TestPrices implements PriceSource {

    private final Map<String, Double> prices = new ConcurrentHashMap<>();
    private volatile Thread ticker;

    TestPrices set(String symbol, double price) {
        prices.put(symbol, price);
        return this;
    }

    @Override
    public double getStockPrice(String symbol) {
        Double price = symbol == null ? null : prices.get(symbol);
        if (price == null) {
            throw new IllegalArgumentException("Invalid symbol");
        }
        return price;
    }

    @Override
    public Map<String, Double> getStockPrices() {
        return Map.copyOf(prices);
    }

    /**
     * Start a daemon thread that, every periodMicros, moves one random stock's
     * price by up to 1% in either direction (rounded to cents, never below 1.00).
     */
    void startTicker(long periodMicros, long seed) {
        List<String> symbols = List.copyOf(prices.keySet());
        Random random = new Random(seed);
        Thread t = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                String symbol = symbols.get(random.nextInt(symbols.size()));
                double price = prices.get(symbol) * (1 + (random.nextDouble() - 0.5) * 0.02);
                prices.put(symbol, Math.max(1.00, Math.round(price * 100) / 100.0));
                long until = System.nanoTime() + periodMicros * 1_000;
                while (System.nanoTime() < until) {
                    Thread.onSpinWait();
                }
            }
        }, "test-price-ticker");
        t.setDaemon(true);
        t.start();
        ticker = t;
    }

    void stopTicker() {
        Thread t = ticker;
        if (t != null) {
            t.interrupt();
        }
    }
}
