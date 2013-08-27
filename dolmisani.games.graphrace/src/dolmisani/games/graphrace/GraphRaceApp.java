package dolmisani.games.graphrace;

import javax.swing.JFrame;

import nl.bluering.ppracing.GraphRace;

public class GraphRaceApp {

	public static void main(String[] args) {

		GraphRace p = new GraphRace();

		JFrame f = new JFrame("GrapRace");
		f.setSize(720, 640);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getContentPane().add(p);
		f.setVisible(true);
	}

}
