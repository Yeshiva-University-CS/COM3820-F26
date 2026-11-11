package edu.yu.parallel;

import java.util.Map;

/**
 * A brokerage holds client accounts (a cash balance and stock positions) and
 * its own inventory of shares. Clients buy shares from, and sell shares to, the
 * brokerage's inventory at the current market price.
 *
 * Every method may be called concurrently by any number of threads.
 *
 * Rules that apply to every method:
 * <ul>
 * <li>Money is held internally as whole cents. Every {@code double} amount
 * accepted or returned is a number of cents divided by 100. Amounts accepted
 * as {@code double} are rounded to the nearest cent.</li>
 * <li>A trade reads each stock's price exactly once. The funds check, the
 * debit or credit, and the portfolio returned all use that price.</li>
 * <li>Every returned {@link Portfolio} is an immutable snapshot of the
 * client's account at one instant. Its stock positions contain only stocks the
 * client holds at least one share of.</li>
 * <li>An operation that throws changes nothing.</li>
 * <li>For every stock, the brokerage's inventory plus all client positions
 * always equals the stock's starting share count. The brokerage's cash plus
 * all client balances always equals the sum of all registration deposits.</li>
 * </ul>
 */
public interface Brokerage {

    public class InsufficientSharesException extends RuntimeException {
    }

    public class InsufficientFundsException extends RuntimeException {
    }

    /**
     * Register a client with the brokerage
     *
     * @param clientId       the id of the client
     * @param initialBalance the initial cash balance of the client
     * @return the client's updated portfolio information
     * @throws IllegalArgumentException if the client is already registered, or
     *                                  the initial balance is negative
     */
    Portfolio registerClient(String clientId, double initialBalance);

    /**
     * Buy shares of a stock for a client
     *
     * @param clientId the id of the client
     * @param symbol   the symbol of the stock
     * @param shares   the number of shares to buy
     * @return the client's updated portfolio information
     * @throws IllegalArgumentException    if the client is not registered, or the
     *                                     symbol is invalid, or the shares is not
     *                                     positive
     * @throws InsufficientSharesException if there are not enough shares available
     * @throws InsufficientFundsException  if the client does not have enough funds
     */
    Portfolio buyShares(String clientId, String symbol, int shares);

    /**
     * Sell shares of a stock for a client
     *
     * @param clientId the client id of the client
     * @param symbol   the symbol of the stock to sell
     * @param shares   the number of shares to sell
     * @return the client's updated portfolio information
     * @throws IllegalArgumentException    if the client is not registered, or the
     *                                     symbol is invalid, or the shares is not
     *                                     positive
     * @throws InsufficientSharesException if the client does not have enough shares
     *                                     to sell
     */
    Portfolio sellShares(String clientId, String symbol, int shares);

    /**
     * Sell shares of one stock and buy shares of another as a single atomic
     * operation: either both legs happen or neither does. The proceeds of the
     * sale count toward the purchase.
     *
     * @param clientId   the id of the client
     * @param sellSymbol the symbol of the stock to sell
     * @param sellShares the number of shares to sell
     * @param buySymbol  the symbol of the stock to buy
     * @param buyShares  the number of shares to buy
     * @return the client's updated portfolio information
     * @throws IllegalArgumentException    if the client is not registered, or
     *                                     either symbol is invalid, or the two
     *                                     symbols are the same, or either share
     *                                     count is not positive
     * @throws InsufficientSharesException if the client does not have enough
     *                                     shares to sell, or the brokerage does
     *                                     not have enough shares to buy
     * @throws InsufficientFundsException  if the client's balance plus the sale
     *                                     proceeds does not cover the purchase
     */
    Portfolio swap(String clientId, String sellSymbol, int sellShares, String buySymbol, int buyShares);

    /**
     * Move shares of a stock from one client to another as a single atomic
     * operation. No cash changes hands.
     *
     * @param fromClientId the id of the client giving the shares
     * @param toClientId   the id of the client receiving the shares
     * @param symbol       the symbol of the stock
     * @param shares       the number of shares to move
     * @return the giving client's updated portfolio information
     * @throws IllegalArgumentException    if either client is not registered, or
     *                                     the two clients are the same, or the
     *                                     symbol is invalid, or the shares is not
     *                                     positive
     * @throws InsufficientSharesException if the giving client does not have
     *                                     enough shares
     */
    Portfolio transfer(String fromClientId, String toClientId, String symbol, int shares);

    /**
     * Get the current portfolio of a client
     *
     * @param clientId the id of the client
     * @return the client's updated portfolio information
     * @throws IllegalArgumentException if the client is not registered
     */
    Portfolio getClientPortfolio(String clientId);

    /**
     * Get the market value of a client's stock positions: the sum over the
     * client's positions of shares times current price. The positions come
     * from a single instant of the client's account, and the prices from a
     * single call to {@link PriceSource#getStockPrices()}. Cash is not
     * included.
     *
     * @param clientId the id of the client
     * @return the market value, rounded to the nearest cent
     * @throws IllegalArgumentException if the client is not registered
     */
    double getMarketValue(String clientId);

    /**
     * Get the current prices of all stocks
     *
     * @return a map of stock symbols to prices
     */
    Map<String, Double> getStockPrices();

    /**
     * Get a consistent snapshot of the entire brokerage as of a single instant,
     * while other operations continue. See {@link BrokerageSnapshot}.
     *
     * @return the snapshot
     */
    BrokerageSnapshot snapshot();

}
