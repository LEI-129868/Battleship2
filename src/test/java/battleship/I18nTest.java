package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class I18nTest {

    @BeforeEach
    void setUp() {
        I18n.setLocale(I18n.PORTUGUESE);
    }

    @AfterEach
    void tearDown() {
        I18n.setLocale(I18n.PORTUGUESE);
    }

    @Test
    void testDefaultPortugueseMessages() {
        assertEquals(I18n.PORTUGUESE, I18n.getLocale());
        assertEquals("Bons ventos!", I18n.get("menu.goodbye"));
        assertEquals("Que comando é esse??? Repete ...", I18n.get("menu.unknown_command"));
        assertEquals("Estado da Frota: 10 a flutuar, 0 afundados!", I18n.get("fleet.status", 10, 0));
    }

    @Test
    void testEnglishMessages() {
        I18n.setLocale("en");
        assertEquals(I18n.ENGLISH, I18n.getLocale());
        assertEquals("Fair winds!", I18n.get("menu.goodbye"));
        assertEquals("What command is that??? Try again ...", I18n.get("menu.unknown_command"));
        assertEquals("Fleet Status: 10 floating, 0 sunk!", I18n.get("fleet.status", 10, 0));
    }

    @Test
    void testPluralFormattingInPortuguese() {
        I18n.setLocale(I18n.PORTUGUESE);
        assertEquals("1 tiro repetido", I18n.get("move.repeated", 1));
        assertEquals("3 tiros repetidos", I18n.get("move.repeated", 3));
        assertEquals("1 tiro na água", I18n.get("move.miss", 1));
        assertEquals("3 tiros na água", I18n.get("move.miss", 3));
        assertEquals("1 tiro exterior", I18n.get("move.outside", 1));
        assertEquals("2 tiros exteriores", I18n.get("move.outside", 2));
    }

    @Test
    void testPluralFormattingInEnglish() {
        I18n.setLocale(I18n.ENGLISH);
        assertEquals("1 repeated shot", I18n.get("move.repeated", 1));
        assertEquals("3 repeated shots", I18n.get("move.repeated", 3));
        assertEquals("1 shot in the water", I18n.get("move.miss", 1));
        assertEquals("3 shots in the water", I18n.get("move.miss", 3));
        assertEquals("1 outside shot", I18n.get("move.outside", 1));
        assertEquals("2 outside shots", I18n.get("move.outside", 2));
    }

    @Test
    void testShipCategoryTranslation() {
        I18n.setLocale(I18n.PORTUGUESE);
        assertEquals("Barca", I18n.getShipCategory("Barca"));
        assertEquals("Caravela", I18n.getShipCategory("Caravela"));
        assertEquals("Nau", I18n.getShipCategory("Nau"));
        assertEquals("Fragata", I18n.getShipCategory("Fragata"));
        assertEquals("Galeao", I18n.getShipCategory("Galeao"));

        I18n.setLocale(I18n.ENGLISH);
        assertEquals("Barge", I18n.getShipCategory("Barca"));
        assertEquals("Caravel", I18n.getShipCategory("Caravela"));
        assertEquals("Carrack", I18n.getShipCategory("Nau"));
        assertEquals("Frigate", I18n.getShipCategory("Fragata"));
        assertEquals("Galleon", I18n.getShipCategory("Galeao"));
    }

    @Test
    void testMissingKey() {
        assertEquals("!non.existent.key!", I18n.get("non.existent.key"));
    }

    @Test
    void testMoveProcessingInPortugueseAndEnglish() {
        List<IPosition> shots = List.of(new Position(0, 0), new Position(0, 1), new Position(0, 2));
        List<IGame.ShotResult> results = List.of(
                new IGame.ShotResult(true, false, null, false),
                new IGame.ShotResult(true, false, null, false),
                new IGame.ShotResult(true, false, null, false)
        );
        Move move = new Move(1, shots, results);

        I18n.setLocale("pt");
        String jsonPt = move.processEnemyFire(false);
        assertNotNull(jsonPt);
        assertTrue(jsonPt.contains("\"missedShots\" : 3"));

        I18n.setLocale("en");
        String jsonEn = move.processEnemyFire(false);
        assertNotNull(jsonEn);
        assertTrue(jsonEn.contains("\"missedShots\" : 3"));
    }
}
