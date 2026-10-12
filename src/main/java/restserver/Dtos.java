package restserver;

import java.util.List;

/**
 * Data Transfer Objects (DTOs) for the Battleship REST protocol.
 *
 * These are plain Java classes (POJOs). Spring Boot + Jackson automatically
 * serialises them to/from JSON — no extra annotations needed for basic use.
 *
 * Kept in a single file so students have one place to read the full contract.
 */
public class Dtos {

	// =========================================================================
	// m0 — Registration
	// =========================================================================

	/**
	 * m0 REQUEST  →  POST /register
	 *
	 * Example JSON:
	 * {
	 *   "playerName": "Alice",
	 *   "callbackUrl": "http://student-host:9090"
	 * }
	 */
	public static class RegistrationRequest {
		public String playerName;
		public String callbackUrl;
	}

	/**
	 * m0 RESPONSE  ←  200 OK
	 *
	 * Example JSON:
	 * {
	 *   "gameId":      "550e8400-e29b-41d4-a716-446655440000",
	 *   "status":      "REGISTERED",
	 *   "shotsPerTurn": 3,
	 *   "message":     "Game ready. You shoot first."
	 * }
	 */
	public static class RegistrationResponse {
		public String gameId;
		public String status;
		public int    shotsPerTurn;
		public String message;
	}

	// =========================================================================
	// m1a / m2a — Shot list  (same structure in both directions)
	// =========================================================================

	/**
	 * m1a REQUEST  →  POST /game/{gameId}/shots      (student → AI)
	 * m2a REQUEST  →  POST {callbackUrl}/game/{gameId}/shots  (AI → student)
	 *
	 * Each position uses the classic notation: row = "A".."J", column = 1..10
	 *
	 * Example JSON:
	 * {
	 *   "shots": [
	 *     { "row": "B", "column": 3 },
	 *     { "row": "G", "column": 7 },
	 *     { "row": "D", "column": 10 }
	 *   ]
	 * }
	 */
	public static class ShotRequest {
		public List<ShotPosition> shots;
	}

	/** A single position in classic notation. */
	public static class ShotPosition {
		public String row;     // "A" .. "J"
		public int    column;  // 1  .. 10
	}

	// =========================================================================
	// m1b / m2b — Cumulative shot results  (same structure in both directions)
	// =========================================================================

	/**
	 * m1b RESPONSE  ←  200 OK  (AI → student, result of student's shots)
	 * m2b RESPONSE  ←  200 OK  (student → AI, result of AI's shots)
	 *
	 * Example JSON:
	 * {
	 *   "results": [
	 *     { "row": "B", "column": 3,  "outcome": "MISS" },
	 *     { "row": "G", "column": 7,  "outcome": "HIT",  "shipType": "caravela" },
	 *     { "row": "D", "column": 10, "outcome": "SUNK", "shipType": "galeao"   }
	 *   ],
	 *   "shipsRemaining": 8,
	 *   "gameStatus": "ONGOING"
	 * }
	 *
	 * gameStatus values: "ONGOING" | "GAME_OVER"
	 * outcome values:    "MISS"    | "HIT" | "SUNK" | "REPEATED" | "INVALID"
	 */
	public static class ShotResponse {
		public List<ShotResult> results;
		public int    shipsRemaining;
		public String gameStatus;
		public String winner;          // null when ONGOING; "AI_WINS" or "STUDENT_WINS" when GAME_OVER
	}

	/** Result for one individual shot. */
	public static class ShotResult {
		public String row;
		public int    column;
		public String outcome;   // "MISS" | "HIT" | "SUNK" | "REPEATED" | "INVALID"
		public String shipType;  // null unless outcome is HIT or SUNK
	}

	// =========================================================================
	// Error response
	// =========================================================================

	/**
	 * Returned with HTTP 4xx when something goes wrong.
	 *
	 * Example JSON:
	 * { "error": "Game not found: abc-123" }
	 */
	public static class ErrorResponse {
		public String error;
		public ErrorResponse(String error) { this.error = error; }
	}
}