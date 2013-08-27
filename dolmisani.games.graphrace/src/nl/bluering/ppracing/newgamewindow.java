package nl.bluering.ppracing;

import java.awt.Button;
import java.awt.Choice;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Point;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
* This class presents a window for starting a new game
* It consists of two parts:
* - Number of players selection
* - Selection of players and names
*/
public class newgamewindow extends Frame
implements ActionListener
{
   Ppracing ppr;
   Game game;

   Choice cm= new Choice();
   Choice[] cm2;
   TextField[] tf;
   Button ok=new Button("ok"),
   		  start=new Button("start"),
		  cancel=new Button("cancel"),
		  normal = new Button("normal"),
		  network = new Button("network");
   Label desc = new Label("Number of players:");

   int count=0;

   /**
   * create a new newgamewindow. It shown immediately. You can use this
   * dialog only once.
   * @param p the applet that wants a new game.
   */
   public newgamewindow(Ppracing p){
    super("New game");
    ppr=p;
	game=ppr.getgame();
    game.wait=true;

	normal.addActionListener(this);
	network.addActionListener(this);
	ok.addActionListener(this);
	cancel.addActionListener(this);
	start.addActionListener(this);

	setLayout(new GridLayout(1,3));
	add(normal);
	add(network);
	add(cancel);

	Point point = ppr.getLocationOnScreen();	// Try to center the window to the mainscreen
	point.translate((int)(ppr.getSize().getWidth()/2-125),(int)(ppr.getSize().getHeight()/2-50));
    setLocation(point);
    pack();
	setResizable(false);
    pack();
	show();
   }


   /**
   * Create a dialog for a normal game, on one computer
   */
   public void normalgame()
   {for(int i=0; i<8; i++)
     cm.add(""+i);
    cm.select("2");

	removeAll();
    setLayout(new GridLayout(2,2));
    add(desc);
    add(cm);
    add(ok);
    add(cancel);
	pack();
   }

   /**
   * Create a new dialog for selecting the type of the players and their names
   */
   void newplayer()
   {int i;
	cm2 = new Choice[count];
	tf = new TextField[count];
    for (i=0; i<count; i++)
    {cm2[i] = new Choice();
     tf[i] = new TextField(10);
	 if (i==0)
	 {cm2[i].add("Human");
	  cm2[i].add("Computer");
	  tf[i].setText("Human");
	 }
	 else
     {cm2[i].add("Computer");
      cm2[i].add("Human");
	  tf[i].setText("Computer "+i);
     }
    }

	removeAll();			// Clear the screen, and fill it again.
	setLayout(new GridLayout(count+1,2));
	for (i=0; i<count; i++)
	{add(cm2[i]);
	 add(tf[i]);
	}
	add(start);
	add(cancel);
	setSize(250,300);
	pack();
   }

   /**
   * this method is called any time a button is pressed or an option selected.
   */
   public void actionPerformed(ActionEvent e)
   {if(e.getSource()==ok)
    {count=Integer.parseInt(cm.getSelectedItem());
	 newplayer();
    }
    else if(e.getSource()==start)
	{int[] player = new int[count];
     String[] name = new String[count];
     for(int i=0; i<count; i++)
	 {player[i]=cm2[i].getSelectedItem().equals("Computer")?Player.COM:Player.HUM;
      name[i]=tf[i].getText();
	 }
     game.newgame(player, name);	// Start a new game, with the given players
	 game.wait=false;
     dispose();
    }
    else if(e.getSource()==cancel)
	{game.wait=false;
     dispose();
    }
    else if(e.getSource()==normal)
	{normalgame();
    }
   
   }
}

