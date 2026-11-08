/**
 * 
 */
package battleship;

public class Main {

	private int Total_Tiros = 0;

	/**
	 * Main.
	 *
	 * @param args the args
	 */
	public static void main(String[] args) {
		System.out.println(I18n.get("menu.title"));

		Tasks.menu();
	}

	public static final int MAX_SHIPS = 10;

}
