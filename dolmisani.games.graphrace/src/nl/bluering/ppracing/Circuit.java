package nl.bluering.ppracing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
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
	
	private static final Stroke TRACK_STROKE = new BasicStroke(2.0f,
			BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f);


	private int[][] circ; // Storage array for circuit-points
	
	private int width;
	private int height;

	private int starty, startx1, startx2; // Location of the start/finish line

	private int checkpoints; // Number of checkpoints; this is used to render the
						// circuit
	
	private Graphics2D gr; // Used to edit the image
	private BufferedImage image;// Graphical representation of the circuit
	
	private GeneralPath curbout, curbin;

	private int[] chkx, chky; // Stores the checkpoints

	private int hsize, vsize, gridSize; // Actual size of the circuit on the screen and
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
	public Circuit(int sx, int sy, int chk, int gridSize) {
		
		width = sx + 2 * MG;
		height = sy + 2 * MG;
		checkpoints = chk;
		
		this.gridSize = gridSize;
	}

	/**
	 * Initialises a new circuit
	 */
	public void init() {
		circ = new int[width][height];
		chkx = new int[checkpoints + MG];
		chky = new int[checkpoints + MG];

		trace(); // Find some random checkpoints, and render a circuit

		setstart(); // Find start/finishline

		dographics(width, height, gridSize);
	}

	/**
	 * Draws the circuit
	 */
	public void dographics(int width, int height, int gridSize) {
		
		hsize = width * gridSize;
		vsize = height * gridSize;

		image = new BufferedImage(hsize, vsize, BufferedImage.TYPE_INT_RGB);
		gr = image.createGraphics();

		gr.setColor(Color.WHITE);
		gr.fillRect(0, 0, hsize, vsize);

		drawGrid(gr); // Draw the circuit-points
		
		curbout = new GeneralPath();
		curbin = new GeneralPath();
		
		omtrek(gr); // Draw the curbstones

		drawstart(gr); // Draw the start/finish line
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

		if ((p.getX() < 0) || (p.getX() >= width) || (p.getY() < 0)
				|| (p.getY() >= height)) {
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
		return width;
	}

	/*
	 * @return the vertical size of the circuit
	 */
	public int getsizey() {
		return height;
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

	public int getGridSize() {
		return gridSize;
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
		int i = 0, j, k, x, y, rx = (int) ((width - 2 * MG) / 2), // Radius X
		ry = (int) ((height - 2 * MG) / 2); // Radius Y
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

		for (x = 1; x < width; x++)
			// Expand circuit to points around the route
			for (y = 1; y < height; y++)
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
		for (x = 0; x < width; x++)
			// Store the circuit in the array
			for (y = 0; y < height; y++)
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
		g.drawImage(image, 0, 0, null);
	}

	/**
	 * Draws the circuit-grid into the Graphics-object.
	 */
	void drawGrid(Graphics2D gr) {

		for (int x = 0; x < width; x++) {
			line(gr, x, 0, x, height, Color.LIGHT_GRAY, GRID_STROKE);
		}
		
		for (int y = 0; y < height; y++) {
			line(gr, 0, y, width, y, Color.LIGHT_GRAY, GRID_STROKE);
		}
	}

	int x, y, vx, vy, x_oud, y_oud, vx_oud, vy_oud, curbcount;
	int[] curboutx, curbouty, curbinx, curbiny;

	/**
	 * Call this function to Draw the curb-stones into the Buffered image.
	 */
	void omtrek(Graphics2D gr) {
		curbcount = 2 * width + 2 * height; // Maximum number of curbstones
		x = 0;
		y = 0;

		do // Search first white dot for the outer curb-stones
		{
			if (x > width) {
				x = 0;
				y++;
			}
			x++;
		} while (terrain(x, y) <= 0);
		x -= 1;
		vx = -1;
		vy = 0;
		createTrackBorder(curbout, curbcount); // Calculate the outer curbstones
		
		x = (int) (MG + width) / 2;
		y = (int) (MG + height) / 2;
		do {
			x++; // Search first white dot for the inner curb-stones
		} while (terrain(x, y) <= 0);

		x -= 1;
		vx = -1;
		vy = 0;
		createTrackBorder(curbin, curbcount); // Calculate the inner curbstones
		
		gr.setColor(Color.BLACK);
		gr.setStroke(TRACK_STROKE);
		gr.draw(curbout);
		gr.draw(curbin);
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
	 * @param borderPoints
	 *            Number of iterations.
	 */
	
	void createTrackBorder(GeneralPath border, int borderPoints) {
		
		int z = 0;
		int i = 0;
		
		boolean flag = false;

		border.moveTo(x*gridSize, y*gridSize);

		x_oud = x;
		y_oud = y;
		vx_oud = vx;
		vy_oud = vy;

		do {

			i = 0;
			flag = false;

			if (rechtdoor()) {
				while (links() && i < 4)
					i++;
				if (i >= 3)
					z = 9999;
			} else {
				if (!flag)
					flag = rechts();
				if (!flag)
					flag = rechts();
				if (!flag)
					flag = rechtdoor();
				if (!flag)
					flag = rechts();
				if (!flag)
					flag = rechtdoor();
				if (!flag)
					flag = rechts();
				if (!flag)
					flag = rechtdoor();
				
				back();

				border.lineTo(x * gridSize, y * gridSize);

				z++;
			}
		} while (z <= borderPoints);
	}

	void back() {
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
	void line(Graphics2D g, int x1, int y1, int x2, int y2, Color c, Stroke stroke) {
		
		g.setColor(c);
		g.setStroke(stroke);

		if ((x1 < 0) || (y1 < 0) || (x2 < 0) || (y2 < 0))
			return;
		
		g.drawLine(x1 * gridSize, y1 * gridSize, x2 * gridSize, y2 * gridSize);
	}

	/**
	 * Little function for drawing the start-finish line
	 */
	public void drawstart(Graphics2D gr) {
		gr.setColor(Color.BLACK);
		for (int i = 0; i < startx2 - startx1; i += 2)
			gr.drawLine((startx1 + i - 1) * gridSize, starty * gridSize,
					(startx1 + i) * gridSize, starty * gridSize);
		for (int i = 1; i < startx2 - startx1; i += 2)
			gr.drawLine((startx1 + i - 1) * gridSize, starty * gridSize + 1,
					(startx1 + i) * gridSize, starty * gridSize + 1);
		gr.setColor(Color.gray);
		gr.drawLine((startx1 - 1) * gridSize, starty * gridSize - 1,
				(startx2 - 1) * gridSize, starty * gridSize - 1);
		gr.drawLine((startx1 - 1) * gridSize, starty * gridSize + 2,
				(startx2 - 1) * gridSize, starty * gridSize + 2);
	}

}
