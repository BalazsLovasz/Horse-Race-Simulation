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
        trackShape = new JComboBox<String>(new String[]{"Oval","Figure-Eight","Straight", "Zig-Zag"});
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
                int radius = 80 + (i * 20); // Different lane distances

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
            RaceTrackPanel raceTrackPanel = new RaceTrackPanel(trackShapeString, numberOfLanes, horses, trackLengthInteger);

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
    private Horse[] horses;
    private String trackShape;
    private int laneCount;
    private Timer raceTimer;
    private int trackLength;


    public RaceTrackPanel(String trackShape, int laneCount, Horse[] horses, int trackLength) 
    {
        this.trackShape = trackShape;
        this.laneCount = laneCount;
        this.horses = horses;
        this.trackLength = trackLength;


        // Timer to update horse positions
        raceTimer = new Timer(100, e -> updateRaceOval());
        raceTimer.start();
    }

    private void updateRaceOval() {
        boolean raceFinished = false;

        for (Horse horse : horses) {
            horse.moveHorseCircular();
            if (horse.getAngle() >= 360) {
                raceFinished = true;
            }
            repaint(); // Redraw the race track
        }

        if (raceFinished) {
            raceTimer.stop();
            JOptionPane.showMessageDialog(this, "Race Over! Check the results.");
        }
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
                Horse horse = horses[i];

                int offset = i * 40;  // Space between each lane
                g.drawOval(offsetX + offset, offsetY + offset, ovalWidth - (offset * 2), ovalHeight - (offset * 2));

                // Calculate position of the horse for each track shape
                int x = horse.getX(width, height, laneCount, trackShape, i); // X position of the horse
                int y = horse.getY(width, height, laneCount, trackShape, i); // Y position of the horse

                // Draw the horse symbol at the calculated position
                g.drawString(String.valueOf(horse.getSymbol()), x, y );
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

                // Right oval (Loop 2) - Moves to the right 
                g.drawOval((width / 2) - 10 + offset, 50, ovalWidth, ovalHeight);
            }
        }

        else if (trackShape.equals("Straight")) 
        {
            // Draw straight lanes
            for (int i = 0; i < laneCount; i++) 
            {
                int laneY = 200 + (i * 40);
                g.drawLine(50, laneY, width - 50, laneY);
            }
        }
        else if (trackShape.equals("Zig-Zag")) 
        {

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
    private double angle; // Angle for oval movement
    private int radius; // Distance from track center

    public Horse(char horseSymbol, String horseName, double horseConfidence) //constructor
    {
        this.horseName = horseName;
        this.horseSymbol = horseSymbol;
        this.horseConfidence = horseConfidence;
        this.hasFallen = false;
        this.horseDistance = 0;
    }

    public Horse(char symbol, String name, double confidence, int radius) {
        this.horseSymbol = symbol;
        this.horseName = name;
        this.horseConfidence = confidence;
        this.angle = 0; // Start at the beginning of the oval
        this.radius = radius;
    }

    public void moveHorse()
    {
        //if the horse has fallen it cannot move, 
        //so only run if it has not fallen
        if  (!this.hasFallen())
        {
            int speed = (int) (Math.random() * 10 * this.horseConfidence + 1);
            this.horseDistance += speed;
        }
            //the probability that the horse will fall is very small (max is 0.1)
            //but will also will depends exponentially on confidence 
            //so if you double the confidence, the probability that it will fall is *2
        else if (Math.random() < (0.1*this.horseConfidence*this.horseConfidence))
        {
           this.hasFallen = true;
        }
    }

    public void moveHorseCircular() 
    {
        // Move based on confidence (higher confidence = faster movement)
        double speed = Math.random() * 2 * this.horseConfidence + 0.5;
        angle += speed;

        // Ensure angle stays within 360 degrees
        if (angle >= 360) 
        {
            angle -= 360;
        }
    }

    // Get the X position based on track shape
    public int getX(int width, int height, int laneCount, String trackShape, int laneIndex) {
        // Depending on track shape, calculate X position
        int offset = laneIndex * 40; // Space between lanes
        int ovalWidth = width - 200;
        int ovalHeight = height - 100;

        
        return (int) (width / 2 + (ovalWidth / 2 + offset) * Math.cos(getAngle()));  // Oval formula
    }

     // Get the Y position based on track shape
     public int getY(int width, int height, int laneCount, String trackShape, int laneIndex) {
        // Depending on track shape, calculate Y position
        int offset = laneIndex * 40; // Space between lanes
        int ovalWidth = width - 200;
        int ovalHeight = height - 100;


        return (int) (height / 2 + (ovalHeight / 2 + offset) * Math.sin(getAngle()));  // Oval formula
    }

    public double getAngle() 
    {
        return Math.toRadians(this.angle); // Convert degrees to radians
    }

    public int getRadius() 
    {
        return this.radius;
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


class HorseRaceSimulationGUI
{
    public static void main(String[] args) //main method
    {   
        StartRaceGUI newRace = new StartRaceGUI();
        newRace.setVisible(true);
    }
}