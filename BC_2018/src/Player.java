
/**
 * @author      MohammadReza DaneshvarAmoli <dos.moreda @ gmail.com>
 * @version     1.6.0                 (current version number of program)
 * @since       1.0.0          (the version of the package this class was first added to)
 */

import bc.*;

public class Player {
	public static void main(String[] args) {
		// Connect to the manager, starting the game
		GameController gc = new GameController();
		Controller c = new Controller(gc);

		while (true) {
			System.out.println("INIT Time: " + gc.getTimeLeftMs());
			// long millis = System.currentTimeMillis();
			c.run();
			if (c.g.round % 40 == 0)
				System.gc();
			// System.out.println("My RUN TIME: " + (System.currentTimeMillis() - millis));
			// millis = System.currentTimeMillis();
			gc.nextTurn();
			// System.out.println("gc.nextTurn() : " + (System.currentTimeMillis() -
			// millis));
		}
	}
}