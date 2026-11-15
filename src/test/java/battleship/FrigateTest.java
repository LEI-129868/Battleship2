package battleship;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários de Frigate.
 * Author: franciscosilva
 * Date: 2026-11-15 13:50 WET
 * Cyclomatic Complexity (CC):
 * - Frigate(Compass, IPosition): 9 na métrica do MetricsTree.
 *   Para uma fragata válida, os quatro rumos executam os respetivos ciclos de quatro posições.
 *   Os dois argumentos nulos são rejeitados pelo construtor de Ship.
 */
class FrigateTest {
	private static final Position START = new Position(5, 5);
	private Frigate frigate;

	@BeforeEach
	void setUp() {
		frigate = new Frigate(Compass.NORTH, START);
	}

	@AfterEach
	void tearDown() {
		frigate = null;
	}

	@Test
	@DisplayName("Norte ocupa quatro linhas na mesma coluna")
	void constructor1() {
		assertFrigate(Compass.NORTH,
				List.of(new Position(5, 5), new Position(6, 5), new Position(7, 5), new Position(8, 5)));
	}

	@Test
	@DisplayName("Sul ocupa quatro linhas na mesma coluna")
	void constructor2() {
		frigate = new Frigate(Compass.SOUTH, START);
		assertFrigate(Compass.SOUTH,
				List.of(new Position(5, 5), new Position(6, 5), new Position(7, 5), new Position(8, 5)));
	}

	@Test
	@DisplayName("Este ocupa quatro colunas na mesma linha")
	void constructor3() {
		frigate = new Frigate(Compass.EAST, START);
		assertFrigate(Compass.EAST,
				List.of(new Position(5, 5), new Position(5, 6), new Position(5, 7), new Position(5, 8)));
	}

	@Test
	@DisplayName("Oeste ocupa quatro colunas na mesma linha")
	void constructor4() {
		frigate = new Frigate(Compass.WEST, START);
		assertFrigate(Compass.WEST,
				List.of(new Position(5, 5), new Position(5, 6), new Position(5, 7), new Position(5, 8)));
	}

	@Test
	@DisplayName("Rejeita uma fragata sem rumo")
	void constructor5() {
		assertThrows(NullPointerException.class, () -> new Frigate(null, START),
				"Um rumo nulo devia lançar NullPointerException.");
	}

	@Test
	@DisplayName("Rejeita uma fragata sem posição inicial")
	void constructor6() {
		assertThrows(NullPointerException.class, () -> new Frigate(Compass.NORTH, null),
				"Uma posição inicial nula devia lançar NullPointerException.");
	}

	@Test
	@DisplayName("Uma fragata nova ou atingida uma vez mantém-se à tona")
	void stillFloatingBeforeLastHit() {
		assertTrue(frigate.stillFloating(), "Uma fragata nova devia flutuar.");
		frigate.shoot(new Position(5, 5));
		assertTrue(frigate.stillFloating(), "Uma fragata com uma posição atingida devia continuar a flutuar.");
	}

	@Test
	@DisplayName("A fragata afunda ao perder as quatro posições")
	void sinksAfterFourHits() {
		List<IPosition> positions = List.copyOf(frigate.getPositions());
		positions.forEach(frigate::shoot);
		assertFalse(frigate.stillFloating(), "A fragata devia afundar depois de todas as posições serem atingidas.");
	}

	private void assertFrigate(Compass expectedBearing, List<IPosition> expectedPositions) {
		assertAll(
				() -> assertEquals("Fragata", frigate.getCategory(), "A categoria devia ser Fragata."),
				() -> assertEquals(expectedBearing, frigate.getBearing(), "O rumo devia coincidir com o argumento."),
				() -> assertEquals(4, frigate.getSize(), "A fragata devia ter tamanho 4."),
				() -> assertEquals(expectedPositions, frigate.getPositions(), "As quatro coordenadas deviam coincidir.")
		);
	}
}
