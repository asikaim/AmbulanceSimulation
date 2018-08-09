package AmbulanceSimulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {
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
	
	public static Semaphore ambulance = new Semaphore(0, true);
	public static Semaphore incidents = new Semaphore(0, true);
	public static Semaphore available = new Semaphore(1, true);
	
	private static int SIMULATION = 0;
	private static final int MAX_INCIDENTS = 20;
	private static boolean threadsCreated = false;
	
	public final static AtomicBoolean simulationFinished = new AtomicBoolean(false);	// flag for thread execution during simulation
	
	static BlockingQueue<Integer> queue = new LinkedBlockingDeque<>(MAX_INCIDENTS);

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
		
		
		// prints all values that have been stored into arrays
		System.out.println("Speed list: " + speedList);
		System.out.println("Count list: " + countList);
		System.out.println("Max loc X list: " + maxLocXList);
		System.out.println("Max loc Y list: " + maxLocYList);
		System.out.println("Hospital X list: " + hospitalXList);
		System.out.println("Hospital Y list: " + hospitalYList);

		// iterates through lists creating thread pools and calling time calculation
		while(SIMULATION < 20) {		// 20 iterations with separate values
			System.out.println("Starting simulation " + SIMULATION + ".\n");
			simulationFinished.set(false);	// flag for running the simulation
			threadsCreated = false;			// flag for making sure that only necessary ambulances/simulations are created
			
			Hospital h = new Hospital(hospitalXList.get(SIMULATION), hospitalYList.get(SIMULATION)); // creates the hospital
			
			printInfo(); // print currently used values
			
			ExecutorService ambulanceExecutor = Executors.newFixedThreadPool(countList.get(SIMULATION));
			ExecutorService incidentExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
			
			while(simulationFinished.get() == false) {
				
				if(threadsCreated == false) {
					// creates ambulances into thread pool until count is filled
					for(int x = 1; x <= countList.get(SIMULATION); x++) {
						ambulanceExecutor.execute(new Ambulance(queue, x, speedList.get(SIMULATION), countList.get(SIMULATION)));
					}
					
					// this creates 20 new incidents into thread pool, randomization is not TRUE RANDOM at the moment
					for(int i = 1; i <= MAX_INCIDENTS; i++) {
						incidentExecutor.execute(new Incident(queue, i, ThreadLocalRandom.current().nextInt(0, maxLocXList.get(SIMULATION)), ThreadLocalRandom.current().nextInt(0, maxLocYList.get(SIMULATION))));
						try{
							Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 10000));
						}catch(InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				
				threadsCreated = true;	// makes sure that no more threads are created
				
				try {	// waits for a while (safety reasons)
					Thread.sleep(15000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				
				// checks if blocking queue is empty
				if(queue.isEmpty()) {	
					//shuts down executor services
					System.out.println("Shutting down executor services");
					ambulanceExecutor.shutdown();
					incidentExecutor.shutdown();
					
					try{	 // executes last tasks(if any)
						ambulanceExecutor.awaitTermination(10, TimeUnit.SECONDS);
						incidentExecutor.awaitTermination(10, TimeUnit.SECONDS);
					}catch(InterruptedException e) {
						e.printStackTrace();
					}
					
					// checks if executor services have been closed
					if(ambulanceExecutor.isShutdown() && incidentExecutor.isShutdown()) {
						simulationFinished.set(true);
					}
				}
			}

			System.out.println("Simulation " + SIMULATION + " has ended.\n");
		
			calculateTime();
			printTime();

			SIMULATION++;
			
			
		}
		
		System.out.println("\n\nAll simulations over\nPrinting best times...");
		bestTime();
	}
	

	// iterates 20 times and adds semi-random values to array lists
	// these can be changed, but i'm using fairly low values right now for testing purposes
	private static void randomize() {
		for(int i = 0; i < 20; i++) {
			//ThreadLocalRandom is more efficient way than using random generator
			speedList.add(ThreadLocalRandom.current().nextInt(1, 200));
			countList.add(ThreadLocalRandom.current().nextInt(1, 15));
			maxLocXList.add(ThreadLocalRandom.current().nextInt(1000, 10000));
			maxLocYList.add(ThreadLocalRandom.current().nextInt(1000, 10000));
			hospitalXList.add(ThreadLocalRandom.current().nextInt(1000, 10000));
			hospitalYList.add(ThreadLocalRandom.current().nextInt(1000, 10000));
		}
	}
	
	// iterates 20 times while asking user to input values into every field, takes really long time
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
	
	// prints info of current values
	private static void printInfo() {
		System.out.println("Hospital location: " + Hospital.getX() + "." + Hospital.getY());
		System.out.println("Ambulance speed : " + speedList.get(SIMULATION) + "\nAmbulance count: " + countList.get(SIMULATION));
		System.out.println("Incident max location X: " + maxLocXList.get(SIMULATION) + "\nIncident max location Y: " + maxLocYList.get(SIMULATION) + "\n");
	}
	
	// calculates sum and average times of last simulation iteration and converts to seconds
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
	
	
	// prints time of last simulation iteration in seconds
	private static void printTime() {
		System.out.println("\nTimes have been converted to seconds.\n");
		System.out.println("Total elapsed time: " + totalElapsedTime);
		System.out.println("Average elapsed time: " + avgElapsedTime);
		System.out.println("Total wait time: " + totalWaitTime);
		System.out.println("Average wait time: " + avgWaitTime + "\n \n");
	}
	
	// finds shortest time from array list and prints it with index
	private static void bestTime() {
		// not really effective way as it traverses array list three round per time but it works for now
		System.out.println("\nTimes have been converted to seconds.\n");
		System.out.println("\nSimulation " + totalElapsedTime.indexOf(Collections.min(totalElapsedTime)) + " had best total elapsed time: " + Collections.min(totalElapsedTime));
		System.out.println("\nSimulation " + avgElapsedTime.indexOf(Collections.min(avgElapsedTime)) + " had best average elapsed time: " + Collections.min(avgElapsedTime));
		System.out.println("\nSimulation " + totalWaitTime.indexOf(Collections.min(totalWaitTime)) + " had best total wait time: " + Collections.min(totalWaitTime));
		System.out.println("\nSimulation " + totalWaitTime.indexOf(Collections.min(avgWaitTime)) + " had best average wait time: " + Collections.min(avgWaitTime));
	}
}
