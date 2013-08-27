package nl.bluering.ppracing;

import java.io.IOException;
import java.net.Socket;

/**
 * This class provides the functionality for playing a network game as server.
 * It takes care of the communication with the clients
 */
public class NetSClient {
	NetServer server; // The server that detects new clients
	NetReceive in[]; // Incoming connections
	NetSend out[]; // Outgoing connections
	Socket client[]; // List of sockets, one for each client
	public boolean enabled[]; // List of enabled clients

	String[] name; // List of names of all players

	Ppracing ppr;
	Game game;
	newnetwindow netwin;

	int ID = 0; // ID of this server. Always equal to 0

	final static int maxplayers = 8; // Maximum number of participants
	int playercount = 1; // Current number of participants

	/**
	 * Creates a new server-client and starts the server
	 */
	public NetSClient(Ppracing p, String n, newnetwindow nw) throws IOException {
		ppr = p;
		server = new NetServer(this);
		server.start();
		in = new NetReceive[maxplayers];
		out = new NetSend[maxplayers];
		client = new Socket[maxplayers];
		enabled = new boolean[maxplayers];
		name = new String[maxplayers];
		name[0] = n;
		netwin = nw;
		game = ppr.getgame();
		enabled[0] = true;
	}

	/**
	 * Constructor for testing
	 */
	NetSClient() throws IOException {
		server = new NetServer(this);
		server.start();
		in = new NetReceive[maxplayers];
		out = new NetSend[maxplayers];
		client = new Socket[maxplayers];
		enabled = new boolean[maxplayers];
		name = new String[maxplayers];
		name[0] = "Test Server";
		enabled[0] = true;
	}

	/**
	 * Adds a player and creates in/outcoming connections
	 */
	public synchronized void addplayer(Socket s) {
		if (game.gamestarted)
			return;
		System.out.println("Add network player:" + playercount);
		if (playercount > maxplayers) {
			new NetSend(s, playercount, this).disconnect();
			System.out
					.println("Maximum netplayers reached... disconnecting from player");
			return;
		}
		client[playercount] = s;
		in[playercount] = new NetReceive(s, playercount, this);
		in[playercount].start();
		out[playercount] = new NetSend(s, playercount, this);
		out[playercount].start();
		out[playercount].acknowledge();
		enabled[playercount] = true;
		playercount++;
		netwin.updatelist(name);
	}

	/**
	 * This method disables the player on the given ID.
	 */
	public void removeplayer(int id) {
		if (id == ID)
			return;
		enabled[id] = false;
		if (!game.gamestarted)
			doremove(); // Removing should be done only, when not in an active
						// game.
						// This is to prevent discrepancy in the game and the
						// network
	}

	/**
	 * This method removes all marked players.
	 */
	public void doremove() {
		String[] n = new String[maxplayers];
		Socket[] s = new Socket[maxplayers];
		NetSend[] ns = new NetSend[maxplayers];
		NetReceive[] nr = new NetReceive[maxplayers];

		int c = 0;
		for (int i = 0; i < maxplayers; i++)
			if (enabled[i]) {
				s[c] = client[i];
				n[c] = name[i];
				ns[c] = out[i];
				nr[c] = in[i];
				enabled[c] = true;
				c++;
			}
		playercount = c;
		name = n;
		client = s;
		netwin.updatelist(name); // Update the playerlist on this machine and
									// the clients'
		outname();
	}

	/**
	 * This function is called by NetReceive, to set the name of the connected
	 * player.
	 */
	public void inname(int i, String s) {
		name[i] = s;
		netwin.updatelist(name);
		outname();
	}

	/**
	 * Sends the player-list to all connected players
	 */
	public void outname() {
		for (int i = 1; i < playercount; i++)
			out[i].players(name);
	}

	/**
	 * @return the ID of the server
	 */
	public int getid() {
		return ID;
	}

	/**
	 * The server sends a message to the players The message goes to all other
	 * players
	 */
	public void outmessage(String s) {
		for (int i = 1; i < playercount; i++)
			if (enabled[i])
				out[i].message(name[0] + "> " + s);
	}

	/**
	 * The server gets a message from a player The message goes to all other
	 * players
	 */
	public void inmessage(int id, String s) {
		showmessage(s);
		for (int i = 1; i < playercount; i++)
			if ((i != id) && enabled[i])
				out[i].message(s);
	}

	/**
	 * The server does a move
	 */
	public void outmove(int m) {
		for (int i = 1; i < playercount; i++)
			if (enabled[i])
				out[i].carmove("" + m);
	}

	/**
	 * A player sends a move to the server
	 */
	public void inmove(int id, String s) {
		int m = Integer.parseInt(s);
		for (int i = 1; i < playercount; i++)
			if ((i != id) && enabled[i])
				out[i].carmove("" + m);
		game.currentplayer().move(m);
	}

	/**
	 * The server starts the game
	 */
	public void startgame() {
		showmessage("[Starting game]");
		for (int i = 1; i < playercount; i++)
			if (enabled[i])
				out[i].newgame();
		game.gamestarted = true;
	}

	/**
	 * The server ends the game
	 */
	public void endgame(String s) {
		for (int i = 1; i < playercount; i++)
			if (enabled[i])
				out[i].endgame(s);
		game.gamestarted = false;
		doremove();
		netwin.showserver();
	}

	/**
	 * A client disconnects from the server
	 */
	public void indisconnect(int id) {
		if (id == ID)
			return;
		out[id].stop();
		in[id].stop();
		removeplayer(id);
		if (game.gamestarted) {
			ppr.getgame().takeover(id);
			netwin.inmessage("[Player " + name[id] + " left the game]");
			outmessage("[Player " + name[id] + " left the game]");
		}
	}

	/**
	 * The server disconnects a client
	 */
	public void outdisconnect(int id) {
		out[id].disconnect();
		out[id].stop();
		in[id].stop();
		try {
			client[id].close();
		} catch (Exception e) {
		}
	}

	/**
	 * The server sends the circuit to all players
	 */
	public void outcircuit() {
		String s = ppr.getgame().getcirc().download();
		for (int i = 1; i < playercount; i++)
			if (enabled[i])
				out[i].circuit(s);
	}

	/**
	 * Stops the server and terminates all connections
	 */
	public void stop() {
		System.out.println("Sending stop-signal to server");
		server.stop();
		for (int i = 1; i < playercount; i++) {
			out[i].endgame("[Server quit the game]");
			out[i].stop();
			in[i].stop();
			try {
				client[i].close();
			} catch (Exception e) {
			}
		}
	}

	// -------------------------------------------------
	// -------- Various util-functions -----------------
	/**
	 * @return The list of currently connected players
	 */
	public String[] getplayerlist() {
		String[] n = new String[playercount];
		for (int i = 0; i < playercount; i++)
			n[i] = name[i];
		return n;
	}

	/**
	 * @return The list of types of the connected players
	 */
	public int[] getplayertype() {
		int[] t = new int[playercount];
		for (int i = 0; i < playercount; i++)
			t[i] = Player.NET;
		t[ID] = Player.HUM;
		return t;
	}

	/**
	 * Shows a message on the chatbox in the newnetwin
	 */
	public void showmessage(String s) {
		netwin.inmessage(s);
	}
}
