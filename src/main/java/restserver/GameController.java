package restserver;

import battleship.*;
import restserver.Dtos.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * REST Controller — exposes the two endpoints of the Battleship protocol.
 *
 *   POST /register                    →  m0  register a player
 *   POST /game/{gameId}/shots         →  m1a receive student shots, fire back (m1b)
 *                                        then call student callback (m2a/m2b) internally
 */
@RestController
public class GameController {

	private final GameRegistry registry;
	private final CallbackClient callbackClient;
	private final AiShotStrategy aiStrategy;

	public GameController(GameRegistry registry,
						  CallbackClient callbackClient,
						  AiShotStrategy aiStrategy) {
		this.registry       = registry;
		this.callbackClient = callbackClient;
		this.aiStrategy     = aiStrategy;
	}

	// =========================================================================
	// m0 — POST /register
	// =========================================================================

	/**
	 * Registers a student player and starts a new game.
	 *
	 * The AI places its own fleet randomly (via Fleet.createRandom()).
	 * The student's fleet is unknown to us — we will discover it shot by shot.
	 *
	 * @param request  { playerName, callbackUrl }
	 * @return         { gameId, status, shotsPerTurn, message }
	 */
	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody RegistrationRequest request) {

		// Basic validation
		if (request.playerName == null || request.playerName.isBlank()) {
			return error(HttpStatus.BAD_REQUEST, "playerName is required");
		}
		if (request.callbackUrl == null || request.callbackUrl.isBlank()) {
			return error(HttpStatus.BAD_REQUEST, "callbackUrl is required");
		}

		// Create session — AI fleet is randomised inside GameSession constructor
		GameSession session = registry.createSession(request.playerName, request.callbackUrl);

		RegistrationResponse response = new RegistrationResponse();
		response.gameId       = session.getGameId();
		response.status       = "REGISTERED";
		response.shotsPerTurn = session.getShotsPerTurn();
		response.message      = "Welcome, " + request.playerName + "! Game ready. You shoot first.";

		System.out.printf("[%s] Registered player '%s' (callback: %s)%n",
				session.getGameId(), request.playerName, request.callbackUrl);

		return ResponseEntity.ok(response);
	}

	// =========================================================================
	// m1a + m1b — POST /game/{gameId}/shots
	// =========================================================================

	/**
	 * Receives the student's shots (m1a), evaluates them against the AI's fleet,
	 * then — if the game is not over — triggers the AI's own shots via the
	 * student's callback URL (m2a/m2b) within the same HTTP round-trip.
	 *
	 * Returns the results of the STUDENT's shots only (m1b).
	 * The AI's shots are communicated via the callback.
	 *
	 * @param gameId   path variable — the UUID from /register
	 * @param request  { shots: [ {row, column}, ... ] }
	 * @return         { results, shipsRemaining, gameStatus, winner }
	 */
	@PostMapping("/game/{gameId}/shots")
	public ResponseEntity<?> receiveShots(@PathVariable String gameId,
										  @RequestBody ShotRequest request) {

		// ── 1. Lookup session ────────────────────────────────────────────────
		GameSession session = registry.getSession(gameId);
		if (session == null) {
			return error(HttpStatus.NOT_FOUND, "Game not found: " + gameId);
		}
		if (session.isGameOver()) {
			return error(HttpStatus.GONE, "Game " + gameId + " is already over. Winner: " + session.getWinner());
		}

		// ── 2. Validate incoming shots ───────────────────────────────────────
		if (request.shots == null || request.shots.size() != Game.NUMBER_SHOTS) {
			return error(HttpStatus.BAD_REQUEST,
					"Exactly " + Game.NUMBER_SHOTS + " shots required per turn");
		}

		// ── 3. Convert DTO positions → IPosition list ────────────────────────
		List<IPosition> positions = new ArrayList<>();
		for (ShotPosition sp : request.shots) {
			if (sp.row == null || sp.row.isBlank() || sp.column < 1 || sp.column > 10) {
				return error(HttpStatus.BAD_REQUEST,
						"Invalid position: row='" + sp.row + "' column=" + sp.column);
			}
			positions.add(new Position(sp.row.toUpperCase().charAt(0), sp.column));
		}

		// ── 4. Fire student's shots at the AI's fleet (m1b logic) ────────────
		IGame game = session.getGame();
		ShotResponse m1bResponse = evaluateShots(positions, game, session);

		// ── 5. Check if student won (all AI ships sunk) ──────────────────────
		if (game.getRemainingShips() == 0) {
			session.markStudentWins();
			m1bResponse.gameStatus = "GAME_OVER";
			m1bResponse.winner     = "STUDENT_WINS";
			System.out.printf("[%s] GAME OVER — student '%s' wins!%n",
					gameId, session.getPlayerName());
			return ResponseEntity.ok(m1bResponse);
		}

		// ── 6. AI fires back — calls student's callback (m2a + m2b) ─────────
		List<IPosition> aiShots = aiStrategy.computeShots(game);
		boolean callbackOk = callbackClient.sendShots(session, aiShots);

		if (!callbackOk) {
			// Callback failed — warn but don't crash the student's turn
			System.err.printf("[%s] WARNING: callback to '%s' failed. AI shots skipped.%n",
					gameId, session.getCallbackUrl());
		}

		// ── 7. Return m1b to student ─────────────────────────────────────────
		return ResponseEntity.ok(m1bResponse);
	}

	// =========================================================================
	// Helpers
	// =========================================================================

	/**
	 * Fires each position against the AI's fleet and builds the ShotResponse (m1b).
	 * Delegates to the existing Game.fireSingleShot() so all existing logic is reused.
	 */
	private ShotResponse evaluateShots(List<IPosition> positions,
									   IGame game,
									   GameSession session) {
		List<ShotResult> results = new ArrayList<>();
		List<IPosition> alreadyThisTurn = new ArrayList<>();

		for (IPosition pos : positions) {
			boolean repeatedInTurn = alreadyThisTurn.contains(pos);
			IGame.ShotResult sr = game.fireSingleShot(pos, repeatedInTurn);
			alreadyThisTurn.add(pos);

			ShotResult dto  = new ShotResult();
			dto.row         = String.valueOf(pos.getClassicRow());
			dto.column      = pos.getClassicColumn();

			if (!sr.valid()) {
				dto.outcome = "INVALID";
			} else if (sr.repeated()) {
				dto.outcome = "REPEATED";
			} else if (sr.ship() == null) {
				dto.outcome = "MISS";
			} else if (sr.sunk()) {
				dto.outcome  = "SUNK";
				dto.shipType = sr.ship().getCategory();
			} else {
				dto.outcome  = "HIT";
				dto.shipType = sr.ship().getCategory();
			}
			results.add(dto);
		}

		ShotResponse response  = new ShotResponse();
		response.results       = results;
		response.shipsRemaining = game.getRemainingShips();
		response.gameStatus    = "ONGOING";
		response.winner        = null;
		return response;
	}

	/** Convenience helper to build a JSON error response. */
	private ResponseEntity<ErrorResponse> error(HttpStatus status, String message) {
		return ResponseEntity.status(status).body(new ErrorResponse(message));
	}
}