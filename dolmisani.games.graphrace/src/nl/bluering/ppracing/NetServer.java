package nl.bluering.ppracing;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
* This class runs as a seperate thread, and deals with incoming new clients
*/
public class NetServer extends ServerSocket
implements Runnable
{
  Socket client;			// Socket to the incoming client
  NetSClient nsc;			// The owner of this server
  public final static int PORT=8190;	// Port on which this server runs

  boolean quit=false;		// True if this thread has to stop.

  /**
  * Constructs a new serversocket for communication with clientsockets
  */
  public NetServer(NetSClient n)
  throws IOException
  {super(PORT);
   nsc=n;
   System.out.println("New server created at port:"+PORT);
  }

  /**
  * Starts the server in a new thread
  */
  public void start()
  {quit=false;
   new Thread(this).start();
  }

  /**
  * Is called when the thread is started
  * This function waits for clients to connect, and then reports them to the ServerClient
  */
  public void run()
  {System.out.println("Server running!");
   quit=false;
   while (!quit)
   {System.out.println("Waiting for new clients to connect...");
    try
    {client=accept();
	 nsc.addplayer(client);
    }
    catch(IOException e)
	{if(quit) System.out.println("Server stopped");
	 else System.out.println("Client failed to connect, waiting for client to retry...");
	}

	try
	{Thread.sleep(1000);
	}catch(Exception e){}

   }
  }

  /**
  * Stops this the thread
  */
  public void stop()
  {quit = true;
   try
   {close();
   }catch(Exception e){}
  }
}