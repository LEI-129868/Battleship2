/**
 * 
 */
package battleship;

public class Main {
	/**
	 * Main.
	 *
	 * @param args the args
	 */
	public static void main(String[] args) {
		System.out.println(I18n.get("menu.title"));

		Tasks.menu();
	}

	public static void testeLongParameterList(int a, int b, int c, String d, String e, double f) {
        // Método com 6 parâmetros para provocar o erro no Qodana
}
