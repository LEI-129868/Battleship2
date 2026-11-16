
package restserver;

import battleship.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for CallbackClient.
 * Author: Tomas Costa (LEI-129836)
 * Date: 2026-11-16 12:52
 *
 * Cyclomatic Complexity (estimated):
 * - Constructor: 1
 * - sendShots(): 9 (including exception paths)
 * - recordAiMove(): 7 (compound conditions)
 *
 * recordAiMove() is tested indirectly through sendShots().
 *
 * Mockito is used to simulate HTTP responses without
 * connecting to an external server.
 */
public class CallbackClientTest {

    private CallbackClient client;
    private HttpClient http;
    private GameSession session;
    private IGame game;
    private List<IPosition> shots;

    @BeforeEach
    void setUp() throws Exception {
        client = new CallbackClient();

        http = mock(HttpClient.class);
        session = mock(GameSession.class);
        game = mock(IGame.class);

        injectField("http", http);

        when(session.getCallbackUrl())
                .thenReturn("http://localhost:8081");
        when(session.getGameId()).thenReturn("test-game");
        when(session.getPlayerName()).thenReturn("Tomas");
        when(session.getGame()).thenReturn(game);

        when(game.getAlienMoves())
                .thenReturn(new ArrayList<>());

        shots = new ArrayList<>();
        shots.add(new Position(1, 1));
    }

    @AfterEach
    void tearDown() {
        client = null;
        http = null;
        session = null;
        game = null;
        shots = null;
    }

    private void injectField(String name, Object value)
            throws Exception {
        Field field = CallbackClient.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(client, value);
    }

    @SuppressWarnings("unchecked")
    private void mockResponse(int status, String body)
            throws Exception {

        HttpResponse<String> response = mock(HttpResponse.class);

        when(response.statusCode()).thenReturn(status);
        when(response.body()).thenReturn(body);

        when(http.send(
                any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class)
        )).thenReturn(response);
    }

    private String responseJson(String outcome,
                                int remaining,
                                String status) {
        return """
                {
                  "results": [
                    {"row":"A","column":1,"outcome":"%s"}
                  ],
                  "shipsRemaining":%d,
                  "gameStatus":"%s"
                }
                """.formatted(outcome, remaining, status);
    }

    @Test
    @DisplayName("Constructor - HTTP client and mapper initialized")
    void constructor() throws Exception {
        CallbackClient newClient = new CallbackClient();

        Field httpField =
                CallbackClient.class.getDeclaredField("http");
        Field mapperField =
                CallbackClient.class.getDeclaredField("mapper");

        httpField.setAccessible(true);
        mapperField.setAccessible(true);

        assertAll(
                () -> assertNotNull(newClient,
                        "Error: CallbackClient should not be null."),
                () -> assertNotNull(httpField.get(newClient),
                        "Error: HTTP client should be initialized."),
                () -> assertNotNull(mapperField.get(newClient),
                        "Error: ObjectMapper should be initialized.")
        );
    }

    @Test
    @DisplayName("SendShots 1 - Successful HTTP request")
    void sendShots1() throws Exception {
        mockResponse(200, responseJson("MISS", 5, "ONGOING"));

        boolean result = client.sendShots(session, shots);

        assertTrue(result,
                "Error: HTTP 200 with valid JSON should succeed.");

        assertEquals(1, game.getAlienMoves().size(),
                "Error: Successful callback should record one move.");
    }

    @Test
    @DisplayName("SendShots 2 - HTTP 400 failure")
    void sendShots2() throws Exception {
        mockResponse(400, "Bad Request");

        boolean result = client.sendShots(session, shots);

        assertFalse(result,
                "Error: HTTP 400 should return false.");
    }

    @Test
    @DisplayName("SendShots 3 - HTTP 500 failure")
    void sendShots3() throws Exception {
        mockResponse(500, "Internal Server Error");

        boolean result = client.sendShots(session, shots);

        assertFalse(result,
                "Error: HTTP 500 should return false.");
    }

    @Test
    @DisplayName("SendShots 4 - HTTP IOException")
    void sendShots4() throws Exception {
        when(http.send(
                any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class)
        )).thenThrow(new IOException("Connection refused"));

        boolean result = client.sendShots(session, shots);

        assertFalse(result,
                "Error: IOException should return false.");
    }

    @Test
    @DisplayName("SendShots 5 - Interrupted HTTP request")
    void sendShots5() throws Exception {
        when(http.send(
                any(HttpRequest.class),
                any(HttpResponse.BodyHandler.class)
        )).thenThrow(new InterruptedException("Interrupted"));

        boolean result = client.sendShots(session, shots);

        assertFalse(result,
                "Error: Interrupted request should return false.");
    }

    @Test
    @DisplayName("SendShots 6 - Invalid JSON response")
    void sendShots6() throws Exception {
        mockResponse(200, "{ invalid json }");

        boolean result = client.sendShots(session, shots);

        assertFalse(result,
                "Error: Invalid JSON should return false.");
    }

    @Test
    @DisplayName("SendShots 7 - AI wins with zero remaining ships")
    void sendShots7() throws Exception {
        mockResponse(200, responseJson("SUNK", 0, "ONGOING"));

        boolean result = client.sendShots(session, shots);

        assertTrue(result,
                "Error: Successful callback should return true.");

        verify(session).markAiWins();
    }

    @Test
    @DisplayName("SendShots 8 - AI wins with GAME_OVER status")
    void sendShots8() throws Exception {
        mockResponse(200, responseJson("SUNK", 2, "GAME_OVER"));

        boolean result = client.sendShots(session, shots);

        assertTrue(result,
                "Error: Valid GAME_OVER response should succeed.");

        verify(session).markAiWins();
    }

    @Test
    @DisplayName("SendShots 9 - Serialization IOException")
    void sendShots9() throws Exception {
        ObjectMapper mapper = mock(ObjectMapper.class);
        injectField("mapper", mapper);

        when(mapper.writeValueAsString(any()))
                .thenThrow(new IOException("Serialization failed"));

        boolean result = client.sendShots(session, shots);

        assertFalse(result,
                "Error: Serialization failure should return false.");

        verifyNoInteractions(http);
    }

    @Test
    @DisplayName("RecordAiMove 1 - MISS outcome")
    void recordAiMove1() throws Exception {
        mockResponse(200, responseJson("MISS", 5, "ONGOING"));

        assertTrue(client.sendShots(session, shots),
                "Error: Callback should succeed.");

        assertEquals(1, game.getAlienMoves().size(),
                "Error: MISS should be recorded in alien moves.");
    }

    @Test
    @DisplayName("RecordAiMove 2 - HIT outcome")
    void recordAiMove2() throws Exception {
        mockResponse(200, responseJson("HIT", 5, "ONGOING"));

        assertTrue(client.sendShots(session, shots),
                "Error: HIT callback should succeed.");

        assertEquals(1, game.getAlienMoves().size(),
                "Error: HIT should be recorded.");
    }

    @Test
    @DisplayName("RecordAiMove 3 - SUNK outcome")
    void recordAiMove3() throws Exception {
        mockResponse(200, responseJson("SUNK", 3, "ONGOING"));

        assertTrue(client.sendShots(session, shots),
                "Error: SUNK callback should succeed.");

        assertEquals(1, game.getAlienMoves().size(),
                "Error: SUNK should be recorded.");
    }

    @Test
    @DisplayName("RecordAiMove 4 - INVALID outcome")
    void recordAiMove4() throws Exception {
        mockResponse(200, responseJson("INVALID", 5, "ONGOING"));

        assertTrue(client.sendShots(session, shots),
                "Error: INVALID outcome should be processed.");

        assertEquals(1, game.getAlienMoves().size(),
                "Error: INVALID shot should still be recorded.");
    }

    @Test
    @DisplayName("RecordAiMove 5 - REPEATED outcome")
    void recordAiMove5() throws Exception {
        mockResponse(200, responseJson("REPEATED", 5, "ONGOING"));

        assertTrue(client.sendShots(session, shots),
                "Error: REPEATED outcome should be processed.");

        assertEquals(1, game.getAlienMoves().size(),
                "Error: REPEATED shot should be recorded.");
    }

    @Test
    @DisplayName("RecordAiMove 6 - Null results list")
    void recordAiMove6() throws Exception {
        String json = """
                {
                  "results": null,
                  "shipsRemaining": 5,
                  "gameStatus": "ONGOING"
                }
                """;

        mockResponse(200, json);

        boolean result = client.sendShots(session, shots);

        assertTrue(result,
                "Error: Null results should not crash callback.");

        assertEquals(0, game.getAlienMoves().size(),
                "Error: Null results should not record a move.");
    }

    @Test
    @DisplayName("RecordAiMove 7 - Empty results list")
    void recordAiMove7() throws Exception {
        String json = """
                {
                  "results": [],
                  "shipsRemaining": 5,
                  "gameStatus": "ONGOING"
                }
                """;

        mockResponse(200, json);

        boolean result = client.sendShots(session, shots);

        assertTrue(result,
                "Error: Empty results should be accepted.");

        assertEquals(1, game.getAlienMoves().size(),
                "Error: Empty results should produce an empty move.");
    }

    @Test
    @DisplayName("SendShots - Request uses POST and correct endpoint")
    void sendShotsRequest() throws Exception {
        mockResponse(200, responseJson("MISS", 5, "ONGOING"));

        assertTrue(client.sendShots(session, shots),
                "Error: Valid callback should succeed.");

        org.mockito.ArgumentCaptor<HttpRequest> captor =
                org.mockito.ArgumentCaptor.forClass(HttpRequest.class);

        verify(http).send(
                captor.capture(),
                any(HttpResponse.BodyHandler.class)
        );

        HttpRequest request = captor.getValue();

        assertAll(
                () -> assertEquals("POST", request.method(),
                        "Error: HTTP method should be POST."),
                () -> assertEquals(
                        "http://localhost:8081/game/test-game/shots",
                        request.uri().toString(),
                        "Error: Callback URL is incorrect."),
                () -> assertEquals(
                        "application/json",
                        request.headers()
                                .firstValue("Content-Type")
                                .orElse(null),
                        "Error: Content-Type should be JSON.")
        );
    }
}
