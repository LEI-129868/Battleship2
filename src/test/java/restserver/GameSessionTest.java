package restserver;

import battleship.Game;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class GameSession.
 * Author: ${user.name}
 * Date: 2026-11-19 15:30
 * Cyclomatic Complexity:
 * - constructor: 1
 * - getGameId(): 1
 * - getPlayerName(): 1
 * - getCallbackUrl(): 1
 * - getGame(): 1
 * - getShotsPerTurn(): 1
 * - isGameOver(): 1
 * - getWinner(): 1
 * - markAiWins(): 1
 * - markStudentWins(): 1
 */
class GameSessionTest {

	private static final String GAME_ID = "id-1";
	private static final String PLAYER = "Daniel";
	private static final String CALLBACK = "http://localhost:9090";

	private GameSession session;

	@BeforeEach
	void setUp() {
		session = new GameSession(GAME_ID, PLAYER, CALLBACK);
	}

	@AfterEach
	void tearDown() {
		session = null;
	}

	@Test
	@DisplayName("constructor: a sessão começa com jogo criado, sem vencedor e não terminada")
	void constructor() {
		assertAll("Estado inicial",
				() -> assertNotNull(session.getGame(), "Error: expected a Game to be created but got null"),
				() -> assertFalse(session.isGameOver(), "Error: expected gameOver=false but got true"),
				() -> assertNull(session.getWinner(), "Error: expected winner=null but got " + session.getWinner()),
				() -> assertEquals(Game.NUMBER_SHOTS, session.getShotsPerTurn(),
						"Error: expected " + Game.NUMBER_SHOTS + " shots per turn but got " + session.getShotsPerTurn()));
	}

	@Test
	@DisplayName("getGameId: devolve o identificador passado no construtor")
	void getGameId() {
		assertEquals(GAME_ID, session.getGameId(),
				"Error: expected gameId '" + GAME_ID + "' but got " + session.getGameId());
	}

	@Test
	@DisplayName("getPlayerName: devolve o nome do jogador passado no construtor")
	void getPlayerName() {
		assertEquals(PLAYER, session.getPlayerName(),
				"Error: expected player '" + PLAYER + "' but got " + session.getPlayerName());
	}

	@Test
	@DisplayName("getCallbackUrl: devolve o URL de callback passado no construtor")
	void getCallbackUrl() {
		assertEquals(CALLBACK, session.getCallbackUrl(),
				"Error: expected callback '" + CALLBACK + "' but got " + session.getCallbackUrl());
	}

	@Test
	@DisplayName("getGame: devolve um jogo com a frota da IA colocada aleatoriamente")
	void getGame() {
		assertAll("Jogo da sessão",
				() -> assertNotNull(session.getGame(), "Error: expected a Game but got null"),
				() -> assertNotNull(session.getGame().getMyFleet(), "Error: expected the AI fleet but got null"),
				() -> assertFalse(session.getGame().getMyFleet().getShips().isEmpty(),
						"Error: expected the random AI fleet to have ships but it was empty"),
				() -> assertSame(session.getGame(), session.getGame(),
						"Error: expected getGame() to always return the same instance but it did not"));
	}

	@Test
	@DisplayName("getShotsPerTurn: devolve Game.NUMBER_SHOTS (3)")
	void getShotsPerTurn() {
		assertEquals(3, session.getShotsPerTurn(),
				"Error: expected 3 shots per turn but got " + session.getShotsPerTurn());
	}

	@Test
	@DisplayName("isGameOver: é false enquanto ninguém ganhou")
	void isGameOver() {
		assertFalse(session.isGameOver(), "Error: expected gameOver=false on a new session but got true");
	}

	@Test
	@DisplayName("getWinner: é null enquanto ninguém ganhou")
	void getWinner() {
		assertNull(session.getWinner(), "Error: expected winner=null on a new session but got " + session.getWinner());
	}

	@Test
	@DisplayName("markAiWins: termina o jogo e regista AI_WINS como vencedor")
	void markAiWins() {
		session.markAiWins();

		assertAll("IA ganha",
				() -> assertTrue(session.isGameOver(), "Error: expected gameOver=true after markAiWins but got false"),
				() -> assertEquals("AI_WINS", session.getWinner(),
						"Error: expected winner 'AI_WINS' but got " + session.getWinner()));
	}

	@Test
	@DisplayName("markStudentWins: termina o jogo e regista STUDENT_WINS como vencedor")
	void markStudentWins() {
		session.markStudentWins();

		assertAll("Aluno ganha",
				() -> assertTrue(session.isGameOver(), "Error: expected gameOver=true after markStudentWins but got false"),
				() -> assertEquals("STUDENT_WINS", session.getWinner(),
						"Error: expected winner 'STUDENT_WINS' but got " + session.getWinner()));
	}
}
