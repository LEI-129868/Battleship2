package restserver;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class GameRegistry.
 * Author: ${user.name}
 * Date: 2026-11-19 15:30
 * Cyclomatic Complexity:
 * - constructor (default): 1
 * - createSession(): 1
 * - getSession(): 1
 * - removeSession(): 1
 */
class GameRegistryTest {

	private GameRegistry registry;

	@BeforeEach
	void setUp() {
		registry = new GameRegistry();
	}

	@AfterEach
	void tearDown() {
		registry = null;
	}

	@Test
	@DisplayName("constructor: um registo novo não contém sessões")
	void constructor() {
		assertAll("Registo vazio",
				() -> assertNotNull(registry, "Error: expected a GameRegistry instance but got null"),
				() -> assertNull(registry.getSession("qualquer-id"),
						"Error: expected null for an unknown id in an empty registry but got a session"));
	}

	@Test
	@DisplayName("createSession: cria, guarda e devolve uma sessão com os dados do jogador")
	void createSession() {
		GameSession session = registry.createSession("Daniel", "http://localhost:9090");

		assertAll("Sessão criada",
				() -> assertNotNull(session, "Error: expected a new GameSession but got null"),
				() -> assertNotNull(session.getGameId(), "Error: expected a generated gameId but got null"),
				() -> assertFalse(session.getGameId().isBlank(), "Error: expected a non-blank gameId but got a blank one"),
				() -> assertEquals("Daniel", session.getPlayerName(),
						"Error: expected player name 'Daniel' but got " + session.getPlayerName()),
				() -> assertEquals("http://localhost:9090", session.getCallbackUrl(),
						"Error: expected callback URL 'http://localhost:9090' but got " + session.getCallbackUrl()),
				() -> assertSame(session, registry.getSession(session.getGameId()),
						"Error: expected the registry to store the created session under its gameId but it did not"));
	}

	@Test
	@DisplayName("createSession: duas sessões criadas têm identificadores diferentes")
	void createSessionUniqueIds() {
		GameSession first = registry.createSession("A", "http://a");
		GameSession second = registry.createSession("B", "http://b");

		assertAll("Identificadores únicos",
				() -> assertNotEquals(first.getGameId(), second.getGameId(),
						"Error: expected different gameIds but both were " + first.getGameId()),
				() -> assertSame(first, registry.getSession(first.getGameId()),
						"Error: expected the first session to remain stored but it was not"),
				() -> assertSame(second, registry.getSession(second.getGameId()),
						"Error: expected the second session to be stored but it was not"));
	}

	@Test
	@DisplayName("getSession: devolve null para um id que não existe")
	void getSession() {
		registry.createSession("Daniel", "http://localhost:9090");

		assertNull(registry.getSession("inexistente"),
				"Error: expected null for an unknown gameId but got a session");
	}

	@Test
	@DisplayName("removeSession: depois de removida, a sessão deixa de existir no registo")
	void removeSession() {
		GameSession session = registry.createSession("Daniel", "http://localhost:9090");
		String id = session.getGameId();

		registry.removeSession(id);

		assertAll("Sessão removida",
				() -> assertNull(registry.getSession(id),
						"Error: expected null after removing the session but it is still stored"),
				() -> assertDoesNotThrow(() -> registry.removeSession("inexistente"),
						"Error: expected removing an unknown id to do nothing but an exception was thrown"));
	}
}
