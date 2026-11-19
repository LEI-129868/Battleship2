package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.InputMismatchException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for interface IMove (static method readMove).
 * IMove is an interface, so the object under test is the Move returned by readMove.
 * Author: ${user.name}
 * Date: 2026-11-19 15:30
 * Cyclomatic Complexity:
 * - readMove(): 2
 */
class IMoveTest {

	private Move move;

	@BeforeEach
	void setUp() {
		move = null;
	}

	@AfterEach
	void tearDown() {
		move = null;
	}

	@Test
	@DisplayName("readMove: com 0 tiros o ciclo não executa e a jogada fica sem tiros")
	void readMove1() {
		move = IMove.readMove(4, new Scanner("0"));

		assertAll("Jogada sem tiros",
				() -> assertNotNull(move, "Error: expected a Move but got null"),
				() -> assertEquals(4, move.getNumber(), "Error: expected move number 4 but got " + move.getNumber()),
				() -> assertTrue(move.getShots().isEmpty(),
						"Error: expected no shots but got " + move.getShots().size()),
				() -> assertTrue(move.getShotResults().isEmpty(),
						"Error: expected no shot results but got " + move.getShotResults().size()));
	}

	@Test
	@DisplayName("readMove: com 3 tiros o ciclo executa 3 vezes e lê as posições por ordem")
	void readMove2() {
		move = IMove.readMove(1, new Scanner("3 0 0 1 1 9 9"));
		List<IPosition> shots = move.getShots();

		assertAll("Jogada com 3 tiros",
				() -> assertEquals(1, move.getNumber(), "Error: expected move number 1 but got " + move.getNumber()),
				() -> assertEquals(3, shots.size(), "Error: expected 3 shots but got " + shots.size()),
				() -> assertEquals(new Position(0, 0), shots.get(0), "Error: expected first shot (0,0) but got " + shots.get(0)),
				() -> assertEquals(new Position(1, 1), shots.get(1), "Error: expected second shot (1,1) but got " + shots.get(1)),
				() -> assertEquals(new Position(9, 9), shots.get(2), "Error: expected third shot (9,9) but got " + shots.get(2)),
				() -> assertTrue(move.getShotResults().isEmpty(),
						"Error: expected an empty results list but got " + move.getShotResults().size()));
	}

	@Test
	@DisplayName("readMove: input vazio lança NoSuchElementException")
	void readMoveThrowsNoSuchElement() {
		assertThrows(NoSuchElementException.class, () -> IMove.readMove(1, new Scanner("")),
				"Error: expected NoSuchElementException for empty input but nothing (or another exception) was thrown");
	}

	@Test
	@DisplayName("readMove: input não numérico lança InputMismatchException")
	void readMoveThrowsInputMismatch() {
		assertThrows(InputMismatchException.class, () -> IMove.readMove(1, new Scanner("abc")),
				"Error: expected InputMismatchException for non-numeric input but nothing (or another exception) was thrown");
	}

	@Test
	@DisplayName("readMove: input com menos coordenadas do que as anunciadas lança NoSuchElementException")
	void readMoveThrowsIncompleteInput() {
		assertThrows(NoSuchElementException.class, () -> IMove.readMove(1, new Scanner("2 0 0 5")),
				"Error: expected NoSuchElementException for incomplete coordinates but nothing (or another exception) was thrown");
	}
}
