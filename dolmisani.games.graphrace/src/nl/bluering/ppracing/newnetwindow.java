package nl.bluering.ppracing;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.Point;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This class presents a window for starting a new game It consists of several
 * parts: - Selection of name - Selection of client / server - Connection-window
 * with chat-functionality
 */
public class newnetwindow extends Frame implements ActionListener {
	Ppracing ppr;
	Game game;

	boolean isclient = false;
	NetSClient nsc;
	NetClient nc;

	Button start = new Button("Start"), quit = new Button("Quit"),
			disconn = new Button("Disconnect"), send = new Button("Send"),
			server = new Button("Server"), client = new Button("Client"),
			connect = new Button("Connect"), ok = new Button("OK"),
			end = new Button("End game");

	TextArea chatbox = new TextArea("");
	TextField input = new TextField("");
	TextField ip = new TextField("");
	TextField name = new TextField("");
	List playerlist = new List(8, false);

	String nam = "", address = "";

	/**
	 * Constructs a new window for the networkgame
	 */
	public newnetwindow(Ppracing p) {
		super("Network game");
		ppr = p;
		game = ppr.getgame();
		game.gamestarted = false;

		ppr.newgame.setEnabled(false);
		ppr.move.setEnabled(false);
		ppr.undo.setEnabled(false);

		server.addActionListener(this);
		client.addActionListener(this);
		connect.addActionListener(this);
		start.addActionListener(this);
		quit.addActionListener(this);
		disconn.addActionListener(this);
		ok.addActionListener(this);
		input.addActionListener(this);
		ip.addActionListener(this);
		send.addActionListener(this);
		end.addActionListener(this);

		init();
		Point point = ppr.getLocationOnScreen(); // Try to center the window to
													// the mainscreen
		point.translate((int) (ppr.getSize().getWidth() / 2 - 125), (int) (ppr
				.getSize().getHeight() / 2 - 50));
		setLocation(point);
		pack();
		show();
		game.wait = true;
	}

	/**
	 * Initializes first window, with choice for server or client
	 */
	public void init() {
		removeAll(); // Clear the screen, and fill it again.
		setLayout(new BorderLayout());
		Panel top = new Panel(), bot = new Panel();
		top.setLayout(new GridLayout(1, 2));
		top.add(new Label("Name:"));
		top.add(name);
		name.requestFocus();
		bot.setLayout(new GridLayout(1, 3));
		bot.add(server);
		bot.add(client);
		bot.add(quit);
		add("North", top);
		add("South", bot);
		setSize(250, 150);
		pack();
	}

	/**
	 * Shows a dialog for entering the server-IP
	 */
	public void chooseserver() {
		removeAll(); // Clear the screen, and fill it again.
		setLayout(new GridLayout(2, 2));
		add(new Label("Server-IP or Hostname:"));
		add(ip);
		ip.requestFocus();
		add(connect);
		add(quit);
		setSize(250, 150);
		pack();
	}

	/**
	 * Shows the window with functionality for the server
	 */
	public void showserver() {
		Panel left = new Panel(new BorderLayout()), right = new Panel(
				new BorderLayout()), buttons = new Panel(new FlowLayout()), chatinput = new Panel(
				new BorderLayout());
		removeAll();
		String ip = "";
		try {
			ip += java.net.InetAddress.getLocalHost();
		} catch (Exception e) {
		}
		left.add("North", new Label("Server: " + ip));
		updatelist(nsc.name);
		left.add("Center", playerlist);
		buttons.add(disconn);

		if (game.gamestarted)
			buttons.add(end);
		else
			buttons.add(start);
		buttons.add(quit);
		left.add("South", buttons);

		right.add("Center", chatbox);
		chatinput.add("Center", input);
		chatinput.add("East", send);
		right.add("South", chatinput);

		setLayout(new GridLayout(1, 2));
		setSize(350, 350);
		add(left);
		add(right);
		show();
	}

	/**
	 * Shows the window with functionality for the client
	 */
	public void showclient() {
		Panel left = new Panel(new BorderLayout()), right = new Panel(
				new BorderLayout()), buttons = new Panel(new FlowLayout()), chatinput = new Panel(
				new BorderLayout());
		removeAll();

		left.add("North", new Label("Server: " + nc.client.getInetAddress()));
		left.add("Center", playerlist);
		buttons.add(disconn);
		left.add("South", buttons);

		right.add("Center", chatbox);
		chatinput.add("Center", input);
		chatinput.add("East", send);
		right.add("South", chatinput);

		setLayout(new GridLayout(1, 2));
		setSize(350, 350);
		add(left);
		add(right);
		show();
	}

	/**
	 * Displays an message and returns to the server/client selection
	 */
	public void message(String s) {
		removeAll();
		setLayout(new BorderLayout());
		TextArea tf = new TextArea(s);
		tf.setEditable(false);
		tf.setColumns(30);
		add("Center", tf);
		add("South", ok);
		pack();
	}

	/**
	 * Displays a message in the chatbox
	 */
	public void inmessage(String s) {
		chatbox.append(s + "\n");
	}

	/**
	 * Updates the playerlist, using the given String[]
	 */
	public void updatelist(String[] l) {
		playerlist.removeAll();
		int ID = isclient ? nc.ID : nsc.ID;
		for (int i = 0; i < l.length; i++)
			if (ID == i)
				playerlist.add("> " + l[i] + " <");
			else
				playerlist.add(l[i]);
	}

	/**
	 * this method is called any time a button is pressed or an option selected.
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == client) {
			nam = name.getText();
			if (nam.equals(""))
				return;
			isclient = true;
			chooseserver();
		} else if (e.getSource() == server) {
			nam = name.getText();
			if (nam.equals(""))
				return;
			isclient = false;
			try {
				nsc = new NetSClient(ppr, nam, this);
				showserver();
			} catch (Exception ex) {
				message("Error:\nThe server could not be started:\n" + ex);
				ex.printStackTrace();
			}
		} else if ((e.getSource() == connect) || (e.getSource() == ip)) {
			address = ip.getText();
			if (address.equals(""))
				return;
			try {
				nc = new NetClient(address, ppr, nam, this);
				showclient();
			} catch (Exception ex) {
				message("Error:\nThe client could not connect to the server:\n"
						+ ex);
				ex.printStackTrace();
			}
		} else if (e.getSource() == disconn) {
			if (isclient) {
				nc.stop();
				message("You disconnected from the game");
				return;
			}
			int index = playerlist.getSelectedIndex();
			nsc.indisconnect(index > 0 ? index : 0);
		} else if (e.getSource() == quit) {
			if (isclient && (nc != null))
				nc.stop();
			else if (nsc != null)
				nsc.stop();

			game.wait = true;
			ppr.newgame.setEnabled(true);
			ppr.move.setEnabled(false);
			ppr.undo.setEnabled(false);
			dispose();
		} else if (e.getSource() == start) {
			if (isclient)
				return;
			game.getcirc().init();
			game.newnetgame(nsc);
			showserver();
			setState(Frame.ICONIFIED);
		} else if ((e.getSource() == send) || (e.getSource() == input)) {
			String s = input.getText();
			if (isclient)
				nc.outmessage(s);
			else
				nsc.outmessage(s);
			chatbox.append(nam + ">  " + s + "\n");
			input.setText("");
		} else if (e.getSource() == ok) {
			ppr.newgame.setEnabled(true);
			dispose();
		} else if (e.getSource() == end) {
			nsc.endgame("[Server ended game]");
			showserver();
		}
	}
}
