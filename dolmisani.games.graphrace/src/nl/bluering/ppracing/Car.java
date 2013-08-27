package nl.bluering.ppracing;

import java.awt.Color;

import dolmisani.games.graphrace.Step;

/**
 * This is the data class for the users car. It contains the locationhistory of
 * the car. It also features an undo-function. The car is moved, using vectors.
 */
public class Car {
	int x, y, // The current position of the car
			startx, starty; // The start position of the car
	Color color; // The color of the car
	int turn = 0; // The number of moves

	/**
	 * When a car hits the grass, the speed is reduced to zero. Therefore, after
	 * the players' move, a second move is made, reducing the speed to zero.
	 * This is counted seperately, to keep track of the real number of turns.
	 */
	boolean[] fault = new boolean[500];
	int faultcount = 0;

	Step[] hist = new Step[500]; // The history, containing all move-vectors

	/**
	 * Builds a new car
	 * 
	 * @param sx
	 *            Horizontal start location
	 * @param sy
	 *            Vertical start location
	 * @param c
	 *            Color of the car
	 */
	public Car(int sx, int sy, Color c) {
		x = sx;
		y = sy;
		startx = sx;
		starty = sy;
		hist[0] = new Step(0, 0);
		color = c;
		for (int i = 0; i < 500; i++)
			fault[i] = false;
	}

	/**
	 * Moves the car using the given vector
	 * 
	 * @param vec
	 *            The new speed-vector
	 */
	public void move(Step vec) {
		x += vec.getDeltaX();
		y += vec.getDeltaY();
		hist[++turn] = vec;
	}

	/**
	 * Is called when the grass is hit, to keep track of erronous counted turns
	 */
	public void fault() {
		fault[turn] = true;
		faultcount++;
	}

	/**
	 * @return The horizontal position of the car
	 */
	public int getx() {
		return x;
	}

	/**
	 * @return The vertical position of the car
	 */
	public int gety() {
		return y;
	}

	/**
	 * @return The horizontal start-position of the car
	 */
	public int getstartx() {
		return startx;
	}

	/**
	 * @return The vertical start-position of the car
	 */
	public int getstarty() {
		return starty;
	}

	/**
	 * @return The number of turns done, including erronous
	 */
	public int getturns() {
		return turn;
	}

	/**
	 * @return The number of turns done by the player
	 */
	public int getplayerturns() {
		return turn - faultcount;
	}

	/**
	 * @return The movement vector on a specific point of time
	 * @param i
	 *            Index of the desired vector
	 */
	public Step gethistory(int i) {
		return hist[i];
	}

	/**
	 * @return The current movement-vector
	 */
	public Step getvector() {
		return hist[turn];
	}

	/**
	 * @return The current speed of the car
	 */
	public double getspeed() {
		return hist[turn].length();
	}

	/**
	 * @return The color of the car
	 */
	public Color getcolor() {
		return color;
	}

	/**
	 * @return A string representation of the car, including position and
	 *         movement
	 */
	public String toString() {
		return ("Car(" + x + "," + y + "," + hist[turn] + ")");
	}

	/**
	 * This function undos the last players' move
	 */
	public void undo() {
		x -= hist[turn].getDeltaX();
		y -= hist[turn].getDeltaY();
		if (--turn < 0)
			turn = 0;
		else if (fault[turn + 1]) {
			undo();
			faultcount--;
			fault[turn + 1] = false;
		}
	}
}
