package restserver;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory store of all active GameSessions.
 *
 * Spring manages this as a singleton bean (@Component), so every
 * request to GameController gets the same registry instance.
 */
@Component
public class GameRegistry {

	private final Map<String, GameSession> sessions = new ConcurrentHashMap<>();

	/** Create, store, and return a new GameSession. */
	public GameSession createSession(String playerName, String callbackUrl) {
		String gameId = UUID.randomUUID().toString();
		GameSession session = new GameSession(gameId, playerName, callbackUrl);
		sessions.put(gameId, session);
		return session;
	}

	/** Retrieve an existing session, or null if not found. */
	public GameSession getSession(String gameId) {
		return sessions.get(gameId);
	}

	/** Remove a session (e.g. after game over). */
	public void removeSession(String gameId) {
		sessions.remove(gameId);
	}
}