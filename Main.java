package AmbulanceSimulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main extends Thread {
	// take values from command line (20 different combinations)
	// ambulance speed, ambulance count, MaxLocX, MaxLocY, hospitalX and hospitalY
	// if not available, randomize in a loop that runs 20 times
	
	// program should calculate total and average elapsed time for each iteration
	// (incident happening -> patient transferred to hospital) 
	// Also, the program should calculate the total and average amount of time for waiting
	// (amount of wait time for the ambulance to arrive)
	
	// run, In a loop, incidents are generated in random times and at random locations in the range 
	// (0, MaxLocX) and (0, MaxLocY)
	
	private static List<Integer> speedList = new ArrayList<Integer>();
	private static List<Integer> countList = new ArrayList<Integer>();
	private static List<Integer> maxLocXList = new ArrayList<Integer>();
	private static List<Integer> maxLocYList = new ArrayList<Integer>();
	private static List<Integer> hospitalXList = new ArrayList<Integer>();
	private static List<Integer> hospitalYList = new ArrayList<Integer>();
	
	private static List<Long> totalElapsedTime = new ArrayList<Long>();
	private static List<Long> totalWaitTime = new ArrayList<Long>();
	private static List<Long> avgElapsedTime = new ArrayList<Long>();
	private static List<Long> avgWaitTime = new ArrayList<Long>();
	

	
	public static Semaphore ambulance = new Semaphore(0);
	public static Semaphore incidents = new Semaphore(0);
	public static Semaphore available = new Semaphore(1);
	
	private int y = 0;
	public final static AtomicBoolean simulationFinished = new AtomicBoolean(false);
	

	public static void main(String [ ] args) {
		
		System.out.println("Press 1 to randomize values \nPress 2 to input values manually\nPress anything else to quit");
		
		Scanner scanner = new Scanner(System.in);
		String choice = scanner.next();
		
		if(choice.equals("1")) {
			randomize();
		} else if(choice.equals("2")) {
			manualInput();
		} else {
			System.exit(0);
		}
		System.out.println("Values stored");
		scanner.close();
		
		System.out.println("Speed list: " + speedList);
		System.out.println("Count list: " + countList);
		System.out.println("Max loc X list: " + maxLocXList);
		System.out.println("Max loc Y list: " + maxLocYList);
		System.out.println("Hospital X list: " + hospitalXList);
		System.out.println("Hospital Y list: " + hospitalYList);

		
		Main main = new Main();
		main.start();
	}
	
	// should i use thread pool and create objects on loop as long as ambulance count is filled???
	public void run() {
		while(y < 20) {
			System.out.println("Starting simulation " + y + ".\n");
			simulationFinished.set(false);
			
			Hospital h = new Hospital(hospitalXList.get(y), hospitalYList.get(y)); // creates the hospital
			
			printInfo();

			while(simulationFinished.get() == false) {
				// creates ambulances until count is filled
				for(int x = 1; x <= countList.get(y); x++) {
					Ambulance a = new Ambulance(speedList.get(y), countList.get(y));
					a.start(); // ambulance ready for work
				}
				
				// this creates new incidents for a while, randomization is not TRUE RANDOM at the moment
				for(int i = 1; i <= 4; i++) {
					Incident in = new Incident(ThreadLocalRandom.current().nextInt(0, maxLocXList.get(y)), ThreadLocalRandom.current().nextInt(0, maxLocYList.get(y)));
					in.start();
					try{
						Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 5000));
					}catch(InterruptedException e) {
						e.printStackTrace();
					}
				}

				try {
					Thread.sleep(30000); // sleep for a while so last patient can be transferred
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				simulationFinished.set(true);
			}
			
			System.out.println("Simulation " + y + " ended.\n");
			calculateTime();
			printTime();

			y++;
			
			h = null;
		}
	}

	private static void randomize() {
		for(int i = 0; i < 20; i++) {
			//ThreadLocalRandom is more efficient way than using random generator
			speedList.add(ThreadLocalRandom.current().nextInt(1, 200));
			countList.add(ThreadLocalRandom.current().nextInt(1, 10));
			maxLocXList.add(ThreadLocalRandom.current().nextInt(1000, 10000));
			maxLocYList.add(ThreadLocalRandom.current().nextInt(1000, 10000));
			hospitalXList.add(ThreadLocalRandom.current().nextInt(1000, 10000));
			hospitalYList.add(ThreadLocalRandom.current().nextInt(1000, 10000));
		}
	}
	
	private static void manualInput() {
		Scanner inputScanner = new Scanner(System.in);
		for(int i = 0; i < 20; i++) {
			System.out.println("Input " + i + " speed value: ");
			speedList.add(inputScanner.nextInt());
			
			System.out.println("Input " + i + " ambulance count value: ");
			countList.add(inputScanner.nextInt());
			
			System.out.println("Input " + i + " MaxLocX value: ");
			maxLocXList.add(inputScanner.nextInt());
			
			System.out.println("Input " + i + " MaxLocY value: ");
			maxLocYList.add(inputScanner.nextInt());
			
			System.out.println("Input " + i + " hospitalX value: ");
			hospitalXList.add(inputScanner.nextInt());
			
			System.out.println("Input " + i + " hospitalY value: ");
			hospitalYList.add(inputScanner.nextInt());
		}
		inputScanner.close();
	}
	
	
	private static void printInfo() {
		System.out.println("Hospital location: " + Hospital.getX() + "." + Hospital.getY());
		System.out.println("Ambulance speed : " + speedList.get(0) + "\nAmbulance count: " + countList.get(0));
		System.out.println("Incident max location X: " + maxLocXList.get(0) + "\nIncident max location Y: " + maxLocYList.get(0) + "\n");
	}
	
	private static void calculateTime() {
		long elapsedSum = 0;
		long waitSum = 0;
		
		for (long i : Ambulance.elapsedTimeList) {
			elapsedSum += i;
		}
		
		for(long z : Incident.waitTimeList) {
			waitSum += z;
		}
		
		long elapsedAvg = elapsedSum / Ambulance.elapsedTimeList.size();
		long waitAvg = waitSum / Incident.waitTimeList.size();
		
		totalElapsedTime.add(elapsedSum/1000);
		totalWaitTime.add(waitSum/1000);
		avgElapsedTime.add(elapsedAvg/1000);
		avgWaitTime.add(waitAvg/1000);
	}
	
	private static void printTime() {
		System.out.println("\nTimes have been converted to seconds.\n");
		System.out.println("Total elapsed time: " + totalElapsedTime);
		System.out.println("Average elapsed time: " + avgElapsedTime);
		System.out.println("Total wait time: " + totalWaitTime);
		System.out.println("Average wait time: " + avgWaitTime + "\n \n");
	}
	
}
