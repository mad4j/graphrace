package nl.bluering.ppracing;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.StringTokenizer;

/**
 * This class implements the procedures for receiving data from the server or a
 * client
 */
public class NetReceive implements Runnable {
	Socket soc; // The socket-implementation of the connection
	NetClient nc; // The client using this NetReceive
	NetSClient nsc; // The server using this NetReceive
	BufferedReader in; // Inputstream

	boolean quit = false, // True if the thread should be stopped
			client = true; // True if the owner is a client
	int ID = -1; // ID of the client, the socket is connected to

	final static String ACKCONN = "!!!ackconn!!!", // Server sends
													// connect-acknowledgement
			DISCONN = "!!!disconn!!!", // Client or server sends disconnect
			PLAYERS = "!!!players!!!", // Server sends playerlist
			MESSAGE = "!!!message!!!", // Server or client sends a message
			NEWGAME = "!!!newgame!!!", // Server starts the newgame
			CARMOVE = "!!!carmove!!!", // Client or server sends carmove
			ENDGAME = "!!!endgame!!!", // Server sends endgame
			CIRCUIT = "!!!circuit!!!", // Server sends the circuit to play
			SETNAME = "!!!setname!!!"; // Client sets its name

	/**
	 * Creates a new platform for the client to receive messages from the
	 * specified server
	 */
	public NetReceive(Socket s, int i, NetClient n) {
		soc = s;
		nc = n;
		ID = i;
		client = true;
	}

	/**
	 * Creates a new platform for the server to receive messages from the
	 * specified client
	 */
	public NetReceive(Socket s, int i, NetSClient n) {
		soc = s;
		nsc = n;
		ID = i;
		client = false;
	}

	/**
	 * Sets the ID of the client, only needed for feedback when debugging
	 */
	public void setid(int id) {
		if (client)
			ID = id;
	}

	/**
	 * Starts this class in a new thread
	 */
	public void start() {
		quit = false;
		new Thread(this).start();
	}

	/**
	 * Stops this Thread
	 */
	public void stop() {
		quit = true;
	}

	/**
	 * Is called when the thread is started This function keeps checking for new
	 * messages, until stopped
	 */
	public void run() {
		String st = "", buf = "";
		System.out.println(ID + " Connecting to socket: " + soc);

		try {
			in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
			while (!quit) {
				try {
					st = "";
					buf = "";
					while (!buf.equals("-1-1-1End-1-1-1")) {
						st += buf;
						buf = in.readLine();
					}
					if (!st.equals(""))
						decode(st);
				} catch (Exception e) {
					System.out.println(ID + " Incoming connection broken! ==> "
							+ soc);
					// e.printStackTrace();
					disconnect(ID);
					stop();
				}

				try {
					Thread.sleep(1000);
				} catch (Exception e) {
				}
			}

			try {
				in.close();
				System.out.println(ID + " Input stream closed");
			} catch (Exception e) {
				System.out.println(ID + " Could not close inputstream: " + e);
			}
		} catch (Exception e) {
			System.out.println(ID + " Failed connecting to inputstream:" + e);
		}

	}

	/**
	 * This functions decodes the specified message string, and calls the
	 * appropriate function
	 */
	public void decode(String s) {
		StringTokenizer st = new StringTokenizer(s, "\t");
		String header, data;
		int id;
		header = st.nextToken().trim();
		id = Integer.parseInt(st.nextToken().trim());
		data = st.nextToken("").trim();

		if (header.equals(ACKCONN))
			acknowledge(id);
		else if (header.equals(DISCONN))
			disconnect(id);
		else if (header.equals(PLAYERS))
			players(data);
		else if (header.equals(MESSAGE))
			message(id, data);
		else if (header.equals(NEWGAME))
			newgame();
		else if (header.equals(CARMOVE))
			carmove(id, data);
		else if (header.equals(ENDGAME))
			endgame();
		else if (header.equals(CIRCUIT))
			circuit(data);
		else if (header.equals(SETNAME))
			setname(id, data);
		else
			System.out.println(ID + " No valid network-message:" + s);
	}

	// ----------------------------------------------
	// ----- For each message-type a function -------

	/**
	 * The client receives it's ID from the server
	 */
	public void acknowledge(int id) {
		if (client)
			nc.inid(id);
	}

	/**
	 * Either a client disconnects from the server or the server disconnects
	 * this client
	 */
	public void disconnect(int id) {
		if (client)
			nc.indisconnect();
		else
			nsc.indisconnect(id);
	}

	/**
	 * Is called when a list of players is received, the list needs decoding.
	 */
	public void players(String s) {
		if (!client)
			return;
		String[] list = new String[50];
		StringTokenizer st = new StringTokenizer(s, "\t");
		boolean empty = false;
		int c = 0;
		while (!empty) {
			try {
				list[c++] = st.nextToken();
			} catch (Exception e) {
				empty = true;
			}
		}
		String[] player = new String[c - 1];
		for (int i = 0; i < c - 1; i++)
			player[i] = list[i];
		nc.inname(player);
	}

	/**
	 * Is called when a message from the server or a client is received
	 */
	public void message(int id, String s) {
		if (!client) {
			nsc.inmessage(id, s);
		} else {
			nc.inmessage(s);
		}
	}

	/**
	 * Is called when the server sends a newgame-request
	 */
	public void newgame() {
		nc.startgame();
	}

	/**
	 * Is called when a player has moved it's car
	 */
	public void carmove(int id, String s) {
		if (client)
			nc.inmove(s);
		else
			nsc.inmove(id, s);
	}

	/**
	 * Is called when the server ends the game
	 */
	public void endgame() {
		nc.endgame();
	}

	/**
	 * Is called when a client sends it's name to the server
	 */
	public void setname(int id, String s) {
		if (!client)
			nsc.inname(id, s);
	}

	/**
	 * Is called when a client receives the circuit from the server
	 */
	public void circuit(String s) {
		nc.incircuit(s);
	}
}
