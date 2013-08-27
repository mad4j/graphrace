package nl.bluering.ppracing;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.ScrollPane;

/**
 * This class builds a scrolling pane around the Paperview. It features a Thread
 * for smooth scrolling, for moving to the next player.
 */
public class Mainview extends ScrollPane implements Runnable {

	GraphRace ppr;
	Paperview paper;
	Player player;
	Game game;

	int frame = 0;
	final int moves = 30;
	int[] mx = new int[2];
	int[] my = new int[2];
	Point point;

	Mainview(GraphRace pr) {
		ppr = pr;
		paper = ppr.paper;
		game = ppr.getgame();
	}

	public void paint(Graphics g) {
		paper.repaint();
		// System.out.println("repaint mainview");
	}

	public final synchronized void update(Graphics g) {
		paint(g);
	}

	/**
	 * is called if a new thread is created for the animation.
	 */
	public void start() {
		Point endpoint;
		frame = 0;
		player = game.currentplayer();
		game.wait = true;

		paper.cursor(false);
		ppr.message("wait...");
		/**
		 * Initialise begin and end points.
		 */
		endpoint = new Point(player.getcar().getCurrentPos().getX() * game.gridsize
				- (int) getSize().getWidth() / 2, player.getcar().getCurrentPos().getY()
				* game.gridsize - (int) getSize().getHeight() / 2);
		point = getScrollPosition();

		/**
		 * Initialise stepsize
		 */
		mx[0] = (int) ((endpoint.getX() - point.getX()) / moves);
		my[0] = (int) ((endpoint.getY() - point.getY()) / moves);
		mx[1] = (int) ((endpoint.getX() - point.getX()) - moves * mx[0]);
		my[1] = (int) ((endpoint.getY() - point.getY()) - moves * my[0]);

		new Thread(this).start();
	}

	/**
	 * is called if someone wants the thread to stop.
	 */
	public void stop() {
		frame = moves;
	}

	/**
	 * this method is the one that runs in its own thread to do the animation.
	 * it calls move() every 30 milliseconds.
	 */
	public void run() {
		try // Little delay before starting the move...
		{
			Thread.sleep(500);
		} catch (Exception e) {
		}

		paper.rebuffer();
		repaint();

		try // Start the move
		{
			while (frame < moves) {
				Thread.sleep(30);
				game.wait = true;
				move();
				frame++;
			}
		} catch (Exception e) {
		}
		paper.cursor(true);
		ppr.setmessages();
		paper.rebuffer();
		repaint();
		paper.requestFocus();
		game.wait = false;

	}

	/**
	 * This routine scrolls the screen towards the next player.
	 */
	public void move() {
		point.translate(mx[0], my[0]);
		if (frame == 0)
			point.translate(mx[1], my[1]);
		setScrollPosition(point);
	}
}
