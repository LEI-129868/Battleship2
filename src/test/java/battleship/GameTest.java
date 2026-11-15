package battleship;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamWriteConstraints;
import com.fasterxml.jackson.core.exc.StreamConstraintsException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.GraphicsEnvironment;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Testes unitários de Game.
 * Author: franciscosilva
 * Date: 2026-11-15 13:50 WET
 * Cyclomatic Complexity (CC) no código-fonte, contando decisões de curto-circuito,
 * pré-condições assert e o tratamento de exceções:
 * - Game(IFleet): 1
 * - printBoard: 7; printShips: 5; printBoardShots: 7; printBoardFrame: 6
 * - jsonShots: 4; randomEnemyFire: 9; readEnemyFire: 7
 * - fireShots: 4; fireSingleShot: 7; repeatedShot: 4
 * - getMyFleet, getAlienFleet, getMyMoves, getAlienMoves: 1 cada
 * - getRepeatedShots, getInvalidShots, getHits, getSunkShips, getRemainingShips: 1 cada
 * - printMyBoard, printAlienBoard, over: 1 cada
 * Os auxiliares privados são exercitados pelas operações públicas.
 */
@ResourceLock("java.lang.System.out")
class GameTest {
	private static final ObjectMapper JSON = new ObjectMapper();
	private static final Position BARGE_POSITION = new Position(1, 1);
	private Fleet fleet;
	private Game game;

	@BeforeEach
	void setUp() {
		fleet = new Fleet();
		game = new Game(fleet);
	}

	@AfterEach
	void tearDown() {
		game = null;
		fleet = null;
	}

	@Test
	@DisplayName("Um jogo novo começa sem jogadas nem tiros registados")
	void constructor() {
		assertAll(
				() -> assertTrue(game.getMyMoves().isEmpty(), "O histórico próprio devia começar vazio."),
				() -> assertTrue(game.getAlienMoves().isEmpty(), "O histórico inimigo devia começar vazio."),
				() -> assertEquals(0, game.getInvalidShots(), "O contador de tiros inválidos devia começar em zero."),
				() -> assertEquals(0, game.getRepeatedShots(), "O contador de tiros repetidos devia começar em zero."),
				() -> assertEquals(0, game.getHits(), "O contador de acertos devia começar em zero."),
				() -> assertEquals(0, game.getSunkShips(), "O contador de afundamentos devia começar em zero.")
		);
	}

	@Test
	@DisplayName("O jogo conserva a frota recebida")
	void getMyFleet() {
		assertSame(fleet, game.getMyFleet(), "O jogo devia guardar a mesma instância de frota.");
	}

	@Test
	@DisplayName("A frota inimiga é distinta e começa vazia")
	void getAlienFleet() {
		IFleet alienFleet = game.getAlienFleet();
		assertAll(
				() -> assertNotSame(fleet, alienFleet, "A frota inimiga devia ser distinta da frota própria."),
				() -> assertTrue(alienFleet.getShips().isEmpty(), "A frota inimiga devia começar sem navios.")
		);
	}

	@Test
	@DisplayName("Uma jogada inimiga deixa o histórico próprio vazio")
	void getMyMoves() {
		fireAtWater();
		assertTrue(game.getMyMoves().isEmpty(), "Uma jogada inimiga não devia ser registada como jogada própria.");
	}

	@Test
	@DisplayName("Uma jogada inimiga é acrescentada ao histórico inimigo")
	void getAlienMoves() {
		List<IPosition> expected = fireAtWater();
		assertEquals(expected, game.getAlienMoves().getFirst().getShots(), "Os tiros registados deviam coincidir.");
	}

	@Test
	@DisplayName("Um disparo fora do tabuleiro incrementa o contador de inválidos")
	void getInvalidShots() {
		game.fireSingleShot(new Position(-1, 0), false);
		assertEquals(1, game.getInvalidShots(), "Devia haver um tiro inválido registado.");
	}

	@Test
	@DisplayName("Um disparo marcado como repetido incrementa o contador")
	void getRepeatedShots() {
		game.fireSingleShot(BARGE_POSITION, true);
		assertEquals(1, game.getRepeatedShots(), "Devia haver um tiro repetido registado.");
	}

	@Test
	@DisplayName("Um disparo que atinge um navio incrementa o contador de acertos")
	void getHits() {
		addBarge();
		game.fireSingleShot(BARGE_POSITION, false);
		assertEquals(1, game.getHits(), "Devia haver um acerto registado.");
	}

	@Test
	@DisplayName("O último tiro num navio incrementa o contador de afundamentos")
	void getSunkShips() {
		addBarge();
		game.fireSingleShot(BARGE_POSITION, false);
		assertEquals(1, game.getSunkShips(), "Devia haver um navio afundado registado.");
	}

	@Test
	@DisplayName("A frota conta apenas os navios que ainda flutuam")
	void getRemainingShips() {
		Barge barge = addBarge();
		Frigate frigate = new Frigate(Compass.EAST, new Position(5, 5));
		assertTrue(fleet.addShip(frigate), "A posição devia permitir acrescentar a fragata.");
		assertEquals(2, game.getRemainingShips(), "Os dois navios deviam contar enquanto flutuam.");
		barge.sink();
		assertEquals(1, game.getRemainingShips(), "Só a fragata devia contar depois de afundar a barca.");
	}

	@Test
	@DisplayName("Rejeita uma jogada com apenas um tiro")
	void fireShots1() {
		assertThrows(IllegalArgumentException.class, () -> game.fireShots(List.of(new Position(2, 3))),
				"Uma jogada com um tiro devia lançar IllegalArgumentException.");
		assertTrue(game.getAlienMoves().isEmpty(), "Uma jogada rejeitada não devia entrar no histórico.");
	}

	@Test
	@DisplayName("Regista três tiros e numera as jogadas em sequência")
	void fireShots2() {
		List<IPosition> first = fireAtWater();
		List<IPosition> second = List.of(new Position(1, 0), new Position(1, 1), new Position(1, 2));
		game.fireShots(second);
		List<IMove> moves = game.getAlienMoves();
		assertEquals(2, moves.size(), "As duas jogadas deviam entrar no histórico.");
		assertAll(
				() -> assertEquals(1, moves.get(0).getNumber(), "A primeira jogada devia ter o número 1."),
				() -> assertEquals(2, moves.get(1).getNumber(), "A segunda jogada devia ter o número 2."),
				() -> assertEquals(first, moves.get(0).getShots(), "Os primeiros tiros deviam manter a ordem."),
				() -> assertEquals(second, moves.get(1).getShots(), "Os segundos tiros deviam manter a ordem.")
		);
	}

	@Test
	@DisplayName("Rejeita uma lista de tiros nula com as assertions JVM ativas")
	void fireShots3() {
		requireGameAssertions();
		assertThrows(AssertionError.class, () -> game.fireShots(null),
				"Uma lista nula devia violar a pré-condição de fireShots.");
	}

	@Test
	@DisplayName("Uma jogada regista o segundo tiro na mesma posição como repetido")
	void fireShots4() {
		game.fireShots(List.of(new Position(2, 3), new Position(2, 3), new Position(2, 4)));
		List<IGame.ShotResult> results = game.getAlienMoves().getFirst().getShotResults();
		assertEquals(3, results.size(), "O jogo devia registar um resultado por tiro.");
		assertAll(
				() -> assertFalse(results.get(0).repeated(), "A primeira ocorrência devia ser nova."),
				() -> assertTrue(results.get(1).repeated(), "A segunda ocorrência devia ser repetida."),
				() -> assertFalse(results.get(2).repeated(), "O tiro noutra posição devia ser novo."),
				() -> assertEquals(1, game.getRepeatedShots(), "Só um tiro da jogada devia ser contado como repetido.")
		);
	}

	@Test
	@DisplayName("Um tiro fora do tabuleiro é inválido")
	void fireSingleShot1() {
		IGame.ShotResult result = game.fireSingleShot(new Position(-1, 5), false);
		assertAll(
				() -> assertFalse(result.valid(), "O tiro com linha negativa devia ser inválido."),
				() -> assertNull(result.ship(), "Um tiro inválido não devia indicar um navio."),
				() -> assertEquals(1, game.getInvalidShots(), "O contador de inválidos devia passar a 1.")
		);
	}

	@Test
	@DisplayName("O indicador de repetição evita processar o acerto")
	void fireSingleShot2() {
		Barge barge = addBarge();
		IGame.ShotResult result = game.fireSingleShot(BARGE_POSITION, true);
		assertAll(
				() -> assertTrue(result.repeated(), "Um tiro marcado como repetido devia produzir um resultado repetido."),
				() -> assertEquals(1, game.getRepeatedShots(), "Devia ser registado um tiro repetido."),
				() -> assertTrue(barge.stillFloating(), "O tiro repetido devia deixar a barca a flutuar.")
		);
	}

	@Test
	@DisplayName("O histórico identifica um tiro repetido mesmo sem indicador")
	void fireSingleShot3() {
		List<IPosition> previous = fireAtWater();
		IGame.ShotResult result = game.fireSingleShot(previous.getFirst(), false);
		assertAll(
				() -> assertTrue(result.repeated(), "Uma coordenada do histórico devia ser identificada como repetida."),
				() -> assertEquals(1, game.getRepeatedShots(), "Devia ser registada uma repetição.")
		);
	}

	@Test
	@DisplayName("Um tiro na água é válido e não atinge nenhum navio")
	void fireSingleShot4() {
		IGame.ShotResult result = game.fireSingleShot(BARGE_POSITION, false);
		assertAll(
				() -> assertTrue(result.valid(), "O tiro dentro do tabuleiro devia ser válido."),
				() -> assertFalse(result.repeated(), "O primeiro tiro nessa posição devia ser novo."),
				() -> assertNull(result.ship(), "Um tiro na água não devia indicar um navio atingido."),
				() -> assertEquals(0, game.getHits(), "Um tiro na água não devia incrementar os acertos.")
		);
	}

	@Test
	@DisplayName("Um acerto na fragata regista o navio e deixa-o a flutuar")
	void fireSingleShot5() {
		Frigate frigate = new Frigate(Compass.NORTH, new Position(5, 5));
		assertTrue(fleet.addShip(frigate), "A posição devia permitir acrescentar a fragata.");
		IGame.ShotResult result = game.fireSingleShot(frigate.getPositions().getFirst(), false);
		assertAll(
				() -> assertSame(frigate, result.ship(), "O resultado devia indicar a fragata atingida."),
				() -> assertFalse(result.sunk(), "A fragata devia continuar a flutuar depois do primeiro tiro."),
				() -> assertEquals(1, game.getHits(), "Devia ser registado um acerto."),
				() -> assertEquals(0, game.getSunkShips(), "Nenhum navio devia ficar afundado.")
		);
	}

	@Test
	@DisplayName("Um acerto na única posição afunda a barca")
	void fireSingleShot6() {
		Barge barge = addBarge();
		IGame.ShotResult result = game.fireSingleShot(barge.getPositions().getFirst(), false);
		assertAll(
				() -> assertSame(barge, result.ship(), "O resultado devia indicar a barca atingida."),
				() -> assertTrue(result.sunk(), "A barca devia afundar com um acerto."),
				() -> assertEquals(1, game.getHits(), "Devia ser registado um acerto."),
				() -> assertEquals(1, game.getSunkShips(), "Devia ser registado um afundamento.")
		);
	}

	@Test
	@DisplayName("Rejeita um alvo nulo com as assertions JVM ativas")
	void fireSingleShot7() {
		requireGameAssertions();
		assertThrows(AssertionError.class, () -> game.fireSingleShot(null, false),
				"Um alvo nulo devia violar a pré-condição de fireSingleShot.");
	}

	@Test
	@DisplayName("Uma posição é nova quando ainda não há jogadas")
	void repeatedShot1() {
		assertFalse(game.repeatedShot(BARGE_POSITION), "Um histórico vazio não devia indicar repetição.");
	}

	@Test
	@DisplayName("Uma posição ausente de um histórico preenchido é nova")
	void repeatedShot2() {
		fireAtWater();
		assertFalse(game.repeatedShot(BARGE_POSITION), "Uma posição ausente do histórico devia ser nova.");
	}

	@Test
	@DisplayName("Encontra um tiro mesmo quando só está na segunda jogada")
	void repeatedShot3() {
		fireAtWater();
		game.fireShots(List.of(BARGE_POSITION, new Position(1, 2), new Position(1, 3)));
		assertTrue(game.repeatedShot(BARGE_POSITION), "A posição da segunda jogada devia ser reconhecida.");
	}

	@Test
	@DisplayName("Rejeita uma posição nula com as assertions JVM ativas")
	void repeatedShot4() {
		requireGameAssertions();
		assertThrows(AssertionError.class, () -> game.repeatedShot(null),
				"Uma posição nula devia violar a pré-condição de repeatedShot.");
	}

	@Test
	@DisplayName("Lê coordenadas compactas e devolve os tiros em JSON")
	void readEnemyFire1() throws JsonProcessingException {
		String json = readEnemyFireInput("a1 B2 C3");
		assertReadShots(json);
	}

	@Test
	@DisplayName("Lê coordenadas com a letra separada da coluna")
	void readEnemyFire2() throws JsonProcessingException {
		String json = readEnemyFireInput("A 1 b 2 C 3");
		assertReadShots(json);
	}

	@Test
	@DisplayName("Rejeita uma letra sem o número da coluna")
	void readEnemyFire3() {
		assertRejectedInput("A");
	}

	@Test
	@DisplayName("Rejeita uma entrada com menos de três tiros")
	void readEnemyFire4() {
		assertRejectedInput("A1 B2");
	}

	@Test
	@DisplayName("Rejeita uma linha sem coordenadas")
	void readEnemyFire5() {
		assertRejectedInput("   ");
	}

	@Test
	@DisplayName("Rejeita uma coordenada com formato inválido")
	void readEnemyFire6() {
		assertRejectedInput("A1 ? C3");
	}

	@Test
	@DisplayName("Rejeita um scanner nulo com as assertions JVM ativas")
	void readEnemyFire7() {
		requireGameAssertions();
		assertThrows(AssertionError.class, () -> game.readEnemyFire(null),
				"Um scanner nulo devia violar a pré-condição de readEnemyFire.");
	}

	@Test
	@DisplayName("Serializa a lista vazia como um array vazio")
	void jsonShots1() throws JsonProcessingException {
		assertEquals(JSON.createArrayNode(), JSON.readTree(Game.jsonShots(List.of())),
				"A lista vazia devia gerar um array JSON vazio.");
	}

	@Test
	@DisplayName("Serializa a primeira e a última posição com coordenadas clássicas")
	void jsonShots2() throws JsonProcessingException {
		String json = Game.jsonShots(List.of(new Position(0, 0), new Position(9, 9)));
		assertEquals(JSON.readTree("""
                [{"row":"A","column":1},{"row":"J","column":10}]
                """), JSON.readTree(json), "O JSON devia conter A1 e J10 com colunas numéricas.");
	}

	@Test
	@DisplayName("Rejeita uma lista nula com as assertions JVM ativas")
	void jsonShots3() {
		requireGameAssertions();
		assertThrows(AssertionError.class, () -> Game.jsonShots(null),
				"Uma lista nula devia violar a pré-condição de jsonShots.");
	}

	@Test
	@DisplayName("Preserva a causa quando o serializador rejeita o nível de aninhamento")
	@ResourceLock("jackson.streamWriteConstraints")
	void jsonShots4() {
		StreamWriteConstraints original = StreamWriteConstraints.defaults();
		try {
			StreamWriteConstraints.overrideDefaultStreamWriteConstraints(
					StreamWriteConstraints.builder().maxNestingDepth(0).build());
			RuntimeException error = assertThrows(RuntimeException.class,
					() -> Game.jsonShots(List.of(BARGE_POSITION)), "Uma falha do Jackson devia ser propagada.");
			assertAll(
					() -> assertEquals("Erro ao serializar o JSON", error.getMessage(),
							"A exceção devia identificar a falha de serialização."),
					() -> assertInstanceOf(StreamConstraintsException.class, error.getCause(),
							"A exceção devia preservar a causa original do Jackson.")
			);
		} finally {
			StreamWriteConstraints.overrideDefaultStreamWriteConstraints(original);
		}
	}

	@Test
	@DisplayName("Num tabuleiro vazio produz uma jogada de três tiros válidos e distintos")
	void randomEnemyFire1() throws JsonProcessingException {
		String json = game.randomEnemyFire();
		assertEquals(1, game.getAlienMoves().size(), "O disparo devia registar uma jogada.");
		List<IPosition> shots = game.getAlienMoves().getFirst().getShots();
		assertAll(
				() -> assertEquals(3, shots.size(), "Uma jogada devia conter três tiros."),
				() -> assertEquals(3, shots.stream().distinct().count(), "Os três tiros deviam ser distintos."),
				() -> assertTrue(shots.stream().allMatch(IPosition::isInside), "Os três tiros deviam estar no tabuleiro."),
				() -> assertEquals(JSON.readTree(Game.jsonShots(shots)), JSON.readTree(json),
						"O JSON devia corresponder aos tiros registados.")
		);
	}

	@Test
	@DisplayName("Evita tiros anteriores e a vizinhança do navio afundado")
	void randomEnemyFire2() {
		Barge barge = addBarge();
		List<IPosition> previous = List.of(BARGE_POSITION, new Position(0, 0), new Position(0, 1));
		game.fireShots(previous);
		assertFalse(barge.stillFloating(), "A primeira jogada devia afundar a barca.");
		game.randomEnemyFire();
		List<IPosition> shots = game.getAlienMoves().getLast().getShots();
		assertAll(
				() -> assertEquals(2, game.getAlienMoves().size(), "O disparo devia acrescentar uma segunda jogada."),
				() -> assertTrue(shots.stream().noneMatch(previous::contains), "Os tiros já usados deviam ser excluídos."),
				() -> assertTrue(shots.stream().noneMatch(barge.getAdjacentPositions()::contains),
						"Os vizinhos da barca afundada deviam ser excluídos.")
		);
	}

	@Test
	@DisplayName("Repete a única posição disponível até completar três tiros")
	void randomEnemyFire3() {
		Position last = new Position(9, 9);
		fillHistoryExcept(Set.of(last));
		game.randomEnemyFire();
		assertEquals(List.of(last, last, last), game.getAlienMoves().getLast().getShots(),
				"A única posição livre devia aparecer nos três tiros.");
	}

	@Test
	@DisplayName("Usa as duas posições restantes e repete a última selecionada")
	void randomEnemyFire4() {
		Set<Position> remaining = Set.of(new Position(9, 8), new Position(9, 9));
		fillHistoryExcept(remaining);
		game.randomEnemyFire();
		List<IPosition> shots = game.getAlienMoves().getLast().getShots();
		assertEquals(3, shots.size(), "O disparo devia completar três tiros.");
		assertAll(
				() -> assertEquals(remaining, Set.copyOf(shots.subList(0, 2)), "Os primeiros dois tiros deviam esgotar as posições livres."),
				() -> assertEquals(shots.get(1), shots.get(2), "O terceiro tiro devia repetir a última posição escolhida.")
		);
	}

	@Test
	@DisplayName("Usa cada posição uma vez quando restam exatamente três")
	void randomEnemyFire5() {
		Set<Position> remaining = Set.of(new Position(9, 7), new Position(9, 8), new Position(9, 9));
		fillHistoryExcept(remaining);
		game.randomEnemyFire();
		List<IPosition> shots = game.getAlienMoves().getLast().getShots();
		assertAll(
				() -> assertEquals(3, shots.size(), "A jogada devia conter três tiros."),
				() -> assertEquals(remaining, Set.copyOf(shots), "Cada uma das três posições devia ser escolhida.")
		);
	}

	@Test
	@DisplayName("Sem tiros nem legenda, apresenta o tabuleiro base")
	void printBoard1() {
		addBarge();
		String output = captureOutput(() -> Game.printBoard(fleet, List.of(), false, false));
		assertAll(
				() -> assertBoardCell(output, BARGE_POSITION, '#'),
				() -> assertFalse(output.contains(I18n.get("board.legend.header")), "A legenda devia estar ausente.")
		);
	}

	@Test
	@DisplayName("Com tiros e sem legenda, mostra só as marcas dos tiros")
	void printBoard2() {
		String output = captureOutput(() -> Game.printBoard(fleet,
				shownShots(new Position(0, 0)), true, false));
		assertAll(
				() -> assertBoardCell(output, new Position(0, 0), 'o'),
				() -> assertFalse(output.contains(I18n.get("board.legend.header")), "A legenda devia estar ausente.")
		);
	}

	@Test
	@DisplayName("Com legenda e sem tiros, conserva a água sem marca")
	void printBoard3() {
		String output = captureOutput(() -> Game.printBoard(fleet,
				shownShots(new Position(0, 0)), false, true));
		assertAll(
				() -> assertBoardCell(output, new Position(0, 0), '.'),
				() -> assertTrue(output.contains(I18n.get("board.legend.header")), "A legenda devia ser apresentada.")
		);
	}

	@Test
	@DisplayName("Apresenta em conjunto um acerto, um tiro na água e a legenda")
	void printBoard4() {
		Barge barge = addBarge();
		barge.sink();
		Position adjacent = new Position(1, 2);
		Position water = new Position(1, 3);
		String output = captureOutput(() -> Game.printBoard(fleet,
				shownShots(BARGE_POSITION, water, adjacent, BARGE_POSITION, new Position(10, 0)), true, true));
		assertAll(
				() -> assertBoardCell(output, BARGE_POSITION, '*'),
				() -> assertBoardCell(output, adjacent, 'o'),
				() -> assertBoardCell(output, water, 'o'),
				() -> assertTrue(output.contains(I18n.get("board.legend.header")), "A legenda devia ser apresentada.")
		);
	}

	@Test
	@DisplayName("Rejeita uma frota nula com as assertions JVM ativas")
	void printBoard5() {
		requireGameAssertions();
		assertThrows(AssertionError.class, () -> Game.printBoard(null, List.of(), false, false),
				"Uma frota nula devia violar a primeira pré-condição de printBoard.");
	}

	@Test
	@DisplayName("Rejeita um histórico nulo com as assertions JVM ativas")
	void printBoard6() {
		requireGameAssertions();
		assertThrows(AssertionError.class, () -> Game.printBoard(fleet, null, false, false),
				"Um histórico nulo devia violar a segunda pré-condição de printBoard.");
	}

	@Test
	@DisplayName("Um navio a flutuar tem marca própria e água nas casas adjacentes")
	void printShips1() {
		addBarge();
		String output = captureOutput(() -> Game.printBoard(fleet, List.of(), false, false));
		assertAll(
				() -> assertBoardCell(output, BARGE_POSITION, '#'),
				() -> assertBoardCell(output, new Position(1, 2), '.')
		);
	}

	@Test
	@DisplayName("Um navio afundado marca a água adjacente")
	void printShips2() {
		Barge barge = addBarge();
		barge.sink();
		String output = captureOutput(() -> Game.printBoard(fleet, List.of(), false, false));
		assertAll(
				() -> assertBoardCell(output, BARGE_POSITION, '#'),
				() -> assertBoardCell(output, new Position(1, 2), '-')
		);
	}

	@Test
	@DisplayName("Uma frota vazia não escreve símbolos de navios")
	void printShips3() {
		String output = captureOutput(() -> Game.printBoard(fleet, List.of(), false, false));
		assertFalse(output.contains("#"), "Um tabuleiro sem navios não devia mostrar marcas de navios.");
	}

	@Test
	@DisplayName("Sem jogadas, pedir as marcas dos tiros deixa as casas intactas")
	void printBoardShots1() {
		String output = captureOutput(() -> Game.printBoard(fleet, List.of(), true, false));
		assertBoardCell(output, new Position(0, 0), '.');
	}

	@Test
	@DisplayName("Um tiro fora do tabuleiro é ignorado na visualização")
	void printBoardShots2() {
		String output = captureOutput(() -> Game.printBoard(fleet, shownShots(new Position(10, 0)), true, false));
		assertAll(
				() -> assertBoardCell(output, new Position(9, 0), '.'),
				() -> assertBoardCell(output, new Position(0, 0), '.')
		);
	}

	@Test
	@DisplayName("O tabuleiro de consola apresenta dez colunas e dez linhas")
	void printBoardFrame() {
		String output = captureOutput(() -> Game.printBoard(fleet, List.of(), false, false));
		assertAll(
				() -> assertTrue(output.lines().anyMatch(line -> line.strip().equals("1 2 3 4 5 6 7 8 9 10")),
						"O cabeçalho devia enumerar as dez colunas."),
				() -> assertEquals(10, output.lines().filter(line -> line.matches(" [A-J] \\|.*")).count(),
						"O tabuleiro devia apresentar dez linhas entre A e J.")
		);
	}

	@Test
	@DisplayName("Mostra a frota própria no modo sem interface gráfica")
	void printMyBoard() {
		requireHeadless();
		addBarge();
		String output = captureOutput(() -> game.printMyBoard(false, false));
		assertBoardCell(output, BARGE_POSITION, '#');
	}

	@Test
	@DisplayName("Mostra a frota inimiga no modo sem interface gráfica")
	void printAlienBoard() {
		requireHeadless();
		Position alienPosition = new Position(3, 3);
		assertTrue(game.getAlienFleet().addShip(new Barge(Compass.NORTH, alienPosition)),
				"A posição devia permitir acrescentar um navio à frota inimiga.");
		String output = captureOutput(() -> game.printAlienBoard(false, false));
		assertBoardCell(output, alienPosition, '#');
	}

	@Test
	@DisplayName("Imprime a mensagem localizada de fim de jogo")
	void over() {
		String output = captureOutput(game::over);
		assertEquals(List.of("", I18n.get("game.over.line1"), I18n.get("game.over.line2"), I18n.get("game.over.line3")),
				output.lines().toList(), "O fim do jogo devia imprimir as três linhas localizadas após uma linha vazia.");
	}

	private Barge addBarge() {
		Barge barge = new Barge(Compass.NORTH, BARGE_POSITION);
		assertTrue(fleet.addShip(barge), "A posição devia permitir acrescentar a barca.");
		return barge;
	}

	private List<IPosition> fireAtWater() {
		List<IPosition> shots = List.of(new Position(0, 0), new Position(0, 1), new Position(0, 2));
		game.fireShots(shots);
		return shots;
	}

	private String readEnemyFireInput(String text) {
		try (Scanner input = new Scanner(text)) {
			return game.readEnemyFire(input);
		}
	}

	private void assertReadShots(String json) throws JsonProcessingException {
		assertEquals(1, game.getAlienMoves().size(), "A leitura devia registar uma jogada.");
		assertEquals(List.of(new Position(0, 0), new Position(1, 1), new Position(2, 2)),
				game.getAlienMoves().getFirst().getShots(), "As coordenadas esperadas eram A1, B2 e C3.");
		assertEquals(JSON.readTree("""
                [{"row":"A","column":1},{"row":"B","column":2},{"row":"C","column":3}]
                """), JSON.readTree(json), "O JSON devia representar os mesmos três tiros.");
	}

	private void assertRejectedInput(String input) {
		assertThrows(IllegalArgumentException.class, () -> readEnemyFireInput(input),
				() -> "A entrada \"" + input + "\" devia ser rejeitada.");
		assertTrue(game.getAlienMoves().isEmpty(), "Uma entrada rejeitada não devia produzir jogadas.");
	}

	private void fillHistoryExcept(Set<Position> available) {
		List<IPosition> used = new ArrayList<>();
		for (int row = 0; row < Game.BOARD_SIZE; row++) {
			for (int col = 0; col < Game.BOARD_SIZE; col++) {
				Position position = new Position(row, col);
				if (!available.contains(position)) {
					used.add(position);
				}
			}
		}
		while (used.size() % Game.NUMBER_SHOTS != 0) {
			used.add(used.getFirst());
		}

		captureOutput(() -> {
			for (int index = 0; index < used.size(); index += Game.NUMBER_SHOTS) {
				game.fireShots(used.subList(index, index + Game.NUMBER_SHOTS));
			}
		});
	}

	private List<IMove> shownShots(IPosition... positions) {
		return List.of(new Move(1, List.of(positions), List.of()));
	}

	private void assertBoardCell(String board, Position position, char expected) {
		String rowPrefix = " " + position.getClassicRow() + " |";
		String row = board.lines()
				.filter(line -> line.startsWith(rowPrefix))
				.findFirst()
				.orElseThrow(() -> new AssertionError("O tabuleiro devia conter a linha " + position.getClassicRow()));
		String[] cells = row.split("\\|")[1].strip().split("\\s+");
		assertEquals(String.valueOf(expected), cells[position.getColumn()],
				() -> "A posição " + position + " devia ter o símbolo " + expected + ".");
	}

	private String captureOutput(Runnable action) {
		PrintStream original = System.out;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try (PrintStream capture = new PrintStream(output, true, StandardCharsets.UTF_8)) {
			System.setOut(capture);
			action.run();
		} finally {
			System.setOut(original);
		}
		return output.toString(StandardCharsets.UTF_8);
	}

	private void requireGameAssertions() {
		assumeTrue(Game.class.desiredAssertionStatus(), "Este teste requer assertions JVM ativas (-ea).");
	}

	private void requireHeadless() {
		assumeTrue(GraphicsEnvironment.isHeadless(), "Este teste requer -Djava.awt.headless=true.");
	}
}
