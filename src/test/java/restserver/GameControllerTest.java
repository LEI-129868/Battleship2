
package restserver;

import battleship.*;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import restserver.Dtos.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for GameController.
 * Author: Tomas Costa (LEI-129836)
 * Date: 2026-11-16 12:52
 *
 * Cyclomatic Complexity (estimated):
 * - Constructor: 1
 * - register(): 5 (compound conditions)
 * - receiveShots(): 11 (compound conditions)
 * - evaluateShots(): 7 (including result decisions)
 * - error(): 1
 *
 * The private helper methods are tested indirectly
 * through the public controller methods.
 */
public class GameControllerTest {

    private GameController controller;
    private GameRegistry registry;
    private CallbackClient callbackClient;
    private AiShotStrategy aiStrategy;
    private GameSession session;
    private IGame game;

    @BeforeEach
    void setUp() {
        registry = mock(GameRegistry.class);
        callbackClient = mock(CallbackClient.class);
        aiStrategy = mock(AiShotStrategy.class);
        session = mock(GameSession.class);
        game = mock(IGame.class);

        controller = new GameController(
                registry, callbackClient, aiStrategy
        );

        when(session.getGame()).thenReturn(game);
        when(session.getGameId()).thenReturn("test-game");
        when(session.getPlayerName()).thenReturn("Tomas");
        when(session.getCallbackUrl())
                .thenReturn("http://localhost:8081");

        when(registry.getSession("test-game"))
                .thenReturn(session);

        when(game.getRemainingShips()).thenReturn(5);
        when(game.fireSingleShot(any(IPosition.class), anyBoolean()))
                .thenReturn(new IGame.ShotResult(
                        true, false, null, false
                ));

        when(aiStrategy.computeShots(any(IGame.class)))
                .thenReturn(new ArrayList<>());

        when(callbackClient.sendShots(
                any(GameSession.class), anyList()
        )).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        controller = null;
        registry = null;
        callbackClient = null;
        aiStrategy = null;
        session = null;
        game = null;
    }

    private RegistrationRequest registration(
            String name, String url
    ) {
        RegistrationRequest request = new RegistrationRequest();
        request.playerName = name;
        request.callbackUrl = url;
        return request;
    }

    private ShotRequest validShots() {
        ShotRequest request = new ShotRequest();
        request.shots = new ArrayList<>();

        for (int i = 0; i < Game.NUMBER_SHOTS; i++) {
            ShotPosition position = new ShotPosition();
            position.row = "A";
            position.column = i + 1;
            request.shots.add(position);
        }

        return request;
    }

    @Test
    @DisplayName("Constructor - Controller initialized")
    void constructor() {
        assertNotNull(controller,
                "Error: GameController should not be null.");
    }

    @Test
    @DisplayName("Register 1 - Valid registration")
    void register1() {
        when(registry.createSession(anyString(), anyString()))
                .thenReturn(session);

        ResponseEntity<?> response = controller.register(
                registration("Tomas", "http://localhost:8081")
        );

        assertAll(
                () -> assertEquals(HttpStatus.OK,
                        response.getStatusCode(),
                        "Error: Expected HTTP 200."),
                () -> assertInstanceOf(
                        RegistrationResponse.class,
                        response.getBody(),
                        "Error: Expected RegistrationResponse."
                )
        );

        RegistrationResponse body =
                (RegistrationResponse) response.getBody();

        assertNotNull(body,
                "Error: Registration response should not be null.");

        assertAll(
                () -> assertEquals("test-game", body.gameId,
                        "Error: Incorrect game ID."),
                () -> assertEquals("REGISTERED", body.status,
                        "Error: Incorrect registration status."),
                () -> assertEquals("Welcome, Tomas! Game ready. You shoot first.",
                        body.message,
                        "Error: Incorrect welcome message.")
        );

        verify(registry).createSession(
                "Tomas", "http://localhost:8081"
        );
    }

    @Test
    @DisplayName("Register 2 - Null player name")
    void register2() {
        ResponseEntity<?> response = controller.register(
                registration(null, "http://localhost:8081")
        );

        assertEquals(HttpStatus.BAD_REQUEST,
                response.getStatusCode(),
                "Error: Null player name should return HTTP 400.");

        verifyNoInteractions(registry);
    }

    @Test
    @DisplayName("Register 3 - Blank player name")
    void register3() {
        ResponseEntity<?> response = controller.register(
                registration("   ", "http://localhost:8081")
        );

        assertEquals(HttpStatus.BAD_REQUEST,
                response.getStatusCode(),
                "Error: Blank player name should return HTTP 400.");

        verifyNoInteractions(registry);
    }

    @Test
    @DisplayName("Register 4 - Null callback URL")
    void register4() {
        ResponseEntity<?> response = controller.register(
                registration("Tomas", null)
        );

        assertEquals(HttpStatus.BAD_REQUEST,
                response.getStatusCode(),
                "Error: Null callback URL should return HTTP 400.");

        verifyNoInteractions(registry);
    }

    @Test
    @DisplayName("Register 5 - Blank callback URL")
    void register5() {
        ResponseEntity<?> response = controller.register(
                registration("Tomas", "  ")
        );

        assertEquals(HttpStatus.BAD_REQUEST,
                response.getStatusCode(),
                "Error: Blank callback URL should return HTTP 400.");

        verifyNoInteractions(registry);
    }

    @Test
    @DisplayName("ReceiveShots 1 - Game not found")
    void receiveShots1() {
        when(registry.getSession("unknown")).thenReturn(null);

        ResponseEntity<?> response = controller.receiveShots(
                "unknown", validShots()
        );

        assertEquals(HttpStatus.NOT_FOUND,
                response.getStatusCode(),
                "Error: Unknown game should return HTTP 404.");
    }

    @Test
    @DisplayName("ReceiveShots 2 - Game already over")
    void receiveShots2() {
        when(session.isGameOver()).thenReturn(true);
        when(session.getWinner()).thenReturn("STUDENT_WINS");

        ResponseEntity<?> response = controller.receiveShots(
                "test-game", validShots()
        );

        assertEquals(HttpStatus.GONE,
                response.getStatusCode(),
                "Error: Finished game should return HTTP 410.");
    }

    @Test
    @DisplayName("ReceiveShots 3 - Null shots list")
    void receiveShots3() {
        ShotRequest request = new ShotRequest();
        request.shots = null;

        ResponseEntity<?> response = controller.receiveShots(
                "test-game", request
        );

        assertEquals(HttpStatus.BAD_REQUEST,
                response.getStatusCode(),
                "Error: Null shots should return HTTP 400.");
    }

    @Test
    @DisplayName("ReceiveShots 4 - Incorrect number of shots")
    void receiveShots4() {
        ShotRequest request = new ShotRequest();
        request.shots = new ArrayList<>();

        ResponseEntity<?> response = controller.receiveShots(
                "test-game", request
        );

        assertEquals(HttpStatus.BAD_REQUEST,
                response.getStatusCode(),
                "Error: Incorrect shot count should return HTTP 400.");
    }

    @Test
    @DisplayName("ReceiveShots 5 - Null row")
    void receiveShots5() {
        ShotRequest request = validShots();
        request.shots.get(0).row = null;

        ResponseEntity<?> response = controller.receiveShots(
                "test-game", request
        );

        assertEquals(HttpStatus.BAD_REQUEST,
                response.getStatusCode(),
                "Error: Null row should return HTTP 400.");
    }

    @Test
    @DisplayName("ReceiveShots 6 - Blank row")
    void receiveShots6() {
        ShotRequest request = validShots();
        request.shots.get(0).row = " ";

        ResponseEntity<?> response = controller.receiveShots(
                "test-game", request
        );

        assertEquals(HttpStatus.BAD_REQUEST,
                response.getStatusCode(),
                "Error: Blank row should return HTTP 400.");
    }

    @Test
    @DisplayName("ReceiveShots 7 - Column below minimum")
    void receiveShots7() {
        ShotRequest request = validShots();
        request.shots.get(0).column = 0;

        ResponseEntity<?> response = controller.receiveShots(
                "test-game", request
        );

        assertEquals(HttpStatus.BAD_REQUEST,
                response.getStatusCode(),
                "Error: Column below 1 should return HTTP 400.");
    }

    @Test
    @DisplayName("ReceiveShots 8 - Column above maximum")
    void receiveShots8() {
        ShotRequest request = validShots();
        request.shots.get(0).column = 11;

        ResponseEntity<?> response = controller.receiveShots(
                "test-game", request
        );

        assertEquals(HttpStatus.BAD_REQUEST,
                response.getStatusCode(),
                "Error: Column above 10 should return HTTP 400.");
    }

    @Test
    @DisplayName("ReceiveShots 9 - Valid shots and callback success")
    void receiveShots9() {
        ResponseEntity<?> response = controller.receiveShots(
                "test-game", validShots()
        );

        assertEquals(HttpStatus.OK,
                response.getStatusCode(),
                "Error: Valid shots should return HTTP 200.");

        ShotResponse body = (ShotResponse) response.getBody();

        assertNotNull(body,
                "Error: Shot response should not be null.");

        assertAll(
                () -> assertEquals(Game.NUMBER_SHOTS,
                        body.results.size(),
                        "Error: Incorrect number of results."),
                () -> assertEquals("ONGOING", body.gameStatus,
                        "Error: Game should be ongoing."),
                () -> assertEquals(5, body.shipsRemaining,
                        "Error: Incorrect remaining ship count."),
                () -> assertNull(body.winner,
                        "Error: Ongoing game should have no winner.")
        );

        verify(callbackClient).sendShots(
                eq(session), anyList()
        );
    }

    @Test
    @DisplayName("ReceiveShots 10 - Student wins")
    void receiveShots10() {
        when(game.getRemainingShips()).thenReturn(0);

        ResponseEntity<?> response = controller.receiveShots(
                "test-game", validShots()
        );

        ShotResponse body = (ShotResponse) response.getBody();

        assertNotNull(body,
                "Error: Expected a shot response.");

        assertAll(
                () -> assertEquals(HttpStatus.OK,
                        response.getStatusCode(),
                        "Error: Winning turn should return HTTP 200."),
                () -> assertEquals("GAME_OVER", body.gameStatus,
                        "Error: Game should be over."),
                () -> assertEquals("STUDENT_WINS", body.winner,
                        "Error: Student should be the winner.")
        );

        verify(session).markStudentWins();
        verifyNoInteractions(callbackClient);
    }

    @Test
    @DisplayName("ReceiveShots 11 - Callback failure does not crash turn")
    void receiveShots11() {
        when(callbackClient.sendShots(
                eq(session), anyList()
        )).thenReturn(false);

        ResponseEntity<?> response = controller.receiveShots(
                "test-game", validShots()
        );

        assertEquals(HttpStatus.OK,
                response.getStatusCode(),
                "Error: Callback failure should not abort the turn.");
    }

    @Test
    @DisplayName("EvaluateShots 1 - Invalid shot")
    void evaluateShots1() {
        when(game.fireSingleShot(
                any(IPosition.class), anyBoolean()
        )).thenReturn(new IGame.ShotResult(
                false, false, null, false
        ));

        ShotResponse body = (ShotResponse) controller.receiveShots(
                "test-game", validShots()
        ).getBody();

        assertNotNull(body, "Error: Response should not be null.");

        assertEquals("INVALID", body.results.get(0).outcome,
                "Error: Invalid shot should have INVALID outcome.");
    }

    @Test
    @DisplayName("EvaluateShots 2 - Repeated shot")
    void evaluateShots2() {
        when(game.fireSingleShot(
                any(IPosition.class), anyBoolean()
        )).thenReturn(new IGame.ShotResult(
                true, true, null, false
        ));

        ShotResponse body = (ShotResponse) controller.receiveShots(
                "test-game", validShots()
        ).getBody();

        assertNotNull(body, "Error: Response should not be null.");

        assertEquals("REPEATED", body.results.get(0).outcome,
                "Error: Repeated shot should have REPEATED outcome.");
    }

    @Test
    @DisplayName("EvaluateShots 3 - Missed shot")
    void evaluateShots3() {
        ShotResponse body = (ShotResponse) controller.receiveShots(
                "test-game", validShots()
        ).getBody();

        assertNotNull(body, "Error: Response should not be null.");

        assertEquals("MISS", body.results.get(0).outcome,
                "Error: Shot without a ship should be MISS.");
    }

    @Test
    @DisplayName("EvaluateShots 4 - Ship hit")
    void evaluateShots4() {
        IShip ship = mock(IShip.class);
        when(ship.getCategory()).thenReturn("barca");

        when(game.fireSingleShot(
                any(IPosition.class), anyBoolean()
        )).thenReturn(new IGame.ShotResult(
                true, false, ship, false
        ));

        ShotResponse body = (ShotResponse) controller.receiveShots(
                "test-game", validShots()
        ).getBody();

        assertNotNull(body, "Error: Response should not be null.");

        assertAll(
                () -> assertEquals("HIT", body.results.get(0).outcome,
                        "Error: Expected HIT outcome."),
                () -> assertEquals("barca", body.results.get(0).shipType,
                        "Error: Incorrect ship category.")
        );
    }

    @Test
    @DisplayName("EvaluateShots 5 - Ship sunk")
    void evaluateShots5() {
        IShip ship = mock(IShip.class);
        when(ship.getCategory()).thenReturn("barca");

        when(game.fireSingleShot(
                any(IPosition.class), anyBoolean()
        )).thenReturn(new IGame.ShotResult(
                true, false, ship, true
        ));

        ShotResponse body = (ShotResponse) controller.receiveShots(
                "test-game", validShots()
        ).getBody();

        assertNotNull(body, "Error: Response should not be null.");

        assertAll(
                () -> assertEquals("SUNK", body.results.get(0).outcome,
                        "Error: Expected SUNK outcome."),
                () -> assertEquals("barca", body.results.get(0).shipType,
                        "Error: Incorrect sunk ship category.")
        );
    }

    @Test
    @DisplayName("EvaluateShots 6 - Lowercase row conversion")
    void evaluateShots6() {
        ShotRequest request = validShots();
        request.shots.get(0).row = "a";

        ResponseEntity<?> response = controller.receiveShots(
                "test-game", request
        );

        assertEquals(HttpStatus.OK,
                response.getStatusCode(),
                "Error: Lowercase row should be accepted.");

        verify(game, atLeastOnce()).fireSingleShot(
                any(IPosition.class), anyBoolean()
        );
    }

    @Test
    @DisplayName("EvaluateShots 7 - Repeated positions in same turn")
    void evaluateShots7() {
        Assumptions.assumeTrue(Game.NUMBER_SHOTS > 1,
                "This test requires multiple shots per turn.");

        ShotRequest request = validShots();
        request.shots.get(1).row = request.shots.get(0).row;
        request.shots.get(1).column = request.shots.get(0).column;

        ResponseEntity<?> response = controller.receiveShots(
                "test-game", request
        );

        assertEquals(HttpStatus.OK,
                response.getStatusCode(),
                "Error: Repeated positions should be processed.");

        verify(game, atLeastOnce()).fireSingleShot(
                any(IPosition.class), eq(true)
        );
    }
}
