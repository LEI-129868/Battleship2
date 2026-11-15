package battleship;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários de Position.
 * Author: franciscosilva
 * Date: 2026-11-15 13:50 WET
 * Cyclomatic Complexity (CC), contando cada decisão de curto-circuito:
 * - Position(int, int), Position(char, int): 1 cada
 * - randomPosition, getRow, getColumn, getClassicRow, getClassicColumn: 1 cada
 * - isInside: 5
 * - isAdjacentTo: 3
 * - adjacentPositions: 3
 * - isOccupied, isHit, occupy, shoot: 1 cada
 * - equals: 5
 * - hashCode, toString: 1 cada
 */
class PositionTest {
	private Position position;

	@BeforeEach
	void setUp() {
		position = new Position(2, 3);
	}

	@AfterEach
	void tearDown() {
		position = null;
	}

	@Test
	@DisplayName("O construtor numérico guarda os índices e inicializa o estado")
	void constructorNumeric() {
		assertAll(
				() -> assertEquals(2, position.getRow(), "A linha interna devia ser 2."),
				() -> assertEquals(3, position.getColumn(), "A coluna interna devia ser 3."),
				() -> assertFalse(position.isOccupied(), "Uma posição nova devia estar livre."),
				() -> assertFalse(position.isHit(), "Uma posição nova devia estar por atingir.")
		);
	}

	@Test
	@DisplayName("O construtor clássico converte c4 nos índices 2 e 3")
	void constructorClassic() {
		Position classic = new Position('c', 4);

		assertAll(
				() -> assertEquals(2, classic.getRow(), "A linha c devia corresponder ao índice 2."),
				() -> assertEquals(3, classic.getColumn(), "A coluna 4 devia corresponder ao índice 3."),
				() -> assertFalse(classic.isOccupied(), "Uma posição nova devia estar livre."),
				() -> assertFalse(classic.isHit(), "Uma posição nova devia estar por atingir.")
		);
	}

	@Test
	@DisplayName("A posição aleatória tem ambos os índices dentro do tabuleiro")
	void randomPosition() {
		Position random = Position.randomPosition();

		assertAll(
				() -> assertTrue(random.getRow() >= 0 && random.getRow() < Game.BOARD_SIZE,
						() -> "A linha aleatória devia estar dentro do tabuleiro, mas foi " + random.getRow() + "."),
				() -> assertTrue(random.getColumn() >= 0 && random.getColumn() < Game.BOARD_SIZE,
						() -> "A coluna aleatória devia estar dentro do tabuleiro, mas foi " + random.getColumn() + ".")
		);
	}

	@Test
	@DisplayName("Devolve a linha interna")
	void getRow() {
		assertEquals(2, position.getRow(), "A linha interna devia ser 2.");
	}

	@Test
	@DisplayName("Devolve a coluna interna")
	void getColumn() {
		assertEquals(3, position.getColumn(), "A coluna interna devia ser 3.");
	}

	@Test
	@DisplayName("Converte a linha interna 2 na letra C")
	void getClassicRow() {
		assertEquals('C', position.getClassicRow(), "O índice de linha 2 devia produzir a letra C.");
	}

	@Test
	@DisplayName("Converte a coluna interna 3 no número 4")
	void getClassicColumn() {
		assertEquals(4, position.getClassicColumn(), "O índice de coluna 3 devia produzir o número 4.");
	}

	@Test
	@DisplayName("O canto inferior direito pertence ao tabuleiro")
	void isInside1() {
		Position corner = new Position(Game.BOARD_SIZE - 1, Game.BOARD_SIZE - 1);
		assertTrue(corner.isInside(), "Os últimos índices válidos deviam ficar dentro do tabuleiro.");
	}

	@Test
	@DisplayName("Uma linha abaixo de zero está fora do tabuleiro")
	void isInside2() {
		assertFalse(new Position(-1, 3).isInside(), "A linha -1 devia ficar fora do tabuleiro.");
	}

	@Test
	@DisplayName("Uma coluna abaixo de zero está fora do tabuleiro")
	void isInside3() {
		assertFalse(new Position(2, -1).isInside(), "A coluna -1 devia ficar fora do tabuleiro.");
	}

	@Test
	@DisplayName("A primeira linha acima do limite está fora do tabuleiro")
	void isInside4() {
		assertFalse(new Position(Game.BOARD_SIZE, 3).isInside(), "A linha igual ao tamanho devia ser inválida.");
	}

	@Test
	@DisplayName("A primeira coluna acima do limite está fora do tabuleiro")
	void isInside5() {
		assertFalse(new Position(2, Game.BOARD_SIZE).isInside(), "A coluna igual ao tamanho devia ser inválida.");
	}

	@Test
	@DisplayName("Uma posição a um passo em cada eixo é adjacente")
	void isAdjacentTo1() {
		assertTrue(position.isAdjacentTo(new Position(3, 4)), "A diagonal a um passo devia ser adjacente.");
	}

	@Test
	@DisplayName("Duas linhas de distância impedem a adjacência")
	void isAdjacentTo2() {
		assertFalse(position.isAdjacentTo(new Position(4, 3)), "Duas linhas de distância deviam excluir a posição.");
	}

	@Test
	@DisplayName("Duas colunas de distância impedem a adjacência")
	void isAdjacentTo3() {
		assertFalse(position.isAdjacentTo(new Position(2, 5)), "Duas colunas de distância deviam excluir a posição.");
	}

	@Test
	@DisplayName("A comparação de adjacência exige outra posição")
	void isAdjacentToRejectsNull() {
		assertThrows(NullPointerException.class, () -> position.isAdjacentTo(null),
				"Comparar adjacência com null devia lançar NullPointerException.");
	}

	@Test
	@DisplayName("A posição interior tem os oito vizinhos, incluindo diagonais")
	void adjacentPositions1() {
		Set<IPosition> expected = Set.of(new Position(1, 2), new Position(1, 3), new Position(1, 4),
				new Position(2, 2), new Position(2, 4), new Position(3, 2), new Position(3, 3), new Position(3, 4));
		assertAdjacentPositions(expected, position.adjacentPositions());
	}

	@Test
	@DisplayName("O canto superior esquerdo tem apenas três vizinhos válidos")
	void adjacentPositions2() {
		Set<IPosition> expected = Set.of(new Position(0, 1), new Position(1, 0), new Position(1, 1));
		assertAdjacentPositions(expected, new Position(0, 0).adjacentPositions());
	}

	@Test
	@DisplayName("Uma posição longe do tabuleiro fica sem vizinhos válidos")
	void adjacentPositions3() {
		assertTrue(new Position(-3, -3).adjacentPositions().isEmpty(),
				"Nenhum vizinho devia estar no tabuleiro para a posição (-3, -3).");
	}

	@Test
	@DisplayName("A posição começa livre")
	void isOccupied() {
		assertFalse(position.isOccupied(), "O estado de ocupação inicial devia ser falso.");
	}

	@Test
	@DisplayName("A posição começa por atingir")
	void isHit() {
		assertFalse(position.isHit(), "O estado de acerto inicial devia ser falso.");
	}

	@Test
	@DisplayName("Ocupar marca a posição como ocupada")
	void occupy() {
		position.occupy();
		assertTrue(position.isOccupied(), "A posição devia ficar ocupada depois de occupy().");
	}

	@Test
	@DisplayName("Disparar marca a posição como atingida")
	void shoot() {
		position.shoot();
		assertTrue(position.isHit(), "A posição devia ficar atingida depois de shoot().");
	}

	@Test
	@DisplayName("Uma posição é igual à própria referência")
	void equals1() {
		assertTrue(position.equals(position), "A comparação com a própria referência devia devolver true.");
	}

	@Test
	@DisplayName("Uma posição difere de null e de objetos de outro tipo")
	void equals2() {
		assertAll(
				() -> assertFalse(position.equals(null), "Uma posição não devia ser igual a null."),
				() -> assertFalse(position.equals("C4"), "Uma posição não devia ser igual a uma String.")
		);
	}

	@Test
	@DisplayName("Uma linha diferente torna as posições diferentes")
	void equals3() {
		assertNotEquals(position, new Position(3, 3), "Linhas distintas deviam produzir posições diferentes.");
	}

	@Test
	@DisplayName("Uma coluna diferente torna as posições diferentes")
	void equals4() {
		assertNotEquals(position, new Position(2, 4), "Colunas distintas deviam produzir posições diferentes.");
	}

	@Test
	@DisplayName("Dois objetos com as mesmas coordenadas são iguais")
	void equals5() {
		assertEquals(position, new Position(2, 3), "Objetos distintos com os mesmos índices deviam ser iguais.");
	}

	@Test
	@DisplayName("Posições com as mesmas coordenadas e estado inicial têm o mesmo hash")
	void hashCodeConsistency() {
		assertEquals(position.hashCode(), new Position(2, 3).hashCode(),
				"Duas posições novas com os mesmos índices deviam ter o mesmo hash.");
	}

	@Test
	@DisplayName("A posição com índices 2 e 3 é apresentada como C4")
	void toStringFormat() {
		assertEquals("C4", position.toString(), "A posição (2, 3) devia ser apresentada como C4.");
	}

	private void assertAdjacentPositions(Set<IPosition> expected, List<IPosition> actual) {
		assertAll(
				() -> assertEquals(expected.size(), actual.size(), "A quantidade de vizinhos devia coincidir."),
				() -> assertEquals(expected, new HashSet<>(actual), "As coordenadas dos vizinhos deviam coincidir.")
		);
	}
}
