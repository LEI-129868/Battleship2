package restserver;

import battleship.Fleet;
import battleship.Game;
import battleship.IGame;

/**
 * Holds all state for one active game between the AI opponent and a student player.
 *
 * One GameSession is created per registration (m0) and stored in the GameRegistry
 * keyed by its gameId. It wraps the existing Game class without modifying it.
 */
public class GameSession {

	/** Unique identifier for this game, returned to the student on registration. */
	private final String gameId;

	/** The student's name, for logging / display purposes. */
	private final String playerName;

	/**
	 * The URL of the student's server where the AI will POST its shots (m2a).
	 * Example: "http://student-host:9090"
	 * The AI will call POST {callbackUrl}/game/{gameId}/shots
	 */
	private final String callbackUrl;

	/**
	 * The core game object from the existing codebase.
	 * myFleet = AI's fleet  (receives student's shots)
	 * alienFleet = tracked knowledge of student's fleet
	 */
	private final IGame game;

	/** Number of shots fired per turn (matches Game.NUMBER_SHOTS = 3). */
	private final int shotsPerTurn;

	/** True once one fleet is completely sunk. */
	private boolean gameOver;

	/** "AI_WINS" or "STUDENT_WINS" — set when gameOver becomes true. */
	private String winner;

	// -------------------------------------------------------------------------

	public GameSession(String gameId, String playerName, String callbackUrl) {
		this.gameId      = gameId;
		this.playerName  = playerName;
		this.callbackUrl = callbackUrl;
		this.game        = new Game(Fleet.createRandom()); // AI places its own fleet randomly
		this.shotsPerTurn = Game.NUMBER_SHOTS;
		this.gameOver    = false;
		this.winner      = null;
	}

	// ── Getters ──────────────────────────────────────────────────────────────

	public String getGameId()      { return gameId; }
	public String getPlayerName()  { return playerName; }
	public String getCallbackUrl() { return callbackUrl; }
	public IGame  getGame()        { return game; }
	public int    getShotsPerTurn(){ return shotsPerTurn; }
	public boolean isGameOver()    { return gameOver; }
	public String getWinner()      { return winner; }

	// ── State transitions ────────────────────────────────────────────────────

	public void markAiWins() {
		this.gameOver = true;
		this.winner   = "AI_WINS";
	}

	public void markStudentWins() {
		this.gameOver = true;
		this.winner   = "STUDENT_WINS";
	}
}