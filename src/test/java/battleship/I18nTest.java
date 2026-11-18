package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários da classe I18n.
 * Aluno: Ricardo Paiva (LEI-129822)
 * Referência: Issue #57
 */
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
    @DisplayName("Construtor privado utilitário de I18n pode ser invocado via reflexão")
    void testPrivateConstructor() throws Exception {
        Constructor<I18n> constructor = I18n.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        I18n instance = constructor.newInstance();
        assertNotNull(instance);
    }

    @Test
    @DisplayName("Mensagens padrão em Português são obtidas corretamente")
    void testDefaultPortugueseMessages() {
        assertEquals(I18n.PORTUGUESE, I18n.getLocale());
        assertEquals("Bons ventos!", I18n.get("menu.goodbye"));
        assertEquals("Que comando é esse??? Repete ...", I18n.get("menu.unknown_command"));
        assertEquals("Estado da Frota: 10 a flutuar, 0 afundados!", I18n.get("fleet.status", 10, 0));
    }

    @Test
    @DisplayName("Mensagens em Inglês após definição de locale com string 'en'")
    void testEnglishMessages() {
        I18n.setLocale("en");
        assertEquals(I18n.ENGLISH, I18n.getLocale());
        assertEquals("Fair winds!", I18n.get("menu.goodbye"));
        assertEquals("What command is that??? Try again ...", I18n.get("menu.unknown_command"));
        assertEquals("Fleet Status: 10 floating, 0 sunk!", I18n.get("fleet.status", 10, 0));
    }

    @Test
    @DisplayName("setLocale com Locale nulo reverte para Português")
    void testSetLocaleWithNullLocale() {
        I18n.setLocale(I18n.ENGLISH);
        assertEquals(I18n.ENGLISH, I18n.getLocale());

        I18n.setLocale((Locale) null);
        assertEquals(I18n.PORTUGUESE, I18n.getLocale());
    }

    @Test
    @DisplayName("setLocale com String nula, vazia ou em branco reverte para Português")
    void testSetLocaleWithNullOrBlankString() {
        I18n.setLocale("en");
        assertEquals(I18n.ENGLISH, I18n.getLocale());

        I18n.setLocale((String) null);
        assertEquals(I18n.PORTUGUESE, I18n.getLocale());

        I18n.setLocale("en");
        I18n.setLocale("");
        assertEquals(I18n.PORTUGUESE, I18n.getLocale());

        I18n.setLocale("en");
        I18n.setLocale("   ");
        assertEquals(I18n.PORTUGUESE, I18n.getLocale());
    }

    @Test
    @DisplayName("setLocale com outros códigos de idioma define Português por omissão")
    void testSetLocaleOtherLanguage() {
        I18n.setLocale("fr");
        assertEquals(I18n.PORTUGUESE, I18n.getLocale());

        I18n.setLocale("es");
        assertEquals(I18n.PORTUGUESE, I18n.getLocale());

        I18n.setLocale("EN_US");
        assertEquals(I18n.ENGLISH, I18n.getLocale());
    }

    @Test
    @DisplayName("Formatação plural em Português")
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
    @DisplayName("Formatação plural em Inglês")
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
    @DisplayName("Tradução das categorias de navios em Português e Inglês")
    void testShipCategoryTranslation() {
        I18n.setLocale(I18n.PORTUGUESE);
        assertEquals("Barca", I18n.getShipCategory("Barca"));
        assertEquals("Barca", I18n.getShipCategory("barge"));
        assertEquals("Caravela", I18n.getShipCategory("Caravela"));
        assertEquals("Caravela", I18n.getShipCategory("caravel"));
        assertEquals("Nau", I18n.getShipCategory("Nau"));
        assertEquals("Nau", I18n.getShipCategory("carrack"));
        assertEquals("Fragata", I18n.getShipCategory("Fragata"));
        assertEquals("Fragata", I18n.getShipCategory("frigate"));
        assertEquals("Galeao", I18n.getShipCategory("Galeao"));
        assertEquals("Galeao", I18n.getShipCategory("galeão"));
        assertEquals("Galeao", I18n.getShipCategory("galleon"));

        I18n.setLocale(I18n.ENGLISH);
        assertEquals("Barge", I18n.getShipCategory("Barca"));
        assertEquals("Barge", I18n.getShipCategory("barge"));
        assertEquals("Caravel", I18n.getShipCategory("Caravela"));
        assertEquals("Caravel", I18n.getShipCategory("caravel"));
        assertEquals("Carrack", I18n.getShipCategory("Nau"));
        assertEquals("Carrack", I18n.getShipCategory("carrack"));
        assertEquals("Frigate", I18n.getShipCategory("Fragata"));
        assertEquals("Frigate", I18n.getShipCategory("frigate"));
        assertEquals("Galleon", I18n.getShipCategory("Galeao"));
        assertEquals("Galleon", I18n.getShipCategory("galleon"));
    }

    @Test
    @DisplayName("getShipCategory com argumento nulo ou desconhecido")
    void testShipCategoryNullOrUnknown() {
        assertEquals("", I18n.getShipCategory(null));
        assertEquals("Submarino", I18n.getShipCategory("Submarino"));
        assertEquals("Cruzador", I18n.getShipCategory("Cruzador"));
    }

    @Test
    @DisplayName("get com chave inexistente retorna chave envolvida por pontos de exclamação")
    void testMissingKey() {
        assertEquals("!non.existent.key!", I18n.get("non.existent.key"));
    }

    @Test
    @DisplayName("get com argumentos nulos ou vazios")
    void testGetWithNullOrEmptyArgs() {
        assertEquals("Bons ventos!", I18n.get("menu.goodbye", (Object[]) null));
        assertEquals("Bons ventos!", I18n.get("menu.goodbye", new Object[0]));
    }

    @Test
    @DisplayName("Processamento de movimentos em Português e Inglês")
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
