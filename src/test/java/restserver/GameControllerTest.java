package restserver;

import battleship.Game;
import battleship.IGame;
import battleship.IPosition;
import battleship.Position;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import restserver.Dtos.RegistrationRequest;
import restserver.Dtos.RegistrationResponse;
import restserver.Dtos.ShotPosition;
import restserver.Dtos.ShotRequest;
import restserver.Dtos.ShotResponse;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for GameController
 * Author: Tomas Costa (LEI-129836)
 * Generated: 2026-11-16
 *
 * Estimated cyclomatic complexities (decision/short-circuit paths):
 * - GameController(...): 1
 * - register(...): 5
 * - receiveShots(...): at least 11
 * - evaluateShots(...): at least 7
 * - error(...): 1
 *
 * No third-party mocking library is used. Some tests use the real
 * GameRegistry/GameSession, so this is a mix of unit and integration tests.
 * The private helper is exercised by reflection for coverage of conversion.
 */
public class GameControllerTest {

    private GameRegistry registry;
    private GameController controller;

    @BeforeEach
    void setUp() {
        // GameRegistry is expected to expose the usual no-argument constructor.
        registry = new GameRegistry();
        // No callback or AI strategy is needed for early validation paths.
        controller = new GameController(registry, null, null);
    }

    @AfterEach
    void tearDown() {
        controller = null;
        registry = null;
    }

    private RegistrationRequest registration(String name, String url) {
        RegistrationRequest r = new RegistrationRequest();
        r.playerName = name;
        r.callbackUrl = url;
        return r;
    }

    private ShotRequest requestWithShots(int number) {
        ShotRequest r = new ShotRequest();
        r.shots = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            ShotPosition p = new ShotPosition();
            p.row = String.valueOf((char) ('A' + (i % 10)));
            p.column = 1 + (i / 10) % 10;
            r.shots.add(p);
        }
        return r;
    }

    private GameSession session() {
        return registry.createSession("Tomas", "http://localhost:1");
    }

    @Test
    @DisplayName("Constructor: stores a usable controller")
    void constructor() {
        assertNotNull(controller, "Error: expected initialized controller, got null");
    }

    @Test
    @DisplayName("Register 1: valid player and URL")
    void register1() {
        ResponseEntity<?> response = controller.register(registration("Tomas", "http://localhost:1"));
        assertEquals(HttpStatus.OK, response.getStatusCode(),
                "Error: expected 200 for valid registration");
        assertInstanceOf(RegistrationResponse.class, response.getBody(),
                "Error: expected RegistrationResponse body");
        RegistrationResponse body = (RegistrationResponse) response.getBody();
        assertAll(
                () -> assertNotNull(body.gameId, "Error: game ID should not be null"),
                () -> assertEquals("REGISTERED", body.status, "Error: wrong registration status"),
                () -> assertTrue(body.message.contains("Tomas"), "Error: missing player name"),
                () -> assertNotNull(registry.getSession(body.gameId),
                        "Error: registered session should exist")
        );
    }

    @Test
    @DisplayName("Register 2: null player name")
    void register2() {
        assertEquals(HttpStatus.BAD_REQUEST,
                controller.register(registration(null, "http://localhost:1")).getStatusCode(),
                "Error: null player name should return 400");
    }

    @Test
    @DisplayName("Register 3: blank player name")
    void register3() {
        assertEquals(HttpStatus.BAD_REQUEST,
                controller.register(registration("  ", "http://localhost:1")).getStatusCode(),
                "Error: blank player name should return 400");
    }

    @Test
    @DisplayName("Register 4: null callback URL")
    void register4() {
        assertEquals(HttpStatus.BAD_REQUEST,
                controller.register(registration("Tomas", null)).getStatusCode(),
                "Error: null callback URL should return 400");
    }

    @Test
    @DisplayName("Register 5: blank callback URL")
    void register5() {
        assertEquals(HttpStatus.BAD_REQUEST,
                controller.register(registration("Tomas", " ")).getStatusCode(),
                "Error: blank callback URL should return 400");
    }

    @Test
    @DisplayName("Receive shots 1: unknown game")
    void receiveShots1() {
        assertEquals(HttpStatus.NOT_FOUND,
                controller.receiveShots("unknown-game", requestWithShots(Game.NUMBER_SHOTS)).getStatusCode(),
                "Error: unknown game should return 404");
    }

    @Test
    @DisplayName("Receive shots 2: finished game")
    void receiveShots2() {
        GameSession session = session();
        session.markStudentWins();
        assertEquals(HttpStatus.GONE,
                controller.receiveShots(session.getGameId(), requestWithShots(Game.NUMBER_SHOTS)).getStatusCode(),
                "Error: finished game should return 410");
    }

    @Test
    @DisplayName("Receive shots 3: null shot list")
    void receiveShots3() {
        GameSession session = session();
        ShotRequest r = new ShotRequest();
        r.shots = null;
        assertEquals(HttpStatus.BAD_REQUEST,
                controller.receiveShots(session.getGameId(), r).getStatusCode(),
                "Error: null shot list should return 400");
    }

    @Test
    @DisplayName("Receive shots 4: wrong number of shots")
    void receiveShots4() {
        GameSession session = session();
        int wrongCount = Game.NUMBER_SHOTS == 0 ? 1 : Game.NUMBER_SHOTS - 1;
        assertEquals(HttpStatus.BAD_REQUEST,
                controller.receiveShots(session.getGameId(), requestWithShots(wrongCount)).getStatusCode(),
                "Error: incorrect number of shots should return 400");
    }

    @Test
    @DisplayName("Receive shots 5: null row")
    void receiveShots5() {
        GameSession session = session();
        ShotRequest r = requestWithShots(Game.NUMBER_SHOTS);
        assertFalse(r.shots.isEmpty(), "Error: a turn should require at least one shot");
        r.shots.get(0).row = null;
        assertEquals(HttpStatus.BAD_REQUEST,
                controller.receiveShots(session.getGameId(), r).getStatusCode(),
                "Error: null row should return 400");
    }

    @Test
    @DisplayName("Receive shots 6: blank row")
    void receiveShots6() {
        GameSession session = session();
        ShotRequest r = requestWithShots(Game.NUMBER_SHOTS);
        r.shots.get(0).row = " ";
        assertEquals(HttpStatus.BAD_REQUEST,
                controller.receiveShots(session.getGameId(), r).getStatusCode(),
                "Error: blank row should return 400");
    }

    @Test
    @DisplayName("Receive shots 7: column smaller than 1")
    void receiveShots7() {
        GameSession session = session();
        ShotRequest r = requestWithShots(Game.NUMBER_SHOTS);
        r.shots.get(0).column = 0;
        assertEquals(HttpStatus.BAD_REQUEST,
                controller.receiveShots(session.getGameId(), r).getStatusCode(),
                "Error: column 0 should return 400");
    }

    @Test
    @DisplayName("Receive shots 8: column greater than 10")
    void receiveShots8() {
        GameSession session = session();
        ShotRequest r = requestWithShots(Game.NUMBER_SHOTS);
        r.shots.get(0).column = 11;
        assertEquals(HttpStatus.BAD_REQUEST,
                controller.receiveShots(session.getGameId(), r).getStatusCode(),
                "Error: column 11 should return 400");
    }

    @Test
    @DisplayName("Evaluate shots 1: returns a result for a valid position")
    void evaluateShots1() throws Exception {
        GameSession session = session();
        List<IPosition> positions = List.of(new Position('A', 1));
        ShotResponse result = invokeEvaluateShots(positions, session);
        assertAll(
                () -> assertEquals(1, result.results.size(), "Error: expected one shot result"),
                () -> assertEquals("A", result.results.get(0).row, "Error: incorrect row"),
                () -> assertEquals(1, result.results.get(0).column, "Error: incorrect column"),
                () -> assertEquals("ONGOING", result.gameStatus, "Error: expected ONGOING status")
        );
    }

    @Test
    @DisplayName("Evaluate shots 2: processes an empty shot list")
    void evaluateShots2() throws Exception {
        GameSession session = session();
        ShotResponse result = invokeEvaluateShots(List.of(), session);
        assertAll(
                () -> assertTrue(result.results.isEmpty(), "Error: expected no shot results"),
                () -> assertNull(result.winner, "Error: winner should be null")
        );
    }

    @Test
    @DisplayName("Evaluate shots 3: duplicate positions are still represented")
    void evaluateShots3() throws Exception {
        GameSession session = session();
        List<IPosition> positions = List.of(new Position('A', 1), new Position('A', 1));
        ShotResponse result = invokeEvaluateShots(positions, session);
        assertEquals(2, result.results.size(),
                "Error: both supplied shots should have a result");
    }

    // Only required because GameController's helper is private and its
    // normal path requires an AI strategy and callback dependencies.
    private ShotResponse invokeEvaluateShots(List<IPosition> positions,
                                             GameSession session) throws Exception {
        Method method = GameController.class.getDeclaredMethod(
                "evaluateShots", List.class, IGame.class, GameSession.class);
        method.setAccessible(true);
        return (ShotResponse) method.invoke(controller, positions, session.getGame(), session);
    }
}
