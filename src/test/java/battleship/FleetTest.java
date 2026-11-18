package battleship;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.ArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Proxy;

/**
 * Test class for Fleet.
 * Author: Tomás Costa - 129836
 * Date: 16-11-2026
 * Time: 12:15
 * Cyclomatic Complexity for each method:
 * - Constructor: 1
 * - addShip: 3
 * - getShips: 1
 * - getShipsLike: 2
 * - getFloatingShips: 2
 * - shipAt: 2
 * - isInsideBoard: 3
 * - colisionRisk: 2
 */
public class FleetTest {

	private Fleet fleet;

	@BeforeEach
	void setUp() {
		fleet = new Fleet();
	}

	@AfterEach
	void tearDown() {
		fleet = null;
	}

	/**
	 * Test for the Fleet constructor.
	 * Cyclomatic Complexity: 1
	 */
	@Test
	void testConstructor() {
		assertNotNull(fleet, "Error: Instance of Fleet should not be null.");
		assertTrue(fleet.getShips().isEmpty(), "Error: Fleet should be initialized with empty ships list.");
	}

	@Test
	void testCreateRandom(){
		IFleet r = Fleet.createRandom();
		assertNotNull(r, "Error: createRandom should return a non-null fleet instance.");
		assertEquals(Fleet.FLEET_SIZE, r.getShips().size(), "Error: createRandom should create a fleet with the correct number of ships.");
	}

	/**
	 * Test for the addShip method (all conditions true).
	 * Cyclomatic Complexity: 3
	 */
	@Test
	void testAddShip1() {
		IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
		assertTrue(fleet.addShip(ship), "Error: Valid ship should be added successfully.");
		assertEquals(1, fleet.getShips().size(), "Error: Fleet should contain one ship after addition.");
	}

	/**
	 * Test for the addShip method (fleet size limit reached).
	 */
	@Test
	void testAddShip2() {
		for (int i = 0; i < Fleet.FLEET_SIZE; i++) {
			fleet.addShip(new Barge(Compass.NORTH, new Position(i, 0)));
		}
		IShip anotherShip = new Barge(Compass.NORTH, new Position(10, 10));
		assertFalse(fleet.addShip(anotherShip), "Error: Should not add ship when fleet size limit is reached.");
	}

	/**
	 * Test for the addShip method (ship outside the board).
	 */
	@Test
	void testAddShip3() {
		IShip shipOutside = new Barge(Compass.NORTH, new Position(99, 99));
		assertFalse(fleet.addShip(shipOutside), "Error: Should not add ship outside the board.");
	}

	/**
	 * Test for the addShip method (collision risk).
	 */
	@Test
	void testAddShip4() {
		IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
		IShip ship2 = new Barge(Compass.NORTH, new Position(1, 1));  // Overlapping position
		fleet.addShip(ship1);
		assertFalse(fleet.addShip(ship2), "Error: Should not add ship with a collision risk.");
	}

	/**
	 * Test for the getShips method.
	 * Cyclomatic Complexity: 1
	 */
	@Test
	void testGetShips() {
		assertTrue(fleet.getShips().isEmpty(), "Error: Fleet's ships list should initially be empty.");
		IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
		fleet.addShip(ship);
		assertEquals(1, fleet.getShips().size(), "Error: Fleet should have size 1 after adding a ship.");
		assertEquals(ship, fleet.getShips().get(0), "Error: Fleet's first ship should match the added ship.");
	}

	/**
	 * Test for the getShipsLike method (ships of specific category).
	 * Cyclomatic Complexity: 2
	 */
	@Test
	void testGetShipsLike() {
		IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
		IShip ship2 = new Caravel(Compass.NORTH, new Position(2, 1));
		fleet.addShip(ship1);
		fleet.addShip(ship2);

		List<IShip> barges = fleet.getShipsLike("Barca");
		assertEquals(1, barges.size(), "Error: There should be exactly one ship of category 'Barca'.");
		assertEquals(ship1, barges.get(0), "Error: The ship of category 'Barca' does not match.");
	}

	/**
	 * Test for the getFloatingShips method.
	 * Cyclomatic Complexity: 2
	 */
	@Test
	void testGetFloatingShips() {
		IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
		IShip ship2 = new Caravel(Compass.NORTH, new Position(4, 4));
		fleet.addShip(ship1);
		fleet.addShip(ship2);

		List<IShip> floatingShips = fleet.getFloatingShips();
		assertEquals(2, floatingShips.size(), "Error: All ships should be floating initially.");

		ship1.getPositions().get(0).shoot();  // Sink ship1
		floatingShips = fleet.getFloatingShips();
		assertEquals(1, floatingShips.size(), "Error: Only one ship should be floating after sinking one.");
		assertEquals(ship2, floatingShips.get(0), "Error: The floating ship should match the expected result.");
	}

	/**
	 * Test for the shipAt method.
	 * Cyclomatic Complexity: 2
	 */
	@Test
	void testShipAt() {
		IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
		fleet.addShip(ship);

		assertEquals(ship, fleet.shipAt(new Position(1, 1)), "Error: Should return the correct ship at the position.");
		assertNull(fleet.shipAt(new Position(5, 5)), "Error: Should return null for empty positions in the fleet.");
	}


	/**
	 * Test for private method colisionRisk.
	 * Cyclomatic Complexity: 2
	 */
	@Test
	void testColisionRisk() throws Exception {
		var method = Fleet.class.getDeclaredMethod("colisionRisk", IShip.class);
		method.setAccessible(true);

		IShip ship1 = new Barge(Compass.NORTH, new Position(1, 1));
		IShip ship2 = new Barge(Compass.NORTH, new Position(1, 1));  // Overlapping position
		fleet.addShip(ship1);

		assertTrue((Boolean) method.invoke(fleet, ship2), "Error: Overlapping ships should be at collision risk.");
		assertFalse((Boolean) method.invoke(fleet, new Barge(Compass.NORTH, new Position(5, 5))),
				"Error: Ships at non-overlapping positions should not have a collision risk.");
	}

	/**
	 * Test for the printStatus method.
	 * Cyclomatic Complexity: 1
	 */
	@Test
	void testPrintStatus() {
		IShip ship = new Barge(Compass.NORTH, new Position(1, 1));
		fleet.addShip(ship);
		assertDoesNotThrow(fleet::printStatus, "Error: printStatus should not throw any exceptions.");
	}

	/** Additional independent paths, preserving the original tests. */
	@Nested
	class AdditionalCoverage {

		private Fleet fleet;

		@BeforeEach
		void setUp() { fleet = new Fleet(); }

		@AfterEach
		void tearDown() { fleet = null; }

		/** Fake interface implementing only behaviors needed by Fleet. */
		private static IShip ship(String category, boolean inside, boolean floating,
								  boolean close, boolean occupies) {
			return (IShip) Proxy.newProxyInstance(IShip.class.getClassLoader(),
					new Class<?>[]{IShip.class}, (proxy, method, args) -> {
						switch (method.getName()) {
							case "getCategory": return category;
							case "isInsideBoard": return inside;
							case "stillFloating": return floating;
							case "tooCloseTo": return close;
							case "occupies": return occupies;
							case "getPositions": return new ArrayList<IPosition>();
							case "toString": return "Ship(" + category + ")";
							case "equals": return proxy == args[0];
							case "hashCode": return System.identityHashCode(proxy);
							default: throw new UnsupportedOperationException(method.getName());
						}
					});
		}

		private static IPosition position() {
			return (IPosition) Proxy.newProxyInstance(IPosition.class.getClassLoader(),
					new Class<?>[]{IPosition.class}, (proxy, method, args) -> {
						if (method.getName().equals("equals")) return proxy == args[0];
						if (method.getName().equals("hashCode")) return System.identityHashCode(proxy);
						if (method.getName().equals("toString")) return "position";
						throw new UnsupportedOperationException(method.getName());
					});
		}

		private String capture(Runnable action) {
			PrintStream original = System.out;
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			try {
				System.setOut(new PrintStream(output));
				action.run();
				return output.toString();
			} finally {
				System.setOut(original);
			}
		}

		@Test @DisplayName("Construtor: a frota começa vazia")
		void fleet() {
			assertAll(
					() -> assertNotNull(fleet.getShips(), "Erro: a lista de navios não deveria ser null"),
					() -> assertTrue(fleet.getShips().isEmpty(), "Erro: a frota nova deveria estar vazia")
			);
		}

		@Test @DisplayName("getShips: devolve a lista mantida pela frota")
		void getShips() {
			List<IShip> ships = fleet.getShips();
			assertSame(ships, fleet.getShips(), "Erro: getShips deveria devolver a mesma lista");
		}

		@Test @DisplayName("addShip 1: aceita navio dentro do tabuleiro sem colisões")
		void addShip1() {
			IShip candidate = ship("barca", true, true, false, false);
			assertAll(
					() -> assertTrue(fleet.addShip(candidate), "Erro: deveria aceitar o navio válido"),
					() -> assertTrue(fleet.getShips().contains(candidate), "Erro: o navio deveria ficar na frota")
			);
		}

		@Test @DisplayName("addShip 2: recusa navio fora do tabuleiro")
		void addShip2() {
			assertFalse(fleet.addShip(ship("barca", false, true, false, false)),
					"Erro: não deveria aceitar navio fora do tabuleiro");
		}

		@Test @DisplayName("addShip 3: recusa colisão com um navio existente")
		void addShip3() {
			fleet.getShips().add(ship("galeao", true, true, true, false));
			assertFalse(fleet.addShip(ship("barca", true, true, false, false)),
					"Erro: um navio próximo de outro deveria ser recusado");
		}

		@Test @DisplayName("addShip 4: recusa quando a lista ultrapassa o limite implementado")
		void addShip4() {
			// O código usa <= FLEET_SIZE (não <); testa-se o ramo falso sem alterar produção.
			for (int i = 0; i <= IFleet.FLEET_SIZE; i++)
				fleet.getShips().add(ship("barca", true, true, false, false));
			assertFalse(fleet.addShip(ship("barca", true, true, false, false)),
					"Erro: deveria recusar uma frota acima do limite implementado");
		}

		@Test @DisplayName("getShipsLike 1: nenhuma correspondência em frota vazia")
		void getShipsLike1() {
			assertTrue(fleet.getShipsLike("barca").isEmpty(), "Erro: frota vazia não deve ter correspondências");
		}

		@Test @DisplayName("getShipsLike 2: devolve apenas navios da categoria escolhida")
		void getShipsLike2() {
			IShip barca = ship("barca", true, true, false, false);
			fleet.getShips().add(barca);
			fleet.getShips().add(ship("nau", true, true, false, false));
			assertEquals(List.of(barca), fleet.getShipsLike("barca"),
					"Erro: deveria devolver apenas os navios da categoria barca");
		}

		@Test @DisplayName("getShipsLike 3: ignora categorias diferentes")
		void getShipsLike3() {
			fleet.getShips().add(ship("nau", true, true, false, false));
			assertTrue(fleet.getShipsLike("barca").isEmpty(), "Erro: não deveria incluir categoria diferente");
		}

		@Test @DisplayName("getFloatingShips 1: frota vazia")
		void getFloatingShips1() {
			assertTrue(fleet.getFloatingShips().isEmpty(), "Erro: lista de flutuantes deveria estar vazia");
		}

		@Test @DisplayName("getFloatingShips 2: inclui os que flutuam")
		void getFloatingShips2() {
			IShip afloat = ship("barca", true, true, false, false);
			fleet.getShips().add(afloat);
			assertEquals(List.of(afloat), fleet.getFloatingShips(), "Erro: deveria incluir navio flutuante");
		}

		@Test @DisplayName("getFloatingShips 3: exclui os afundados")
		void getFloatingShips3() {
			fleet.getShips().add(ship("barca", true, false, false, false));
			assertTrue(fleet.getFloatingShips().isEmpty(), "Erro: afundados não devem flutuar");
		}

		@Test @DisplayName("getSunkShips 1: frota vazia")
		void getSunkShips1() {
			assertTrue(fleet.getSunkShips().isEmpty(), "Erro: lista de afundados deveria estar vazia");
		}

		@Test @DisplayName("getSunkShips 2: inclui os afundados")
		void getSunkShips2() {
			IShip sunk = ship("barca", true, false, false, false);
			fleet.getShips().add(sunk);
			assertEquals(List.of(sunk), fleet.getSunkShips(), "Erro: deveria incluir navio afundado");
		}

		@Test @DisplayName("getSunkShips 3: exclui os flutuantes")
		void getSunkShips3() {
			fleet.getShips().add(ship("barca", true, true, false, false));
			assertTrue(fleet.getSunkShips().isEmpty(), "Erro: flutuantes não deveriam estar afundados");
		}

		@Test @DisplayName("shipAt 1: frota vazia devolve null")
		void shipAt1() {
			assertNull(fleet.shipAt(position()), "Erro: não existe navio na posição da frota vazia");
		}

		@Test @DisplayName("shipAt 2: encontra navio que ocupa a posição")
		void shipAt2() {
			IShip target = ship("barca", true, true, false, true);
			fleet.getShips().add(target);
			assertSame(target, fleet.shipAt(position()), "Erro: deveria encontrar o navio na posição");
		}

		@Test @DisplayName("shipAt 3: nenhum navio ocupa a posição")
		void shipAt3() {
			fleet.getShips().add(ship("barca", true, true, false, false));
			assertNull(fleet.shipAt(position()), "Erro: posição sem navio deveria devolver null");
		}

		@Test @DisplayName("printShips 1: lista vazia não imprime navios")
		void printShips1() {
			assertTrue(capture(() -> fleet.printShips(List.of())).isEmpty(),
					"Erro: não deveria imprimir navios quando a lista está vazia");
		}

		@Test @DisplayName("printShips 2: imprime representação dos navios")
		void printShips2() {
			String result = capture(() -> fleet.printShips(List.of(ship("barca", true, true, false, false))));
			assertTrue(result.contains("Ship(barca)"), "Erro: deveria imprimir o navio barca");
		}

		@Test @DisplayName("printStatus: imprime o estado da frota")
		void printStatus() {
			assertFalse(capture(fleet::printStatus).isBlank(), "Erro: o estado da frota deveria ser impresso");
		}

		@Test @DisplayName("printShipsByCategory: imprime apenas a categoria pedida")
		void printShipsByCategory() {
			fleet.getShips().add(ship("barca", true, true, false, false));
			fleet.getShips().add(ship("nau", true, true, false, false));
			String result = capture(() -> fleet.printShipsByCategory("barca"));
			assertAll(
					() -> assertTrue(result.contains("Ship(barca)"), "Erro: deveria imprimir barca"),
					() -> assertFalse(result.contains("Ship(nau)"), "Erro: não deveria imprimir nau")
			);
		}

		@Test @DisplayName("printFloatingShips: imprime apenas os flutuantes")
		void printFloatingShips() {
			fleet.getShips().add(ship("barca", true, true, false, false));
			fleet.getShips().add(ship("nau", true, false, false, false));
			String result = capture(fleet::printFloatingShips);
			assertAll(
					() -> assertTrue(result.contains("Ship(barca)"), "Erro: deveria imprimir o navio flutuante"),
					() -> assertFalse(result.contains("Ship(nau)"), "Erro: não deveria imprimir o afundado")
			);
		}

		@Test @DisplayName("printAllShips: imprime todos os navios")
		void printAllShips() {
			fleet.getShips().add(ship("barca", true, true, false, false));
			assertTrue(capture(fleet::printAllShips).contains("Ship(barca)"),
					"Erro: deveria imprimir todos os navios da frota");
		}
	}
}
