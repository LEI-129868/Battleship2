package restserver;

import battleship.IPosition;
import battleship.Position;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CallbackClient
 * Author: Tomas Costa (LEI-129836)
 * Generated: 2026-11-16
 *
 * Estimated cyclomatic complexities (including short-circuit decisions):
 * - CallbackClient(): 1
 * - sendShots(...): at least 9
 * - recordAiMove(...): at least 7
 *
 * A local JDK HTTP server simulates student callbacks. These tests are
 * local integration tests, without external network connections.
 */
public class CallbackClientTest {

    private CallbackClient client;
    private GameRegistry registry;
    private HttpServer server;
    private String baseUrl;
    private final AtomicReference<String> requestBody = new AtomicReference<>();
    private final AtomicReference<String> requestPath = new AtomicReference<>();
    private final AtomicReference<String> requestMethod = new AtomicReference<>();

    private volatile int status;
    private volatile String responseBody;

    @BeforeEach
    void setUp() throws IOException {
        client = new CallbackClient();
        registry = new GameRegistry();
        status = 200;
        responseBody = json("MISS", 5, "ONGOING");
        requestBody.set(null);
        requestPath.set(null);
        requestMethod.set(null);

        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", this::handle);
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterEach
    void tearDown() {
        if (server != null) server.stop(0);
        server = null;
        client = null;
        registry = null;
    }

    private void handle(HttpExchange exchange) throws IOException {
        requestPath.set(exchange.getRequestURI().getPath());
        requestMethod.set(exchange.getRequestMethod());
        requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
        byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (var output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private GameSession session() {
        return registry.createSession("Tomas", baseUrl);
    }

    private List<IPosition> oneShot() {
        return List.of(new Position('A', 1));
    }

    private String json(String outcome, int remaining, String gameStatus) {
        return "{\"results\":[{\"row\":\"A\",\"column\":1,\"outcome\":\""
                + outcome + "\"}],\"shipsRemaining\":" + remaining
                + ",\"gameStatus\":\"" + gameStatus + "\"}";
    }

    @Test
    @DisplayName("Constructor: CallbackClient is initialized")
    void constructor() {
        assertNotNull(client, "Error: expected initialized CallbackClient");
    }

    @Test
    @DisplayName("Send shots 1: successful HTTP 200 response")
    void sendShots1() {
        GameSession session = session();
        int before = session.getGame().getAlienMoves().size();
        assertTrue(client.sendShots(session, oneShot()),
                "Error: a successful HTTP 200 response should return true");
        assertAll(
                () -> assertEquals(before + 1, session.getGame().getAlienMoves().size(),
                        "Error: a move should have been recorded"),
                () -> assertEquals("POST", requestMethod.get(), "Error: expected POST"),
                () -> assertEquals("/game/" + session.getGameId() + "/shots", requestPath.get(),
                        "Error: incorrect callback endpoint"),
                () -> assertTrue(requestBody.get().contains("shots"),
                        "Error: request body should contain shots")
        );
    }

    @Test
    @DisplayName("Send shots 2: HTTP 400 returns false")
    void sendShots2() {
        status = 400;
        responseBody = "{\"error\":\"bad request\"}";
        assertFalse(client.sendShots(session(), oneShot()),
                "Error: HTTP 400 should return false");
    }

    @Test
    @DisplayName("Send shots 3: HTTP 500 returns false")
    void sendShots3() {
        status = 500;
        responseBody = "{\"error\":\"server error\"}";
        assertFalse(client.sendShots(session(), oneShot()),
                "Error: HTTP 500 should return false");
    }

    @Test
    @DisplayName("Send shots 4: malformed JSON returns false")
    void sendShots4() {
        responseBody = "{ broken json";
        assertFalse(client.sendShots(session(), oneShot()),
                "Error: malformed response JSON should return false");
    }

    @Test
    @DisplayName("Send shots 5: unavailable local server returns false")
    void sendShots5() {
        GameSession session = session();
        server.stop(0);
        server = null;
        assertFalse(client.sendShots(session, oneShot()),
                "Error: refused connection should return false");
    }

    @Test
    @DisplayName("Send shots 6: zero remaining ships marks AI victory")
    void sendShots6() {
        responseBody = json("SUNK", 0, "ONGOING");
        GameSession session = session();
        assertTrue(client.sendShots(session, oneShot()),
                "Error: valid response should return true");
        assertAll(
                () -> assertTrue(session.isGameOver(), "Error: AI victory should finish game"),
                () -> assertNotNull(session.getWinner(),
                        "Error: winner should be recorded after AI victory")
        );
    }

    @Test
    @DisplayName("Send shots 7: GAME_OVER marks AI victory")
    void sendShots7() {
        responseBody = json("HIT", 2, "GAME_OVER");
        GameSession session = session();
        assertTrue(client.sendShots(session, oneShot()),
                "Error: valid GAME_OVER response should return true");
        assertTrue(session.isGameOver(), "Error: GAME_OVER should finish game");
    }

    @Test
    @DisplayName("Record AI move 1: missing results does not add a move")
    void recordAiMove1() {
        responseBody = "{\"results\":null,\"shipsRemaining\":5,\"gameStatus\":\"ONGOING\"}";
        GameSession session = session();
        int before = session.getGame().getAlienMoves().size();
        assertTrue(client.sendShots(session, oneShot()),
                "Error: null results should be accepted by implementation");
        assertEquals(before, session.getGame().getAlienMoves().size(),
                "Error: null results should not record a move");
    }

    @Test
    @DisplayName("Record AI move 2: empty result list adds empty move")
    void recordAiMove2() {
        responseBody = "{\"results\":[],\"shipsRemaining\":5,\"gameStatus\":\"ONGOING\"}";
        GameSession session = session();
        int before = session.getGame().getAlienMoves().size();
        assertTrue(client.sendShots(session, oneShot()),
                "Error: empty results should be accepted");
        assertEquals(before + 1, session.getGame().getAlienMoves().size(),
                "Error: empty result list should still generate a move");
    }

    @Test
    @DisplayName("Record AI move 3: INVALID outcome is processed")
    void recordAiMove3() {
        responseBody = json("INVALID", 5, "ONGOING");
        GameSession session = session();
        assertTrue(client.sendShots(session, oneShot()),
                "Error: INVALID outcome should be processed");
        assertEquals(1, session.getGame().getAlienMoves().size(),
                "Error: INVALID outcome should record a move");
    }

    @Test
    @DisplayName("Record AI move 4: REPEATED outcome is processed")
    void recordAiMove4() {
        responseBody = json("REPEATED", 5, "ONGOING");
        GameSession session = session();
        assertTrue(client.sendShots(session, oneShot()),
                "Error: REPEATED outcome should be processed");
        assertEquals(1, session.getGame().getAlienMoves().size(),
                "Error: REPEATED outcome should record a move");
    }

    @Test
    @DisplayName("Record AI move 5: SUNK outcome is processed")
    void recordAiMove5() {
        responseBody = json("SUNK", 3, "ONGOING");
        GameSession session = session();
        assertTrue(client.sendShots(session, oneShot()),
                "Error: SUNK outcome should be processed");
        assertEquals(1, session.getGame().getAlienMoves().size(),
                "Error: SUNK outcome should record a move");
    }

    @Test
    @DisplayName("Record AI move 6: fewer results than shots")
    void recordAiMove6() {
        responseBody = json("MISS", 5, "ONGOING");
        GameSession session = session();
        List<IPosition> twoShots = new ArrayList<>();
        twoShots.add(new Position('A', 1));
        twoShots.add(new Position('B', 2));
        assertTrue(client.sendShots(session, twoShots),
                "Error: a shorter result list should not crash callback");
        assertEquals(1, session.getGame().getAlienMoves().size(),
                "Error: one move should be recorded");
    }

    @Test
    @DisplayName("Record AI move 7: no shots with empty results")
    void recordAiMove7() {
        responseBody = "{\"results\":[],\"shipsRemaining\":5,\"gameStatus\":\"ONGOING\"}";
        GameSession session = session();
        assertTrue(client.sendShots(session, List.of()),
                "Error: empty shots should be serializable");
        assertEquals(1, session.getGame().getAlienMoves().size(),
                "Error: empty move should be recorded");
    }
}
