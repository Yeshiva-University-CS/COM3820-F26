package edu.yu.parallel;

import java.util.Map;

/**
 * The state of the entire brokerage as of a single instant.
 *
 * A snapshot must be consistent: it reflects every operation that completed
 * before that instant and no part of any operation that completed after it.
 * In particular, for every stock, the brokerage's inventory plus all client
 * positions equals the stock's starting share count, and the brokerage's cash
 * plus all client balances equals the sum of all registration deposits.
 *
 * All maps must be unmodifiable and must not change after the snapshot is
 * returned.
 *
 * @param portfolios     every registered client's portfolio, keyed by client id
 * @param inventory      the number of shares of each stock the brokerage holds,
 *                       keyed by symbol; every stock appears, including stocks
 *                       with zero shares left
 * @param brokerageCash  the brokerage's own cash: everything clients have paid
 *                       for shares minus everything the brokerage has paid
 *                       clients for shares. Starts at zero and may become
 *                       negative.
 */
public record BrokerageSnapshot(
        Map<String, Portfolio> portfolios,
        Map<String, Integer> inventory,
        double brokerageCash) {
}
