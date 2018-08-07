package AmbulanceSimulation;

import java.util.ArrayList;
import java.util.List;

public class Ambulance extends Thread {
	
	//states for ambulance
	private enum State{
		IDLE, TO_INCIDENT, TO_HOSPITAL, TRANSFERING
	};
	
	private State ambulanceState;
	
	public static int speed = 100;
	
	private static int distance;
	private static int ambulances;
	private static int freeAmbulances;	
	private long start = 0;
	
	public static List<Long> elapsedTimeList = new ArrayList<Long>();

	
	public Ambulance(int spd, int count) {
		speed = spd;
		ambulances = count;
		freeAmbulances = ambulances;
		this.ambulanceState = State.IDLE;
	}

	public static int getFreeAmbulances() {
		return freeAmbulances;
	}

	public static void setFreeAmbulances(int freeAmbulances) {
		Ambulance.freeAmbulances = freeAmbulances;
	}

	
	public void run() {
		while(true) {
			try {
				switch(ambulanceState) {
					case IDLE:
						System.out.println("Ambulance " + Thread.currentThread().getId() + " is idle");
						Main.incidents.acquire();	// looks for incidents
						 							// if none available, goes to sleep
						Main.available.release(); // at this time, has woken up
						start = System.currentTimeMillis();
						freeAmbulances++;  // one ambulance gets free
						this.ambulanceState = State.TO_INCIDENT;
						break;
					case TO_INCIDENT:
						System.out.println("Ambulance " + Thread.currentThread().getId() + " heading to incident site");
						Main.ambulance.release(); //ambulance is ready for transport
						Main.available.release();	//no need to lock availability anymore
							// added * 100 to sleep times so code won't run too fast
						Thread.sleep((calculateDistance() / speed) * 100);
						System.out.println("Ambulance " + Thread.currentThread().getId() + " at incident site");
						this.ambulanceState = State.TO_HOSPITAL;
						break;
					case TO_HOSPITAL:
						System.out.println("Ambulance " + Thread.currentThread().getId() + " coming back from an incident site");
						Thread.sleep((calculateDistance() / speed) * 100); //heading back to hospital
						this.ambulanceState = State.TRANSFERING;
						break;
					case TRANSFERING:
						System.out.println("Ambulance " + Thread.currentThread().getId() + " transfering patient to hospital");
						Thread.sleep(10000);
						System.out.println("Patient transferred");
						elapsedTimeList.add(System.currentTimeMillis() - start);
						System.out.println("Elapsed time: " + (System.currentTimeMillis() - start));
						this.ambulanceState = State.IDLE;
						break;
				}	
			} catch (InterruptedException e) {
				System.out.println(Main.simulationFinished.get());
				Thread.currentThread().interrupt();
				return;
			}
		}
	}
	
	public static int calculateDistance() {
		double ddist = Math.hypot(Hospital.getX() - Incident.getLocX(), Hospital.getY() - Incident.getLocY()); 
		distance = (int) Math.round(ddist);
		return distance;
	}
	
}
