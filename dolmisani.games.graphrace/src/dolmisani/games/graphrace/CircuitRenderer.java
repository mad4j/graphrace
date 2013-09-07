package dolmisani.games.graphrace;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;

import nl.bluering.ppracing.Circuit;

public class CircuitRenderer {

	private static final Color BACKGROUND_COLOR = Color.WHITE;
	private static final Color GRID_COLOR = Color.LIGHT_GRAY;

	private static final Stroke GRID_STROKE = new BasicStroke(1.0f,
			BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] {
					5.0f, 2.0f }, 0.0f);

	private static final Stroke TRACK_STROKE = new BasicStroke(2.0f,
			BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f);

	public static BufferedImage render(Circuit c) {

		int gridSize = c.getGridSize();
		int canvasWidth = c.getsizex() * gridSize;
		int canvasHeight = c.getsizey() * gridSize;

		BufferedImage image = new BufferedImage(canvasWidth, canvasHeight,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();

		g.setColor(BACKGROUND_COLOR);
		g.fillRect(0, 0, canvasWidth, canvasHeight);

		drawGrid(g, canvasWidth, canvasHeight, gridSize);

		// Draw the curbstones
		omtrek(g, canvasWidth, canvasHeight, gridSize);

		drawstart(g); // Draw the start/finish line

		return image;
	}

	private static void drawGrid(Graphics2D g, int canvasWidth,
			int canvasHeight, int gridSize) {

		g.setColor(GRID_COLOR);
		g.setStroke(GRID_STROKE);

		for (int x = 0; x < canvasHeight / gridSize; x++) {
			g.drawLine(x * gridSize, 0, x * gridSize, canvasHeight);
		}

		for (int y = 0; y < canvasHeight / gridSize; y++) {
			g.drawLine(0, y * gridSize, canvasWidth, y * gridSize);
		}
	}

	private static void omtrek(Graphics2D g, Circuit c, int canvasWidth,
			int canvasHeight, int gridSize) {

		GeneralPath curbout = new GeneralPath();
		GeneralPath curbin = new GeneralPath();

		int width = canvasWidth / gridSize;
		int height = canvasHeight / gridSize;

		// Maximum number of curbstones
		int curbcount = 2 * width + 2 * height;

		int x = 0;
		int y = 0;

		do // Search first white dot for the outer curb-stones
		{
			if (x > width) {
				x = 0;
				y++;
			}
			x++;
		} while (c.terrain(x, y) <= 0);
		x -= 1;
		vx = -1;
		vy = 0;
		createTrackBorder(curbout, curbcount); // Calculate the outer curbstones

		x = (int) (MG + width) / 2;
		y = (int) (MG + height) / 2;
		do {
			x++; // Search first white dot for the inner curb-stones
		} while (c.terrain(x, y) <= 0);

		x -= 1;
		vx = -1;
		vy = 0;
		createTrackBorder(curbin, curbcount); // Calculate the inner curbstones

		g.setColor(Color.BLACK);
		g.setStroke(TRACK_STROKE);
		g.draw(curbout);
		g.draw(curbin);
	}

	void createTrackBorder(GeneralPath border, int borderPoints) {

		int z = 0;
		int i = 0;

		boolean flag = false;

		border.moveTo(x * gridSize, y * gridSize);

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

}
