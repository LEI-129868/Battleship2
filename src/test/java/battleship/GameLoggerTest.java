package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for GameLogger.
 * Author: Tomás Costa -129836;
 * Generated: 2026-11-16;
 * Cyclomatic complexity:
 * GameLogger(): 1 (implicit); logMove(int,String,String): 3
 * (turn > 1 and IOException catch). This yields three independent paths.
 * Note: the production code uses a fixed relative path, so tests preserve and
 * restore any pre-existing file in the working directory.
 */
@ResourceLock("historico_jogadas.txt")
class GameLoggerTest {
    private static final Path LOG = Path.of("historico_jogadas.txt");
    private GameLogger logger;
    private byte[] previousContent;
    private boolean previouslyPresent;

    @BeforeEach
    void setUp() throws IOException {
        logger = new GameLogger();
        previouslyPresent = Files.exists(LOG);
        if (previouslyPresent) {
            if (!Files.isRegularFile(LOG))
                throw new IOException("O caminho do log já existe e não é um ficheiro: " + LOG);
            previousContent = Files.readAllBytes(LOG);
            Files.delete(LOG);
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        if (Files.isDirectory(LOG)) Files.delete(LOG);
        else Files.deleteIfExists(LOG);
        if (previouslyPresent) Files.write(LOG, previousContent);
        logger = null;
    }

    @Test @DisplayName("Construtor: GameLogger pode ser instanciado")
    void gameLogger() {
        assertNotNull(logger, "Erro: GameLogger deveria poder ser instanciado");
    }

    @Test @DisplayName("logMove 1: primeira jogada cria ou substitui histórico")
    void logMove1() throws IOException {
        GameLogger.logMove(1, "A1", "água");
        String result = Files.readString(LOG, StandardCharsets.UTF_8);
        assertAll(
                () -> assertTrue(result.contains("--- Jogada 1 ---"), "Erro: deveria registar o turno 1"),
                () -> assertTrue(result.contains("Shots: A1"), "Erro: deveria registar os tiros"),
                () -> assertTrue(result.contains("Result: água"), "Erro: deveria registar o resultado")
        );
    }

    @Test @DisplayName("logMove 2: jogada posterior acrescenta sem apagar o histórico")
    void logMove2() throws IOException {
        GameLogger.logMove(1, "A1", "água");
        GameLogger.logMove(2, "B2", "acertou");
        String result = Files.readString(LOG, StandardCharsets.UTF_8);
        assertAll(
                () -> assertTrue(result.contains("--- Jogada 1 ---"), "Erro: primeiro turno deveria ser preservado"),
                () -> assertTrue(result.contains("--- Jogada 2 ---"), "Erro: segundo turno deveria ser acrescentado"),
                () -> assertTrue(result.indexOf("--- Jogada 1 ---") < result.indexOf("--- Jogada 2 ---"),
                        "Erro: a ordem das jogadas deveria ser mantida")
        );
    }

    @Test @DisplayName("logMove 3: uma falha de escrita é capturada sem lançar IOException")
    void logMove3() throws IOException {
        // Diretoria com o mesmo nome impede FileWriter de abrir um ficheiro ali.
        Files.createDirectory(LOG);
        assertAll(
                () -> assertDoesNotThrow(() -> GameLogger.logMove(1, "A1", "água"),
                        "Erro: IOException deveria ser tratada internamente"),
                () -> assertTrue(Files.isDirectory(LOG),
                        "Erro: a diretoria que força o IOException deveria manter-se")
        );
    }
}
