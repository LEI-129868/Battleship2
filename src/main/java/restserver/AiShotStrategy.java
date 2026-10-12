package restserver;

import battleship.*;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * AI shooting strategy using a Hunt and Target approach.
 *
 * Phase 1 — HUNT:   fire at a checkerboard pattern of cells not yet tried.
 * Phase 2 — TARGET: when a HIT is recorded, attack adjacent cells until the
 *                   ship is SUNK, then revert to HUNT.
 *
 * This is intentionally simple so students can understand and beat it.
 * You can make it harder by replacing the checkerboard hunt with a
 * probability-density map, or easier by using purely random shots.
 */
@Component
public class AiShotStrategy {

	/**
	 * Compute the next set of AI shots based on current game knowledge.
	 *
	 * The game object holds alienMoves (AI's previous shots) which we inspect
	 * to avoid repeating positions and to identify hit-but-not-sunk cells.
	 *
	 * @param game  the current game (used to read past moves and fleet state)
	 * @return      a list of exactly Game.NUMBER_SHOTS positions
	 */
	public List<IPosition> computeShots(IGame game) {

		// Build set of all positions already shot by the AI
		Set<IPosition> alreadyShot = new HashSet<>();
		for (IMove move : game.getAlienMoves()) {
			alreadyShot.addAll(move.getShots());
		}

		// Collect hit-but-not-sunk positions (TARGET phase candidates)
		List<IPosition> hitTargets = getHitButNotSunkPositions(game, alreadyShot);

		List<IPosition> chosen = new ArrayList<>();

		// ── Phase 2 TARGET: prioritise neighbours of existing hits ──────────
		if (!hitTargets.isEmpty()) {
			for (IPosition hit : hitTargets) {
				if (chosen.size() >= Game.NUMBER_SHOTS) break;
				for (IPosition neighbour : hit.adjacentPositions()) {
					if (chosen.size() >= Game.NUMBER_SHOTS) break;
					if (!alreadyShot.contains(neighbour) && !chosen.contains(neighbour)) {
						chosen.add(neighbour);
					}
				}
			}
		}

		// ── Phase 1 HUNT: fill remaining slots from checkerboard pattern ────
		if (chosen.size() < Game.NUMBER_SHOTS) {
			List<IPosition> huntPool = buildHuntPool(alreadyShot, chosen);
			Collections.shuffle(huntPool, new Random(System.currentTimeMillis()));
			for (IPosition p : huntPool) {
				if (chosen.size() >= Game.NUMBER_SHOTS) break;
				chosen.add(p);
			}
		}

		// Safety fallback: if still not enough shots, pick any unseen cell
		if (chosen.size() < Game.NUMBER_SHOTS) {
			for (int r = 0; r < Game.BOARD_SIZE && chosen.size() < Game.NUMBER_SHOTS; r++) {
				for (int c = 0; c < Game.BOARD_SIZE && chosen.size() < Game.NUMBER_SHOTS; c++) {
					IPosition p = new Position(r, c);
					if (!alreadyShot.contains(p) && !chosen.contains(p)) {
						chosen.add(p);
					}
				}
			}
		}

		System.out.print("[AI] Firing: ");
		chosen.forEach(p -> System.out.print(p + " "));
		System.out.println();

		return chosen;
	}

	// ── Private helpers ───────────────────────────────────────────────────────

	/**
	 * Returns positions the AI has HIT in a previous move but the ship is not yet sunk.
	 * We detect "sunk" by looking at whether all adjacent positions of a hit cluster
	 * have also been hit — in the absence of a direct reference to the student's fleet,
	 * we use a simple heuristic: a position is a "live target" if it was hit and at
	 * least one of its orthogonal neighbours has NOT been shot yet.
	 */
	private List<IPosition> getHitButNotSunkPositions(IGame game, Set<IPosition> alreadyShot) {

		// Gather all positions the AI hit
		List<IPosition> hits = new ArrayList<>();
		for (IMove move : game.getAlienMoves()) {
			for (int i = 0; i < move.getShots().size(); i++) {
				IGame.ShotResult result = move.getShotResults().isEmpty()
						? null
						: (i < move.getShotResults().size() ? move.getShotResults().get(i) : null);

				// If we have result metadata, use it precisely
				if (result != null && result.valid() && !result.repeated()
						&& result.ship() != null && !result.sunk()) {
					hits.add(move.getShots().get(i));
				}
			}
		}
		return hits;
	}

	/**
	 * Builds a checkerboard-patterned pool of candidate positions for the HUNT phase.
	 * Using only cells where (row + col) % 2 == 0 means minimum ship size 2
	 * (Barca) will always intersect at least one cell — halves the search space.
	 */
	private List<IPosition> buildHuntPool(Set<IPosition> alreadyShot, List<IPosition> alreadyChosen) {
		List<IPosition> pool = new ArrayList<>();
		for (int r = 0; r < Game.BOARD_SIZE; r++) {
			for (int c = 0; c < Game.BOARD_SIZE; c++) {
				if ((r + c) % 2 == 0) {
					IPosition p = new Position(r, c);
					if (!alreadyShot.contains(p) && !alreadyChosen.contains(p)) {
						pool.add(p);
					}
				}
			}
		}
		return pool;
	}
}