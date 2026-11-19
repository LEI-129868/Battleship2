package restserver;

import battleship.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class AiShotStrategy.
 * The private helpers getHitButNotSunkPositions() and buildHuntPool() are covered through computeShots().
 * Author: ${user.name}
 * Date: 2026-11-19 15:30
 * Cyclomatic Complexity:
 * - constructor (default): 1
 * - computeShots(): 20
 * - getHitButNotSunkPositions() (private): 10 (coberto via computeShots)
 * - buildHuntPool() (private): 6 (coberto via computeShots)
 */
class AiShotStrategyTest {

	private AiShotStrategy strategy;
	private Game game;
	private IShip anyShip;
	private int moveNumber;

	@BeforeEach
	void setUp() {
		strategy = new AiShotStrategy();
		game = new Game(new Fleet());
		anyShip = new Barge(Compass.NORTH, new Position(0, 0));
		moveNumber = 1;
	}

	@AfterEach
	void tearDown() {
		strategy = null;
		game = null;
		anyShip = null;
	}

	// ── helpers ──────────────────────────────────────────────────────────────

	/** Records a previous AI move (shots + optional results) in the game. */
	private void addAlienMove(List<IPosition> shots, List<IGame.ShotResult> results) {
		game.getAlienMoves().add(new Move(moveNumber++, shots, results));
	}

	private IGame.ShotResult hitNotSunk() {
		return new IGame.ShotResult(true, false, anyShip, false);
	}

	private IGame.ShotResult water() {
		return new IGame.ShotResult(true, false, null, false);
	}

	/** Every board cell, except the ones given, as already-shot positions (no results). */
	private List<IPosition> allCellsExcept(List<IPosition> excluded, boolean onlyEven) {
		List<IPosition> cells = new ArrayList<>();
		for (int r = 0; r < Game.BOARD_SIZE; r++)
			for (int c = 0; c < Game.BOARD_SIZE; c++) {
				IPosition p = new Position(r, c);
				if ((!onlyEven || (r + c) % 2 == 0) && !excluded.contains(p))
					cells.add(p);
			}
		return cells;
	}

	private static boolean isEvenCell(IPosition p) {
		return (p.getRow() + p.getColumn()) % 2 == 0;
	}

	private static boolean allDistinct(List<IPosition> shots) {
		Set<String> seen = new HashSet<>();
		for (IPosition p : shots)
			if (!seen.add(p.getRow() + "," + p.getColumn()))
				return false;
		return true;
	}

	private void assertHuntOnly(List<IPosition> shots, List<IPosition> previous) {
		assertAll("Só fase HUNT (padrão xadrez)",
				() -> assertEquals(Game.NUMBER_SHOTS, shots.size(),
						"Error: expected " + Game.NUMBER_SHOTS + " shots but got " + shots.size()),
				() -> assertTrue(shots.stream().allMatch(AiShotStrategyTest::isEvenCell),
						"Error: expected only checkerboard cells ((row+col) even) but got " + shots),
				() -> assertTrue(allDistinct(shots), "Error: expected distinct shots but got " + shots),
				() -> assertTrue(shots.stream().noneMatch(previous::contains),
						"Error: expected no repeated positions from " + previous + " but got " + shots));
	}

	// ── computeShots ─────────────────────────────────────────────────────────

	@Test
	@DisplayName("computeShots: jogo sem jogadas anteriores usa só a fase HUNT")
	void computeShots1() {
		List<IPosition> shots = strategy.computeShots(game);

		assertHuntOnly(shots, List.of());
	}

	@Test
	@DisplayName("computeShots: jogada anterior sem resultados (lista vazia) não gera alvos e não repete tiros")
	void computeShots2() {
		List<IPosition> previous = List.of(new Position(0, 0));
		addAlienMove(previous, List.of());

		List<IPosition> shots = strategy.computeShots(game);

		assertHuntOnly(shots, previous);
	}

	@Test
	@DisplayName("computeShots: jogada com menos resultados do que tiros trata o tiro sem resultado como desconhecido")
	void computeShots3() {
		List<IPosition> previous = List.of(new Position(0, 0), new Position(0, 2));
		addAlienMove(previous, List.of(water()));

		List<IPosition> shots = strategy.computeShots(game);

		assertHuntOnly(shots, previous);
	}

	@Test
	@DisplayName("computeShots: um acerto não afundado em (5,5) leva a disparar nos 3 primeiros vizinhos (TARGET)")
	void computeShots4() {
		addAlienMove(List.of(new Position(5, 5)), List.of(hitNotSunk()));

		List<IPosition> shots = strategy.computeShots(game);

		List<IPosition> expected = List.of(new Position(4, 5), new Position(5, 6), new Position(6, 5));
		assertEquals(expected, shots, "Error: expected TARGET shots " + expected + " but got " + shots);
	}

	@Test
	@DisplayName("computeShots: TARGET só arranja 1 vizinho livre e a fase HUNT completa os restantes")
	void computeShots5() {
		List<IPosition> previous = List.of(new Position(0, 0), new Position(0, 1), new Position(1, 0));
		addAlienMove(previous, List.of(hitNotSunk(), water(), water()));

		List<IPosition> shots = strategy.computeShots(game);

		assertAll("TARGET + HUNT",
				() -> assertEquals(3, shots.size(), "Error: expected 3 shots but got " + shots.size()),
				() -> assertEquals(new Position(1, 1), shots.get(0),
						"Error: expected first shot at the only free neighbour (1,1) but got " + shots.get(0)),
				() -> assertTrue(shots.subList(1, 3).stream().allMatch(AiShotStrategyTest::isEvenCell),
						"Error: expected the remaining shots from the checkerboard but got " + shots.subList(1, 3)),
				() -> assertTrue(allDistinct(shots), "Error: expected distinct shots but got " + shots),
				() -> assertTrue(shots.stream().noneMatch(previous::contains),
						"Error: expected no repeated positions but got " + shots));
	}

	@Test
	@DisplayName("computeShots: com dois acertos, o 1.º já preenche os 3 tiros e o 2.º é ignorado (break exterior)")
	void computeShots6() {
		addAlienMove(List.of(new Position(5, 5), new Position(2, 2)), List.of(hitNotSunk(), hitNotSunk()));

		List<IPosition> shots = strategy.computeShots(game);

		List<IPosition> expected = List.of(new Position(4, 5), new Position(5, 6), new Position(6, 5));
		assertEquals(expected, shots, "Error: expected only neighbours of (5,5) " + expected + " but got " + shots);
	}

	@Test
	@DisplayName("computeShots: vizinho já escolhido para outro acerto não é escolhido duas vezes")
	void computeShots7() {
		// (5,5) e (6,6) são acertos; todos os vizinhos de (5,5) já foram tiro, exceto (5,6).
		// (5,6) é também o 1.º vizinho (norte) de (6,6), portanto já estará em 'chosen'.
		List<IPosition> previous = List.of(
				new Position(5, 5), new Position(6, 6),
				new Position(4, 5), new Position(6, 5), new Position(5, 4),
				new Position(6, 4), new Position(4, 6), new Position(4, 4));
		List<IGame.ShotResult> results = List.of(
				hitNotSunk(), hitNotSunk(),
				water(), water(), water(), water(), water(), water());
		addAlienMove(previous, results);

		List<IPosition> shots = strategy.computeShots(game);

		List<IPosition> expected = List.of(new Position(5, 6), new Position(6, 7), new Position(7, 6));
		assertAll("Vizinho partilhado",
				() -> assertEquals(expected, shots, "Error: expected " + expected + " but got " + shots),
				() -> assertTrue(allDistinct(shots), "Error: expected (5,6) only once but got " + shots));
	}

	@Test
	@DisplayName("computeShots: resultado inválido (valid=false) não é considerado alvo")
	void computeShots8() {
		List<IPosition> previous = List.of(new Position(5, 5));
		addAlienMove(previous, List.of(new IGame.ShotResult(false, false, anyShip, false)));

		assertHuntOnly(strategy.computeShots(game), previous);
	}

	@Test
	@DisplayName("computeShots: tiro repetido (repeated=true) não é considerado alvo")
	void computeShots9() {
		List<IPosition> previous = List.of(new Position(5, 5));
		addAlienMove(previous, List.of(new IGame.ShotResult(true, true, anyShip, false)));

		assertHuntOnly(strategy.computeShots(game), previous);
	}

	@Test
	@DisplayName("computeShots: tiro na água (ship=null) não é considerado alvo")
	void computeShots10() {
		List<IPosition> previous = List.of(new Position(5, 5));
		addAlienMove(previous, List.of(water()));

		assertHuntOnly(strategy.computeShots(game), previous);
	}

	@Test
	@DisplayName("computeShots: navio já afundado (sunk=true) não é considerado alvo")
	void computeShots11() {
		List<IPosition> previous = List.of(new Position(5, 5));
		addAlienMove(previous, List.of(new IGame.ShotResult(true, false, anyShip, true)));

		assertHuntOnly(strategy.computeShots(game), previous);
	}

	@Test
	@DisplayName("computeShots: sem casas pares livres, o fallback escolhe as primeiras casas ímpares livres")
	void computeShots12() {
		addAlienMove(allCellsExcept(List.of(), true), List.of());

		List<IPosition> shots = strategy.computeShots(game);

		List<IPosition> expected = List.of(new Position(0, 1), new Position(0, 3), new Position(0, 5));
		assertEquals(expected, shots, "Error: expected fallback shots " + expected + " but got " + shots);
	}

	@Test
	@DisplayName("computeShots: HUNT só tem 1 casa livre e o fallback completa os restantes tiros")
	void computeShots13() {
		addAlienMove(allCellsExcept(List.of(new Position(9, 9)), true), List.of());

		List<IPosition> shots = strategy.computeShots(game);

		List<IPosition> expected = List.of(new Position(9, 9), new Position(0, 1), new Position(0, 3));
		assertEquals(expected, shots, "Error: expected HUNT (9,9) + fallback " + expected + " but got " + shots);
	}

	@Test
	@DisplayName("computeShots: tabuleiro quase cheio devolve menos de NUMBER_SHOTS tiros (só os 2 livres)")
	void computeShots14() {
		List<IPosition> free = List.of(new Position(0, 1), new Position(9, 8));
		addAlienMove(allCellsExcept(free, false), List.of());

		List<IPosition> shots = strategy.computeShots(game);

		assertAll("Tabuleiro quase cheio",
				() -> assertEquals(2, shots.size(),
						"Error: expected only 2 shots (the 2 free cells) but got " + shots.size()),
				() -> assertEquals(free, shots, "Error: expected " + free + " but got " + shots));
	}

	@Test
	@DisplayName("computeShots: acerto com todos os vizinhos já atingidos não gera tiros TARGET e a fase HUNT preenche os 3")
	void computeShots15() {
		List<IPosition> previous = List.of(new Position(0, 0), new Position(0, 1), new Position(1, 0), new Position(1, 1));
		addAlienMove(previous, List.of(hitNotSunk(), water(), water(), water()));

		assertHuntOnly(strategy.computeShots(game), previous);
	}

	@Test
	@DisplayName("computeShots: tiros de várias jogadas anteriores são todos excluídos")
	void computeShots16() {
		List<IPosition> first = List.of(new Position(0, 0), new Position(0, 2));
		List<IPosition> second = List.of(new Position(2, 0), new Position(2, 2));
		addAlienMove(first, List.of(water(), water()));
		addAlienMove(second, List.of(water(), water()));
		List<IPosition> previous = new ArrayList<>(first);
		previous.addAll(second);

		assertHuntOnly(strategy.computeShots(game), previous);
	}

	@Test
	@DisplayName("computeShots: 1.º acerto dá 2 tiros, o 2.º acerto dá o 3.º e o ciclo interior termina (break interior)")
	void computeShots17() {
		List<IPosition> previous = List.of(new Position(0, 0), new Position(0, 1), new Position(9, 9));
		addAlienMove(previous, List.of(hitNotSunk(), water(), hitNotSunk()));

		List<IPosition> shots = strategy.computeShots(game);

		List<IPosition> expected = List.of(new Position(1, 0), new Position(1, 1), new Position(8, 9));
		assertEquals(expected, shots, "Error: expected " + expected + " but got " + shots);
	}

	@Test
	@DisplayName("computeShots: HUNT com exatamente 3 casas livres usa-as todas e o fallback não é usado")
	void computeShots18() {
		List<IPosition> free = List.of(new Position(0, 0), new Position(4, 4), new Position(9, 9));
		addAlienMove(allCellsExcept(free, true), List.of());

		List<IPosition> shots = strategy.computeShots(game);

		assertAll("HUNT com 3 casas",
				() -> assertEquals(3, shots.size(), "Error: expected 3 shots but got " + shots.size()),
				() -> assertTrue(shots.containsAll(free), "Error: expected exactly " + free + " (any order) but got " + shots));
	}

	@Test
	@DisplayName("computeShots: o fallback salta a casa livre que a fase HUNT já escolheu (!chosen.contains falso)")
	void computeShots19() {
		// Só (0,0) [par], (0,1) e (0,3) [ímpares] estão livres: HUNT escolhe (0,0);
		// o fallback começa em (0,0), que já está em 'chosen', e salta-a.
		List<IPosition> free = List.of(new Position(0, 0), new Position(0, 1), new Position(0, 3));
		addAlienMove(allCellsExcept(free, false), List.of());

		List<IPosition> shots = strategy.computeShots(game);

		assertAll("Fallback ignora casa já escolhida",
				() -> assertEquals(free, shots, "Error: expected " + free + " but got " + shots),
				() -> assertTrue(allDistinct(shots), "Error: expected (0,0) only once but got " + shots));
	}

	@Test
	@DisplayName("computeShots: tabuleiro totalmente atingido devolve uma lista vazia")
	void computeShots20() {
		addAlienMove(allCellsExcept(List.of(), false), List.of());

		List<IPosition> shots = strategy.computeShots(game);

		assertTrue(shots.isEmpty(), "Error: expected no shots on a fully shot board but got " + shots);
	}
}
