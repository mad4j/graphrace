package nl.bluering.ppracing;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * This class implements the procedures for sending data from the server or a
 * client
 */
public class NetSend implements Runnable {
	Socket soc; // The socketimplementation
	NetClient nc; // The client using this NetSend
	NetSClient nsc; // The server using this NetSend
	String type, message; // The message and type to be send
	boolean succeed = false, // True if the message has been send
			client = true, // True if the owner is a client
			newmsg = false, // True if a new message is awaiting
			quit = false; // True if the thread has to stop
	int ID; // The ID of the client to which the socket is connected

	PrintWriter out; // Outputstream

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
	 * Creates a new sendingplatform for a client
	 */
	public NetSend(Socket s, int i, NetClient n) {
		soc = s;
		nc = n;
		ID = i;
	}

	/**
	 * Creates a new sendingplatform for the server
	 */
	public NetSend(Socket s, int i, NetSClient n) {
		soc = s;
		nsc = n;
		ID = i;
	}

	/**
	 * Function to start this thread, must be called once
	 */
	public void start() {
		new Thread(this).start();
	}

	/**
	 * Tells this class to start sending the new message. It waits until the
	 * previous operation is finished.
	 */
	public synchronized void send(String t, String m) {
		while (newmsg)
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
			}
		newmsg = true;
		type = t;
		message = m;
	}

	/**
	 * Stops this thread
	 */
	public void stop() {
		quit = true;
	}

	/**
	 * Resets the client id to the new value
	 */
	public void setid(int id) {
		ID = id;
	}

	/**
	 * Is called when the thread is started This function attempts to send the
	 * message 30 times, then cancelles the action
	 */
	public void run() {
		int i;
		while (!quit) {
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
			}

			if (newmsg) {
				try {
					out = new PrintWriter(soc.getOutputStream(), true);
					out.println(type);
					out.println("\t");
					out.println(ID);
					out.println("\t");
					out.println(message);
					out.println("-1-1-1End-1-1-1"); // Standard footer of a
													// message
				} catch (IOException ioe) {
					System.out.println(ID + " Send failed: " + ioe);
				}
				newmsg = false; // Message has been send or cancelled, ready for
								// new message
			}
		}

		try {
			out.flush();
			out.close();
			System.out.println(ID + " Outputstream closed");
		} catch (Exception e) {
			System.out.println(ID + " Closing outputstream failed");
		}
	}

	// ----------------------------------------------
	// ----- For each message-type a function -------
	/**
	 * Sends acknowledgement with ID to the client
	 */
	public void acknowledge() {
		send(ACKCONN, "You're in game!");
	}

	/**
	 * Disconnects client from server
	 */
	public void disconnect() {
		if (client)
			send(DISCONN, "Client disconnects");
		else
			send(DISCONN, "Disconnected by server");
	}

	/**
	 * Sends playerlist to each client
	 */
	public void players(String[] s) {
		String list = "";
		list += s[0];
		for (int i = 1; i < s.length; i++)
			if (s[i] != null)
				list += "\t" + s[i];
		send(PLAYERS, list);
	}

	/**
	 * Sends the clients name to the server
	 */
	public void sendname(String s) {
		send(SETNAME, s);
	}

	/**
	 * Sends a message to the server or each player
	 */
	public void message(String s) {
		send(MESSAGE, s);
	}

	/**
	 * The server starts a new game
	 */
	public void newgame() {
		send(NEWGAME, "Game started by server");
	}

	/**
	 * Sends a move to the server or players
	 */
	public void carmove(String s) {
		send(CARMOVE, s);
	}

	/**
	 * The server ends the current game
	 */
	public void endgame(String s) {
		send(ENDGAME, s);
	}

	/**
	 * The client sends it's name to the server
	 */
	public void setname(String s) {
		send(SETNAME, s);
	}

	/**
	 * The server sends the circuit to the client
	 */
	public void circuit(String s) {
		send(CIRCUIT, s);
	}
}
