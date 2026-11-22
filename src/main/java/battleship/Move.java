package battleship;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.*;

/**
 * Shot
 *
 * @author Your Name
 * Date: 20/02/2026
 * Time: 19:39
 */
public class Move implements IMove {

	/**
	 * Contadores dos tiros de uma jogada (válidos, repetidos, na água e exteriores).
	 */
	private record ShotCounts(int valid, int repeated, int missed, int outside) {
	}

	// Chaves do JSON devolvido por processEnemyFire
	private static final String KEY_VALID_SHOTS = "validShots";
	private static final String KEY_OUTSIDE_SHOTS = "outsideShots";
	private static final String KEY_REPEATED_SHOTS = "repeatedShots";
	private static final String KEY_MISSED_SHOTS = "missedShots";
	private static final String KEY_SUNK_BOATS = "sunkBoats";
	private static final String KEY_HITS_ON_BOATS = "hitsOnBoats";
	private static final String KEY_TYPE = "type";
	private static final String KEY_COUNT = "count";
	private static final String KEY_HITS = "hits";
	private static final String KEY_CORDS = "cords";

	//-------------------------------------------------------------------
	private final int number;
	private final List<IPosition> shots;
	private final List<IGame.ShotResult> shotResults;

	//-------------------------------------------------------------------
	public Move(int moveNumber, List<IPosition> moveShots, List<IGame.ShotResult> moveResults) {
		this.number = moveNumber;
		this.shots = moveShots;
		this.shotResults = moveResults;
	}

	@Override
	public String toString() {
		return "Move{" +
				"number=" + number +
				", shots=" + shots.size() +
				", results=" + shotResults.size() +
				'}';
	}

	@Override
	public int getNumber() {
		return this.number;
	}

	@Override
	public List<IPosition> getShots() {
		return this.shots;
	}

	@Override
	public List<IGame.ShotResult> getShotResults() {
		return this.shotResults;
	}

	/**
	 * Processes the results of enemy fire on the game board, analyzing the outcomes of shots,
	 * such as valid shots, repeated shots, missed shots, hits on ships, and sunk ships. It can
	 * also display a detailed summary of the shot results if verbose mode is activated.
	 *
	 * @param verbose a boolean indicating whether a detailed summary should be printed to the console
	 *                for the processed enemy fire data.
	 * @return a JSON-formatted string that encapsulates the results, including counts of valid shots,
	 *         repeated shots, missed shots, shots outside the game board, and details of hits and
	 *         sunk ships.
	 */
	@Override
	public String processEnemyFire(boolean verbose) {

		int validShots = 0;
		int repeatedShots = 0;
		int missedShots = 0;

		Map<String, Integer> sunkBoatsCount = new HashMap<>(); // Rastrear quantos navios de cada tipo afundaram
		Map<String, Integer> hitsPerBoat = new HashMap<>();
		Map<String, List<String>> coordinatesPerBoat = new HashMap<>();

		// Processar cada resultado de tiro
		for (int i = 0; i < this.shotResults.size(); i++) {
			IGame.ShotResult result = this.shotResults.get(i);
			if (!result.valid()) {
				// Tiro inválido - apenas ignorar
				continue;
			}

			if (result.repeated())
				repeatedShots++; // tiro repetido
			else {
				// Tiro válido
				validShots++;
				IShip ship = result.ship();
				if (ship == null)
					missedShots++; // Tiro na água
				else {
					String boatName = ship.getCategory();

					if (ship.stillFloating()) {
						hitsPerBoat.put(
								boatName, hitsPerBoat.getOrDefault(boatName, 0) + 1
						);

						IPosition shot = this.shots.get(i);
						String coordinate = "" + shot.getClassicRow()
								+ shot.getClassicColumn();

						coordinatesPerBoat
								.computeIfAbsent(boatName, key -> new ArrayList<>())
								.add(coordinate);
					}

					if (result.sunk())
						sunkBoatsCount.put(
								boatName, sunkBoatsCount.getOrDefault(boatName, 0) + 1
						);
				}
			}
		}

		// Determinar número de tiros fora do tabuleiro
		int outsideShots = Game.NUMBER_SHOTS - validShots - repeatedShots;

		ShotCounts counts = new ShotCounts(validShots, repeatedShots, missedShots, outsideShots);

		if (verbose) {
			// Imprimir na consola se verbose for true
			String summary = buildRoundSummary(counts, sunkBoatsCount, hitsPerBoat);
			System.out.println(I18n.get("move.round_prefix", this.number, summary));
		}

		// Criar o mapa para o JSON
		Map<String, Object> response = buildJsonResponse(counts, hitsPerBoat, coordinatesPerBoat);

		// Serializar o JSON utilizando Jackson
		String jsonString;

		// Serializar os tiros gerados em JSON usando a biblioteca Jackson
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

		try {
			jsonString = objectMapper.writeValueAsString(response);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Erro ao serializar o JSON dos resultados da jogada", e);
		}

		System.out.println(jsonString);
		System.out.println();

		// Retornar o JSON
		return jsonString;
	}

	/**
	 * Builds the human-readable summary of a round (used in verbose mode).
	 */
	private static String buildRoundSummary(ShotCounts counts,
			Map<String, Integer> sunkBoatsCount, Map<String, Integer> hitsPerBoat) {
		// Construção da mensagem de saída
		StringBuilder output = new StringBuilder();

		if (counts.valid() == 0 && counts.repeated() > 0) {
			output.append(I18n.get("move.repeated", counts.repeated()));
		} else {
			if (counts.valid() > 0) {
				output.append(I18n.get("move.valid", counts.valid()));
			}

			// Atualizar lógica para contar múltiplos barcos afundados do mesmo tipo
			if (!sunkBoatsCount.isEmpty()) {
				for (Map.Entry<String, Integer> entry : sunkBoatsCount.entrySet()) {
					String boatName = entry.getKey();
					int count = entry.getValue();
					String localizedBoat = I18n.getShipCategory(boatName);
					output.append(I18n.get("move.sunk", count, localizedBoat)).append(" + ");
				}
			}

			if (!hitsPerBoat.isEmpty()) {
				for (Map.Entry<String, Integer> entry : hitsPerBoat.entrySet()) {
					String boatName = entry.getKey();
					int hits = entry.getValue();
					String localizedBoat = I18n.getShipCategory(boatName);
					output.append(I18n.get("move.hit", hits, localizedBoat)).append(" + ");

				}
			}

			if (counts.missed() > 0) {
				output.append(I18n.get("move.miss", counts.missed()));
			} else if (!sunkBoatsCount.isEmpty() || !hitsPerBoat.isEmpty()) {
				output.setLength(output.length() - 2); // Remover o "+" final
			}

			if (counts.repeated() > 0) {
				if (counts.valid() > 0) {
					output.append(", ");
				}
				output.append(I18n.get("move.repeated", counts.repeated()));
			}
		}

		// Adicionar contagem de tiros fora do tabuleiro
		if (counts.outside() > 0) {
			if (!output.isEmpty()) {
				output.append(", ");
			}
			output.append(I18n.get("move.outside", counts.outside()));
		}

		return output.toString();
	}

	/**
	 * Builds the map that is serialized as the JSON result of a round.
	 */
	private Map<String, Object> buildJsonResponse(ShotCounts counts,
			Map<String, Integer> hitsPerBoat, Map<String, List<String>> coordinatesPerBoat) {
		Map<String, Object> response = new HashMap<>();
		response.put(KEY_VALID_SHOTS, counts.valid());
		response.put(KEY_OUTSIDE_SHOTS, counts.outside());
		response.put(KEY_REPEATED_SHOTS, counts.repeated());
		response.put(KEY_MISSED_SHOTS, counts.missed());

		// Criar a lista de barcos afundados
		List<Map<String, Object>> sunkBoats = new ArrayList<>();
		for (IGame.ShotResult result : this.shotResults) {
			IShip ship = result.ship();
			if (result.valid() && !result.repeated()
					&& ship != null && result.sunk()) {

				Map<String, Object> boat = new HashMap<>();
				boat.put(KEY_TYPE, ship.getCategory());
				boat.put(KEY_COUNT, 1);
				boat.put(KEY_CORDS, ship.getPositions().stream()
						.map(pos -> "" + pos.getClassicRow()
								+ pos.getClassicColumn())
						.toList());

				sunkBoats.add(boat);
			}
		}
		response.put(KEY_SUNK_BOATS, sunkBoats);

		// Criar a lista de acertos em barcos que não foram afundados
		List<Map<String, Object>> boatHits = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : hitsPerBoat.entrySet()) {
			Map<String, Object> boat = new HashMap<>();
			boat.put(KEY_TYPE, entry.getKey());
			boat.put(KEY_HITS, entry.getValue());
			boat.put(KEY_CORDS, coordinatesPerBoat.get(entry.getKey()));
			boatHits.add(boat);
		}
		response.put(KEY_HITS_ON_BOATS, boatHits);

		return response;
	}
}
