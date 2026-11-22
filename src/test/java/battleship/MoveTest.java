package battleship;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for class Move.
 * Author: ${user.name}
 * Date: 2026-11-19 15:30
 * Cyclomatic Complexity:
 * - constructor: 1
 * - toString(): 1
 * - getNumber(): 1
 * - getShots(): 1
 * - getShotResults(): 1
 * - processEnemyFire(): 30
 */
class MoveTest {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private Move move;
	private PrintStream originalOut;
	private ByteArrayOutputStream outContent;

	@BeforeEach
	void setUp() {
		I18n.setLocale("pt");
		originalOut = System.out;
		outContent = new ByteArrayOutputStream();
		System.setOut(new PrintStream(outContent, true, StandardCharsets.UTF_8));
		move = new Move(1, new ArrayList<>(), new ArrayList<>());
	}

	@AfterEach
	void tearDown() {
		System.setOut(originalOut);
		I18n.setLocale("pt");
		move = null;
	}

	// ── helpers ──────────────────────────────────────────────────────────────

	/** Barca em A1 (0,0), já afundada. */
	private static Barge sunkBarge(int row, int col) {
		Barge barge = new Barge(Compass.NORTH, new Position(row, col));
		barge.shoot(new Position(row, col));
		return barge;
	}

	/** Caravela horizontal em C3-C4, atingida em C3 (2,2) e ainda a flutuar. */
	private static Caravel hitCaravel() {
		Caravel caravel = new Caravel(Compass.EAST, new Position(2, 2));
		caravel.shoot(new Position(2, 2));
		return caravel;
	}

	/** Nau horizontal em E5-E7, atingida em E5 e E6 e ainda a flutuar. */
	private static Carrack twiceHitCarrack() {
		Carrack carrack = new Carrack(Compass.EAST, new Position(4, 4));
		carrack.shoot(new Position(4, 4));
		carrack.shoot(new Position(4, 5));
		return carrack;
	}

	private static IGame.ShotResult invalid() {
		return new IGame.ShotResult(false, false, null, false);
	}

	private static IGame.ShotResult repeated() {
		return new IGame.ShotResult(true, true, null, false);
	}

	private static IGame.ShotResult water() {
		return new IGame.ShotResult(true, false, null, false);
	}

	private static IGame.ShotResult hit(IShip ship) {
		return new IGame.ShotResult(true, false, ship, false);
	}

	private static IGame.ShotResult sunk(IShip ship) {
		return new IGame.ShotResult(true, false, ship, true);
	}

	/** Builds the move under test with aligned shots/results and returns the parsed JSON. */
	private JsonNode fire(boolean verbose, List<IPosition> shots, List<IGame.ShotResult> results) throws Exception {
		move = new Move(1, shots, results);
		return MAPPER.readTree(move.processEnemyFire(verbose));
	}

	/** Line printed in verbose mode ("Jogada nº1 -> ..."), or null when nothing was printed. */
	private String roundLine() {
		String out = outContent.toString(StandardCharsets.UTF_8);
		for (String line : out.split("\\R"))
			if (line.startsWith("Jogada nº") || line.startsWith("Move #") || line.contains(" -> "))
				return line;
		return null;
	}

	private static List<IPosition> positions(int... rowCol) {
		List<IPosition> list = new ArrayList<>();
		for (int i = 0; i < rowCol.length; i += 2)
			list.add(new Position(rowCol[i], rowCol[i + 1]));
		return list;
	}

	private static void assertCounters(JsonNode json, int valid, int outside, int repeated, int missed) {
		assertAll("Contadores do JSON",
				() -> assertEquals(valid, json.get("validShots").asInt(),
						"Error: expected validShots=" + valid + " but got " + json.get("validShots")),
				() -> assertEquals(outside, json.get("outsideShots").asInt(),
						"Error: expected outsideShots=" + outside + " but got " + json.get("outsideShots")),
				() -> assertEquals(repeated, json.get("repeatedShots").asInt(),
						"Error: expected repeatedShots=" + repeated + " but got " + json.get("repeatedShots")),
				() -> assertEquals(missed, json.get("missedShots").asInt(),
						"Error: expected missedShots=" + missed + " but got " + json.get("missedShots")));
	}

	// ── constructor & simple accessors ───────────────────────────────────────

	@Test
	@DisplayName("constructor: guarda número, tiros e resultados")
	void constructor() {
		List<IPosition> shots = positions(0, 0);
		List<IGame.ShotResult> results = List.of(water());
		Move m = new Move(7, shots, results);

		assertAll("Estado inicial",
				() -> assertEquals(7, m.getNumber(), "Error: expected number 7 but got " + m.getNumber()),
				() -> assertSame(shots, m.getShots(), "Error: expected the same shots list but got another one"),
				() -> assertSame(results, m.getShotResults(), "Error: expected the same results list but got another one"));
	}

	@Test
	@DisplayName("toString: mostra o número da jogada e o tamanho das listas de tiros e resultados")
	void testToString() {
		Move m = new Move(1, positions(0, 0, 1, 1), List.of(water()));

		assertEquals("Move{number=1, shots=2, results=1}", m.toString(),
				"Error: expected 'Move{number=1, shots=2, results=1}' but got " + m);
	}

	@Test
	@DisplayName("getNumber: devolve o número passado no construtor")
	void getNumber() {
		assertEquals(1, move.getNumber(), "Error: expected number 1 but got " + move.getNumber());
	}

	@Test
	@DisplayName("getShots: devolve a lista de tiros passada no construtor")
	void getShots() {
		List<IPosition> shots = positions(3, 3);
		Move m = new Move(2, shots, new ArrayList<>());

		assertSame(shots, m.getShots(), "Error: expected the same shots list but got " + m.getShots());
	}

	@Test
	@DisplayName("getShotResults: devolve a lista de resultados passada no construtor")
	void getShotResults() {
		List<IGame.ShotResult> results = List.of(water());
		Move m = new Move(2, positions(3, 3), results);

		assertSame(results, m.getShotResults(), "Error: expected the same results list but got " + m.getShotResults());
	}

	// ── processEnemyFire ─────────────────────────────────────────────────────

	@Test
	@DisplayName("processEnemyFire: sem resultados e verbose=false -> 3 tiros exteriores e nada impresso na ronda")
	void processEnemyFire1() throws Exception {
		JsonNode json = fire(false, new ArrayList<>(), new ArrayList<>());

		assertAll(
				() -> assertCounters(json, 0, 3, 0, 0),
				() -> assertEquals(0, json.get("sunkBoats").size(), "Error: expected no sunk boats but got " + json.get("sunkBoats")),
				() -> assertEquals(0, json.get("hitsOnBoats").size(), "Error: expected no hits but got " + json.get("hitsOnBoats")),
				() -> assertNull(roundLine(), "Error: expected no round summary with verbose=false but got " + roundLine()));
	}

	@Test
	@DisplayName("processEnemyFire: sem resultados e verbose=true -> mensagem só com os tiros exteriores (sem vírgula)")
	void processEnemyFire2() throws Exception {
		JsonNode json = fire(true, new ArrayList<>(), new ArrayList<>());

		assertAll(
				() -> assertCounters(json, 0, 3, 0, 0),
				() -> assertEquals("Jogada nº1 -> 3 tiros exteriores", roundLine(),
						"Error: expected 'Jogada nº1 -> 3 tiros exteriores' but got " + roundLine()));
	}

	@Test
	@DisplayName("processEnemyFire: tiro inválido é ignorado (continue) e conta como exterior")
	void processEnemyFire3() throws Exception {
		JsonNode json = fire(false, positions(0, 0), List.of(invalid()));

		assertCounters(json, 0, 3, 0, 0);
	}

	@Test
	@DisplayName("processEnemyFire: 3 tiros repetidos (validShots==0 && repeatedShots>0) -> só mensagem de repetidos")
	void processEnemyFire4() throws Exception {
		JsonNode json = fire(true, positions(0, 0, 0, 1, 0, 2), List.of(repeated(), repeated(), repeated()));

		assertAll(
				() -> assertCounters(json, 0, 0, 3, 0),
				() -> assertEquals("Jogada nº1 -> 3 tiros repetidos", roundLine(),
						"Error: expected 'Jogada nº1 -> 3 tiros repetidos' but got " + roundLine()));
	}

	@Test
	@DisplayName("processEnemyFire: 1 repetido + 2 inválidos -> repetidos seguidos de ', 2 tiros exteriores'")
	void processEnemyFire5() throws Exception {
		JsonNode json = fire(true, positions(0, 0, 0, 1, 0, 2), List.of(repeated(), invalid(), invalid()));

		assertAll(
				() -> assertCounters(json, 0, 2, 1, 0),
				() -> assertEquals("Jogada nº1 -> 1 tiro repetido, 2 tiros exteriores", roundLine(),
						"Error: expected 'Jogada nº1 -> 1 tiro repetido, 2 tiros exteriores' but got " + roundLine()));
	}

	@Test
	@DisplayName("processEnemyFire: 1 tiro repetido com verbose=false só afeta o JSON")
	void processEnemyFire6() throws Exception {
		JsonNode json = fire(false, positions(0, 0), List.of(repeated()));

		assertAll(
				() -> assertCounters(json, 0, 2, 1, 0),
				() -> assertNull(roundLine(), "Error: expected no round summary but got " + roundLine()));
	}

	@Test
	@DisplayName("processEnemyFire: 1 tiro na água -> válido, água e exteriores na mensagem")
	void processEnemyFire7() throws Exception {
		JsonNode json = fire(true, positions(5, 5), List.of(water()));

		assertAll(
				() -> assertCounters(json, 1, 2, 0, 1),
				() -> assertEquals("Jogada nº1 -> 1 tiro válido: 1 tiro na água, 2 tiros exteriores", roundLine(),
						"Error: expected valid + water + outside message but got " + roundLine()));
	}

	@Test
	@DisplayName("processEnemyFire: 3 tiros na água -> sem tiros exteriores (outsideShots == 0)")
	void processEnemyFire8() throws Exception {
		JsonNode json = fire(true, positions(5, 5, 5, 6, 5, 7), List.of(water(), water(), water()));

		assertAll(
				() -> assertCounters(json, 3, 0, 0, 3),
				() -> assertEquals("Jogada nº1 -> 3 tiros válidos: 3 tiros na água", roundLine(),
						"Error: expected 'Jogada nº1 -> 3 tiros válidos: 3 tiros na água' but got " + roundLine()));
	}

	@Test
	@DisplayName("processEnemyFire: acerto numa caravela que continua a flutuar -> hitsOnBoats com C3 e '+' final removido")
	void processEnemyFire9() throws Exception {
		JsonNode json = fire(true, positions(2, 2), List.of(hit(hitCaravel())));
		JsonNode boat = json.get("hitsOnBoats").get(0);

		assertAll(
				() -> assertCounters(json, 1, 2, 0, 0),
				() -> assertEquals("Caravela", boat.get("type").asText(), "Error: expected type Caravela but got " + boat.get("type")),
				() -> assertEquals(1, boat.get("hits").asInt(), "Error: expected 1 hit but got " + boat.get("hits")),
				() -> assertEquals("C3", boat.get("cords").get(0).asText(), "Error: expected cord C3 but got " + boat.get("cords")),
				() -> assertEquals(0, json.get("sunkBoats").size(), "Error: expected no sunk boats but got " + json.get("sunkBoats")),
				() -> assertEquals("Jogada nº1 -> 1 tiro válido: 1 tiro num(a) Caravela , 2 tiros exteriores", roundLine(),
						"Error: expected hit message without trailing '+' but got " + roundLine()));
	}

	@Test
	@DisplayName("processEnemyFire: barca afundada -> sunkBoats com Barca em A1 e mensagem 'ao fundo'")
	void processEnemyFire10() throws Exception {
		JsonNode json = fire(true, positions(0, 0), List.of(sunk(sunkBarge(0, 0))));
		JsonNode boat = json.get("sunkBoats").get(0);

		assertAll(
				() -> assertCounters(json, 1, 2, 0, 0),
				() -> assertEquals("Barca", boat.get("type").asText(), "Error: expected type Barca but got " + boat.get("type")),
				() -> assertEquals(1, boat.get("count").asInt(), "Error: expected count 1 but got " + boat.get("count")),
				() -> assertEquals("A1", boat.get("cords").get(0).asText(), "Error: expected cord A1 but got " + boat.get("cords")),
				() -> assertEquals(0, json.get("hitsOnBoats").size(), "Error: expected no hits on floating boats but got " + json.get("hitsOnBoats")),
				() -> assertEquals("Jogada nº1 -> 1 tiro válido: 1 Barca ao fundo , 2 tiros exteriores", roundLine(),
						"Error: expected sunk message but got " + roundLine()));
	}

	@Test
	@DisplayName("processEnemyFire: navio que já não flutua mas sunk=false -> nem acerto nem afundado (else-if F,F)")
	void processEnemyFire11() throws Exception {
		JsonNode json = fire(true, positions(0, 0), List.of(hit(sunkBarge(0, 0))));

		assertAll(
				() -> assertCounters(json, 1, 2, 0, 0),
				() -> assertEquals(0, json.get("sunkBoats").size(), "Error: expected no sunk boats but got " + json.get("sunkBoats")),
				() -> assertEquals(0, json.get("hitsOnBoats").size(), "Error: expected no hits but got " + json.get("hitsOnBoats")),
				() -> assertEquals("Jogada nº1 -> 1 tiro válido: , 2 tiros exteriores", roundLine(),
						"Error: expected only the valid prefix and outside shots but got " + roundLine()));
	}

	@Test
	@DisplayName("processEnemyFire: água + repetido -> ', ' antes dos repetidos porque validShots > 0")
	void processEnemyFire12() throws Exception {
		JsonNode json = fire(true, positions(5, 5, 5, 6), List.of(water(), repeated()));

		assertAll(
				() -> assertCounters(json, 1, 1, 1, 1),
				() -> assertEquals("Jogada nº1 -> 1 tiro válido: 1 tiro na água, 1 tiro repetido, 1 tiro exterior", roundLine(),
						"Error: expected water + repeated + outside message but got " + roundLine()));
	}

	@Test
	@DisplayName("processEnemyFire: acerto + repetido (sem água) -> '+' removido e depois ', 1 tiro repetido'")
	void processEnemyFire13() throws Exception {
		JsonNode json = fire(true, positions(2, 2, 5, 5), List.of(hit(hitCaravel()), repeated()));

		assertAll(
				() -> assertCounters(json, 1, 1, 1, 0),
				() -> assertEquals("Jogada nº1 -> 1 tiro válido: 1 tiro num(a) Caravela , 1 tiro repetido, 1 tiro exterior", roundLine(),
						"Error: expected hit + repeated + outside message but got " + roundLine()));
	}

	@Test
	@DisplayName("processEnemyFire: afundado + acerto + água -> percorre os dois ciclos da mensagem e missedShots > 0")
	void processEnemyFire14() throws Exception {
		JsonNode json = fire(true, positions(0, 0, 2, 2, 5, 5),
				List.of(sunk(sunkBarge(0, 0)), hit(hitCaravel()), water()));

		assertAll(
				() -> assertCounters(json, 3, 0, 0, 1),
				() -> assertEquals(1, json.get("sunkBoats").size(), "Error: expected 1 sunk boat but got " + json.get("sunkBoats")),
				() -> assertEquals(1, json.get("hitsOnBoats").size(), "Error: expected 1 hit boat but got " + json.get("hitsOnBoats")),
				() -> assertEquals("Jogada nº1 -> 3 tiros válidos: 1 Barca ao fundo + 1 tiro num(a) Caravela + 1 tiro na água",
						roundLine(), "Error: expected sunk + hit + water message but got " + roundLine()));
	}

	@Test
	@DisplayName("processEnemyFire: afundado + acerto sem água -> '+' final removido (else-if com 1.º termo verdadeiro)")
	void processEnemyFire15() throws Exception {
		JsonNode json = fire(true, positions(0, 0, 2, 2), List.of(sunk(sunkBarge(0, 0)), hit(hitCaravel())));

		assertAll(
				() -> assertCounters(json, 2, 1, 0, 0),
				() -> assertEquals("Jogada nº1 -> 2 tiros válidos: 1 Barca ao fundo + 1 tiro num(a) Caravela , 1 tiro exterior",
						roundLine(), "Error: expected sunk + hit message without trailing '+' but got " + roundLine()));
	}

	@Test
	@DisplayName("processEnemyFire: duas barcas afundadas -> contagem agrupada '2 Barcas ao fundo' e 2 entradas no JSON")
	void processEnemyFire16() throws Exception {
		JsonNode json = fire(true, positions(0, 0, 9, 9), List.of(sunk(sunkBarge(0, 0)), sunk(sunkBarge(9, 9))));

		assertAll(
				() -> assertCounters(json, 2, 1, 0, 0),
				() -> assertEquals(2, json.get("sunkBoats").size(), "Error: expected 2 sunk boats but got " + json.get("sunkBoats")),
				() -> assertTrue(roundLine().contains("2 Barcas ao fundo"),
						"Error: expected '2 Barcas ao fundo' in the message but got " + roundLine()));
	}

	@Test
	@DisplayName("processEnemyFire: dois acertos na mesma nau -> hits=2 e coordenadas E5 e E6")
	void processEnemyFire17() throws Exception {
		Carrack carrack = twiceHitCarrack();
		JsonNode json = fire(false, positions(4, 4, 4, 5), List.of(hit(carrack), hit(carrack)));
		JsonNode boat = json.get("hitsOnBoats").get(0);

		assertAll(
				() -> assertCounters(json, 2, 1, 0, 0),
				() -> assertEquals("Nau", boat.get("type").asText(), "Error: expected type Nau but got " + boat.get("type")),
				() -> assertEquals(2, boat.get("hits").asInt(), "Error: expected 2 hits but got " + boat.get("hits")),
				() -> assertEquals("E5", boat.get("cords").get(0).asText(), "Error: expected first cord E5 but got " + boat.get("cords")),
				() -> assertEquals("E6", boat.get("cords").get(1).asText(), "Error: expected second cord E6 but got " + boat.get("cords")));
	}

	@Test
	@DisplayName("processEnemyFire: dois acertos na mesma nau com verbose -> mensagem '2 tiros num(a) Nau'")
	void processEnemyFire18() throws Exception {
		Carrack carrack = twiceHitCarrack();
		fire(true, positions(4, 4, 4, 5), List.of(hit(carrack), hit(carrack)));

		assertTrue(roundLine().contains("2 tiros num(a) Nau"),
				"Error: expected '2 tiros num(a) Nau' in the message but got " + roundLine());
	}

	@Test
	@DisplayName("processEnemyFire: caravela atingida e afundada na mesma jogada -> só conta como afundada")
	void processEnemyFire19() throws Exception {
		Caravel caravel = new Caravel(Compass.EAST, new Position(2, 2));
		caravel.shoot(new Position(2, 2));
		caravel.shoot(new Position(2, 3));
		JsonNode json = fire(false, positions(2, 2, 2, 3), List.of(hit(caravel), sunk(caravel)));

		assertAll(
				() -> assertCounters(json, 2, 1, 0, 0),
				() -> assertEquals(1, json.get("sunkBoats").size(), "Error: expected 1 sunk boat but got " + json.get("sunkBoats")),
				() -> assertEquals(0, json.get("hitsOnBoats").size(),
						"Error: expected no hits (ship no longer floating) but got " + json.get("hitsOnBoats")));
	}

	@Test
	@DisplayName("processEnemyFire: JSON sunkBoats ignora resultado inválido mesmo com sunk=true (valid=false)")
	void processEnemyFire20() throws Exception {
		JsonNode json = fire(false, positions(0, 0), List.of(new IGame.ShotResult(false, false, sunkBarge(0, 0), true)));

		assertAll(
				() -> assertCounters(json, 0, 3, 0, 0),
				() -> assertEquals(0, json.get("sunkBoats").size(), "Error: expected no sunk boats but got " + json.get("sunkBoats")));
	}

	@Test
	@DisplayName("processEnemyFire: JSON sunkBoats ignora tiro repetido mesmo com sunk=true (repeated=true)")
	void processEnemyFire21() throws Exception {
		JsonNode json = fire(false, positions(0, 0), List.of(new IGame.ShotResult(true, true, sunkBarge(0, 0), true)));

		assertAll(
				() -> assertCounters(json, 0, 2, 1, 0),
				() -> assertEquals(0, json.get("sunkBoats").size(), "Error: expected no sunk boats but got " + json.get("sunkBoats")));
	}

	@Test
	@DisplayName("processEnemyFire: JSON sunkBoats ignora tiro sem navio mesmo com sunk=true (ship=null)")
	void processEnemyFire22() throws Exception {
		JsonNode json = fire(false, positions(0, 0), List.of(new IGame.ShotResult(true, false, null, true)));

		assertAll(
				() -> assertCounters(json, 1, 2, 0, 1),
				() -> assertEquals(0, json.get("sunkBoats").size(), "Error: expected no sunk boats but got " + json.get("sunkBoats")));
	}

	@Test
	@DisplayName("processEnemyFire: JSON sunkBoats ignora acerto em navio não afundado (sunk=false)")
	void processEnemyFire23() throws Exception {
		JsonNode json = fire(false, positions(2, 2), List.of(hit(hitCaravel())));

		assertAll(
				() -> assertEquals(0, json.get("sunkBoats").size(), "Error: expected no sunk boats but got " + json.get("sunkBoats")),
				() -> assertEquals(1, json.get("hitsOnBoats").size(), "Error: expected 1 hit boat but got " + json.get("hitsOnBoats")));
	}

	@Test
	@DisplayName("processEnemyFire: coordenadas de um navio afundado incluem todas as suas posições (C3 e C4)")
	void processEnemyFire24() throws Exception {
		Caravel caravel = new Caravel(Compass.EAST, new Position(2, 2));
		caravel.sink();
		JsonNode json = fire(false, positions(2, 3), List.of(sunk(caravel)));
		JsonNode cords = json.get("sunkBoats").get(0).get("cords");

		assertAll(
				() -> assertEquals(2, cords.size(), "Error: expected 2 cords but got " + cords),
				() -> assertEquals("C3", cords.get(0).asText(), "Error: expected C3 but got " + cords.get(0)),
				() -> assertEquals("C4", cords.get(1).asText(), "Error: expected C4 but got " + cords.get(1)));
	}

	@Test
	@DisplayName("processEnemyFire: em inglês a mensagem usa as traduções (Move/ship names em EN)")
	void processEnemyFire25() throws Exception {
		I18n.setLocale("en");
		fire(true, positions(5, 5), List.of(water()));
		String expected = I18n.get("move.round_prefix", 1,
				I18n.get("move.valid", 1) + I18n.get("move.miss", 1) + ", " + I18n.get("move.outside", 2));

		String printed = outContent.toString(StandardCharsets.UTF_8);
		assertTrue(printed.contains(expected), "Error: expected English summary '" + expected + "' but got " + printed);
	}

	@Test
	@DisplayName("processEnemyFire: o JSON devolvido é o mesmo que é impresso e vem indentado")
	void processEnemyFire26() {
		move = new Move(1, positions(5, 5), List.of(water()));
		String json = move.processEnemyFire(false);

		assertAll(
				() -> assertTrue(outContent.toString(StandardCharsets.UTF_8).contains(json),
						"Error: expected the returned JSON to be printed but it was not"),
				() -> assertTrue(json.contains(System.lineSeparator()) || json.contains("\n"),
						"Error: expected indented (multi-line) JSON but got " + json));
	}

	@Test
	@DisplayName("processEnemyFire: mistura completa com verbose=false calcula o JSON sem imprimir a ronda")
	void processEnemyFire27() throws Exception {
		JsonNode json = fire(false, positions(0, 0, 2, 2, 5, 5),
				List.of(sunk(sunkBarge(0, 0)), hit(hitCaravel()), water()));

		assertAll(
				() -> assertCounters(json, 3, 0, 0, 1),
				() -> assertEquals(1, json.get("sunkBoats").size(), "Error: expected 1 sunk boat but got " + json.get("sunkBoats")),
				() -> assertEquals(1, json.get("hitsOnBoats").size(), "Error: expected 1 hit boat but got " + json.get("hitsOnBoats")),
				() -> assertNull(roundLine(), "Error: expected no round summary but got " + roundLine()));
	}

	@Test
	@DisplayName("processEnemyFire: inválido + água + repetido -> 1 válido, 1 repetido e 1 exterior")
	void processEnemyFire28() throws Exception {
		JsonNode json = fire(true, positions(0, 0, 5, 5, 6, 6), List.of(invalid(), water(), repeated()));

		assertAll(
				() -> assertCounters(json, 1, 1, 1, 1),
				() -> assertEquals("Jogada nº1 -> 1 tiro válido: 1 tiro na água, 1 tiro repetido, 1 tiro exterior", roundLine(),
						"Error: expected valid + water + repeated + outside message but got " + roundLine()));
	}

	@Test
	@DisplayName("processEnemyFire: acertos em dois tipos de navio diferentes -> duas entradas em hitsOnBoats")
	void processEnemyFire29() throws Exception {
		Carrack carrack = new Carrack(Compass.EAST, new Position(4, 4));
		carrack.shoot(new Position(4, 4));
		JsonNode json = fire(false, positions(2, 2, 4, 4), List.of(hit(hitCaravel()), hit(carrack)));
		JsonNode hits = json.get("hitsOnBoats");

		List<String> types = new ArrayList<>();
		hits.forEach(h -> types.add(h.get("type").asText()));

		assertAll(
				() -> assertEquals(2, hits.size(), "Error: expected 2 boats hit but got " + hits),
				() -> assertTrue(types.contains("Caravela") && types.contains("Nau"),
						"Error: expected Caravela and Nau in hitsOnBoats but got " + types));
	}

	@Test
	@DisplayName("processEnemyFire: afundado + repetido -> '+' removido e depois ', 1 tiro repetido'")
	void processEnemyFire30() throws Exception {
		JsonNode json = fire(true, positions(0, 0, 5, 5), List.of(sunk(sunkBarge(0, 0)), repeated()));

		assertAll(
				() -> assertCounters(json, 1, 1, 1, 0),
				() -> assertEquals("Jogada nº1 -> 1 tiro válido: 1 Barca ao fundo , 1 tiro repetido, 1 tiro exterior", roundLine(),
						"Error: expected sunk + repeated + outside message but got " + roundLine()));
	}
}
