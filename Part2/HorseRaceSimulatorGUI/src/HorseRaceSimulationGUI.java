/**
 * This class stores personal information about each horse object. 
 * 
 * @author Balazs Mano Lovasz 
 * @version 1
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;


class StartRaceGUI extends JFrame 
{
    private JPanel customisingPanel, mainPanel, horsePanel;
    private JTextField trackLength;
    private JButton startRaceButton, restartButton, exitButton, viewMetricsButton;
    private JComboBox<String> laneCountList;
    private JComboBox<String> weatherCondition;
    private JComboBox<String> trackShape;

    //to do with horses
    private Horse[] horses = new Horse[5]; // Max 5 horses
    private ArrayList<Horse> allHorses = new ArrayList<>(); // this stores horses that have ever raced
    private JTextField[] horseNames = new JTextField[5];
    private JTextField[] horseSymbols = new JTextField[5];
    private JTextField[] horseConfidences = new JTextField[5];

    private int trackLengthInteger;
    private int numberOfLanes;
    private String trackShapeString;
    private String weatherConditionString;

    private RaceTrackPanel currentRacePanel;
    private JTextArea metricsArea;
    private JScrollPane metricsScrollPane;
    private JTabbedPane metricsTabs;
    private JPanel metricsPanel;

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
        mainPanel.setBackground(Color.LIGHT_GRAY);
        add(mainPanel, BorderLayout.CENTER);

        //Custumising panel
        customisingPanel = new JPanel();
        customisingPanel.setLayout(new GridLayout(5,2,10,10));
        add(customisingPanel, BorderLayout.NORTH);

        //Choosing Track Length
        customisingPanel.add(new JLabel("Track Length (metres):"));
        trackLength = new JTextField("200"); //making a default value of 200m
        customisingPanel.add(trackLength);

        //Choosing the number of lanes
        customisingPanel.add(new JLabel("Number of Lanes:"));
        laneCountList = new JComboBox<String>(new String[]{"2","3","4","5"});
        customisingPanel.add(laneCountList);

        // Horse Input Panel (Initially Empty)
        horsePanel = new JPanel(new GridLayout(0, 2, 2, 2));

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

        // Metrics display area 
        metricsPanel = new JPanel(new BorderLayout());
        metricsTabs = new JTabbedPane();
        metricsPanel.add(metricsTabs, BorderLayout.CENTER);
        metricsPanel.setVisible(false);

        //This panel is used to organise the horse panel and metrics panel
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(horsePanel, BorderLayout.NORTH);
        southPanel.add(metricsPanel, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);

        //Start Button
        startRaceButton = new JButton("Start Race");
        startRaceButton.addActionListener(new StartRaceButtonListener());
        startRaceButton.setFont(new Font("Arial", Font.BOLD, 16));
        startRaceButton.setBackground(new Color(50, 150, 250)); // sets color to light blue
        startRaceButton.setForeground(Color.WHITE);
        startRaceButton.setPreferredSize(new Dimension(200, 30));
        startRaceButton.setMargin(new Insets(2, 5, 2, 5));
        
        //Exit Button
        exitButton = new JButton("Exit Race");
        exitButton.addActionListener(new ExitButtonListener());
        exitButton.setFont(new Font("Arial", Font.BOLD, 14));
        exitButton.setBackground(new Color(200, 100, 100)); // Red color
        exitButton.setForeground(Color.WHITE);
        exitButton.setPreferredSize(new Dimension(100, 30));
        exitButton.setMargin(new Insets(2, 5, 2, 5));
        exitButton.setVisible(false); // Hidden until race starts

        // Restart Button
        restartButton = new JButton("Restart");
        restartButton.addActionListener(new RestartButtonListener());
        restartButton.setFont(new Font("Arial", Font.BOLD, 14));
        restartButton.setBackground(new Color(100, 200, 100));
        restartButton.setForeground(Color.WHITE);
        restartButton.setPreferredSize(new Dimension(100, 30)); // Smaller than start button
        restartButton.setMargin(new Insets(2, 5, 2, 5));
        restartButton.setVisible(false);

        //Performance metrics button
        viewMetricsButton = new JButton("Metrics");
        viewMetricsButton.addActionListener(new ViewMetricsButtonListener());
        viewMetricsButton.setFont(new Font("Arial", Font.BOLD, 14));
        viewMetricsButton.setBackground(new Color(128, 128, 128));
        viewMetricsButton.setForeground(Color.WHITE);
        viewMetricsButton.setPreferredSize(new Dimension(100, 30)); // Smaller than start button
        viewMetricsButton.setMargin(new Insets(2, 5, 2, 5));
        viewMetricsButton.setVisible(false);

        // Container panel for proper positioning
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.add(startRaceButton);
        buttonPanel.add(restartButton);
        buttonPanel.add(exitButton);
        buttonPanel.add(viewMetricsButton);
        mainPanel.add(buttonPanel, BorderLayout.NORTH);

        

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

        for (int i = 0; i < numLanes; i++) 
        {
            horsePanel.add(new JLabel("Horse " + (i + 1) + " Name:"));
            // Only set default if no existing horse
            String defaultName = (i < horses.length && horses[i] != null) ? horses[i].getName() : "Horse" + (i + 1);
            horseNames[i] = new JTextField(defaultName);
            horsePanel.add(horseNames[i]);

            horsePanel.add(new JLabel("Horse " + (i + 1) + " Symbol:"));
            char defaultSymbol = (i < horses.length && horses[i] != null) ? horses[i].getSymbol() : '#';
            horseSymbols[i] = new JTextField(String.valueOf(defaultSymbol));
            horsePanel.add(horseSymbols[i]);

            horsePanel.add(new JLabel("Horse " + (i + 1) + " Confidence:"));
            double defaultConfidence = (i < horses.length && horses[i] != null) ? horses[i].getConfidence() : 0.5;
            horseConfidences[i] = new JTextField(String.valueOf(defaultConfidence));
            horsePanel.add(horseConfidences[i]);
        }

        horsePanel.revalidate();
        horsePanel.repaint();
        trackShapeString = (String) trackShape.getSelectedItem();
        numberOfLanes = Integer.parseInt((String) laneCountList.getSelectedItem());
    }

    // Action Listener for "Start Race" button
    private class StartRaceButtonListener implements ActionListener 
    {
        @Override
        public void actionPerformed(ActionEvent e) 
        {
            metricsPanel.setVisible(false);
            viewMetricsButton.setText("Metrics");

            trackLengthInteger = 0;
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
            numberOfLanes = Integer.parseInt((String) laneCountList.getSelectedItem()); // Get the user input values
            weatherConditionString =(String) weatherCondition.getSelectedItem(); // Get the user input values
            trackShapeString = (String) trackShape.getSelectedItem(); // Get the user input values
            
            Horse[] oldHorses = horses != null ? horses.clone() : new Horse[0];
            Horse[] newHorses = new Horse[numberOfLanes];

            for (int i = 0; i < numberOfLanes; i++) 
            {
                String horseName = horseNames[i].getText();
                char horseSymbol = horseSymbols[i].getText().charAt(0);
                double confidence = 0.5;

                try {
                    confidence = Double.parseDouble(horseConfidences[i].getText());
                    if (confidence < 0 || confidence > 1) {
                        throw new IllegalArgumentException("Confidence must be between 0 and 1");
                    }
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(null, "Please enter a valid confidence value between 0 and 1!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Horse existingHorse = null;
                for (Horse h : allHorses) 
                {
                    if (h.getName().equals(horseName)) 
                    {
                        existingHorse = h;
                        break;
                    }
                }
                
                if (existingHorse != null) 
                {
                    // Update existing horse properties
                    existingHorse.setSymbol(horseSymbol);
                    existingHorse.setConfidence(confidence);
                    existingHorse.goBackToStart();
                    newHorses[i] = existingHorse;
                } 
                else 
                {
                    // Create new horse and add to history
                    Horse newHorse = new Horse(horseSymbol, horseName, confidence, trackLengthInteger);
                    newHorses[i] = newHorse;
                    allHorses.add(newHorse);
                }
            }
            
            horses = newHorses;
            
            //hide the customising panles
            horsePanel.setVisible(false);
            customisingPanel.setVisible(false);

            // this create the graphical track panel
            currentRacePanel = new RaceTrackPanel(trackShapeString, numberOfLanes, horses, trackLengthInteger);

            // this updates the race display panel
            customisingPanel.setVisible(false);
            horsePanel.setVisible(false);
            startRaceButton.setVisible(false);
            restartButton.setVisible(true);
            exitButton.setVisible(true);
            viewMetricsButton.setVisible(true);


            mainPanel.add(currentRacePanel, BorderLayout.CENTER);  // Adds the new track panel
            currentRacePanel.resetRace();

            // Refresh the race display panel
            mainPanel.revalidate();
            mainPanel.repaint();
        }
    }
    private class RestartButtonListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e) 
        {
            if (metricsPanel.isVisible()) 
            {
                metricsPanel.setVisible(false);
                viewMetricsButton.setText("Metrics");
                currentRacePanel.resumeRace(); // Ensure race resumes if paused for metrics
            }
            
            currentRacePanel.resetRace();

            mainPanel.remove(currentRacePanel);
            mainPanel.add(currentRacePanel, BorderLayout.CENTER);
            mainPanel.revalidate();
            mainPanel.repaint();
        }
    }
 
    private class ExitButtonListener implements ActionListener 
    {
        @Override
        public void actionPerformed(ActionEvent e) 
        {
            currentRacePanel.stopRace();

            metricsPanel.setVisible(false);
            viewMetricsButton.setText("Metrics");
    
            // 3. Reset button states
            customisingPanel.setVisible(true);
            horsePanel.setVisible(true);
            startRaceButton.setVisible(true);
            restartButton.setVisible(false);
            exitButton.setVisible(false);
            viewMetricsButton.setVisible(false);
            
            mainPanel.remove(currentRacePanel);
            mainPanel.revalidate();
            mainPanel.repaint();

            updateHorseInputs();
        }
    }
 
    private class ViewMetricsButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
           
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
    boolean raceFinished;

    private long startTime;
    private long endTime;
    private boolean raceStarted;
    private boolean isPaused;

    private String winningHorseName;
 
    public RaceTrackPanel(String trackShape, int laneCount, Horse[] horses, int trackLength) 
    {
        this.trackShape = trackShape;
        this.laneCount = laneCount;
        this.horses = horses;
        this.trackLength = trackLength;

        this.raceFinished = false;
        this.raceStarted = false;
        this.isPaused = false;

        // Timer to update horse positions
        raceTimer = new Timer(50, e -> {
            if (!raceStarted) 
            {
                startTime = System.currentTimeMillis();
                raceStarted = true;
            }
            updateRace();
        });
        raceTimer.start();
    }
    //calculate the race duration
    public double getRaceDuration() 
    {  
        if (!raceFinished) {
            return -1; // Indicate race isn't finished
        }
        
        long durationMillis = endTime - startTime;
        return durationMillis / 1000.0; // Convert to seconds with decimal
    }

    public void pauseRace() 
    {
        isPaused = true;
        if (raceTimer != null) 
        {
            raceTimer.stop();
        }
    }
    
    public void resumeRace() 
    {
        isPaused = false;
        if (raceTimer != null && !raceFinished) 
        {
            raceTimer.start();
        }
    }
    
    public boolean isPaused() 
    {
        return isPaused;
    }
 
    public long getStartTime() 
    {
        return startTime;
    }
 
    public void resetRace() 
    {
        // Stop the current race timer if running
        if (raceTimer != null) 
        {
            raceTimer.stop();
        }
        
        // Reset all horses' race state
        for (Horse horse : horses) 
        {
            if (horse != null) {
                horse.goBackToStart();
                horse.setFinishTime(-1);  // Reset finish time
            }
        }
        
        // Reset race state variables
        raceFinished = false;
        isPaused = false;
        winningHorseName = null;
        startTime = 0;
        endTime = 0;
        
        // Create new timer
        raceTimer = new Timer(50, e -> updateRace());
        raceTimer.start();
        
        // Repaint to show horses at starting positions
        repaint();
    }

    public void stopRace()
        {
            raceTimer.stop();
        }

    private void updateRace() {
        if (isPaused || raceFinished) return;
    
        boolean allHorsesDone = true;
        
        for (Horse horse : horses) {
            if (!horse.hasFallen() && horse.getFinishTime() == -1) {
                if (horse.getDistanceTravelled() < trackLength) {
                    horse.moveHorse(trackShape);
                    allHorsesDone = false;
                } else {
                    // Horse just finished now
                    horse.setFinishTime(System.currentTimeMillis());
                    System.out.println(horse.getName() + " finished at: " + horse.getFinishTime());
                }
            }
        }
    
        if (allHorsesDone) {
            raceFinished = true;
            raceTimer.stop();
            endTime = System.currentTimeMillis();
    
            // Determine actual winner (earliest finish time)
            Horse winner = null;
            long earliestTime = Long.MAX_VALUE;
            
            for (Horse horse : horses) {
                if (horse.getFinishTime() != -1 && horse.getFinishTime() < earliestTime) {
                    earliestTime = horse.getFinishTime();
                    winner = horse;
                }
            }
    
            // Update confidences
            for (Horse horse : horses) {
                boolean won = (winner != null) && horse.getName().equals(winner.getName());
                boolean fell = horse.hasFallen();
                horse.updateConfidenceAfterRace(won, fell);
            }
    
            // Show results
            String message = (winner != null) 
                ? "Race Over! Winner: " + winner.getName() + "!"
                : "Race Over! All horses have fallen!";
            JOptionPane.showMessageDialog(this, message);
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        int width = getWidth();
        int height = getHeight();
        
        g.setColor(Color.BLACK);
        
        if (trackShape.equals("Oval")) 
        {
            //drawing the ovals
            for (int i = 0; i < laneCount; i++) 
            {
                int ovalWidth = width - 200 - (i * 80);
                int ovalHeight = height - 100 - (i * 80);
                g.drawOval(100 + (i * 40), 50 + (i * 40), ovalWidth, ovalHeight);

                // Draw horizontal red finish line (thick and spans all lanes)
                g.setColor(Color.RED);
                g.fillRect(100, height/2+5, i*40, 5);

                //Draw horizontal green start line
                g.setColor(Color.GREEN);
                g.fillRect(100, height/2, i*40, 5);
                g.setColor(Color.BLACK);
            }

            // Draw horses
            g.setColor(Color.BLACK);
            for (int i = 0; i < laneCount; i++) 
            {
                Horse horse = horses[i];
                int x = horse.getX(width, height, trackShape, i);
                int y = horse.getY(width, height, trackShape, i);

                if (horse.hasFallen()) 
                {
                    // Drawing a red "X" for fallen horses
                    g2d.setColor(Color.RED);
                    g2d.setStroke(new BasicStroke(3)); // Line thickness
                    g2d.drawLine(x - 7, y - 9, x + 7, y + 9); // Diagonal \
                    g2d.drawLine(x + 7, y - 9, x - 7, y + 9); // Diagonal /
                } 
                else
                {
                    g.setColor(Color.BLACK);
                    g.drawString(String.valueOf(horse.getSymbol()), x, y);
                }
            }
        }
        else if (trackShape.equals("Figure-Eight")) 
        {  
            //drawing the track
            for (int i = 0; i < laneCount; i++) 
            {
                int laneOffset = i * 40;
                int resolution = 100; // Points to draw
                int[] xPoints = new int[resolution];
                int[] yPoints = new int[resolution];
                
                for (int j = 0; j < resolution; j++) 
                {
                    double t = 2 * Math.PI * j / resolution;
                    double scale = (Math.min(width, height)/2) - laneOffset;
                    xPoints[j] = (int) (width/2 + scale * Math.sin(t));
                    yPoints[j] = (int) (height/2 + (scale/1.5) * Math.sin(t) * Math.cos(t));
                }
                g2d.drawPolyline(xPoints, yPoints, resolution);

                // Drawing horizontal red finish line
                g.setColor(Color.RED);
                g.fillOval(width/2-7, height/2-6, 12, 12);
                g.setColor(Color.BLACK);
            }

            //drawing the horses
            for (int i = 0; i < laneCount; i++) 
            {
                Horse horse = horses[i];
                int x = horse.getX(width, height, trackShape, i);
                int y = horse.getY(width, height, trackShape, i);
                
                if (horse.hasFallen()) 
                {
                    // Draw X
                    g2d.setColor(Color.RED);
                    g2d.setStroke(new BasicStroke(3));
                    g2d.drawLine(x-7, y-9, x+7, y+9);
                    g2d.drawLine(x+7, y-9, x-7, y+9);
                } 
                else 
                {
                    // Draw horse symbol
                    g.setColor(Color.BLACK);
                    g.drawString(String.valueOf(horse.getSymbol()), x, y);
                }
            }
        } 
        else if (trackShape.equals("Straight")) 
        {
            // Draw straight lanes
            for (int i = 0; i < laneCount; i++) 
            {
                g.drawLine(50, 100+(i*40), width - 50, 100+(i*40));

                // Draw horizontal red finish line (thick and spans all lanes)
                g.setColor(Color.RED);
                g.fillRect(width - 50, 100, 5, i*40);

                // Draw horizontal green start line
                g.setColor(Color.GREEN);
                g.fillRect(50, 100, 5, i*40);
                g.setColor(Color.BLACK);
            }

                // Draw horses
            g.setColor(Color.BLACK);
            for (int i = 0; i < laneCount; i++) 
            {
                Horse horse = horses[i];
                int x = horse.getX(width, height, trackShape, i);
                int y = horse.getY(width, height, trackShape, i);

                if (horse.hasFallen()) 
                {
                    // Drawing a red "X" for fallen horses
                    g2d.setColor(Color.RED);
                    g2d.setStroke(new BasicStroke(3));
                    g2d.drawLine(x-7, y-9, x+7, y+9);
                    g2d.drawLine(x+7, y-9, x-7, y+9);
                } 
                else
                {
                    g.setColor(Color.BLACK);
                    g.drawString(String.valueOf(horse.getSymbol()), x, y);
                }
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
    private double distanceTravelled;
    private boolean hasFallen;
    private double horseConfidence;
    private  int trackLength;
    private boolean hasFinished = false;

    //accelereation related variables
    private double currentSpeed;
    private double acceleration;
    private double progress;

    //time
    private long finishTime = -1;  // -1 means hasn't finished



    public Horse(char horseSymbol, String horseName, double horseConfidence, int trackLength) //constructor
    {
        this.horseName = horseName;
        this.horseSymbol = horseSymbol;
        this.horseConfidence = horseConfidence;
        this.hasFallen = false;
        this.distanceTravelled = 0;
        this.trackLength = trackLength;
        this.currentSpeed = 0.0;
        this.acceleration =  0.05 + (horseConfidence * 0.01);
    }

    public void setFinishTime(long time) 
    {
        this.finishTime = time;
    }
    
    public long getFinishTime() 
    {
        return finishTime;
    }

    public boolean hasFinished() 
    {
        return hasFinished;
    }
    
    public void setFinished(boolean finished) 
    {
        this.hasFinished = finished;
    }

    public double getCurrentSpeed()
    {
        return this.currentSpeed;
    }
    

    public void updateConfidenceAfterRace(boolean wonRace, boolean fell) 
    {
        double change;
        if (wonRace) 
        {
            change = 0.1 * (1 - horseConfidence); // Bigger boost if confidence was low
        } 
        else if (fell) 
        {
            change = -0.15 * horseConfidence; // Bigger penalty if confidence was high
        } 
        else 
        {
            change = -0.05 * horseConfidence; // Small penalty for losing but not falling
        }
        
        double newConfidence = Math.max(0, Math.min(1, horseConfidence + change));
        horseConfidence = newConfidence;
    }
    
    public void moveHorse(String trackShape) 
    {
        boolean inDecelZone = false;

        // Use the progress value that was already calculated in getX/getY
        if (trackShape.equals("Oval") && (progress >= 0.4 && progress <= 0.5)) 
        {
            inDecelZone = true;
        } 
        else if (trackShape.equals("Figure-Eight") && (progress >= 0.45 && progress < 0.5 || progress >= 0.93 && progress < 0.99)) 
        {
            inDecelZone = true;
        }

        if (inDecelZone) 
        {
            currentSpeed = Math.max(currentSpeed - acceleration*2, this.horseConfidence);
        }
        else 
        {
            double targetSpeed = 1.0 + (Math.random() * 8 * this.horseConfidence);
            currentSpeed = Math.min(currentSpeed + acceleration, targetSpeed);
        }
        this.distanceTravelled += currentSpeed;

        if (Math.random() < 0.0001 * Math.exp(this.horseConfidence)) 
        {
            this.hasFallen = true;
        }
    }
    


    public int getX(int width, int height, String trackShape, int lane) 
    {
        
        if (trackShape.equals("Oval")) 
        {
            progress = this.distanceTravelled / this.trackLength;
            progress = progress % 1.0; // Keep within 0-1 range
            int ovalWidth = width - 200;
            int ovalHeight = height - 100;
            int centerX = width / 2;
            int centerY = height / 2;
            int laneOffset = 40 * lane;
            
            // Calculate angle based on progress (0-2π)
            double angle = progress * 2 * Math.PI + Math.PI;
            
            // Calculate position on oval (squashed circle)
            return centerX + (int)((ovalWidth/2 - laneOffset) * Math.cos(angle));
        }
        else if (trackShape.equals("Straight")) 
        {
            progress = this.distanceTravelled / this.trackLength;
            progress = progress % 1.0; 
            // Simple linear movement for straight track
            return 50 + (int)((width - 100) * progress);
        }
        else if(trackShape.equals("Figure-Eight"))
        {
            progress = (this.distanceTravelled / trackLength) % 1.0;
            double angle = progress * 2 * Math.PI;

            int laneOffset = lane * 40;
            double scale = (Math.min(width, height) / 2) - laneOffset;
            
            return (int) (width/2 + scale * Math.sin(angle));
        }        
        return 0;
    }
    
    public int getY(int width, int height, String trackShape, int lane) 
    {
        
        if (trackShape.equals("Oval")) 
        {
            progress = this.distanceTravelled / this.trackLength;
            progress = progress % 1.0;

            int ovalWidth = width - 200;
            int ovalHeight = height - 100;
            int centerX = width / 2;
            int centerY = height / 2;
            int laneOffset = 40 * lane;
            
            double angle = progress * 2 * Math.PI + Math.PI;
            
            return centerY + (int)((ovalHeight/2 - laneOffset) * Math.sin(angle));
        }
        else if (trackShape.equals("Straight")) 
        {
            progress = this.distanceTravelled / this.trackLength;
            progress = progress % 1.0;
            // Fixed Y position for each lane
            return 100 + (lane * 40);
        }  
        else if (trackShape.equals("Figure-Eight")) 
        {
            progress = (this.distanceTravelled / trackLength) % 1.0;
            double angle = progress * 2 * Math.PI + Math.PI/2;

            int laneOffset = lane * 40;
            double scale = (Math.min(width, height) / 2) - laneOffset;
            
            return (int) (height/2 + (scale/1.5) * Math.sin(angle) * Math.cos(angle));
        }
    
        return 0;
    }

    public double getConfidence() //returns the confidence of the horse
    {
        return this.horseConfidence;
    }

    public double getDistanceTravelled() 
    {
        return this.distanceTravelled;
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
        this.distanceTravelled = 0;
        this.currentSpeed = 0;
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
  
  
class HorseRaceSimultionGUI
{
    public static void main(String[] args) //main method
    {   
        StartRaceGUI newRace = new StartRaceGUI();
        newRace.setVisible(true);
    }
}