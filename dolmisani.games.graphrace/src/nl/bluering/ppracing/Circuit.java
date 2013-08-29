package nl.bluering.ppracing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.image.BufferedImage;

import dolmisani.games.graphrace.Position;

/**
 * This class constructs a new circuit, and makes a representation of it in a
 * BufferedImage
 */
public class Circuit {

	private static final Stroke GRID_STROKE = new BasicStroke(1.0f,
			BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] {
					5.0f, 2.0f }, 0.0f);
	
	private static final Stroke TRACK_STROKE = new BasicStroke(2.5f,
			BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);


	int[][] circ; // Storage array for circuit-points
	int sizex, sizey; // Horizontal and vertical size of the circuit

	public int starty, startx1, startx2; // Location of the start/finish line

	int checkpoints; // Number of checkpoints; this is used to render the
						// circuit
	Graphics2D gr; // Used to edit the image
	BufferedImage image;// Graphical representation of the circuit
	GraphRace ppr; // Parental class
	Polygon curbout, curbin; // Inner field and outter field
	public boolean correct = false;

	int[] chkx, chky; // Stores the checkpoints

	int hsize, vsize, gridsize; // Actual size of the circuit on the screen and
								// the size of the grid
	final int MG = 5; // Margin around the circuit

	/**
	 * Constructs a new circuit
	 * 
	 * @param sx
	 *            Horizontal size of the circuit
	 * @param sy
	 *            Vertical size of the circuit
	 * @param chk
	 *            Number of checkpoints, or corners in the circuit
	 * @param p
	 *            Parental game object
	 */
	public Circuit(int sx, int sy, int chk, GraphRace p) {
		sizex = sx + 2 * MG;
		sizey = sy + 2 * MG;
		checkpoints = chk;
		ppr = p;
	}

	/**
	 * Initialises a new circuit
	 */
	public void init() {
		circ = new int[sizex][sizey];
		chkx = new int[checkpoints + MG];
		chky = new int[checkpoints + MG];

		trace(); // Find some random checkpoints, and render a circuit

		setstart(); // Find start/finishline

		gridsize = ppr.getgame().getgridsize();
		dographics();
		correct = true;
	}

	/**
	 * Draws the circuit
	 */
	public void dographics() {
		hsize = sizex * gridsize;
		vsize = sizey * gridsize;

		image = new BufferedImage(hsize, vsize, BufferedImage.TYPE_INT_RGB);
		gr = image.createGraphics();

		gr.setColor(Color.WHITE);
		gr.fillRect(0, 0, hsize, vsize);

		teken_grid(); // Draw the circuit-points
		curbout = new Polygon();
		curbin = new Polygon();
		omtrek(); // Draw the curbstones

		// fill(curbin, Color.cyan);
		drawstart(); // Draw the start/finish line
	}

	/*
	 * @param x Desired x-coordinate
	 * 
	 * @param y Desired y-coordinate
	 * 
	 * @return -1 if the if coordinates are out of range
	 * 
	 * @return 0 if the coordinate contains grass
	 * 
	 * @return 1 or higher if the coordinate contains tarmac
	 */
	public int terrain(Position p) {

		if ((p.getX() < 0) || (p.getX() >= sizex) || (p.getY() < 0)
				|| (p.getY() >= sizey)) {
			return -1;
		}
		return circ[p.getX()][p.getY()];
	}

	// TODO: to be removed
	public int terrain(int x, int y) {
		return terrain(new Position(x, y));
	}

	/*
	 * @return the horizontal size of the circuit
	 */
	public int getsizex() {
		return sizex;
	}

	/*
	 * @return the vertical size of the circuit
	 */
	public int getsizey() {
		return sizey;
	}

	/*
	 * @return the vertical coordinate of the start/finish
	 */
	public int getstarty() {
		return starty;
	}

	/*
	 * @return the 1st horizontal coordinate of the start/finish
	 */
	public int getstartx1() {
		return startx1;
	}

	/*
	 * @return the 2nd horizontal coordinate of the start/finish
	 */
	public int getstartx2() {
		return startx2;
	}

	/*--------------------------------------------------------------*/
	/*-------------- These functions render the circuit ------------*/

	/**
	 * This function generates a random circuit, following the given number of
	 * checkpoints. These marks are stored in the circuit-matrix with value 1.
	 * The circuit always has a width of three, but because of overlapping it
	 * has sometimes more.
	 */
	void trace() {
		int i = 0, j, k, x, y, rx = (int) ((sizex - 2 * MG) / 2), // Radius X
		ry = (int) ((sizey - 2 * MG) / 2); // Radius Y
		double b, rc;

		for (b = 0; b <= 2 * Math.PI; b += (2 * Math.PI) / checkpoints) // Generate
																		// random
																		// marks.
		{
			chkx[i] = (int) ((Math.random() * (.5 * rx) + .5 * rx)
					* Math.cos(b) + rx);
			chky[i] = (int) ((Math.random() * (.5 * ry) + .5 * ry)
					* Math.sin(b) + ry);
			i++;
		}

		for (i = 0; i < 5; i++) // Save extra 5 marks, for completing the circle
		{
			chkx[i + checkpoints] = chkx[i];
			chky[i + checkpoints] = chky[i];
		}

		for (i = 0; i < checkpoints; i++) {
			k = i + 1;
			if (i == checkpoints - 1)
				k = 0; // Draw a line between the first and last mark.

			rc = ((float) (chky[k] - chky[i]) / (float) (chkx[k] - chkx[i]));

			if (rc >= 1 || rc < -1) // Vertical itereration
			{
				rc = 1 / rc;
				if (chky[i] < chky[k])
					for (j = 0; j <= (chky[k] - chky[i]); j++)
						circ[chkx[i] + (int) (rc * j) + MG][chky[i] + j + MG] = 1;

				if (chky[i] >= chky[k])
					for (j = 0; j >= (chky[k] - chky[i]); j--)
						circ[chkx[i] + (int) (rc * j) + MG][chky[i] + j + MG] = 1;
			} else if (chkx[i] == chkx[k]) // Vertical exception: rc==infinite.
			{
				if (chky[i] < chky[k])
					for (j = 0; j <= (chky[k] - chky[i]); j++)
						circ[chkx[i] + MG][chky[i] + j + MG] = 1;
				if (chky[i] >= chky[k])
					for (j = 0; j >= (chky[k] - chky[i]); j--)
						circ[chkx[i] + MG][chky[i] + j + MG] = 1;
			} else // Horizontal itereration.
			{
				if (chkx[k] > chkx[i])
					for (j = 0; j <= (chkx[k] - chkx[i]); j++)
						circ[chkx[i] + j + MG][chky[i] + (int) (rc * j) + MG] = 1;

				if (chkx[k] <= chkx[i])
					for (j = 0; j >= (chkx[k] - chkx[i]); j--)
						circ[chkx[i] + j + MG][chky[i] + (int) (rc * j) + MG] = 1;
			}
		}

		for (x = 1; x < sizex; x++)
			// Expand circuit to points around the route
			for (y = 1; y < sizey; y++)
				if (circ[x][y] == 1) {
					if (circ[x + 1][y] != 1)
						circ[x + 1][y] = 2;
					if (circ[x - 1][y] != 1)
						circ[x - 1][y] = 2;
					if (circ[x][y + 1] != 1)
						circ[x][y + 1] = 2;
					if (circ[x][y - 1] != 1)
						circ[x][y - 1] = 2;
					if (circ[x + 1][y + 1] != 1)
						circ[x + 1][y + 1] = 2;
					if (circ[x - 1][y + 1] != 1)
						circ[x - 1][y + 1] = 2;
					if (circ[x + 1][y - 1] != 1)
						circ[x + 1][y - 1] = 2;
					if (circ[x - 1][y - 1] != 1)
						circ[x - 1][y - 1] = 2;
				}
		for (x = 0; x < sizex; x++)
			// Store the circuit in the array
			for (y = 0; y < sizey; y++)
				if (circ[x][y] == 2)
					circ[x][y] = 1;

	}

	/**
	 * This function calculates the start/finish coordinates
	 */
	void setstart() {

		starty = getsizey() / 2;
		Position p = new Position(getsizex() / 2, starty);

		while (terrain(p) == 0) {
			p.moveTo(+1, 0);
		}

		startx1 = p.getX();

		while (terrain(p) != 0) {
			p.moveTo(+1, 0);
		}

		startx2 = p.getX() + 1;
	}

	/*--------------------------------------------------------------*/
	/*-----From here all functions apply to the graphical image.----*/

	/**
	 * Paint the buffered image onto the screen
	 */
	public void paint(Graphics g) {
		g.drawImage(image, 0, 0, ppr);
	}

	/**
	 * Draws the circuit-grid into the Graphics-object.
	 */
	void teken_grid() {
		int a, b, x, y;
		// Color c = new Color(70, 70, 70);

		Color c = Color.LIGHT_GRAY;

		for (x = 0; x < sizex; x++)
			// Draw vertical gridlines
			line(x, 0, x, sizey, c, GRID_STROKE);
		for (y = 0; y < sizey; y++)
			// Draw horizontal gridlines
			line(0, y, sizex, y, c, GRID_STROKE);
	}

	int x, y, vx, vy, x_oud, y_oud, vx_oud, vy_oud, curbcount;
	int[] curboutx, curbouty, curbinx, curbiny;

	/**
	 * Call this function to Draw the curb-stones into the Buffered image.
	 */
	void omtrek() {
		curbcount = 2 * sizex + 2 * sizey; // Maximum number of curbstones
		x = 0;
		y = 0;

		do // Search first white dot for the outer curb-stones
		{
			if (x > sizex) {
				x = 0;
				y++;
			}
			x++;
		} while (terrain(x, y) <= 0);
		x -= 1;
		vx = -1;
		vy = 0;
		do_omtrek(curbout, curbcount); // Calculate the outer curbstones

		x = (int) (MG + sizex) / 2;
		y = (int) (MG + sizey) / 2;
		do {
			x++; // Search first white dot for the inner curb-stones
		} while (terrain(x, y) <= 0);

		x -= 1;
		vx = -1;
		vy = 0;
		do_omtrek(curbin, curbcount); // Calculate the inner curbstones
	}

	/**
	 * Calculates the circuit-contour This function uses the functions links,
	 * rechts, terug and rechtdoor to find the borders of the circuit step by
	 * step.
	 * 
	 * @param curbx
	 *            Array with x-coordinates for the curb-polygon
	 * @param curby
	 *            Array with y-coordinates for the curb-polygon
	 * @param end
	 *            Number of iterations.
	 */
	void do_omtrek(Polygon pol, int end) {
		int z = 0, i = 0, x1, y1, n = 0;

		boolean curb = true;
		boolean baan = false;

		pol.addPoint(x * gridsize, y * gridsize);

		x_oud = x;
		y_oud = y;
		x1 = x_oud;
		y1 = y_oud;
		vx_oud = vx;
		vy_oud = vy;

		do {
			x1 = x;
			y1 = y;

			i = 0;
			baan = false;

			if (rechtdoor()) {
				while (links() && i < 4)
					i++;
				if (i >= 3)
					z = 9999;
			} else {
				if (!baan)
					baan = rechts();
				if (!baan)
					baan = rechts();
				if (!baan)
					baan = rechtdoor();
				if (!baan)
					baan = rechts();
				if (!baan)
					baan = rechtdoor();
				if (!baan)
					baan = rechts();
				if (!baan)
					baan = rechtdoor();
				terug();
				curb = !curb;
				line(x1, y1, x, y, Color.BLACK, TRACK_STROKE);
				n++;
				pol.addPoint(x * gridsize, y * gridsize);
				z++;
			}
		} while (z <= end);
	}

	void terug() {
		x = x_oud;
		y = y_oud;
		vx = vx_oud;
		vy = vy_oud;
	}

	boolean rechtdoor() {
		x_oud = x;
		y_oud = y;
		vx_oud = vx;
		vy_oud = vy;
		x += vx;
		y += vy;

		if (terrain(x, y) != 0) {
			x -= vx;
			y -= vy;
			return true;
		}
		return false;
	}

	boolean rechts() {
		x_oud = x;
		y_oud = y;
		vx_oud = vx;
		vy_oud = vy;
		if (vy == 0) {
			if (vx == 1) {
				vy = 1;
				vx = 0;
			} else if (vx == -1) {
				vy = -1;
				vx = 0;
			}
		} else {
			if (vy == 1) {
				vy = 0;
				vx = -1;
			} else if (vy == -1) {
				vy = 0;
				vx = 1;
			}
		}
		x += vx;
		y += vy;

		if (terrain(x, y) != 0) {
			x -= vx;
			y -= vy;
			return true;
		}
		return false;
	}

	boolean links() {
		x_oud = x;
		y_oud = y;
		vx_oud = vx;
		vy_oud = vy;

		if (vy == 0) {
			if (vx == 1) {
				vy = -1;
				vx = 0;
			} else if (vx == -1) {
				vy = 1;
				vx = 0;
			}
		} else {
			if (vy == 1) {
				vy = 0;
				vx = 1;
			} else if (vy == -1) {
				vy = 0;
				vx = -1;
			}
		}
		x += vx;
		y += vy;

		if (terrain(x, y) != 0) {
			x -= vx;
			y -= vy;
			return true;
		}
		x -= vx;
		y -= vy;
		return false;
	}

	/**
	 * Draws a line between two points
	 */
	void line(int x1, int y1, int x2, int y2, Color c, Stroke stroke) {
		
		gr.setColor(c);
		gr.setStroke(stroke);

		if ((x1 < 0) || (y1 < 0) || (x2 < 0) || (y2 < 0))
			return;
		
		gr.drawLine(x1 * gridsize, y1 * gridsize, x2 * gridsize, y2 * gridsize);
	}

	/**
	 * Little function for drawing the start-finish line
	 */
	public void drawstart() {
		gr.setColor(Color.white);
		for (int i = 0; i < startx2 - startx1; i += 2)
			gr.drawLine((startx1 + i - 1) * gridsize, starty * gridsize,
					(startx1 + i) * gridsize, starty * gridsize);
		for (int i = 1; i < startx2 - startx1; i += 2)
			gr.drawLine((startx1 + i - 1) * gridsize, starty * gridsize + 1,
					(startx1 + i) * gridsize, starty * gridsize + 1);
		gr.setColor(Color.gray);
		gr.drawLine((startx1 - 1) * gridsize, starty * gridsize - 1,
				(startx2 - 1) * gridsize, starty * gridsize - 1);
		gr.drawLine((startx1 - 1) * gridsize, starty * gridsize + 2,
				(startx2 - 1) * gridsize, starty * gridsize + 2);
	}

	/**
	 * Draw a filled polygon
	 */
	void fill(Polygon pol, Color c) {
		gr.setColor(c);
		gr.fillPolygon(pol);
	}

	/**
	 * Draw a filled circle
	 */
	void fillcircle(int x, int y, Color c) {
		gr.setColor(c);
		int s = (int) gridsize;
		if ((x < 0) || (y < 0))
			return;
		gr.fillOval(x * gridsize - s, y * gridsize - s, 2 * s, 2 * s);
	}

	/*********************************************************
	 * For testing purposes only:
	 */
	public Circuit(GraphRace p) {
		ppr = p;
	}
}
