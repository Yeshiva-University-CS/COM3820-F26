package edu.yu.parallel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import edu.yu.parallel.Fixtures.Market;
import edu.yu.parallel.Impl.BrokerServiceImpl;

/**
 * Public functional tests (F5): every route over HTTP, with the status codes
 * specified in the BrokerService Javadoc.
 */
@Timeout(60)
class PublicHttpTest {

    private static final double CENT = 0.001;

    private Market market;
    private TradeServer server;
    private HttpClient client;
    private String base;

    @BeforeEach
    void setUp() throws Exception {
        market = new Market(
                "AAA", 12.34, 1_000,
                "BBB", 50.00, 500);
        server = new TradeServer(0, 4, new BrokerServiceImpl(market.brokerage));
        server.start();
        base = "http://localhost:" + server.getPort();
        client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    private HttpResponse<String> call(String method, String pathAndQuery) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(base + pathAndQuery))
                .timeout(Duration.ofSeconds(10))
                .method(method, HttpRequest.BodyPublishers.noBody())
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private JSONObject ok(String method, String pathAndQuery) throws Exception {
        HttpResponse<String> r = call(method, pathAndQuery);
        assertEquals(200, r.statusCode(), method + " " + pathAndQuery + " returned " + r.body());
        return new JSONObject(r.body());
    }

    private void status(int expected, String method, String pathAndQuery) throws Exception {
        assertEquals(expected, call(method, pathAndQuery).statusCode(), method + " " + pathAndQuery);
    }

    @Test
    @DisplayName("F5a: GET /prices returns every symbol and price")
    void prices() throws Exception {
        HttpResponse<String> r = call("GET", "/prices");
        assertEquals(200, r.statusCode());
        JSONArray array = new JSONArray(r.body());
        assertEquals(2, array.length(), "one entry per stock");
        boolean found = false;
        for (int i = 0; i < array.length(); i++) {
            JSONObject o = array.getJSONObject(i);
            if (o.getString("symbol").equals("AAA")) {
                assertEquals(12.34, o.getDouble("price"), CENT);
                found = true;
            }
        }
        assertTrue(found, "AAA missing from /prices");
    }

    @Test
    @DisplayName("F5b: register, buy, sell, portfolio and market value")
    void tradingRoutes() throws Exception {
        JSONObject p = ok("POST", "/register?clientId=alice&funds=1000");
        assertEquals("alice", p.getString("clientId"));
        assertEquals(1000.00, p.getDouble("accountBalance"), CENT);
        assertEquals(0, p.getJSONObject("stockPositions").length(), "no positions yet");

        p = ok("POST", "/buy?clientId=alice&symbol=AAA&shares=10");
        assertEquals(876.60, p.getDouble("accountBalance"), CENT);
        assertEquals(10, p.getJSONObject("stockPositions").getInt("AAA"));

        p = ok("POST", "/sell?clientId=alice&symbol=AAA&shares=4");
        assertEquals(6, p.getJSONObject("stockPositions").getInt("AAA"));

        p = ok("GET", "/portfolio?clientId=alice");
        assertEquals(876.60 + 4 * 12.34, p.getDouble("accountBalance"), CENT);

        JSONObject mv = ok("GET", "/marketvalue?clientId=alice");
        assertEquals(6 * 12.34, mv.getDouble("marketvalue"), CENT);
    }

    @Test
    @DisplayName("F5c: swap and transfer")
    void swapAndTransfer() throws Exception {
        ok("POST", "/register?clientId=alice&funds=1000");
        ok("POST", "/register?clientId=bob&funds=0");
        ok("POST", "/buy?clientId=alice&symbol=AAA&shares=10");

        JSONObject p = ok("POST", "/swap?clientId=alice&sellSymbol=AAA&sellShares=5&buySymbol=BBB&buyShares=1");
        assertEquals(5, p.getJSONObject("stockPositions").getInt("AAA"));
        assertEquals(1, p.getJSONObject("stockPositions").getInt("BBB"));

        p = ok("POST", "/transfer?fromClientId=alice&toClientId=bob&symbol=AAA&shares=2");
        assertEquals("alice", p.getString("clientId"));
        assertEquals(3, p.getJSONObject("stockPositions").getInt("AAA"));
        assertEquals(2, ok("GET", "/portfolio?clientId=bob").getJSONObject("stockPositions").getInt("AAA"));
    }

    @Test
    @DisplayName("F5d: rejected requests return 400")
    void badRequests() throws Exception {
        ok("POST", "/register?clientId=alice&funds=100");
        status(400, "POST", "/register?clientId=alice&funds=100");        // duplicate
        status(400, "GET", "/portfolio?clientId=nobody");                  // unregistered
        status(400, "GET", "/marketvalue?clientId=nobody");
        status(400, "POST", "/buy?clientId=alice&symbol=ZZZ&shares=1");   // invalid symbol
        status(400, "POST", "/buy?clientId=alice&symbol=AAA&shares=0");   // not positive
        status(400, "POST", "/buy?clientId=alice&symbol=BBB&shares=3");   // insufficient funds
        status(400, "POST", "/sell?clientId=alice&symbol=AAA&shares=1");  // insufficient shares
        status(400, "POST", "/swap?clientId=alice&sellSymbol=AAA&sellShares=1&buySymbol=BBB&buyShares=1");
        status(400, "POST", "/transfer?fromClientId=alice&toClientId=alice&symbol=AAA&shares=1");
        assertEquals(100.00, ok("GET", "/portfolio?clientId=alice").getDouble("accountBalance"), CENT,
                "rejected requests changed the account");
    }

    @Test
    @DisplayName("F5e: the wrong HTTP method returns 405")
    void wrongMethod() throws Exception {
        status(405, "POST", "/prices");
        status(405, "GET", "/register?clientId=alice&funds=100");
        status(405, "POST", "/portfolio?clientId=alice");
        status(405, "POST", "/marketvalue?clientId=alice");
        status(405, "GET", "/buy?clientId=alice&symbol=AAA&shares=1");
        status(405, "GET", "/sell?clientId=alice&symbol=AAA&shares=1");
        status(405, "GET", "/swap?clientId=alice&sellSymbol=AAA&sellShares=1&buySymbol=BBB&buyShares=1");
        status(405, "GET", "/transfer?fromClientId=alice&toClientId=bob&symbol=AAA&shares=1");
        status(400, "GET", "/portfolio?clientId=alice"); // the GET with the wrong method did not register alice
    }
}
