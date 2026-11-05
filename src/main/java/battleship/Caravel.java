package battleship;

/**
 * The type Caravel represents a ship with a size of 2 units.
 * It is positioned on the game board based on its bearing and initial position.
 *
 * Author: britoeabreu
 * Date: 2023-10-10
 * Time: 15:30
 */
public class Caravel extends Ship {

	/**
	 * Instantiates a new Caravel.
	 *
	 * @param bearing The bearing of the ship (NORTH, SOUTH, EAST, or WEST).
	 * @param pos     The initial position of the ship on the game board.
	 */
	public Caravel(Compass bearing, IPosition pos) {
		super("Caravela", bearing, pos, 2);

		switch (bearing) {
			case NORTH:
				for (int r = 0; r < this.getSize(); r++)
					getPositions().add(new Position(pos.getRow() + r, pos.getColumn()));
				break;
			case SOUTH:
				for (int r = 0; r < this.getSize(); r++)
					getPositions().add(new Position(pos.getRow() + r, pos.getColumn()));
				break;
			case EAST:
				for (int c = 0; c < this.getSize(); c++)
					getPositions().add(new Position(pos.getRow(), pos.getColumn() + c));
				break;
			case WEST:
				for (int c = 0; c < this.getSize(); c++)
					getPositions().add(new Position(pos.getRow(), pos.getColumn() + c));
				break;
		}
	}


	public void testeLongMethod129836() {
		int resultado = 0;

		resultado += 1;
		resultado += 2;
		resultado += 3;
		resultado += 4;
		resultado += 5;
		resultado += 6;
		resultado += 7;
		resultado += 8;
		resultado += 9;
		resultado += 10;
		resultado += 11;
		resultado += 12;
		resultado += 13;
		resultado += 14;
		resultado += 15;
		resultado += 16;
		resultado += 17;
		resultado += 18;
		resultado += 19;
		resultado += 20;
		resultado += 21;
		resultado += 22;
		resultado += 23;
		resultado += 24;
		resultado += 25;
		resultado += 26;
		resultado += 27;
		resultado += 28;
		resultado += 29;
		resultado += 30;
		resultado += 31;
		resultado += 32;
		resultado += 33;
		resultado += 34;
		resultado += 35;

		System.out.println(resultado);
	}

}