package nl.bluering.ppracing;

import java.awt.*;
import java.util.*;
import java.io.*;
import java.awt.event.*;
import java.net.*;
import java.awt.image.*;

/*
* full contact info:
* Ernst van Rheenen
* homepage www.bluering.nl
* coauthor Sieuwert van Otterloo
* thanks to Maarten Jansen for helping with ideas and the very fist version in C
*/


/**
* Paper & Pencil Racing
* Designed by
* <a href="http://www.bluering.nl">bluering software development </a>.
* You can send questions about this source to
* <a href="mailto:ernst@bluering.nl">ernst@bluering.nl</a>
* this applet can be freely used for whatever you want as long as you first ask permission from
* the author.
* <h2>project overview</h2>
* This applet consists of 15 classes.We have put all these classes in one file
* for easy downloading. To get the right javadoc documentation, we had to make all classes
* and many methods public. You might want to put each class in a separate file with the
* filename equal to the class name. You can also make all classes except reversi non-public
* (just remove the word public) to avoid errors. The drawback is that you will not see
* those classes in the documentation.
* <p>The javadoc files contain much of the comment, but not all of it. Check the sourcecode
* for the last details. The list of all classes is:
* <dl>
* <dt>Ppracing</dt><dd>the toplevel applet class. It contains and handles buttons.</dd>
* <dt>Circuit </dt><dd>The class containing the circuit, and routines for generating a new one.</dd>
* <dt>Player </dt><dd>Contains the routines for handling the player as well as information about him or her.</dd>
* <dt>Car</dt><dd>Contains the basic information about the car, it's speed and it's history</dd>
* <dt>Vector</dt><dd>Basic data class for handling movement-vectors</dd>
* <dt>Mainview</dt><dd> Puts scroll bars around the board</dd>
* <dt>Paperview</dt><dd> the class that paints the board and the cars.</dd>
* <dt>newgamewindow</dt><dd>the window that allows you to set up a new game. It appears when
* you press new game</dd>
* <dt>newnetwindow</dt><dd>the window that allows you to set up a network game. It also shows
* a chat-interface.</dd>
* <dt>NetClient</dt><dd>The class that handles all events concerning a client</dd>
* <dt>NetSClient</dt><dd>The class that handles all events concerning the server</dd>
* <dt>NetSClient</dt><dd>The class that handles all events concerning the server</dd>
* <dt>NetSend</dt><dd>This class handles the outgoing communication</dd>
* <dt>NetReceive</dt><dd>This class handles the incoming communication</dd>
* <dt>NetServer</dt><dd>a simple class for detecting new clients.</dd>
* </dl>
* <p>
*/
public class Ppracing extends java.applet.Applet
  implements MouseListener, KeyListener, ActionListener
{
	Paperview paper;	// The viewport for the circuit
	Mainview main;		// Scrollbar-window for the paperview
	Game game;

	/**
	* Graphical items for displaying status-information and buttons
	*/
	Label playerlabel = new Label(),
		  turnlabel = new Label("Turn: 0"),
		  messagelabel=new Label("Do a move");

	Button newgame=new Button("new game"),
           undo   =new Button("undo move"),
		   move   =new Button("computer move");
	Panel bottompanel = new Panel();

	/**
	* The init method is called when the applet is loaded by the browser.
	* It contains mainly layout-concerning items
	*/
	public void init()
	{ System.out.println("Initialising..");
	  game=new Game(this);
	  paper = new Paperview(game.getcirc(), this);
	  main = new Mainview(this);
	  main.add(paper);
	  main.setSize(600,500);

	  setBackground(Color.black);   //you can set the background color of the applet in this line
	  messagelabel.setForeground(Color.white);
	  messagelabel.setAlignment(Label.CENTER);
	  turnlabel.setForeground(Color.white);
	  turnlabel.setAlignment(Label.CENTER);

	  Panel buttonpanel=new Panel();//panel to keep buttons together
	  buttonpanel.setLayout(new GridLayout(1,3)); //add three buttons beside each other
	  buttonpanel.add(newgame);
	  buttonpanel.add(move);
	  buttonpanel.add(undo);
	  Panel superpanel=new Panel();
	  superpanel.setLayout(new GridLayout(2,1));
	  setLayout(new BorderLayout());

	  bottompanel.setLayout(new GridLayout(1,3));
	  bottompanel.add(playerlabel);
	  bottompanel.add(messagelabel);
	  bottompanel.add(turnlabel);

	  superpanel.add(buttonpanel);
	  superpanel.add(bottompanel);
	  add("Center",main);
	  add("South",superpanel);

	  paper.addMouseListener(this);
	  paper.addKeyListener(this);
	  addKeyListener(this);
	  newgame.addActionListener(this);
	  undo.addActionListener(this);
	  move.addActionListener(this);

	  int [] startplayers={Player.COM,Player.HUM};
	  String[] names = {"Computer","Human"};
	  game.newgame(startplayers, names);
	}

	public Ppracing()
	{init();
	}


	/**
	* this method is quick & dirty trick for running this applet as
	* an application. It is not used when a browser runs this class
	* as an applet.
	*/
	public static void main(String[] ps)
	{ Frame f=new Frame("Paper & Pencil racing");
	  Ppracing p=new Ppracing();
	  f.setSize(720,640);
	  f.addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){System.exit(0);}});
	  f.add("Center",p);
	  //p.init();
	  p.setVisible(true);
	  f.show();
	}


//-----------------------------------
//------- Graphics and layout -------
 	/**
	* Updates the graphics
	*/
	public void paint(Graphics g)
	{main.paint(g);
	}

	/**
	* sets a given text in the message label.
	* @param s the message to display.
	*/
	public void message(String s)
	{messagelabel.setText(s);
	}

	/**
	* sets a given text in the player label.
	*/
	public void showcurrentplayer()
	{playerlabel.setForeground(game.currentplayer().getcar().getcolor());
	 playerlabel.setText("Player: "+game.currentplayer().getname()+", Moving: "+game.currentplayer().getcar().getvector());
	}

	public void setmessages()
	{showcurrentplayer();
	 turnlabel.setText("Turn: "+game.currentplayer().getcar().getplayerturns());
	 if(game.finished()) message(game.getplayer(game.winner).getname()+" WINS!!!");
	 else if (game.currentplayer().isai()) message("Click or press a key");
	 else if (game.currentplayer().isnet()) message("Waiting for networkplayer to move.");
	 else message("Do a move");
	}


//-------------------------------------------
//------ Some various util-functions --------
	/**
	* @return The current game
	*/
	public Game getgame()
	{return game;
	}

	/**
	* @return The current paperview
	*/
	public Paperview getpaper()
	{return paper;
	}


//---------------------------------------------------------
//------ Next all actionlistener methods are listed -------
	/**
	*Detects mouse clicks
	*/
	public void mouseClicked(MouseEvent evt){}
	public void mouseEntered(MouseEvent evt){}
	public void mouseExited(MouseEvent evt){}
	public void mousePressed(MouseEvent evt){}
	public void mouseReleased(MouseEvent evt)
	{double x =evt.getX(),
	        y =evt.getY();
	 if (game.wait||game.finished()) return;
	 game.wait =true;
	 if (game.currentplayer().type()!=Player.HUM)
	  game.currentplayer().ask();
	 else
	  game.currentplayer().clicked((int)Math.round(x/game.gridsize),
	                          	   (int)Math.round(y/game.gridsize));
	 repaint();
	 game.wait=false;
	}

	/**
	* Detects when a key is pressed
	*/
	public void keyPressed(KeyEvent kvt)
	{int k = kvt.getKeyCode();
	 boolean move = false;

	 Point p = main.getScrollPosition();
 	 switch (k)		// Next four items are for scrolling
	 {
	  case KeyEvent.VK_LEFT    : p.translate(-11,0); move=true; break;
	  case KeyEvent.VK_RIGHT   : p.translate(11,0);  move=true; break;
	  case KeyEvent.VK_UP      : p.translate(0,-11); move=true; break;
	  case KeyEvent.VK_DOWN    : p.translate(0,11);  move=true; break;
	 }
	 main.setScrollPosition(p);

	 if(game.finished()||game.wait) return; // Check before move is made
	 game.wait=true;

	 if((game.currentplayer().type()!=Player.HUM) && (!move))
	 {game.currentplayer().ask();
	  game.wait=false;
	  return;
	 }

	 switch (k)
	 {case KeyEvent.VK_NUMPAD4 : game.currentplayer().move(0); break;
	  case KeyEvent.VK_NUMPAD6 : game.currentplayer().move(1); break;
	  case KeyEvent.VK_NUMPAD8 : game.currentplayer().move(2); break;
	  case KeyEvent.VK_NUMPAD2 : game.currentplayer().move(3); break;
	  case KeyEvent.VK_NUMPAD5 : game.currentplayer().move(4); break;
	 }

	 repaint();
	 game.wait=false;
	}
	public void keyReleased(KeyEvent kvt){}
	public void keyTyped(KeyEvent kvt){}

	/**
	* Detects when a button is pressed
	*/
	public void actionPerformed(ActionEvent avt)
	{ if (avt.getSource()==newgame)
	   new newgamewindow(this);
	  else if ((avt.getSource()==undo)&&!game.wait)
	   game.undo();
	  else if ((avt.getSource()==move)&&!game.wait)
	   game.currentplayer().ask();
	  repaint();
	}
}
