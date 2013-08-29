package nl.bluering.ppracing;

import java.awt.Color;

public class Game {
	GraphRace ppr;
	Circuit circ;
	Paperview paper;
	Player[] player; // Array with all participating players
	int playercount; // Number of players
	int curplayer = 0; // The currentplayer
	int winner = 0; // The player who has won
	boolean finish = false, // A player has won
			wait = true, // Waiting for a routine to finish
			gamestarted = false; // True if the game is running
	final static int gridsize = 25; // The size of the grid

	public Game(GraphRace p) {
		ppr = p;
		circ = new Circuit(50, 40, 30, ppr);
	}

	// --------------------------------------------------------
	// ------ Various functions for newgames and players ------
	/**
	 * This method is called to start a new game
	 * 
	 * @param ai
	 *            Array with value 'true' for computer player and 'false' for
	 *            human
	 * @param name
	 *            Array with the names of the players
	 */
	public void newgame(int[] type, String[] name) {
		System.out.println("\n---NEW GAME---");
		gamestarted = true;
		paper = ppr.getpaper();

		circ.init(); // Initialise the circuit, in network-mode this is done
							// before
		int sx = circ.getstartx1(), // Get the start/finish location
		sy = circ.getstarty();

		playercount = type.length; // Get the number of players
		player = new Player[playercount];
		Color c;
		for (int i = 0; i < playercount; i++) {
			c = new Color(75 + (int) (Math.random() * 180),
					75 + (int) (Math.random() * 180),
					75 + (int) (Math.random() * 180));
			player[i] = new Player(name[i], type[i], ppr, startpos(i), sy, c);
		}

		paper.rebuffer();
		ppr.showcurrentplayer();
		ppr.turnlabel.setText("Turn: 0");
		finish = false;
		winner = 0;
		
		ppr.move.setEnabled(true);

		curplayer = playercount;
		nextplayer();
	}

	/**
	 * Shifts the turn to the next player
	 */
	public void nextplayer() {
		curplayer = ++curplayer >= playercount ? 0 : curplayer;
		if (curplayer == 0)
			win();
		ppr.main.start();
		if (!finished())
			ppr.setmessages();
	}

	
	// ---------------------------------------------------------------------
	// ------ Functions for checking if a player has finshed already -------
	/**
	 * This checks if there is a winner. If so, it stops the game and makes an
	 * anouncement
	 */
	public void win() {
		int w = checkfinished();
		if (w == -1)
			return;
		gamestarted = false;
		winner = w;
		finish = true;
		curplayer = winner;
		ppr.message(getplayer(winner).getname() + " WINS!!!");
		ppr.move.setEnabled(false);
		
	}

	/**
	 * Returns the player who has won or -1 The player who started last is
	 * always in advantage, for starting in outer row. If more players finish in
	 * the same turn, the last of them wins the game.
	 */
	int checkfinished() {
		if (player[0].getcar().getturns() < 10)
			return -1; // Don't do nothing during the first turns
		Car c;
		int vx, vy, x1, y1, ret = -1;
		double rc = 0;
		double x;
		int sy = circ.starty, sx1 = circ.startx1 - 1, sx2 = circ.startx2 - 1;
		for (int i = 0; i < playercount; i++) // Repeat for all players
		{
			c = player[i].getcar();
			vx = c.getLastStep().getDeltaX();
			vy = c.getLastStep().getDeltaY();
			x1 = c.getCurrentPos().getX() - vx;
			y1 = c.getCurrentPos().getY() - vy;
			if (vx == 0) // Car goes Vertical
			{
				if ((x1 + vx >= sx1) && (x1 + vx <= sx2)) // Within finish line
															// on x-axis
				{
					if (((y1 <= sy) && (y1 + vy >= sy))
							|| ((y1 >= sy) && (y1 + vy <= sy))) // Around or on
																// finish on
																// y-axis
					{
						ret = i;
					}
				}
			} else {
				rc = (double) vy / (double) vx;
				if (rc == 0) // Car goes Horizontal
				{
					if ((x1 + vx >= sx1) && (x1 + vx <= sx2)) // Ending on
																// finsh-line on
																// x-axis
					{
						if (y1 + vy == sy) // Ending on finsh-line on y-axis
						{
							ret = i;
						}
					}
				} else // Car goes Diagonal
				{
					x = (double) (sy - y1) / rc;
					x += x1;
					if (((x >= x1) && (x <= x1 + vx))
							|| ((x <= x1) && (x >= x1 + vx))) // Within the
																// vector, not
																// just 'in
																// line'
					{
						if ((x >= sx1) && (x <= sx2)) // Within the start/finish
														// line
						{
							ret = i;
						}
					}
				}
			}
		}
		return ret;
	}

	/**
	 * @return True if the game has ended
	 */
	public boolean finished() {
		return finish;
	}

	// -------------------------------------------
	// ------ Some various util-functions --------
	/**
	 * @return The size of the grid
	 */
	public int getgridsize() {
		return gridsize;
	}

	/**
	 * @return The current circuit
	 */
	public Circuit getcirc() {
		return circ;
	}

	/**
	 * @return the number of players
	 */
	public int getplayercount() {
		return playercount;
	}

	/**
	 * @return The requested player
	 * @param i
	 *            Index of the player requested
	 */
	public Player getplayer(int i) {
		if ((i < playercount) && (i >= 0))
			return player[i];
		// System.out.println("Special Player requested:"+i);
		return currentplayer();
	}

	/**
	 * @return The current player
	 */
	public Player currentplayer() {// System.out.println("Current player requested:"+curplayer+", "+player[curplayer]);
		return player[curplayer];
	}

	/**
	 * this method undoes a move, if possible.
	 */
	public void undo() {
		if (--curplayer < 0)
			curplayer = playercount - 1;
//		currentplayer().getcar().undo();
		ppr.showcurrentplayer();
		ppr.main.start();
	}

	public int startpos(int i) {
		int x1 = circ.getstartx1(), // Get the start/finish location
		x2 = circ.getstartx2();
		return (i % (x2 - x1)) + x1;
	}
}
