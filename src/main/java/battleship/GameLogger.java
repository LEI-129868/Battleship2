package battleship;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class GameLogger {
    private static final String LOG_FILE = "historico_jogadas.txt";

    public static void logMove(int turn, String shotsData, String resultData) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println("--- Jogada " + turn + " ---");
            pw.println("Shots: " + shotsData);
            pw.println("Result: " + resultData);
            pw.println();
        } catch (IOException e) {
            System.err.println("Erro ao gravar histórico no ficheiro: " + e.getMessage());
        }
    }
}
