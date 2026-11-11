package edu.yu.parallel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.yu.parallel.Impl.BrokerageImpl;

/** Shared setup and invariant checks for the public tests. */
final class Fixtures {

    private Fixtures() {
    }

    /** A brokerage plus what it started with, for conservation checks. */
    static final class Market {
        final TestPrices prices = new TestPrices();
        final Map<String, Integer> startingShares = new LinkedHashMap<>();
        final Brokerage brokerage;
        private long depositsCents = 0;

        Market(Object... symbolPriceShares) {
            List<Stock> stocks = new ArrayList<>();
            for (int i = 0; i < symbolPriceShares.length; i += 3) {
                String symbol = (String) symbolPriceShares[i];
                double price = ((Number) symbolPriceShares[i + 1]).doubleValue();
                int shares = ((Number) symbolPriceShares[i + 2]).intValue();
                prices.set(symbol, price);
                startingShares.put(symbol, shares);
                stocks.add(new TestStock(symbol, symbol + " Inc", price, shares));
            }
            brokerage = new BrokerageImpl(stocks, prices);
        }

        Portfolio register(String clientId, double balance) {
            Portfolio p = brokerage.registerClient(clientId, balance);
            synchronized (this) {
                depositsCents += cents(balance);
            }
            return p;
        }

        synchronized long depositsCents() {
            return depositsCents;
        }

        /** Fails unless the snapshot conserves shares and cash exactly, with nothing negative. */
        void assertConserved(BrokerageSnapshot snap, String when) {
            Fixtures.assertConserved(snap, startingShares, depositsCents(), when);
        }
    }

    static long cents(double amount) {
        return Math.round(amount * 100);
    }

    static void assertConserved(BrokerageSnapshot snap, Map<String, Integer> startingShares,
            long depositsCents, String when) {
        Map<String, Long> held = new LinkedHashMap<>();
        long cash = cents(snap.brokerageCash());
        for (Portfolio p : snap.portfolios().values()) {
            if (p.getBalance() < 0) {
                fail(when + ": client " + p.getClientId() + " has a negative balance " + p.getBalance());
            }
            cash += cents(p.getBalance());
            for (Map.Entry<String, Integer> pos : p.getStockPositions().entrySet()) {
                if (pos.getValue() < 0) {
                    fail(when + ": client " + p.getClientId() + " holds " + pos.getValue() + " " + pos.getKey());
                }
                held.merge(pos.getKey(), (long) pos.getValue(), Long::sum);
            }
        }
        for (Map.Entry<String, Integer> start : startingShares.entrySet()) {
            String symbol = start.getKey();
            Integer inv = snap.inventory().get(symbol);
            assertTrue(inv != null, when + ": snapshot inventory is missing " + symbol);
            assertTrue(inv >= 0, when + ": inventory of " + symbol + " is negative: " + inv);
            long total = inv + held.getOrDefault(symbol, 0L);
            assertEquals((long) start.getValue(), total,
                    when + ": shares of " + symbol + " (inventory + client positions) not conserved");
        }
        assertEquals(depositsCents, cash,
                when + ": cash in cents (brokerage + all clients) not conserved");
    }

    static int position(Portfolio p, String symbol) {
        return p.getStockPositions().getOrDefault(symbol, 0);
    }
}
