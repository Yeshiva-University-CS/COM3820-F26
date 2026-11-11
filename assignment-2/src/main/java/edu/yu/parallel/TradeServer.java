package edu.yu.parallel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;

public class TradeServer {
    private final int brokerCount;
    private final HttpServer server;
    private final ExecutorService brokers;

    /**
     * Create a trade server
     *
     * @param port          the port to listen on; 0 picks any free port
     * @param brokerCount   the number of broker threads
     * @param brokerService the service that handles requests
     * @throws IOException if the port cannot be bound
     */
    public TradeServer(int port, int brokerCount, BrokerService brokerService) throws IOException {
        this.brokerCount = brokerCount;

        // Create and configure the HttpServer
        this.server =  HttpServer.create(new InetSocketAddress(port), 0);
        this.brokers = Executors.newFixedThreadPool(brokerCount);
        this.server.setExecutor(this.brokers);

        // set up the routes to the broker service
        this.server.createContext("/prices", brokerService::handleGetPrices);
        this.server.createContext("/register", brokerService::handleRegisterClient);
        this.server.createContext("/portfolio", brokerService::handleGetPortfolio);
        this.server.createContext("/marketvalue", brokerService::handleGetMarketValue);
        this.server.createContext("/buy", brokerService::handleBuyRequest);
        this.server.createContext("/sell", brokerService::handleSellRequest);
        this.server.createContext("/swap", brokerService::handleSwapRequest);
        this.server.createContext("/transfer", brokerService::handleTransferRequest);
    }
    
    /**
     * Starts the HttpServer
     */
    public void start() {
        this.server.start();
    }

    /**
     * Stops the HttpServer
     *
     * @param delay the maximum time in seconds to wait until exchanges have finished
     * @throws IllegalArgumentException if delay is less than zero
     */
    public void stop(int delay) {
        this.server.stop(delay);
        this.brokers.shutdown();
    }

    /**
     * Get the port the server is listening on
     * @return the port
     */
    public int getPort() {
        return server.getAddress().getPort();
    }

    /**
     * Get the number of broker threads
     * @return the number of broker threads
     */
    public int getBrokerCount() {
        return brokerCount;
    }
}
