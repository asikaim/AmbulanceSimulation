package AmbulanceSimulation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class Incident implements Runnable{
	/*	HELP NOTES:
	 * 	loop creates these randomly in range(0, MaxLocX) and (0, MaxLocY)
	 *	states(waiting, ambulance on site)
	 *
	 */	
	public enum State{
		WAITING, ON_SITE
	};
	
	//assign private etc.
	private static int id;
	private static int locX;
	private static int locY;
	private State incidentState;
	private long start = 0;
	private BlockingQueue<Integer> queue;

	public static List<Long> waitTimeList = new ArrayList<Long>();

	public Incident(BlockingQueue<Integer> queue, int i, int x, int y) {
		this.queue = queue;
		this.id = i;
		this.locX = x;
		this.locY = y;
		incidentState = State.WAITING;
	}
	
	@Override
	public void run() {
		while(incidentState == State.WAITING && Main.simulationFinished.get() == false) {
			// runs as long as ambulance isn't on site and main loop is running	
			try {
				System.out.println("Patient " + this.getId() + " called ambulance to " + this.locX + "." + this.locY);
				start = System.currentTimeMillis();
				Main.available.acquire();
				if(Ambulance.getFreeAmbulances() > 0) {
					Ambulance.setFreeAmbulances(Ambulance.getFreeAmbulances() - 1); // decrease number of free ambulances
					Main.incidents.release(); // notify ambulance that there is an incident
					Main.available.release(); // no need to lock available ambulances anymore
					queue.put(this.getId());
					try {
						Main.ambulance.acquire();
						Thread.sleep((Ambulance.calculateDistance() / Ambulance.speed) * 100 + 50); //sleeps a bit longer for syncing purposes
						waitTimeList.add(System.currentTimeMillis() - start);
						System.out.println("Patient " + this.getId() + " is being transported");
						System.out.println("Wait time: " + (System.currentTimeMillis() - start));
						Main.available.release(); // no need to lock available ambulances anymore
						incidentState = State.ON_SITE;
					}catch(InterruptedException e) {
						System.out.println(this.getId() + " interrupted.");
					}		
				}else {
					System.out.println("No available ambulances. Adding " + this.getId() + " into queue.");
					queue.put(this.getId());
				}
			}catch(InterruptedException e) {
				System.out.println(this.getId() + " interrupted.");
			}
		}
		Thread.currentThread().interrupt();
	}
	
	public static int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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
