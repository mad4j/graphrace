package dolmisani.games.graphrace;

public class Step {
	
	private int deltaX;
	private int deltaY;

	public Step(int deltaX, int deltaY) {

		this.deltaX = deltaX;
		this.deltaY = deltaY;
	}

	public double length() {
		return Math.sqrt(deltaX*deltaX + deltaY*deltaY);
	}


	public int getDeltaX() {
		
		return deltaX;
	}

	public int getDeltaY() {
		
		return deltaY;
	}

	public void setDeltaX(int deltaX) {
		
		this.deltaX = deltaX;
	}

	public void setDeltaY(int deltaY) {
		
		this.deltaY = deltaY;
	}

	@Override
	public String toString() {
		
		return String.format("[%d, %d]", deltaX, deltaY);
	}
}
