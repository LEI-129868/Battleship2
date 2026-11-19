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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários da classe Main.
 * Aluno: Ricardo Paiva (LEI-129822)
 * Referência: Issue #57
 */
public class MainTest {

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
    @DisplayName("Instanciação do construtor da classe Main")
    void testConstructor() {
        Main main = new Main();
        assertNotNull(main);
    }

    @Test
    @DisplayName("main executa com sucesso imprimindo o título do menu e saindo com desisto")
    void testMainMethod() {
        String input = "desisto\n";
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));

        Main.main(new String[0]);

        String output = outContent.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains(I18n.get("menu.title")));
        assertTrue(output.contains(I18n.get("menu.goodbye")));
    }
}
