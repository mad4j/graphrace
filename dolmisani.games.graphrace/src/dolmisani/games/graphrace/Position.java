package dolmisani.games.graphrace;

public class Position {

	private int x;
	private int y;
	
	public Position(int x, int y) {
		
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
	
	public void moveTo(Step s) {
		this.x += s.getDeltaX();
		this.y += s.getDeltaY();
	}
	
	public void backFrom(Step s) {
		this.x -= s.getDeltaX();
		this.y -= s.getDeltaY();
	}
	
	@Override
	public String toString() {
		return String.format("(%d , %d)", x, y);
	}

}
