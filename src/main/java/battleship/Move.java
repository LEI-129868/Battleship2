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

	//-------------------------------------------------------------------
	private final int number;
	private final List<IPosition> shots;
	private final List<IGame.ShotResult> shotResults;

	// Novo campo para guardar o tempo de reflexão (em milissegundos)
	private long durationMillis;

	//-------------------------------------------------------------------
	public Move(int moveNumber, List<IPosition> moveShots, List<IGame.ShotResult> moveResults) {
		this.number = moveNumber;
		this.shots = moveShots;
		this.shotResults = moveResults;

		//Inicializa o tempo de reflexão
		this.durationMillis = 0;
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
		Map<String, List<String>> cordsPerBoat = new HashMap<>();

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
				if (result.ship() == null)
					missedShots++; // Tiro na água
				else {
					String boatName = result.ship().getCategory();

					if (result.ship().stillFloating()) {
						hitsPerBoat.put(
								boatName, hitsPerBoat.getOrDefault(boatName, 0) + 1
						);

						IPosition shot = this.shots.get(i);
						String cord = "" + shot.getClassicRow()
								+ shot.getClassicColumn();

						cordsPerBoat
								.computeIfAbsent(boatName, key -> new ArrayList<>())
								.add(cord);
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

		if (verbose) {
			// Construção da mensagem de saída
			StringBuilder output = new StringBuilder();

			if (validShots == 0 && repeatedShots > 0) {
				output.append(I18n.get("move.repeated", repeatedShots));
			} else {
				if (validShots > 0) {
					output.append(I18n.get("move.valid", validShots));
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

				if (missedShots > 0) {
					output.append(I18n.get("move.miss", missedShots));
				} else if (!sunkBoatsCount.isEmpty() || !hitsPerBoat.isEmpty()) {
					output.setLength(output.length() - 2); // Remover o "+" final
				}

				if (repeatedShots > 0) {
					if (validShots > 0) {
						output.append(", ");
					}
					output.append(I18n.get("move.repeated", repeatedShots));
				}
			}

			// Adicionar contagem de tiros fora do tabuleiro
			if (outsideShots > 0) {
				if (!output.isEmpty()) {
					output.append(", ");
				}
				output.append(I18n.get("move.outside", outsideShots));
			}

			// Imprimir na consola se verbose for true
			System.out.println(I18n.get("move.round_prefix", this.number, output));
		}

		// Criar o mapa para o JSON
		Map<String, Object> response = new HashMap<>();
		response.put("validShots", validShots);
		response.put("outsideShots", outsideShots);
		response.put("repeatedShots", repeatedShots);
		response.put("missedShots", missedShots);

		// Criar a lista de barcos afundados
		List<Map<String, Object>> sunkBoats = new ArrayList<>();
		for (IGame.ShotResult result : this.shotResults) {
			if (result.valid() && !result.repeated()
					&& result.ship() != null && result.sunk()) {

				Map<String, Object> boat = new HashMap<>();
				boat.put("type", result.ship().getCategory());
				boat.put("count", 1);
				boat.put("cords", result.ship().getPositions().stream()
						.map(pos -> "" + pos.getClassicRow()
								+ pos.getClassicColumn())
						.toList());

				sunkBoats.add(boat);
			}
		}
		response.put("sunkBoats", sunkBoats);

		// Criar a lista de acertos em barcos que não foram afundados
		List<Map<String, Object>> boatHits = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : hitsPerBoat.entrySet()) {
			Map<String, Object> boat = new HashMap<>();
			boat.put("type", entry.getKey());
			boat.put("hits", entry.getValue());
			boat.put("cords", cordsPerBoat.get(entry.getKey()));
			boatHits.add(boat);
		}
		response.put("hitsOnBoats", boatHits);

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

	public long getDurationMillis() {
		return durationMillis;
	}

	public void setDurationMillis(long durationMillis) {
		this.durationMillis = durationMillis;
	}

	public double getDurationInSeconds() {
		return durationMillis / 1000.0;
	}
}
