package nl.bluering.ppracing;

import java.awt.Color;

import dolmisani.games.graphrace.Position;
import dolmisani.games.graphrace.Step;

/**
 * This is the data class for the users car. It contains the locationhistory of
 * the car. It also features an undo-function. The car is moved, using vectors.
 */
public class Car {
	
	private Position currentPos;
	private Position startPos;
	
	private Color color;
	private int turn = 0;

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

		currentPos = new Position(sx, sy);
		startPos = new Position(sx, sy);
		
		hist[0] = new Step(0, 0);
		color = c;
		for (int i = 0; i < 500; i++)
			fault[i] = false;
	}

	/**
	 * Moves the car using the given vector
	 * 
	 * @param step
	 *            The new speed-vector
	 */
	public void move(Step step) {
		
		currentPos.moveTo(step);
		hist[++turn] = step;
	}

	/**
	 * Is called when the grass is hit, to keep track of erronous counted turns
	 */
	public void fault() {
		fault[turn] = true;
		faultcount++;
	}

	public Position getCurrentPos() {
		
		return currentPos;
	}

	public Position getStartPos() {
		return startPos;
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
	public Step getLastStep() {
		return hist[turn];
	}

	/**
	 * @return The current speed of the car
	 */
	public double getspeed() {
		return hist[turn].length();
	}

	
	public Color getColor() {
		return color;
	}

	@Override
	public String toString() {
		return String.format("Car[%s - %s]", currentPos, getLastStep());
	}

	/**
	 * This function undos the last players' move
	 */
	public void undo() {
		
		currentPos.backFrom(getLastStep());
		
		if (--turn < 0)
			turn = 0;
		else if (fault[turn + 1]) {
			undo();
			faultcount--;
			fault[turn + 1] = false;
		}
	}
}
