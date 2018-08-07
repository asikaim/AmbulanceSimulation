package AmbulanceSimulation;

public class Hospital {
	static int x;
	static int y;
	
	public Hospital(int locX, int locY) {
		x = locX;
		y = locY;
	}

	public static int getX() {
		return x;
	}

	public void setX(int x) {
		Hospital.x = x;
	}

	public static int getY() {
		return y;
	}

	public void setY(int y) {
		Hospital.y = y;
	}
	
}
