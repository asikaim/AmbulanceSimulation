# AmbulanceSimulation

The goal of this assignment is to simulate an ambulance service. Your task is to model the process and perform reporting. 
The model consists of three main components. 
1.	Incidents
2.	Ambulances
3.	Hospital
General parameters of the model include the hospital location (hospitalX and hospitalY), ambulance properties (speed, count).
In a loop, incidents are generated in random times and at random locations in the range (0, MaxLocX) and (0, MaxLocY). Each incident can have the following states:
1.	Waiting for ambulance
2.	Ambulance on site

An incident is initially in the waiting for an ambulance stage. Once an ambulance becomes free, it moves (with its speed) toward the incident.  The path can be considered as a direct line. To simulate the move one can wait the correct amount of time to reach from Hospital to incident location.  
When on site, the ambulance transfers the patient to the hospital (speed and distance to calculate time). 
Each ambulance has the following states:
1.	Idle
2.	Moving to incident
3.	On site
4.	Moving to hospital
5.	Transfer the patient once arrived in the hospital
The ambulances are in idle state upon generation and when there are no incident to take care of.  An incident (once happened) will be assigned to a free ambulance in idle state if available, otherwise, the program should wait for an ambulance to become free. Once receiving an incident information, an idle ambulance goes to the second state (“Move to incident”) and will arrive in a specific time depending on the speed and the distance. The ambulance takes the patient immediately inside the car (no spent time), but it needs 10 second to transfer the patient inside the hospital from the ambulance once it reaches the hospital. 
Use the correct design to model this problem. The parameters (ambulance speed, ambulance count, MaxLocX, MaxLocY, hospitalX and hospitalY) can be received from the command line input or can be assigned in a loop with random values. If received through the command line 20 different value combinations should be used. Similarly, the loop for random case, should run for 20 iterations (20 combinations). 
The program should calculate the total and average elapsed time in each iteration, from the incidents happening to receiving the care at the hospital.  Also, the program should calculate the total and average amount of time, from the time of incident occurrence and the ambulance arrival (amount of wait time for the ambulance to arrive). Finally, the best set of parameters must be presented. 
