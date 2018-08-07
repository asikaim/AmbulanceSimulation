package AmbulanceSimulation;

import java.util.ArrayList;
import java.util.List;

public class Incident extends Thread {
	/*	HELP NOTES:
	 * 	loop creates these randomly in range(0, MaxLocX) and (0, MaxLocY)
	 *	states(waiting, ambulance on site)
	 *
	 */	
	public enum State{
		WAITING, ON_SITE
	};
	
	//assign private etc.
	private static int locX;
	private static int locY;
	private State incidentState;
	private long start = 0;
	
	public static List<Long> waitTimeList = new ArrayList<Long>();

	
	public Incident(int x, int y) {
		this.locX = x;
		this.locY = y;
		this.incidentState = State.WAITING;
	}
	
	public void run() {
		while(this.incidentState == State.WAITING) {	// runs as long as ambulance isn't on site
				try {
					System.out.println("Patient " + Thread.currentThread().getId() + " called ambulance to " + this.locX + "." + this.locY);
					start = System.currentTimeMillis();
					Main.available.acquire();
					if(Ambulance.getFreeAmbulances() > 0) {
						Ambulance.setFreeAmbulances(Ambulance.getFreeAmbulances() - 1); // decrease number of free ambulances
						Main.incidents.release(); // notify ambulance that there is an incident
						Main.available.release(); // no need to lock available ambulances anymore
						try {
							Main.ambulance.acquire();
							Thread.sleep((Ambulance.calculateDistance() / Ambulance.speed) * 100 + 50); //sleeps a bit longer for syncing purposes
							waitTimeList.add(System.currentTimeMillis() - start);
							System.out.println("Patient " + Thread.currentThread().getId() + " is being transported");
							System.out.println("Wait time: " + (System.currentTimeMillis() - start));
							this.incidentState = State.ON_SITE;
						}catch(InterruptedException e) {
							e.printStackTrace();
						}
						
					}/*else {
						//no free ambulances
						System.out.println("No free ambulances. Incident " + locX + "." + locY + " added to queue");
						//add incident to queue
						//originally had notTransported = false; but it doesn't work like that
					}*/
				}catch(InterruptedException e) {
					Thread.currentThread().interrupt();
					return;
				}
		}
	}
	
	
	public static int getLocX() {
		return locX;
	}

	public void setLocX(int locX) {
		this.locX = locX;
	}

	public static int getLocY() {
		return locY;
	}
	

	public void setLocY(int locY) {
		this.locY = locY;
	}
	
}
