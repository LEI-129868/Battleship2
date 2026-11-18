package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários da classe Tasks.
 * Aluno: Ricardo Paiva (LEI-129822)
 * Referência: Issue #57
 */
public class TasksTest {

	private final InputStream originalIn = System.in;
	private final PrintStream originalOut = System.out;
	private ByteArrayOutputStream outContent;

	@BeforeEach
	void setUp() {
		outContent = new ByteArrayOutputStream();
		System.setOut(new PrintStream(outContent, true, StandardCharsets.UTF_8));
		I18n.setLocale(I18n.PORTUGUESE);
	}

	@AfterEach
	void tearDown() {
		System.setIn(originalIn);
		System.setOut(originalOut);
		I18n.setLocale(I18n.PORTUGUESE);
	}

	@Test
	@DisplayName("Instanciação do construtor da classe Tasks")
	void testConstructor() {
		Tasks tasks = new Tasks();
		assertNotNull(tasks);
	}

	@Test
	@DisplayName("menuHelp imprime as instruções de ajuda do menu")
	void testMenuHelp() {
		Tasks.menuHelp();
		String output = outContent.toString(StandardCharsets.UTF_8);
		assertFalse(output.isBlank());
		assertTrue(output.contains("gerafrota"));
		assertTrue(output.contains("desisto"));
	}

	@Test
	@DisplayName("readPosition lê corretamente linha e coluna inteiras")
	void testReadPosition() {
		Scanner scanner = new Scanner("4 7");
		Position pos = Tasks.readPosition(scanner);
		assertNotNull(pos);
		assertEquals(4, pos.getRow());
		assertEquals(7, pos.getColumn());
	}

	@Test
	@DisplayName("readClassicPosition lê formato compacto (ex: B4 e minúscula c10)")
	void testReadClassicPositionCompact() {
		Scanner scannerCompact = new Scanner("B4");
		IPosition pos = Tasks.readClassicPosition(scannerCompact);
		assertEquals(1, pos.getRow());
		assertEquals(3, pos.getColumn()); // 4 - 1 = 3

		Scanner scannerLower = new Scanner("c10");
		IPosition posLower = Tasks.readClassicPosition(scannerLower);
		assertEquals(2, posLower.getRow());
		assertEquals(9, posLower.getColumn()); // 10 - 1 = 9
	}

	@Test
	@DisplayName("readClassicPosition lê formato com espaço (ex: A 5)")
	void testReadClassicPositionSpaced() {
		Scanner scannerSpaced = new Scanner("A 5");
		IPosition pos = Tasks.readClassicPosition(scannerSpaced);
		assertEquals(0, pos.getRow());
		assertEquals(4, pos.getColumn()); // 5 - 1 = 4
	}

	@Test
	@DisplayName("readClassicPosition lança exceção quando o scanner está vazio")
	void testReadClassicPositionEmpty() {
		Scanner emptyScanner = new Scanner("");
		assertThrows(IllegalArgumentException.class, () -> Tasks.readClassicPosition(emptyScanner));
	}

	@Test
	@DisplayName("readClassicPosition lança exceção com formato inválido")
	void testReadClassicPositionInvalid() {
		Scanner invalidScanner1 = new Scanner("12");
		assertThrows(IllegalArgumentException.class, () -> Tasks.readClassicPosition(invalidScanner1));

		Scanner invalidScanner2 = new Scanner("???");
		assertThrows(IllegalArgumentException.class, () -> Tasks.readClassicPosition(invalidScanner2));
	}

	@Test
	@DisplayName("readShip lê e constrói diferentes tipos de navios válidos")
	void testReadShipValidKinds() {
		Scanner inBarge = new Scanner("barca 2 3 n");
		Ship barge = Tasks.readShip(inBarge);
		assertNotNull(barge);
		assertEquals("Barca", barge.getCategory());

		Scanner inCaravel = new Scanner("caravela 1 1 e");
		Ship caravel = Tasks.readShip(inCaravel);
		assertNotNull(caravel);
		assertEquals("Caravela", caravel.getCategory());

		Scanner inCarrack = new Scanner("nau 0 0 s");
		Ship carrack = Tasks.readShip(inCarrack);
		assertNotNull(carrack);
		assertEquals("Nau", carrack.getCategory());

		Scanner inFrigate = new Scanner("fragata 3 3 o");
		Ship frigate = Tasks.readShip(inFrigate);
		assertNotNull(frigate);
		assertEquals("Fragata", frigate.getCategory());

		Scanner inGalleon = new Scanner("galeao 4 4 n");
		Ship galleon = Tasks.readShip(inGalleon);
		assertNotNull(galleon);
		assertEquals("Galeao", galleon.getCategory());
	}

	@Test
	@DisplayName("readShip retorna null para tipo de navio desconhecido")
	void testReadShipUnknownKind() {
		Scanner inUnknown = new Scanner("submarino 2 2 n");
		Ship unknown = Tasks.readShip(inUnknown);
		assertNull(unknown);
	}

	@Test
	@DisplayName("buildFleet constrói frota completa de 11 navios tratando navios desconhecidos e sobrepostos")
	void testBuildFleet() {
		String input = String.join("\n",
				"submarino 0 0 n", // navio desconhecido (s == null)
				"barca 0 0 n",     // 1: Barca em (0,0)
				"barca 0 0 n",     // navio repetido/sobreposto (addShip retorna false)
				"barca 0 2 n",     // 2: Barca em (0,2)
				"barca 0 4 n",     // 3: Barca em (0,4)
				"barca 0 6 n",     // 4: Barca em (0,6)
				"barca 0 8 n",     // 5: Barca em (0,8)
				"barca 2 0 n",     // 6: Barca em (2,0)
				"barca 2 2 n",     // 7: Barca em (2,2)
				"barca 2 4 n",     // 8: Barca em (2,4)
				"barca 2 6 n",     // 9: Barca em (2,6)
				"barca 2 8 n",     // 10: Barca em (2,8)
				"barca 4 0 n"      // 11: Barca em (4,0) completa a frota (FLEET_SIZE = 11)
		);

		Scanner in = new Scanner(input);
		Fleet fleet = Tasks.buildFleet(in);
		assertNotNull(fleet);
		assertEquals(Fleet.FLEET_SIZE, fleet.getShips().size());
	}

	@Test
	@DisplayName("menu executa comandos ajuda, help e termina com desisto")
	void testMenuHelpAndDesisto() {
		String input = "ajuda\nhelp\ndesisto\n";
		System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));

		Tasks.menu();

		String out = outContent.toString(StandardCharsets.UTF_8);
		assertTrue(out.contains(I18n.get("menu.goodbye")));
	}

	@Test
	@DisplayName("menu executa alteração de idioma via idioma e lang")
	void testMenuLanguageChange() {
		String input = "idioma en\nlang pt\ndesisto\n";
		System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));

		Tasks.menu();

		String out = outContent.toString(StandardCharsets.UTF_8);
		assertTrue(out.contains(I18n.get("menu.goodbye")));
	}

	@Test
	@DisplayName("menu executa comandos com frota nula sem lançar exceções")
	void testMenuNullFleetCommands() {
		String input = "estado\nmapa\ntiros\nrajada\ncomando_desconhecido\ndesisto\n";
		System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));

		Tasks.menu();

		String out = outContent.toString(StandardCharsets.UTF_8);
		assertTrue(out.contains(I18n.get("menu.unknown_command")));
		assertTrue(out.contains(I18n.get("menu.goodbye")));
	}

	@Test
	@DisplayName("menu executa gerafrota, estado, mapa, tiros e termina com quit")
	void testMenuGerafrotaAndStatus() {
		String input = "gerafrota\nestado\nmapa\ntiros\nquit\n";
		System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));

		Tasks.menu();

		String out = outContent.toString(StandardCharsets.UTF_8);
		assertTrue(out.contains(I18n.get("menu.goodbye")));
	}

	@Test
	@DisplayName("menu executa lefrota com construção de frota pelo utilizador")
	void testMenuLefrota() {
		String fleetInput = String.join("\n",
				"lefrota",
				"barca 0 0 n",
				"barca 0 2 n",
				"barca 0 4 n",
				"barca 0 6 n",
				"barca 0 8 n",
				"barca 2 0 n",
				"barca 2 2 n",
				"barca 2 4 n",
				"barca 2 6 n",
				"barca 2 8 n",
				"barca 4 0 n",
				"estado",
				"mapa",
				"desisto"
		);
		System.setIn(new ByteArrayInputStream(fleetInput.getBytes(StandardCharsets.UTF_8)));

		Tasks.menu();

		String out = outContent.toString(StandardCharsets.UTF_8);
		assertTrue(out.contains(I18n.get("menu.goodbye")));
	}

	@Test
	@DisplayName("menu executa rajada após geração de frota")
	void testMenuRajada() {
		String input = String.join("\n",
				"gerafrota",
				"rajada A1 B2 C3",
				"desisto"
		);
		System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));

		Tasks.menu();

		String out = outContent.toString(StandardCharsets.UTF_8);
		assertTrue(out.contains(I18n.get("menu.goodbye")));
	}
}
