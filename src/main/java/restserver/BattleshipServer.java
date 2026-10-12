package restserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Battleship REST opponent server.
 *
 * Start with:  java -jar battleship-server.jar
 * Then listen on http://localhost:8080
 */
@SpringBootApplication
public class BattleshipServer {
	public static void main(String[] args) {
		SpringApplication.run(BattleshipServer.class, args);
	}
}