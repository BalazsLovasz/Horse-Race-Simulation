Horse Race Simulator

A Java-based horse race simulation project featuring both a textual interface and a graphical user interface (GUI). The project allows users to simulate horse races with customizable tracks, weather conditions, and horse attributes, as well as having a usable dynamic betting system.
Project Features

Textual Version (Part 1)
•	Text-based horse race simulation
•	Multiple horses racing on a track
•	Horse confidence levels affecting performance
•	Real-time race progress display

GUI Version (Part 2)
•	Interactive graphical interface
•	Customisable track options: 
•	Track length options ((m): 200, 400, 600, 800, 1000)
•	Number of lanes (2-5)
•	Weather conditions (Dry/Muddy/Icy)
•	Track shapes (Oval/Figure-Eight/Straight)
•	Dynamic horse customisation:
•	Custom horse names
•	Custom horse breeds
•	Custom horse colours
•	Custom horse saddles
•	Confidence levels
•	Visual race progress display
•	Acceleration and deceleration of horses
•	Virtual betting system
•	Dynamic odds of horses

Setup Instructions
1.	Unzip the HorseRaceSimulator directory
2.	Open a terminal/command prompt
3.	Navigate to the unzipped directory:

cd path/to/HorseRaceSimulator

Compilation Instructions
1.	Compile Part 1 (Textual Version):

cd Part1
javac HorseRaceSimulation.java

2.	Compile Part 2 (GUI Version):

cd ../Part2/HorseRaceSimulatorGUI/src
javac HorseRaceSimulationGUI.java

Running the Program
Part 1 - Textual Version
To run the textual version of the simulator:
1.	Navigate to Part1 directory:
cd Part1
2.	Run the program:
java HorseRaceSimulation
3.	The startRace method will be called automatically when you run the program

Part 2 - GUI Version
To run the graphical version of the simulator:
1.	Navigate to Part2/HorseRaceSimulatorGUI/src directory:
cd Part2/HorseRaceSimulatorGUI/src
2.	Run the program:
java HorseRaceSimulationGUI
3.	The startRaceGUI method will be initiated automatically when you run the program

Using the GUI Version
1.	Upon launching the GUI version, you can:
•	Set the track length (in meters)
•	Choose the number of racing lanes (2-5)
•	Select weather conditions (effects the fall rate of the horses)
•	Choose track shape
•	Customize each horse's attributes
2.	For each horse, you can set:
•	Horse name (can chose from the default horses created or make your own horse by giving a custom name) 
•	Horse breed (Different breeds have different effects on their speed)
•	Horse shoes (different shoes can effect the probability of the horse falling in certain weather conditions e.g. spiked shoes decreases chance of horse falling in icy/muddy conditions)
•	Horse saddle (The saddles effect the speed of the horse)
•	Confidence level (0-1)(displayed only, cannot be changed directly through the GUI
3.	Click the "Ready" button to store the selected data.
4.	Click the, “Start Race” to start the simulation, “Metrics” to show each horses data, “Place Bets” to place a bet on a horse”, “Exit” to go exit.

Dependencies
•	Java Development Kit (JDK) 8 or higher
•	No additional libraries or tools required
•	All necessary files are included in the submission

Project Structure
HorseRaceSimulator/
├── Part1/                     # Textual version
│   └── [Java file]
├── Part2/                     # GUI version
│   └── HorseRaceSimulatorGUI/
│       └── src/
│           └── [Java files]
├── .git/                      # Git repository
└── Report.pdf                 # Project report

Important Notes
•	This program is designed to run using standard command-line tools
•	No specific IDE or special tools are required
•	All necessary files are included in the submission
•	Both versions use pure Java with no external dependencies

Author
Balazs Mano Lovasz
