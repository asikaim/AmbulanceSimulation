package AmbulanceSimulation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class Ambulance implements Runnable {
	
	//states for ambulance
	private enum State{
		IDLE, TO_INCIDENT, TO_HOSPITAL, TRANSFERING
	};
	
	private State ambulanceState;
	
	public static int speed;
	
	private int id;
	private static int distance;
	private static int ambulances;
	private static int freeAmbulances;
	private long start = 0;
	private int incidentID;
	private String location;
	private BlockingQueue<Integer> queue;
	
	public static List<Long> elapsedTimeList = new ArrayList<Long>();

	
	public Ambulance(BlockingQueue<Integer> queue, int i, int spd, int count) {
		this.queue = queue;
		this.id = i;
		speed = spd;
		ambulances = count;
		freeAmbulances = ambulances;
		ambulanceState = State.IDLE;
	}
	
	@Override
	public void run() {
		while(Main.simulationFinished.get() == false && !Thread.currentThread().isInterrupted()) {
			try {
				switch(ambulanceState) {
					case IDLE:
						System.out.println("Ambulance " + this.getId() + " is idle");
						Main.incidents.acquire();	// looks for incidents, if none available, goes to sleep
						Main.available.release(); // at this time, has woken up
						freeAmbulances++;  // one ambulance gets free
						start = System.currentTimeMillis();
						distance = calculateDistance();
						location = Incident.getLocX() + "." + Incident.getLocY();
						incidentID = queue.take();
						ambulanceState = State.TO_INCIDENT;
						break;
					case TO_INCIDENT:
						System.out.println("Ambulance " + this.getId() + " heading to incident site " + location);
						Main.ambulance.release(); //ambulance is ready for transport
						Main.available.release();	//no need to lock availability anymore
						Thread.sleep(distance);	// added * 100 to sleep times so code won't run too fast
						System.out.println("Ambulance " + this.getId() + " at incident site " );
						ambulanceState = State.TO_HOSPITAL;
						break;
					case TO_HOSPITAL:
						System.out.println("Ambulance " + this.getId() + " coming back from an incident site " + location);
						Thread.sleep(distance); //heading back to hospital
						ambulanceState = State.TRANSFERING;
						break;
					case TRANSFERING:
						System.out.println("Ambulance " + this.getId() + " transfering patient " + incidentID + " to hospital");
						Thread.sleep(10000);
						System.out.println("Ambulance " + this.getId() + " has transferred the patient " + incidentID);
						elapsedTimeList.add(System.currentTimeMillis() - start);
						System.out.println("Elapsed time: " + (System.currentTimeMillis() - start));
						ambulanceState = State.IDLE;
						break;
					default:
						Thread.currentThread().interrupt();
				}	
			} catch (InterruptedException e) {
				System.out.println(Thread.currentThread().getId() + " interrupted.");
			}
		}
		Thread.currentThread().interrupt();
	}

	public static int getFreeAmbulances() {
		return freeAmbulances;
	}

	public static void setFreeAmbulances(int freeAmbulances) {
		Ambulance.freeAmbulances = freeAmbulances;
	}
	
	public static int calculateDistance() {
		double ddist = Math.hypot(Hospital.getX() - Incident.getLocX(), Hospital.getY() - Incident.getLocY()); 
		distance = ((int) Math.round(ddist) / speed) * 100;
		return distance;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
}
