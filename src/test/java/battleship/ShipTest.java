package battleship;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Test class for Ship.
 * Author: Tomás Costa - 129836
 * Date: 16-11-2026
 * Time: 12:18
 * Cyclomatic Complexity for each method:
 * - Constructor: 1
 * - getCategory: 1
 * - getSize: 1
 * - getBearing: 1
 * - getPositions: 1
 * - stillFloating: 2
 * - shoot: 2
 * - occupies: 2
 * - tooCloseTo (IShip): 2
 * - tooCloseTo (IPosition): 2
 * - getTopMostPos: 2
 * - getBottomMostPos: 2
 * - getLeftMostPos: 2
 * - getRightMostPos: 2
 */
public class ShipTest {

    private Ship ship, shipBorder;

    @BeforeEach
    void setUp() {
        // Since Ship is abstract, instantiate it with a concrete subclass (e.g., Barge)
        ship = new Barge(Compass.NORTH, new Position(5, 5));
        shipBorder = new Barge(Compass.NORTH, new Position(0, 0));
    }

    @AfterEach
    void tearDown() {
        ship = null;
    }

    /**
     * Test for the constructor.
     * Cyclomatic Complexity: 1
     */
    @Test
    void testConstructor() {
        assertNotNull(ship, "Error: Ship instance should not be null.");
        assertEquals("Barca", ship.getCategory(), "Error: Ship category is incorrect.");
        assertEquals(Compass.NORTH, ship.getBearing(), "Error: Ship bearing is incorrect.");
        assertEquals(1, ship.getSize(), "Error: Ship size is incorrect.");
        assertFalse(ship.getPositions().isEmpty(), "Error: Ship positions should not be empty.");
    }

    /**
     * Test for the getCategory method.
     * Cyclomatic Complexity: 1
     */
    @Test
    void testGetCategory() {
        assertEquals("Barca", ship.getCategory(), "Error: Ship category should be 'Barca'.");
    }

    /**
     * Test for the getSize method.
     * Cyclomatic Complexity: 1
     */
    @Test
    void testGetSize() {
        assertEquals(1, ship.getSize(), "Error: Ship size should be 1.");
    }

    /**
     * Test for the getBearing method.
     * Cyclomatic Complexity: 1
     */
    @Test
    void testGetBearing() {
        assertEquals(Compass.NORTH, ship.getBearing(), "Error: Ship bearing should be NORTH.");
    }

    /**
     * Test for the getPositions method.
     * Cyclomatic Complexity: 1
     */
    @Test
    void testGetPositions() {
        List<IPosition> positions = ship.getPositions();
        assertNotNull(positions, "Error: Ship positions should not be null.");
        assertEquals(1, positions.size(), "Error: Ship should have exactly one position.");
        assertEquals(5, positions.get(0).getRow(), "Error: Position's row should be 5.");
        assertEquals(5, positions.get(0).getColumn(), "Error: Position's column should be 5.");
    }

    /**
     * Test for the stillFloating method (all positions intact).
     * Cyclomatic Complexity: 2
     */
    @Test
    void testStillFloating1() {
        assertTrue(ship.stillFloating(), "Error: Ship should still be floating.");
    }

    /**
     * Test for the stillFloating method (all positions hit).
     */
    @Test
    void testStillFloating2() {
        ship.getPositions().get(0).shoot();
        assertFalse(ship.stillFloating(), "Error: Ship should no longer be floating after being hit.");
    }

    /**
     * Test for the shoot method (valid position).
     * Cyclomatic Complexity: 2
     */
    @Test
    void testShoot1() {
        Position target = new Position(5, 5);
        ship.shoot(target);
        assertTrue(ship.getPositions().get(0).isHit(), "Error: Position should be marked as hit.");
    }

    /**
     * Test for the shoot method (invalid position).
     */
    @Test
    void testShoot2() {
        Position target = new Position(0, 0);
        ship.shoot(target); // No exception expected
        assertFalse(ship.getPositions().get(0).isHit(), "Error: Position should not be marked as hit for an invalid target.");
    }

    /**
     * Test for the occupies method (position occupied).
     * Cyclomatic Complexity: 2
     */
    @Test
    void testOccupies1() {
        Position pos = new Position(5, 5);
        assertTrue(ship.occupies(pos), "Error: Ship should occupy position (5, 5).");
    }

    /**
     * Test for the occupies method (position not occupied).
     */
    @Test
    void testOccupies2() {
        Position pos = new Position(1, 1);
        assertFalse(ship.occupies(pos), "Error: Ship should not occupy position (1, 1).");
    }

    /**
     * Test for the tooCloseTo method with another IShip (ships too close).
     * Cyclomatic Complexity: 2
     */
    @Test
    void testTooCloseToShip1() {
        Ship nearbyShip = new Barge(Compass.NORTH, new Position(5, 6));
        assertTrue(ship.tooCloseTo(nearbyShip), "Error: Ships should be too close.");
    }

    /**
     * Test for the tooCloseTo method with another IShip (ships not close).
     */
    @Test
    void testTooCloseToShip2() {
        Ship farShip = new Barge(Compass.NORTH, new Position(10, 10));
        assertFalse(ship.tooCloseTo(farShip), "Error: Ships should not be too close.");
    }

    /**
     * Test for the tooCloseTo method with an IPosition (positions adjacent).
     * Cyclomatic Complexity: 2
     */
    @Test
    void testTooCloseToPosition1() {
        Position pos = new Position(5, 6); // Adjacent position
        assertTrue(ship.tooCloseTo(pos), "Error: Ship should be too close to the given position.");
    }

    /**
     * Test for the tooCloseTo method with an IPosition (positions not adjacent).
     */
    @Test
    void testTooCloseToPosition2() {
        Position pos = new Position(7, 7); // Non-adjacent position
        assertFalse(ship.tooCloseTo(pos), "Error: Ship should not be too close to the given position.");
    }

    /**
     * Test for the getTopMostPos method.
     * Cyclomatic Complexity: 2
     */
    @Test
    void testGetTopMostPos() {
        assertEquals(5, ship.getTopMostPos(), "Error: The topmost position should be 5.");
    }

    /**
     * Test for the getBottomMostPos method.
     * Cyclomatic Complexity: 2
     */
    @Test
    void testGetBottomMostPos() {
        assertEquals(5, ship.getBottomMostPos(), "Error: The bottommost position should be 5.");
    }

    /**
     * Test for the getLeftMostPos method.
     * Cyclomatic Complexity: 2
     */
    @Test
    void testGetLeftMostPos() {
        assertEquals(5, ship.getLeftMostPos(), "Error: The leftmost position should be 5.");
    }

    /**
     * Test for the getRightMostPos method.
     * Cyclomatic Complexity: 2
     */
    @Test
    void testGetRightMostPos() {
        assertEquals(5, ship.getRightMostPos(), "Error: The rightmost position should be 5.");
    }

    /**
     * Test for the getRightMostPos method.
     * Cyclomatic Complexity: 2
     */
    @Test
    void testGetAdjacentPositions() {
        assertEquals(8, ship.getAdjacentPositions().size(), "Error: The number of adjacent positions of a barge in the middle of the tray should be 8.");
        assertEquals(3, shipBorder.getAdjacentPositions().size(), "Error: The number of adjacent positions of a barge in the middle of the tray should be 8.");
    }



    /** Additional independent paths, preserving the original tests. */
    @Nested
    class AdditionalCoverage {

        private Ship ship;
        private Compass bearing;
        private IPosition origin;

        private static final class Cell {
            final int row;
            final int column;
            boolean hit;
            List<IPosition> neighbours = new ArrayList<>();
            Cell(int row, int column, boolean hit) {
                this.row = row;
                this.column = column;
                this.hit = hit;
            }
            IPosition proxy() {
                return (IPosition) Proxy.newProxyInstance(IPosition.class.getClassLoader(),
                        new Class<?>[]{IPosition.class}, (p, method, args) -> {
                            switch (method.getName()) {
                                case "getRow": return row;
                                case "getColumn": return column;
                                case "isHit": return hit;
                                case "shoot": hit = true; return null;
                                case "isInside": return row >= 0 && row < Game.BOARD_SIZE && column >= 0 && column < Game.BOARD_SIZE;
                                case "adjacentPositions": return neighbours;
                                case "isAdjacentTo": {
                                    IPosition other = (IPosition) args[0];
                                    return Math.abs(row - other.getRow()) <= 1
                                            && Math.abs(column - other.getColumn()) <= 1;
                                }
                                case "equals": {
                                    Object other = args[0];
                                    if (!(other instanceof IPosition)) return false;
                                    return row == ((IPosition) other).getRow()
                                            && column == ((IPosition) other).getColumn();
                                }
                                case "hashCode": return Objects.hash(row, column);
                                case "toString": return "(" + row + "," + column + ")";
                                default: throw new UnsupportedOperationException(method.getName());
                            }
                        });
            }
        }

        private static IPosition cell(int r, int c) { return new Cell(r, c, false).proxy(); }

        private Ship makeShip(String category, IPosition first, IPosition... others) {
            Ship result = new Ship(category, bearing, first, 1 + others.length) { };
            result.getPositions().add(first);
            result.getPositions().addAll(List.of(others));
            return result;
        }

        @BeforeEach
        void setUp() {
            bearing = Compass.randomBearing();
            origin = cell(2, 2);
            ship = makeShip("barca", origin);
        }

        @AfterEach
        void tearDown() { ship = null; bearing = null; origin = null; }

        @Test @DisplayName("Ship: inicializa corretamente os atributos")
        void ship() {
            assertAll(
                    () -> assertEquals("barca", ship.getCategory(), "Erro: categoria esperada barca"),
                    () -> assertSame(bearing, ship.getBearing(), "Erro: bearing deveria ser o recebido"),
                    () -> assertSame(origin, ship.getPosition(), "Erro: posição deveria ser a recebida"),
                    () -> assertEquals(1, ship.getSize(), "Erro: tamanho esperado 1")
            );
        }

        @Test @DisplayName("Ship: rejeita categoria null")
        void shipNullCategory() {
            assertThrows(NullPointerException.class, () -> new Ship(null, bearing, origin, 1) { },
                    "Erro: a categoria null deveria causar NullPointerException");
        }

        @Test @DisplayName("Ship: rejeita bearing null")
        void shipNullBearing() {
            assertThrows(NullPointerException.class, () -> new Ship("barca", null, origin, 1) { },
                    "Erro: bearing null deveria causar NullPointerException");
        }

        @Test @DisplayName("Ship: rejeita posição null")
        void shipNullPosition() {
            assertThrows(NullPointerException.class, () -> new Ship("barca", bearing, null, 1) { },
                    "Erro: posição null deveria causar NullPointerException");
        }

        @Test @DisplayName("getCategory: devolve a categoria")
        void getCategory() {
            assertEquals("barca", ship.getCategory(), "Erro: a categoria deveria ser barca");
        }

        @Test @DisplayName("getPositions: devolve as posições da instância")
        void getPositions() {
            assertEquals(List.of(origin), ship.getPositions(), "Erro: lista de posições diferente da esperada");
        }

        @Test @DisplayName("getPosition: devolve posição inicial")
        void getPosition() {
            assertSame(origin, ship.getPosition(), "Erro: deveria devolver posição inicial");
        }

        @Test @DisplayName("getBearing: devolve orientação")
        void getBearing() {
            assertSame(bearing, ship.getBearing(), "Erro: orientação incorreta");
        }

        @Test @DisplayName("getSize: devolve tamanho")
        void getSize() {
            assertEquals(1, ship.getSize(), "Erro: tamanho deveria ser 1");
        }

        @Test @DisplayName("getAdjacentPositions 1: sem posições ocupadas não há adjacências")
        void getAdjacentPositions1() {
            ship.getPositions().clear();
            assertTrue(ship.getAdjacentPositions().isEmpty(), "Erro: sem posições não deveria haver adjacentes");
        }

        @Test @DisplayName("getAdjacentPositions 2: devolve adjacente externo")
        void getAdjacentPositions2() {
            Cell a = new Cell(2, 2, false);
            IPosition first = a.proxy();
            a.neighbours.add(cell(2, 3));
            ship = makeShip("barca", first);
            assertEquals(1, ship.getAdjacentPositions().size(), "Erro: deveria incluir posição adjacente externa");
        }

        @Test @DisplayName("getAdjacentPositions 3: exclui posição já ocupada")
        void getAdjacentPositions3() {
            Cell a = new Cell(2, 2, false);
            IPosition first = a.proxy();
            IPosition second = cell(2, 3);
            a.neighbours.add(second);
            ship = makeShip("barca", first, second);
            assertTrue(ship.getAdjacentPositions().isEmpty(), "Erro: posição ocupada não deveria ser adjacente externa");
        }

        @Test @DisplayName("getAdjacentPositions 4: elimina duplicados entre posições")
        void getAdjacentPositions4() {
            Cell a = new Cell(2, 2, false);
            Cell b = new Cell(2, 3, false);
            IPosition target = cell(3, 3);
            a.neighbours.add(target);
            b.neighbours.add(target);
            ship = makeShip("barca", a.proxy(), b.proxy());
            assertEquals(1, ship.getAdjacentPositions().size(), "Erro: adjacente repetido deveria surgir uma vez");
        }

        @Test @DisplayName("getAdjacentPositions 5: posição sem vizinhos")
        void getAdjacentPositions5() {
            assertTrue(ship.getAdjacentPositions().isEmpty(), "Erro: nenhuma vizinhança foi configurada");
        }

        @Test @DisplayName("stillFloating 1: tamanho zero devolve false")
        void stillFloating1() {
            ship = new Ship("barca", bearing, origin, 0) { };
            assertFalse(ship.stillFloating(), "Erro: navio de tamanho zero não deveria flutuar");
        }

        @Test @DisplayName("stillFloating 2: existe posição não atingida")
        void stillFloating2() {
            assertTrue(ship.stillFloating(), "Erro: navio intacto deveria flutuar");
        }

        @Test @DisplayName("stillFloating 3: todas as posições atingidas")
        void stillFloating3() {
            Cell a = new Cell(2, 2, true);
            Cell b = new Cell(2, 3, true);
            ship = makeShip("barca", a.proxy(), b.proxy());
            assertFalse(ship.stillFloating(), "Erro: navio totalmente atingido deveria afundar");
        }

        @Test @DisplayName("getTopMostPos 1: navio de uma posição")
        void getTopMostPos1() {
            assertEquals(2, ship.getTopMostPos(), "Erro: linha mínima esperada 2");
        }

        @Test @DisplayName("getTopMostPos 2: nova linha menor")
        void getTopMostPos2() {
            ship = makeShip("barca", cell(4, 3), cell(1, 3));
            assertEquals(1, ship.getTopMostPos(), "Erro: linha mínima esperada 1");
        }

        @Test @DisplayName("getTopMostPos 3: linha posterior maior")
        void getTopMostPos3() {
            ship = makeShip("barca", cell(1, 3), cell(4, 3));
            assertEquals(1, ship.getTopMostPos(), "Erro: linha mínima deveria permanecer 1");
        }

        @Test @DisplayName("getBottomMostPos 1: apenas uma posição")
        void getBottomMostPos1() {
            assertEquals(2, ship.getBottomMostPos(), "Erro: linha máxima esperada 2");
        }

        @Test @DisplayName("getBottomMostPos 2: nova linha maior")
        void getBottomMostPos2() {
            ship = makeShip("barca", cell(1, 3), cell(4, 3));
            assertEquals(4, ship.getBottomMostPos(), "Erro: linha máxima esperada 4");
        }

        @Test @DisplayName("getBottomMostPos 3: linha posterior menor")
        void getBottomMostPos3() {
            ship = makeShip("barca", cell(4, 3), cell(1, 3));
            assertEquals(4, ship.getBottomMostPos(), "Erro: linha máxima deveria permanecer 4");
        }

        @Test @DisplayName("getLeftMostPos 1: apenas uma posição")
        void getLeftMostPos1() {
            assertEquals(2, ship.getLeftMostPos(), "Erro: coluna mínima esperada 2");
        }

        @Test @DisplayName("getLeftMostPos 2: nova coluna menor")
        void getLeftMostPos2() {
            ship = makeShip("barca", cell(2, 4), cell(2, 1));
            assertEquals(1, ship.getLeftMostPos(), "Erro: coluna mínima esperada 1");
        }

        @Test @DisplayName("getLeftMostPos 3: coluna posterior maior")
        void getLeftMostPos3() {
            ship = makeShip("barca", cell(2, 1), cell(2, 4));
            assertEquals(1, ship.getLeftMostPos(), "Erro: coluna mínima deveria permanecer 1");
        }

        @Test @DisplayName("getRightMostPos 1: apenas uma posição")
        void getRightMostPos1() {
            assertEquals(2, ship.getRightMostPos(), "Erro: coluna máxima esperada 2");
        }

        @Test @DisplayName("getRightMostPos 2: nova coluna maior")
        void getRightMostPos2() {
            ship = makeShip("barca", cell(2, 1), cell(2, 4));
            assertEquals(4, ship.getRightMostPos(), "Erro: coluna máxima esperada 4");
        }

        @Test @DisplayName("getRightMostPos 3: coluna posterior menor")
        void getRightMostPos3() {
            ship = makeShip("barca", cell(2, 4), cell(2, 1));
            assertEquals(4, ship.getRightMostPos(), "Erro: coluna máxima deveria permanecer 4");
        }

        @Test @DisplayName("occupies 1: navio sem posições não ocupa nenhuma")
        void occupies1() {
            ship = new Ship("barca", bearing, origin, 0) { };
            assertFalse(ship.occupies(origin), "Erro: sem posições não deveria ocupar a célula");
        }

        @Test @DisplayName("occupies 2: posição ocupada")
        void occupies2() {
            assertTrue(ship.occupies(cell(2, 2)), "Erro: deveria ocupar a posição (2,2)");
        }

        @Test @DisplayName("occupies 3: posição não ocupada")
        void occupies3() {
            assertFalse(ship.occupies(cell(3, 3)), "Erro: não deveria ocupar a posição (3,3)");
        }

        @Test @DisplayName("tooCloseTo(IPosition) 1: tamanho zero")
        void tooCloseToPosition1() {
            ship = new Ship("barca", bearing, origin, 0) { };
            assertFalse(ship.tooCloseTo(cell(2, 3)), "Erro: sem células não deveria haver proximidade");
        }

        @Test @DisplayName("tooCloseTo(IPosition) 2: posição adjacente")
        void tooCloseToPosition2() {
            assertTrue(ship.tooCloseTo(cell(2, 3)), "Erro: posição vizinha deveria ser próxima");
        }

        @Test @DisplayName("tooCloseTo(IPosition) 3: posição distante")
        void tooCloseToPosition3() {
            assertFalse(ship.tooCloseTo(cell(5, 5)), "Erro: posição distante não deveria ser próxima");
        }

        @Test @DisplayName("tooCloseTo(IShip) 1: outro navio sem posições")
        void tooCloseToShip1() {
            Ship other = new Ship("barca", bearing, cell(5, 5), 0) { };
            assertFalse(ship.tooCloseTo(other), "Erro: navio sem posições não deveria ser próximo");
        }

        @Test @DisplayName("tooCloseTo(IShip) 2: navio vizinho")
        void tooCloseToShip2() {
            Ship other = makeShip("barca", cell(2, 3));
            assertTrue(ship.tooCloseTo(other), "Erro: navios adjacentes deveriam ser próximos");
        }

        @Test @DisplayName("tooCloseTo(IShip) 3: navio distante")
        void tooCloseToShip3() {
            Ship other = makeShip("barca", cell(5, 5));
            assertFalse(ship.tooCloseTo(other), "Erro: navios distantes não deveriam ser próximos");
        }

        @Test @DisplayName("isInsideBoard 1: todas as coordenadas válidas")
        void isInsideBoard1() {
            assertTrue(ship.isInsideBoard(), "Erro: posição interior deveria ser válida");
        }

        @Test @DisplayName("isInsideBoard 2: coluna esquerda negativa")
        void isInsideBoard2() {
            ship = makeShip("barca", cell(2, -1));
            assertFalse(ship.isInsideBoard(), "Erro: coluna negativa deveria estar fora do tabuleiro");
        }

        @Test @DisplayName("isInsideBoard 3: coluna direita fora do limite")
        void isInsideBoard3() {
            ship = makeShip("barca", cell(2, Game.BOARD_SIZE));
            assertFalse(ship.isInsideBoard(), "Erro: coluna >= limite deveria estar fora");
        }

        @Test @DisplayName("isInsideBoard 4: linha superior negativa")
        void isInsideBoard4() {
            ship = makeShip("barca", cell(-1, 2));
            assertFalse(ship.isInsideBoard(), "Erro: linha negativa deveria estar fora");
        }

        @Test @DisplayName("isInsideBoard 5: linha inferior fora do limite")
        void isInsideBoard5() {
            ship = makeShip("barca", cell(Game.BOARD_SIZE, 2));
            assertFalse(ship.isInsideBoard(), "Erro: linha >= limite deveria estar fora");
        }

        @Test @DisplayName("shoot 1: sem posições ocupadas")
        void shoot1() {
            ship.getPositions().clear();
            assertDoesNotThrow(() -> ship.shoot(cell(2, 2)), "Erro: disparar sobre navio sem posições não deveria falhar");
        }

        @Test @DisplayName("shoot 2: atinge a célula certa")
        void shoot2() {
            Cell target = new Cell(2, 2, false);
            ship = makeShip("barca", target.proxy());
            ship.shoot(cell(2, 2));
            assertTrue(target.hit, "Erro: a célula correspondente deveria ser atingida");
        }

        @Test @DisplayName("shoot 3: não atinge célula diferente")
        void shoot3() {
            Cell target = new Cell(2, 2, false);
            ship = makeShip("barca", target.proxy());
            ship.shoot(cell(3, 3));
            assertFalse(target.hit, "Erro: a célula não correspondente deveria permanecer intacta");
        }

        @Test @DisplayName("sink 1: navio vazio")
        void sink1() {
            ship.getPositions().clear();
            assertDoesNotThrow(ship::sink, "Erro: afundar um navio sem posições não deveria falhar");
        }

        @Test @DisplayName("sink 2: todas as posições ficam atingidas")
        void sink2() {
            Cell first = new Cell(2, 2, false);
            Cell second = new Cell(2, 3, false);
            ship = makeShip("barca", first.proxy(), second.proxy());
            ship.sink();
            assertAll(
                    () -> assertTrue(first.hit, "Erro: primeira célula deveria ficar atingida"),
                    () -> assertTrue(second.hit, "Erro: segunda célula deveria ficar atingida")
            );
        }

        @Test @DisplayName("toString: mostra categoria, orientação e posição")
        void toStringTest() {
            assertEquals("[barca " + bearing + " " + origin + "]", ship.toString(),
                    "Erro: representação textual do navio diferente da esperada");
        }

        @Test @DisplayName("buildShip 1: cria barca")
        void buildShip1() {
            assertInstanceOf(Barge.class, Ship.buildShip("barca", bearing, Position.randomPosition()),
                    "Erro: barca deveria produzir Barge");
        }

        @Test @DisplayName("buildShip 2: cria caravela")
        void buildShip2() {
            assertInstanceOf(Caravel.class, Ship.buildShip("caravela", bearing, Position.randomPosition()),
                    "Erro: caravela deveria produzir Caravel");
        }

        @Test @DisplayName("buildShip 3: cria nau")
        void buildShip3() {
            assertInstanceOf(Carrack.class, Ship.buildShip("nau", bearing, Position.randomPosition()),
                    "Erro: nau deveria produzir Carrack");
        }

        @Test @DisplayName("buildShip 4: cria fragata")
        void buildShip4() {
            assertInstanceOf(Frigate.class, Ship.buildShip("fragata", bearing, Position.randomPosition()),
                    "Erro: fragata deveria produzir Frigate");
        }

        @Test @DisplayName("buildShip 5: cria galeão")
        void buildShip5() {
            assertInstanceOf(Galleon.class, Ship.buildShip("galeao", bearing, Position.randomPosition()),
                    "Erro: galeão deveria produzir Galleon");
        }

        @Test @DisplayName("buildShip 6: tipo desconhecido devolve null")
        void buildShip6() {
            assertNull(Ship.buildShip("desconhecido", bearing, Position.randomPosition()),
                    "Erro: tipo desconhecido deveria devolver null");
        }
    }
}
