package edu.yu.parallel;

import static edu.yu.parallel.Fixtures.position;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.yu.parallel.Brokerage.InsufficientFundsException;
import edu.yu.parallel.Brokerage.InsufficientSharesException;
import edu.yu.parallel.Fixtures.Market;

/**
 * Public functional tests (F1-F4, F6): single-threaded behavior of the
 * Brokerage, as specified in its Javadoc.
 */
class PublicFunctionalTest {

    private static final double CENT = 0.001;

    private Market market;
    private Brokerage brokerage;

    @BeforeEach
    void setUp() {
        market = new Market(
                "AAA", 12.34, 1_000,
                "BBB", 50.00, 500,
                "CCC", 0.10, 100);
        brokerage = market.brokerage;
    }

    /** Asserts the operation throws the given exception and changes nothing. */
    private void assertRejected(Class<? extends Throwable> type, Runnable op, String what) {
        BrokerageSnapshot before = brokerage.snapshot();
        assertThrows(type, op::run, what);
        BrokerageSnapshot after = brokerage.snapshot();
        assertEquals(before.inventory(), after.inventory(), what + ": inventory changed");
        assertEquals(before.brokerageCash(), after.brokerageCash(), CENT, what + ": brokerage cash changed");
        for (String id : before.portfolios().keySet()) {
            Portfolio b = before.portfolios().get(id);
            Portfolio a = after.portfolios().get(id);
            assertEquals(b.getBalance(), a.getBalance(), CENT, what + ": balance of " + id + " changed");
            assertEquals(b.getStockPositions(), a.getStockPositions(), what + ": positions of " + id + " changed");
        }
    }

    // ---------------------------------------------------------------- F1

    @Test
    @DisplayName("F1a: registering returns an empty portfolio with the deposit")
    void registerReturnsPortfolio() {
        Portfolio p = market.register("alice", 1_000.00);
        assertEquals("alice", p.getClientId());
        assertEquals(1_000.00, p.getBalance(), CENT);
        assertTrue(p.getStockPositions().isEmpty(), "a new client holds no stock");

        Portfolio again = brokerage.getClientPortfolio("alice");
        assertEquals(1_000.00, again.getBalance(), CENT);
    }

    @Test
    @DisplayName("F1b: duplicate and unknown client ids are rejected")
    void registrationErrors() {
        market.register("alice", 1_000.00);
        assertThrows(IllegalArgumentException.class, () -> brokerage.registerClient("alice", 5.00),
                "registering the same id twice");
        assertThrows(IllegalArgumentException.class, () -> brokerage.registerClient("bob", -1.00),
                "negative initial balance");
        assertThrows(IllegalArgumentException.class, () -> brokerage.getClientPortfolio("nobody"),
                "portfolio of an unregistered client");
        assertEquals(1_000.00, brokerage.getClientPortfolio("alice").getBalance(), CENT);
    }

    @Test
    @DisplayName("F1c: a snapshot lists every client and every stock")
    void snapshotListsEverything() {
        market.register("alice", 10.00);
        market.register("bob", 20.00);
        BrokerageSnapshot snap = brokerage.snapshot();
        assertEquals(2, snap.portfolios().size(), "number of clients in snapshot");
        assertEquals(Map.of("AAA", 1_000, "BBB", 500, "CCC", 100), snap.inventory());
        assertEquals(0.00, snap.brokerageCash(), CENT, "brokerage cash starts at zero");
    }

    // ---------------------------------------------------------------- F2

    @Test
    @DisplayName("F2a: buying moves shares and exact cents")
    void buy() {
        market.register("alice", 1_000.00);
        Portfolio p = brokerage.buyShares("alice", "AAA", 10);
        assertEquals(876.60, p.getBalance(), CENT, "1000.00 - 10 x 12.34");
        assertEquals(Map.of("AAA", 10), p.getStockPositions());

        BrokerageSnapshot snap = brokerage.snapshot();
        assertEquals(990, (int) snap.inventory().get("AAA"), "inventory after buying 10");
        assertEquals(123.40, snap.brokerageCash(), CENT, "brokerage cash after the sale");
        market.assertConserved(snap, "after a buy");
    }

    @Test
    @DisplayName("F2b: selling uses the price at the time of the sale")
    void sell() {
        market.register("alice", 1_000.00);
        brokerage.buyShares("alice", "AAA", 10);
        market.prices.set("AAA", 13.01);
        Portfolio p = brokerage.sellShares("alice", "AAA", 4);
        assertEquals(876.60 + 52.04, p.getBalance(), CENT, "proceeds of 4 x 13.01");
        assertEquals(Map.of("AAA", 6), p.getStockPositions());

        Portfolio all = brokerage.sellShares("alice", "AAA", 6);
        assertTrue(all.getStockPositions().isEmpty(), "a position sold to zero disappears");
        market.assertConserved(brokerage.snapshot(), "after sells");
    }

    @Test
    @DisplayName("F2c: amounts are exact to the cent")
    void cents() {
        market.register("alice", 1.00);
        Portfolio p = null;
        for (int i = 0; i < 10; i++) {
            p = brokerage.buyShares("alice", "CCC", 1);
        }
        assertEquals(0.00, p.getBalance(), CENT, "ten purchases at 0.10 from 1.00");
        assertEquals(10, position(p, "CCC"));
        assertThrows(InsufficientFundsException.class, () -> brokerage.buyShares("alice", "CCC", 1),
                "nothing left to spend");
    }

    @Test
    @DisplayName("F2d: market value and prices")
    void marketValue() {
        market.register("alice", 10_000.00);
        brokerage.buyShares("alice", "AAA", 10);
        brokerage.buyShares("alice", "BBB", 3);
        assertEquals(10 * 12.34 + 3 * 50.00, brokerage.getMarketValue("alice"), CENT);
        market.prices.set("BBB", 40.00);
        assertEquals(10 * 12.34 + 3 * 40.00, brokerage.getMarketValue("alice"), CENT,
                "market value uses current prices");
        assertEquals(Map.of("AAA", 12.34, "BBB", 40.00, "CCC", 0.10), brokerage.getStockPrices());
        assertThrows(IllegalArgumentException.class, () -> brokerage.getMarketValue("nobody"));
    }

    // ---------------------------------------------------------------- F3

    @Test
    @DisplayName("F3a: invalid arguments are rejected and change nothing")
    void invalidArguments() {
        market.register("alice", 1_000.00);
        assertRejected(IllegalArgumentException.class, () -> brokerage.buyShares("alice", "ZZZ", 1), "invalid symbol");
        assertRejected(IllegalArgumentException.class, () -> brokerage.buyShares("alice", "AAA", 0), "zero shares");
        assertRejected(IllegalArgumentException.class, () -> brokerage.buyShares("alice", "AAA", -5), "negative shares");
        assertRejected(IllegalArgumentException.class, () -> brokerage.buyShares("nobody", "AAA", 1), "unregistered buyer");
        assertRejected(IllegalArgumentException.class, () -> brokerage.sellShares("alice", "ZZZ", 1), "invalid symbol on sell");
        assertRejected(IllegalArgumentException.class, () -> brokerage.sellShares("alice", "AAA", 0), "zero shares on sell");
        assertRejected(IllegalArgumentException.class, () -> brokerage.sellShares("nobody", "AAA", 1), "unregistered seller");
    }

    @Test
    @DisplayName("F3b: insufficient funds and shares are rejected and change nothing")
    void insufficient() {
        market.register("alice", 100.00);
        assertRejected(InsufficientFundsException.class, () -> brokerage.buyShares("alice", "BBB", 3),
                "3 x 50.00 with 100.00");
        market.register("rich", 1_000_000.00);
        assertRejected(InsufficientSharesException.class, () -> brokerage.buyShares("rich", "CCC", 101),
                "more than the brokerage holds");
        assertRejected(InsufficientSharesException.class, () -> brokerage.sellShares("alice", "AAA", 1),
                "selling stock the client does not hold");
        brokerage.buyShares("alice", "AAA", 2);
        assertRejected(InsufficientSharesException.class, () -> brokerage.sellShares("alice", "AAA", 3),
                "selling more than the client holds");
    }

    // ---------------------------------------------------------------- F4

    @Test
    @DisplayName("F4a: swap sells one stock and buys another, using the proceeds")
    void swap() {
        market.register("alice", 10.00);
        market.prices.set("AAA", 1.00);
        brokerage.buyShares("alice", "AAA", 10); // balance 0.00
        Portfolio p = brokerage.swap("alice", "AAA", 10, "CCC", 50); // +10.00, -5.00
        assertEquals(5.00, p.getBalance(), CENT);
        assertEquals(Map.of("CCC", 50), p.getStockPositions());
        market.assertConserved(brokerage.snapshot(), "after a swap");
    }

    @Test
    @DisplayName("F4b: a swap that cannot complete changes nothing")
    void swapFailures() {
        market.register("alice", 100.00);
        brokerage.buyShares("alice", "AAA", 5);
        assertRejected(InsufficientSharesException.class, () -> brokerage.swap("alice", "AAA", 6, "CCC", 1),
                "selling more than held");
        assertRejected(InsufficientSharesException.class, () -> brokerage.swap("alice", "AAA", 1, "CCC", 101),
                "buying more than the brokerage holds");
        assertRejected(InsufficientFundsException.class, () -> brokerage.swap("alice", "AAA", 1, "BBB", 3),
                "balance plus proceeds short of the cost");
        assertRejected(IllegalArgumentException.class, () -> brokerage.swap("alice", "AAA", 1, "AAA", 1),
                "swapping a stock for itself");
        assertRejected(IllegalArgumentException.class, () -> brokerage.swap("alice", "AAA", 0, "CCC", 1),
                "zero shares");
        assertRejected(IllegalArgumentException.class, () -> brokerage.swap("alice", "AAA", 1, "ZZZ", 1),
                "invalid symbol");
        assertRejected(IllegalArgumentException.class, () -> brokerage.swap("nobody", "AAA", 1, "CCC", 1),
                "unregistered client");
    }

    @Test
    @DisplayName("F4c: transfer moves shares between clients, no cash")
    void transfer() {
        market.register("alice", 1_000.00);
        market.register("bob", 0.00);
        brokerage.buyShares("alice", "AAA", 10);
        Portfolio from = brokerage.transfer("alice", "bob", "AAA", 4);
        assertEquals("alice", from.getClientId(), "transfer returns the giver's portfolio");
        assertEquals(6, position(from, "AAA"));
        Portfolio bob = brokerage.getClientPortfolio("bob");
        assertEquals(Map.of("AAA", 4), bob.getStockPositions());
        assertEquals(0.00, bob.getBalance(), CENT, "no cash moves");
        market.assertConserved(brokerage.snapshot(), "after a transfer");
    }

    @Test
    @DisplayName("F4d: a transfer that cannot complete changes nothing")
    void transferFailures() {
        market.register("alice", 1_000.00);
        market.register("bob", 0.00);
        brokerage.buyShares("alice", "AAA", 3);
        assertRejected(InsufficientSharesException.class, () -> brokerage.transfer("alice", "bob", "AAA", 4),
                "giving more than held");
        assertRejected(IllegalArgumentException.class, () -> brokerage.transfer("alice", "alice", "AAA", 1),
                "transfer to self");
        assertRejected(IllegalArgumentException.class, () -> brokerage.transfer("alice", "nobody", "AAA", 1),
                "unregistered receiver");
        assertRejected(IllegalArgumentException.class, () -> brokerage.transfer("nobody", "bob", "AAA", 1),
                "unregistered giver");
        assertRejected(IllegalArgumentException.class, () -> brokerage.transfer("alice", "bob", "ZZZ", 1),
                "invalid symbol");
        assertRejected(IllegalArgumentException.class, () -> brokerage.transfer("alice", "bob", "AAA", 0),
                "zero shares");
    }

    // ---------------------------------------------------------------- F6

    @Test
    @DisplayName("F6: a returned portfolio never changes")
    void portfolioImmutable() {
        market.register("alice", 1_000.00);
        Portfolio held = brokerage.buyShares("alice", "AAA", 1);
        Map<String, Integer> positions = held.getStockPositions();

        brokerage.buyShares("alice", "AAA", 5);
        brokerage.buyShares("alice", "BBB", 1);

        assertEquals(1_000.00 - 12.34, held.getBalance(), CENT, "balance of an old portfolio changed");
        assertEquals(Map.of("AAA", 1), held.getStockPositions(), "positions of an old portfolio changed");
        assertEquals(Map.of("AAA", 1), positions, "a positions map handed out earlier changed");

        try {
            positions.put("AAA", 1_000);
        } catch (UnsupportedOperationException expected) {
            // an unmodifiable map is the preferred design
        }
        assertEquals(6, position(brokerage.getClientPortfolio("alice"), "AAA"),
                "modifying a returned map changed the account");
    }
}
