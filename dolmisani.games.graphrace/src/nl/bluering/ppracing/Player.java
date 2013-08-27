package nl.bluering.ppracing;

import java.awt.Color;

import dolmisani.games.graphrace.Step;

/**
* This class implements both the data concerning each player, as well as functions
* for calculating the route for the computer players.
*/
public class Player
{
	int type;				// Decide if this is a computer player
	String name;			// Name of the player
	Car car;				// The player's car
    Circuit circ;			// The Circuit
	GraphRace ppr;			// The parental applet
	Game game;

	/**
	* The directions to which a player can go
	*/
	public final static int LEFT=0, RIGHT=1, UP=2, DOWN=3, SAME=4;
	
	/**
	* The type of the player: Computer, Human or Network-player
	*/
	public final static int COM=0, HUM=1, NET=2;

	/**
	* Constructs a new player
	* @param nm The name of the player
	* @param aip Is the player a computer player?
	* @param p Parental applet
	* @param startx Horizontal start location
	* @param starty Vertical start location
	* @param col Color of the player
	*/
	public Player(String nm, int t, GraphRace p, int startx, int starty, Color col)
	{name = nm;
	 type = t;
	 ppr = p;
	 game=ppr.getgame();
	 circ = game.getcirc();
	 car = new Car(startx, starty, col);
	}


	/**
	* Is used to make the computer do a move
	*/
	public void ask()
	{if (type==COM)
	 {ppr.message("Thinking...");
	  game.wait=true;	
	  moveai();
	 }
	}

	/**
	* @return True if the player is a computer player
	*/
	public boolean isai()
	{return type==COM;
	}

	/**
	* @return True if the player is a network player
	*/
	public boolean isnet()
	{return type==NET;
	}
	
	/**
	* @return The type of this player: HUM, COM, or NET
	*/
	public int type()
	{return type;
	}
	
	public void settype(int i)
	{type = i;
	}

	/**
	* For user input from mouse
	* @param x Horizontal (circuit) location
	* @param x Vertical (circuit) location
	*/
	public synchronized void clicked(int x, int y)
	{int cx = car.getCurrentPos().getX()+car.getLastStep().getDeltaX(),
	     cy	= car.getCurrentPos().getY()+car.getLastStep().getDeltaY();
	 int m=-1;
	 if((x==cx) && (y==cy)) m=SAME;
	 else if ((cx-x==-1)&& (cy-y==0)) m=RIGHT;
	 else if ((cx-x==1) && (cy-y==0)) m=LEFT;
	 else if ((cx-x==0) && (cy-y==-1))m=DOWN;
	 else if ((cx-x==0) && (cy-y==1)) m=UP;
	 if(m>=0) move(m);
	}

	/**
	* For actually moving the car
	* @param m One of: LEFT, RIGHT, UP, DOWN, SAME
	*/
	public synchronized void move(int m)
	{Step v = car.getLastStep();
	 if(game.finished()) return;
	 switch (m)
	 {case LEFT:  movecar(v.getDeltaX()-1,v.getDeltaY()); break;
	  case RIGHT: movecar(v.getDeltaX()+1,v.getDeltaY()); break;
	  case UP:    movecar(v.getDeltaX()  ,v.getDeltaY()-1); break;
	  case DOWN:  movecar(v.getDeltaX()  ,v.getDeltaY()+1); break;
	  case SAME:  movecar(v.getDeltaX()  ,v.getDeltaY()); break;
	 }
	 
	 ppr.paper.rebuffer();
	 ppr.repaint();
	 
	 game.nextplayer();
	 game.wait=false;
	}

	/**
	*Internal function for moving the car to the new position.
	*/
	public synchronized void movecar(int x, int y)
	{car.move(new Step(x,y));
	}

	/**
	* Checks if the car has hit the grass. If so, the speed is reduced to (0,0)
	*/
	public synchronized void checkterrain()
	{if (circ.terrain(car.getCurrentPos())==0)
	 {car.move(new Step(0,0));					// Grass/gravel Speed reduced to zero
	  car.fault();
	 }
	}

	/**
	* @return The name of the player
	*/
	public String getname()
	{return name;
	}

	/**
	* @return The car of the player
	*/
	public Car getcar()
	{return car;
	}
	
	public String toString()
	{return getname();
	}

//--------------------------------------------------------
//------- Functions for the computer player --------------

	int cx,cy,level,i,j;
	double distance=0;	// The current distance of the players' car
	boolean grass;

	/**
	* This function is called by ask() to move the computer player
	*/
	public void moveai()
	{ double mv;	
	  if (!isai()) return;	// To make sure this method isn't used illegally
	  
	  cx = circ.getsizex()/2;	// The x-coordinate of the center of the circuit
	  cy = circ.getsizey()/2;	// The y-coordinate of the center of the circuit

	  level = 6 + (int)Math.round(car.getspeed()*1.4);	// Level is the depth of the search
	  grass = (circ.terrain(car.getCurrentPos()) <= 0);					// Is the car currently at the grass?

   	  Step v = car.getLastStep();
	  distance = Math.atan2(-(cy-car.getCurrentPos().getY()),(cx-car.getCurrentPos().getX()))+Math.PI;	// Update the current distance of the car
	  mv = ai(car.getCurrentPos().getX(),car.getCurrentPos().getY(),v.getDeltaX(),v.getDeltaY(),level);
 	  move((int) mv);
	}

	/**
	* This method does a depth-first search for the longest arc-distance that can be made
	* within the given number of moves (level). As soon as the required depth is reached,
	* the method returns the distance of the trip. For each level, the longest trip is chosen,
	* until the best direction is found. When all possibilities have been searched, the function
	* returns the best move to make.
	* To reduce the number of searches, the hit of grass will reduce the depthlevel by 2. 
	* If the car actually hits the grass, it will crash... :-) (Due to the algorithm) Probably the 
	* only way to solve this problem accurately, is by using a genetic alghorithm. Yet this routine 
	* is clean and simple :-)
	* @param x Horizontal start location
	* @param y Vertical start location
	* @param vx Current horizontal speed
	* @param vy Current vertical speed
	* @param l Level of search (or number of moves to maximize)
	*/
	public double ai(int x, int y, int vx, int vy, int l)
	{
	  double[] e = new double[5];

	  double d = Math.atan2(-(cy-y),(cx-x))+Math.PI;	// Calculate the distance
	  
	  // The arc-distance values range from 0 to 2 PI. To solve border-cases
	  // in the from 1.5 PI to 0.5 PI, checking is needed.
	  if((d-distance)>Math.PI)						
	   d -= 2*Math.PI;
	  if((d-distance)<-Math.PI)
	   d += 2*Math.PI;

	  if (l > 0)
	  {
		// Check borders
		if (circ.terrain(x,y) <= 0)
		{vx=0; vy=0;
	     if (!grass)	
	      l-=2;	// Penalty to get quicker results.
		}

		// If we're not done yet, check all possible routes.
		if (l > 0)
		{ e[0] = ai(x+vx - 1, y+vy,     vx - 1, vy    , l-1);
		  e[1] = ai(x+vx + 1, y+vy,     vx + 1, vy    , l-1);
		  e[2] = ai(x+vx,     y+vy - 1, vx,     vy - 1, l-1);
		  e[3] = ai(x+vx,     y+vy + 1, vx,     vy + 1, l-1);
		  e[4] = ai(x+vx,     y+vy,     vx,     vy    , l-1);

		  // Next get the largest distance
		  for (i=0; i<5; i++)
		  {if (e[i] > d)
		   { d = e[i];
		     j = i;
		   }
		  }
		}
	  }

	  if (l==level) return j;		// If this was the top-thread, return the best route
	  return d;						// Else return the distance
	}

// Old routine which used the actual distance instead of the arc-distance. This
// resulted in some unexpected behaviour:
//	public int ai(int x, int y, int vx, int vy, int l, int k)
//	{
//	  int d = 0, i, j = 0, dx,dy;
//
//	  int[] e = new int[5];
//	  if (l > 0)
//	  {
//		// Check borders
//		int t = circ.terrain(x,y);
//		if (t <= 0)
//		{ l-=2;
//	      vx=0; vy=0;
//		  d  = 0;
//		}
//
//		if (l > 0)
//		{ e[0] = ai(x+vx - 1, y+vy,     vx - 1, vy    , l-1,k);
//		  e[1] = ai(x+vx + 1, y+vy,     vx + 1, vy    , l-1,k);
//		  e[2] = ai(x+vx,     y+vy - 1, vx,     vy - 1, l-1,k);
//		  e[3] = ai(x+vx,     y+vy + 1, vx,     vy + 1, l-1,k);
//		  e[4] = ai(x+vx,     y+vy,     vx,     vy    , l-1,k);
//
//		  for (i=0; i<5; i++)
//		  if (e[i] > d)
//		  { d = e[i];
//		    j = i;		// j is de te kiezen route als l == m
//		  }
//		}
//	  }
//
//	  d += Math.sqrt(vx*vx + vy*vy);
//	  if (l == k) return j;
//	  return d;
//	}

}

