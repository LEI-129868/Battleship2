package restserver;

import battleship.*;
import restserver.Dtos.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Sends the AI's shots to the student's server (m2a) and reads back the results (m2b).
 *
 * Uses Java's built-in java.net.http.HttpClient (available since Java 11) —
 * no extra dependency needed.
 *
 * The AI calls:  POST {callbackUrl}/game/{gameId}/shots
 * with the same ShotRequest JSON format that the student sends to us.
 *
 * The student must respond with the same ShotResponse JSON format that we
 * send back to them — this symmetric design keeps the student contract simple.
 */
@Component
public class CallbackClient {

	private final HttpClient   http;
	private final ObjectMapper mapper;

	public CallbackClient() {
		this.http   = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(5))
				.build();
		this.mapper = new ObjectMapper();
	}

	/**
	 * Fire AI shots at the student's callback URL and record the results in the game.
	 *
	 * @param session   the active game session (provides callbackUrl and gameId)
	 * @param aiShots   the positions the AI wants to fire
	 * @return true if the callback succeeded and results were processed; false on error
	 */
	public boolean sendShots(GameSession session, List<IPosition> aiShots) {

		// ── 1. Build m2a request body ────────────────────────────────────────
		ShotRequest dto = new ShotRequest();
		dto.shots = new ArrayList<>();
		for (IPosition pos : aiShots) {
			ShotPosition sp = new ShotPosition();
			sp.row    = String.valueOf(pos.getClassicRow());
			sp.column = pos.getClassicColumn();
			dto.shots.add(sp);
		}

		String url  = session.getCallbackUrl() + "/game/" + session.getGameId() + "/shots";
		String body;
		try {
			body = mapper.writeValueAsString(dto);
		} catch (IOException e) {
			System.err.println("[Callback] Failed to serialize AI shots: " + e.getMessage());
			return false;
		}

		// ── 2. POST to student's server ──────────────────────────────────────
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.header("Content-Type", "application/json")
				.timeout(Duration.ofSeconds(10))
				.POST(HttpRequest.BodyPublishers.ofString(body))
				.build();

		HttpResponse<String> response;
		try {
			response = http.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (IOException | InterruptedException e) {
			System.err.println("[Callback] HTTP call to student failed: " + e.getMessage());
			return false;
		}

		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			System.err.printf("[Callback] Student returned HTTP %d: %s%n",
					response.statusCode(), response.body());
			return false;
		}

		// ── 3. Parse m2b response ────────────────────────────────────────────
		ShotResponse m2b;
		try {
			m2b = mapper.readValue(response.body(), ShotResponse.class);
		} catch (IOException e) {
			System.err.println("[Callback] Failed to parse student m2b response: " + e.getMessage());
			return false;
		}

		// ── 4. Record the AI's move in the game (for strategy state tracking) ─
		recordAiMove(session.getGame(), aiShots, m2b);

		// ── 5. Check if AI won ────────────────────────────────────────────────
		if (m2b.shipsRemaining == 0 || "GAME_OVER".equals(m2b.gameStatus)) {
			session.markAiWins();
			System.out.printf("[%s] GAME OVER — AI wins against '%s'!%n",
					session.getGameId(), session.getPlayerName());
		}

		return true;
	}

	/**
	 * Records the AI's shots and the student's reported results into the Game object.
	 * This allows AiShotStrategy to see past hits when computing the next move.
	 */
	private void recordAiMove(IGame game, List<IPosition> shots, ShotResponse m2b) {
		if (m2b.results == null) return;

		List<IGame.ShotResult> shotResults = new ArrayList<>();
		for (int i = 0; i < shots.size() && i < m2b.results.size(); i++) {
			ShotResult r = m2b.results.get(i);

			boolean valid    = !"INVALID".equals(r.outcome);
			boolean repeated = "REPEATED".equals(r.outcome);
			boolean sunk     = "SUNK".equals(r.outcome);

			// We don't have the actual IShip reference for the student's fleet,
			// so we pass null for ship — the strategy uses outcome strings instead.
			shotResults.add(new IGame.ShotResult(valid, repeated, null, sunk));
		}

		// Record the move in the game's alien moves list so strategy can inspect it
		Move move = new Move(game.getAlienMoves().size() + 1, shots, shotResults);
		game.getAlienMoves().add(move);
	}
}