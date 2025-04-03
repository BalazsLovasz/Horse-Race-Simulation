/**
 * This class stores personal information about each horse object. 
 * 
 * @author Balazs Mano Lovasz 
 * @version 1
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;
import javax.swing.*;
import java.util.ArrayList;
import javax.security.auth.Refreshable;
import javax.swing.plaf.ColorUIResource;

class StartRaceGUI extends JFrame 
{
    private JPanel customisingPanel, raceDisplayPanel, mainPanel, horsePanel;
    private JTextField trackLength;
    private JButton startRaceButton;
    private JComboBox<String> laneCountList;
    private JComboBox<String> weatherCondition;
    private JComboBox<String> trackShape;

    private JTextField[] horseNames = new JTextField[5];
    private JTextField[] horseSymbols = new JTextField[5];
    private JTextField[] horseConfidences = new JTextField[5];

    public StartRaceGUI()
    {
        //setting up the window
        setTitle("Horse Race Simulation");
        setSize(800,600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        //this panel will have the race running and the button (helps with display)
        // to wrap button and race panel
        mainPanel = new JPanel(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

        //This panel will display the race
        raceDisplayPanel = new JPanel();
        raceDisplayPanel.setBackground(Color.LIGHT_GRAY);
        mainPanel.add(raceDisplayPanel, BorderLayout.CENTER);

        //Custumising panel
        customisingPanel = new JPanel();
        customisingPanel.setLayout(new GridLayout(5,2,10,10));
        add(customisingPanel, BorderLayout.NORTH);

        //Choosing Track Length
        customisingPanel.add(new JLabel("Track Length (metres):"));
        trackLength = new JTextField("100"); //making a default value of 100m
        customisingPanel.add(trackLength);

        //Choosing the number of lanes
        customisingPanel.add(new JLabel("Number of Lanes:"));
        laneCountList = new JComboBox<String>(new String[]{"2","3","4","5"});
        customisingPanel.add(laneCountList);

        // Horse Input Panel (Initially Empty)
        horsePanel = new JPanel(new GridLayout(0, 2, 2, 2));
        add(horsePanel, BorderLayout.SOUTH); 

        // Listener to update horse inputs dynamically
        laneCountList.addActionListener(e -> updateHorseInputs());

        //Choosing the weather
        customisingPanel.add(new JLabel("Weather Condition:"));
        weatherCondition = new JComboBox<String>(new String[]{"Dry","Muddy","Icy"});
        customisingPanel.add(weatherCondition);

        //Choosing the track shape
        customisingPanel.add(new JLabel("Track Shape:"));
        trackShape = new JComboBox<String>(new String[]{"Oval","Figure-Eight","Custom"});
        customisingPanel.add(trackShape);

        //Start Button
        startRaceButton = new JButton("Start Race");
        startRaceButton.addActionListener(new StartRaceButtonListener());
        startRaceButton.setFont(new Font("Arial", Font.BOLD, 16));
        startRaceButton.setBackground(new Color(50, 150, 250)); // sets color to light blue
        startRaceButton.setForeground(Color.WHITE);
        mainPanel.add(startRaceButton, BorderLayout.NORTH);

        //setting the font for every text
        Font labelFont = new Font("Arial", Font.PLAIN, 14);
        Component[] components = customisingPanel.getComponents();
        for (int i = 0; i < components.length; i++) //loops through all JLabel components
        {
            if (components[i] instanceof JLabel) 
            {
                components[i].setFont(labelFont); // sets the font to all JLabels
            }
        }
        // Triggers the updateHorseInputs method on initialization
        updateHorseInputs();
    }

    //this method updates the number of horses that show up on the screen
    private void updateHorseInputs() 
    {
        horsePanel.removeAll();
        int numLanes = Integer.parseInt((String) laneCountList.getSelectedItem());

        //adds the JTextFields to the horse panel
        for (int i = 0; i < numLanes; i++) 
        {
            horsePanel.add(new JLabel("Horse " + (i + 1) + " Name:")); //name
            horseNames[i] = new JTextField("Horse" + (i + 1));
            horsePanel.add(horseNames[i]);

            horsePanel.add(new JLabel("Horse " + (i + 1) + " Symbol:")); //symbol(a character)
            horseSymbols[i] = new JTextField("#");
            horsePanel.add(horseSymbols[i]);

            horsePanel.add(new JLabel("Horse " + (i + 1) + " Confidence:")); //confidence
            horseConfidences[i] = new JTextField("0.5");
            horsePanel.add(horseConfidences[i]);
        }

        //refreshes the panel
        horsePanel.revalidate();
        horsePanel.repaint();

        String trackShapeString = (String) trackShape.getSelectedItem(); //Get the user input values
        int numberOfLanes = Integer.parseInt((String) laneCountList.getSelectedItem()); // Get the user input values

        RaceTrackPanel raceTrackPanel = new RaceTrackPanel(trackShapeString, numberOfLanes);  // Create a new race track panel

        // Remove the old race track panel and add the new one
        raceDisplayPanel.removeAll();
        raceDisplayPanel.add(raceTrackPanel, BorderLayout.CENTER);  // Adds the new track panel

        // Refresh the race display panel
        raceDisplayPanel.revalidate();
        raceDisplayPanel.repaint();
    }

    // Action Listener for "Start Race" button
    private class StartRaceButtonListener implements ActionListener 
     {
        @Override
        public void actionPerformed(ActionEvent e) 
        {
            int trackLengthInteger = 0;
            try
            {
                trackLengthInteger = Integer.parseInt(trackLength.getText()); // Get the user input values
                if (trackLengthInteger <= 0) 
                {
                    JOptionPane.showMessageDialog(null, "Track length must be positive!", "Error", JOptionPane.ERROR_MESSAGE); //displays error message
                    return;
                }
            }
            catch (NumberFormatException ex) //incase the track length is not a digit
            {
                JOptionPane.showMessageDialog(null, "Please enter a valid number!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int numberOfLanes = Integer.parseInt((String) laneCountList.getSelectedItem()); // Get the user input values
            String weatherConditionString =(String) weatherCondition.getSelectedItem(); // Get the user input values
            String trackShapeString = (String) trackShape.getSelectedItem(); // Get the user input values
            
            //Stores the number of horses in the race
            Horse[] horses = new Horse[numberOfLanes];

            // Adding horses based on selected number of lanes
            for (int i = 0; i < numberOfLanes; i++) 
            {
                String horseName = horseNames[i].getText();
                char horseSymbol = horseSymbols[i].getText().charAt(0);
                double confidence = 0.5;
                try
                {
                    confidence = Double.parseDouble(horseConfidences[i].getText());
                    if (confidence < 0 || confidence > 1)
                    {
                        throw new IllegalArgumentException("Confidence must be between 0 and 1");
                    }
                }
                catch (IllegalArgumentException ex)
                {
                    JOptionPane.showMessageDialog(null, "Please enter a valid confidence value between 0 and 1!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                horses[i] = new Horse(horseSymbol, horseName, confidence);
            }
            //hide the customising panles
            horsePanel.setVisible(false);
            customisingPanel.setVisible(false);

            // this create the graphical track panel
            RaceTrackPanel raceTrackPanel = new RaceTrackPanel(trackShapeString, numberOfLanes);

            // this updates the race display panel
            mainPanel.removeAll();  // Removes anything that was previously there
            mainPanel.add(raceTrackPanel, BorderLayout.CENTER);  // Adds the new track panel

            // Refresh the race display panel
            mainPanel.revalidate();
            mainPanel.repaint();
        }
    }
}

class RaceTrackPanel extends JPanel 
{
    private String trackShape;
    private int laneCount;

    public RaceTrackPanel(String trackShape, int laneCount) 
    {
        this.trackShape = trackShape;
        this.laneCount = laneCount;
    }

    @Override
    protected void paintComponent(Graphics g) 
    {
        super.paintComponent(g); // Clear previous drawings

        int width = getWidth();   // Panel width
        int height = getHeight(); // Panel height

        g.setColor(Color.BLACK); 

        if (trackShape.equals("Oval")) 
        {
            // Calculate dimensions dynamically
            int ovalWidth = width - 200;  // Base oval width
            int ovalHeight = height - 100;  // Base oval height
            int offsetX = 50;  // Offset from left
            int offsetY = 50;  // Offset from top

            // Draw the outer and inner ovals based on lane count
            for (int i = 0; i < laneCount; i++) 
            {
                int offset = i * 40;  // Space between each lane
                g.drawOval(offsetX + offset, offsetY + offset, ovalWidth - (offset * 2), ovalHeight - (offset * 2));
            }
        } 
        else if (trackShape.equals("Figure-Eight")) 
        {  
            int laneSpacing = 40; // Controls the gap between lanes
            int ovalWidth = (width / 2) - 60; // Constant width of the ovals
            int ovalHeight = height - 100;   // Constant height of the ovals

            

            for (int i = 0; i < laneCount; i++) {
                // Offset controls the spacing between each lane
                int offset = i * laneSpacing;

                // Left oval (Loop 1) - Moves to the right
                g.drawOval(50 + offset, 50, ovalWidth, ovalHeight);

                // Right oval (Loop 2) - Moves to the right but with a fixed distance between
                g.drawOval((width / 2) - 10 + offset, 50, ovalWidth, ovalHeight);
            }
        }

        else if (trackShape.equals("Straight")) 
        {
            // Draw straight lanes
            for (int i = 1; i <= 5; i++) 
            {
                int laneY = 50 + (i * 40);
                g.drawLine(50, laneY, width - 50, laneY);
            }
        }
    }
}


class Horse
{
    //instance variable set to private to improve security
    private String horseName;
    private char horseSymbol;
    private int horseDistance;
    private boolean hasFallen;
    private double horseConfidence;

    public Horse(char horseSymbol, String horseName, double horseConfidence) //constructor
    {
        this.horseName = horseName;
        this.horseSymbol = horseSymbol;
        this.horseConfidence = horseConfidence;
        this.hasFallen = false;
        this.horseDistance = 0;
    }

    public void fall() //method that sets horse to fallen
    {
        this.hasFallen = true;
    }

    public double getConfidence() //returns the confidence of the horse
    {
        return this.horseConfidence;
    }

    public int getDistanceTravelled() //returns the distance travelled by the horse
    {
        return this.horseDistance;
    }

    public String getName() //returns the name of the horse
    {
        return this.horseName;
    }

    public char getSymbol() //returns the symbol of the horse
    {
        return this.horseSymbol;
    }

    public void goBackToStart() //set the horses back to the start line
    {
        this.horseDistance = 0;
        this.hasFallen = false;
    }

    public boolean hasFallen()// returns wether the horse has fallen or not
    {
       return this.hasFallen;
    }

    public void moveForward() //moves the horse forward by 1 index;
    {
        this.horseDistance++;
    }
    
    public void setConfidence(double newConfidence) //asigns a new confident to the horse
    {
        if(newConfidence >= 0 && newConfidence <=1)
        {
            this.horseConfidence = newConfidence;
        }
        else
        {
            throw new IllegalArgumentException("Confidence must be between 0 and 1"); //incase the inputed confidence is out of bounds
        }
    }

    public void setSymbol(char newSymbol)
    {
        this.horseSymbol = newSymbol;
    }
}

/**
 * A three-horse race, each horse running in its own lane
 * for a given distance
 * 
 * @author McRaceface
 * @version 1.0
 */
class Race
{
    private int trackLength;
    private int laneCount;
    private String trackShape;
    private String weatherCondition;
    private Horse lane1Horse;
    private Horse lane2Horse;
    private Horse lane3Horse;
    private Horse lane4Horse;
    private Horse lane5Horse;
    private static ArrayList<Horse> numberOfHorses = new ArrayList<Horse>();

    /**
     * Constructor for objects of class Race
     * Initially there are no horses in the lanes
     * 
     * @param trackLength the length of the racetrack (in metres...)
     */
    public Race(int trackLength, int laneCount, String trackShape, String weatherCondition)
    {
        // initialise instance variables
        this.trackLength = trackLength;
        this.laneCount = laneCount;
        this.trackShape = trackShape;
        this.weatherCondition = weatherCondition;
        lane1Horse = null;
        lane2Horse = null;
        lane3Horse = null;
        lane4Horse = null;
        lane5Horse = null;

    }
    
    /**
     * Adds a horse to the race in a given lane
     * 
     * @param theHorse the horse to be added to the race
     * @param laneNumber the lane that the horse will be added to
     */
    public void addHorse(Horse theHorse, int laneNumber)
    {
        if (laneNumber == 1)
        {
            lane1Horse = theHorse;
        }
        else if (laneNumber == 2)
        {
            lane2Horse = theHorse;
        }
        else if (laneNumber == 3)
        {
            lane3Horse = theHorse;
        }
        else if (laneNumber == 4)
        {
            lane4Horse = theHorse;
        }
        else if (laneNumber == 5)
        {
            lane5Horse = theHorse;
        }
        else
        {
            System.out.println("Cannot add horse to lane " + laneNumber + " because there is no such lane");
            return;
        }
        numberOfHorses.add(theHorse);
    }
    
    /**
     * Start the race
     * The horse are brought to the start and
     * then repeatedly moved forward until the 
     * race is finished
     */
    public void startRace()
    {
    
        //declare a local variable to tell us when the race is finished
        boolean finished = false;
        
        //reset all the lanes (all horses not fallen and back to 0). 
        for (int i = 0; i<laneCount; i++) 
        {
            numberOfHorses.get(i).goBackToStart();
        }
                      
        while (!finished)
        { 
            //print the race positions
            printRace();
            
            //if any of the three horses has won the race is finished
            if ( raceWonBy(lane1Horse) || raceWonBy(lane2Horse) || raceWonBy(lane3Horse) || raceWonBy(lane4Horse) || raceWonBy(lane5Horse))
            {
                finished = true;
            }
            if (lane1Horse.hasFallen() && lane2Horse.hasFallen() && lane3Horse.hasFallen() && lane4Horse.hasFallen() && lane5Horse.hasFallen()) 
            {
                System.out.println("All horses have fallen! The race is canceled.");
                finished = true;
            }
           
            //wait for 100 milliseconds
            try{
                TimeUnit.MILLISECONDS.sleep(100);
            }catch(Exception e){}
        }
    }
    
    /**
     * Randomly make a horse move forward or fall depending
     * on its confidence rating
     * A fallen horse cannot move
     * 
     * @param theHorse the horse to be moved
     */
    private void moveHorse(Horse theHorse)
    {
        //if the horse has fallen it cannot move, 
        //so only run if it has not fallen
        if  (!theHorse.hasFallen())
        {
            //the probability that the horse will move forward depends on the confidence;
            if (Math.random() < theHorse.getConfidence())
            {
               theHorse.moveForward();
            }
            
            //the probability that the horse will fall is very small (max is 0.1)
            //but will also will depends exponentially on confidence 
            //so if you double the confidence, the probability that it will fall is *2
            else if (Math.random() < (0.1*theHorse.getConfidence()*theHorse.getConfidence()))
            {
                theHorse.fall();
            }
        }
    }
        
    /** 
     * Determines if a horse has won the race
     *
     * @param theHorse The horse we are testing
     * @return true if the horse has won, false otherwise.
     */
    private boolean raceWonBy(Horse theHorse)
    {
        if (theHorse.getDistanceTravelled() >= trackLength)
        {
            System.err.println("And the winner is... " + theHorse.getName());
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /***
     * Print the race on the terminal
     */
    private void printRace()
    {
        System.out.print("\033[H\033[2J");
        System.out.flush();

        multiplePrint('=',trackLength+3); //top edge of track
        System.out.println();
        
        for(int i = 0; i<numberOfHorses.size(); i++)
        {
            printLane(numberOfHorses.get(i));
            System.out.println();
        }
        
        multiplePrint('=',trackLength+3); //bottom edge of track
        System.out.println();    
    }
    
    /**
     * print a horse's lane during the race
     * for example
     * |           X                      |
     * to show how far the horse has run
     */
    private void printLane(Horse theHorse)
    {
        //calculate how many spaces are needed before
        //and after the horse
        int spacesBefore = theHorse.getDistanceTravelled();
        int spacesAfter = trackLength - theHorse.getDistanceTravelled();
        
        //print a | for the beginning of the lane
        System.out.print('|');
        
        //print the spaces before the horse
        multiplePrint(' ',spacesBefore);
        
        //if the horse has fallen then print dead
        //else print the horse's symbol
        if(theHorse.hasFallen())
        {
            System.out.print('X');
        }
        else
        {
            System.out.print(theHorse.getSymbol());
        }
        
        //print the spaces after the horse
        multiplePrint(' ',spacesAfter);
        
        //print the | for the end of the track
        System.out.print("|  " + theHorse.getName() + " (Current confidence " + theHorse.getConfidence() + ")");
    }
        
    
    /***
     * print a character a given number of times.
     * e.g. printmany('x',5) will print: xxxxx
     * 
     * @param aChar the character to Print
     */
    private void multiplePrint(char aChar, int times)
    {
        int i = 0;
        while (i < times)
        {
            System.out.print(aChar);
            i = i + 1;
        }
    }
    //returns the length of the track
    public int getTrackLength() 
    {
        return this.trackLength;
    }

    public int getLaneCount()
    {
        return this.laneCount;
    }
}

class HorseRaceSimulationGUI
{
    public static void main(String[] args) //main method
    {   
        StartRaceGUI newRace = new StartRaceGUI();
        newRace.setVisible(true);
    }
}