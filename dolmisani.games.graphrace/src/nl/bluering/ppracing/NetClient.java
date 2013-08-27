package nl.bluering.ppracing;

import java.io.IOException;
import java.net.Socket;

/**
 * This class provides the functionality for playing a network game as client.
 * It takes care of the communication with the server
 */
public class NetClient {
	NetReceive in; // Incoming connection
	NetSend out; // Outgoing connection
	Socket client; // The socket-implementation for the connections
	int ID = -1; // ID of this client
	public final static int PORT = NetServer.PORT; // Connection-port on the
													// server

	Ppracing ppr;
	Game game;
	newnetwindow netwin;

	String[] name; // List of names of all players
	String clientname; // Name of this player

	/**
	 * Creates a new client and connects to the server on port 8192
	 */
	public NetClient(String server, Ppracing p, String n, newnetwindow nw)
			throws IOException {
		client = new Socket(server, PORT);
		ppr = p;
		in = new NetReceive(client, ID, this);
		in.start();
		clientname = n;
		netwin = nw;
		game = ppr.getgame();
	}

	/**
	 * Constructor for testing
	 */
	NetClient(String server) throws IOException {
		client = new Socket(server, PORT);
		in = new NetReceive(client, ID, this);
		in.start();
		clientname = "Test Client";
	}

	/**
	 * Gives this client an id number
	 */
	public void inid(int i) {
		ID = i;
		System.out.println("This client got ID:" + ID);
		in.setid(ID);
		out = new NetSend(client, ID, this);
		out.start();
		outname();
		showmessage("[Connected to server]");
	}

	/**
	 * @return the client ID
	 */
	public int getid() {
		return ID;
	}

	/**
	 * Sends this clients' name to the server
	 */
	public void outname() {
		out.sendname(clientname);
	}

	/**
	 * Is called when a list of players is recieved from the server
	 */
	public void inname(String[] n) {
		name = n;
		netwin.updatelist(name);
	}

	/**
	 * Sends a message to the server
	 */
	public void outmessage(String s) {
		out.message(clientname + "> " + s);
	}

	/**
	 * Is called when a message is received from the server
	 */
	public void inmessage(String s) {
		showmessage(s);
	}

	/**
	 * Sends this clients' move to the server
	 */
	public void outmove(int m) {
		out.carmove("" + m);
	}

	/**
	 * Receives the current player's move from the server
	 */
	public void inmove(String s) {
		game.currentplayer().move(Integer.parseInt(s));
	}

	/**
	 * Is called when the startgame-signal is received
	 */
	public void startgame() {
		showmessage("[Starting game...]");
		game.newnetgame(this);
		netwin.setState(java.awt.Frame.ICONIFIED);
	}

	/**
	 * Is called when the endgame-signal is received
	 */
	public void endgame() {
		showmessage("[Game ended by server]");
	}

	/**
	 * Is called when the server disconnects this client
	 */
	public void indisconnect() {
		showmessage("[Server has disconnected or quit]");
		in.stop();
		out.stop();
	}

	/**
	 * Disconnects this client from the server
	 */
	public void outdisconnect() {
		showmessage("[Disconnected]");
		out.disconnect();
	}

	/**
	 * Loads the circuit send by the server
	 */
	public void incircuit(String s) {
		ppr.getgame().getcirc().upload(s);
	}

	/**
	 * Stops all in/outcoming connections
	 */
	public void stop() {
		outdisconnect();
		in.stop();
		out.stop();
	}

	// -------------------------------------------------
	// -------- Various util-functions -----------------
	/**
	 * @return The list of currently connected players
	 */
	public String[] getplayerlist() {
		return name;
	}

	/**
	 * @return The list of types of the connected players
	 */
	public int[] getplayertype() {
		int l = name.length;
		int[] t = new int[l];
		for (int i = 0; i < l; i++)
			t[i] = Player.NET;
		t[ID] = Player.HUM;
		return t;
	}

	/**
	 * Shows the incoming message in the chatbox of the newnetwindow
	 */
	public void showmessage(String s) {
		netwin.inmessage(s);
	}
}
