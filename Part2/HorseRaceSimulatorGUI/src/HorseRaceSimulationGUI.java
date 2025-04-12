/**
 * This class stores personal information about each horse object. 
 * 
 * @author Balazs Mano Lovasz 
 * @version 1
 */

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;


class StartRaceGUI extends JFrame 
{
    // Constants
    private static final String DATA_DIR = System.getProperty("user.home") + "/HorseRaceData";
    private static final String CSV_FILE = DATA_DIR + "/horse_data.csv";
    private static final String CONFIDENCE_HISTORY_FILE = DATA_DIR + "/confidence_history.csv";
    private static final String TRACK_RECORDS_FILE = DATA_DIR + "/track_records.csv";
    

    // Main Panels
    private JPanel customisingPanel, mainPanel, horsePanel;
    private JPanel metricsPanel;
    private JTabbedPane metricsTabs;

    // Race Configuration Components
    private JComboBox<String> laneCountList, weatherCondition, trackShape, trackLengthList;
    private JComboBox<String>[] horseColors = new JComboBox[5];
    private JComboBox<String>[] horseBreeds = new JComboBox[5];
    private JComboBox<String>[] horseshoeTypes = new JComboBox[5];
    private JComboBox<String>[] horseSaddles = new JComboBox[5];
    private int trackLengthInteger;
    private int numberOfLanes;
    private String trackShapeString;
    private String weatherConditionString;

    // Horse Management
    private Horse[] horses = new Horse[5]; // Max 5 horses
    private ArrayList<Horse> allHorses = new ArrayList<>(); // Stores all horses that have raced
    private JTextField[] horseNames = new JTextField[5];
    private JTextField[] horseSymbols = new JTextField[5];
    private JTextField[] horseConfidences = new JTextField[5];

    // Buttons
    private JButton readyButton, restartButton, exitButton, viewMetricsButton, placeBetsButton, startRaceButton;
    private RaceTrackPanel currentRacePanel;

    public StartRaceGUI()
    {
        setupDataDirectory();
        setupWindow();
        setupMainPanels();
        setupCustomisingPanel();
        setupHorsePanel();
        setupMetricsPanel();
        setupButtons();
        // Set default track length before loading horses
        trackLengthInteger = Integer.parseInt((String) trackLengthList.getSelectedItem());
        loadHorsesFromCSV();
        horses = allHorses.toArray(new Horse[0]);
        setupFonts();
        updateHorseInputs();
    }

    private void setupDataDirectory() 
    {
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists() && !dataDir.mkdirs()) 
        {
            JOptionPane.showMessageDialog(this, 
                "Could not create data directory at: " + DATA_DIR,
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupWindow() 
    {
        setTitle("Horse Race Simulation");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    private void setupMainPanels() 
    {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.LIGHT_GRAY);
        add(mainPanel, BorderLayout.CENTER);

        customisingPanel = new JPanel();
        customisingPanel.setLayout(new GridLayout(5, 2, 10, 10));
        add(customisingPanel, BorderLayout.NORTH);
    }

    public void setCurrentRacePanel(RaceTrackPanel panel) 
    {
        this.currentRacePanel = panel;
    }

    private void setupCustomisingPanel() 
    {
        // Track Length Selection
        customisingPanel.add(new JLabel("Track Length (metres):"));
        String[] trackLengths = {"200", "400", "600", "800", "1000"};
        trackLengthList = new JComboBox<>(trackLengths);
        customisingPanel.add(trackLengthList);

        // Lane Count Selection
        customisingPanel.add(new JLabel("Number of Lanes:"));
        laneCountList = new JComboBox<>(new String[]{"2","3","4","5"});
        customisingPanel.add(laneCountList);

        // Weather Selection
        customisingPanel.add(new JLabel("Weather Condition:"));
        weatherCondition = new JComboBox<>(new String[]{"Dry","Muddy","Icy"});
        customisingPanel.add(weatherCondition);

        // Track Shape Selection
        customisingPanel.add(new JLabel("Track Shape:"));
        trackShape = new JComboBox<>(new String[]{"Oval","Figure-Eight","Straight"});
        customisingPanel.add(trackShape);
    }

    private void setupHorsePanel() 
    {
        horsePanel = new JPanel(new GridLayout(0, 2, 2, 2));
        laneCountList.addActionListener(e -> updateHorseInputs());
    }

    private void setupMetricsPanel() 
    {
        metricsPanel = new JPanel(new BorderLayout());
        metricsTabs = new JTabbedPane();
        metricsPanel.add(metricsTabs, BorderLayout.CENTER);
        metricsPanel.setVisible(false);

        // Create a panel that will contain both horse panel and metrics
        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.add(horsePanel, BorderLayout.NORTH);
        containerPanel.add(metricsPanel, BorderLayout.CENTER);
        
        // Set explicit sizes
        int metricsHeight = 400;  // Adjust this value as needed
        metricsPanel.setPreferredSize(new Dimension(getWidth(), metricsHeight));
        metricsPanel.setMinimumSize(new Dimension(getWidth(), metricsHeight));
        
        // Add the container panel to the main frame
        add(containerPanel, BorderLayout.SOUTH);
    }

    private void setupButtons() 
    {
        // Start Race Button
        readyButton = createButton("Ready", new Color(50, 150, 250), 16, 200);
        readyButton.addActionListener(new ReadyButtonListener());

        // Exit Button
        exitButton = createButton("Exit Race", new Color(200, 100, 100), 14, 100);
        exitButton.addActionListener(new ExitButtonListener());
        exitButton.setVisible(false);

        // Restart Button
        restartButton = createButton("Restart", new Color(100, 200, 100), 14, 100);
        restartButton.addActionListener(new RestartButtonListener());
        restartButton.setVisible(false);

        // Metrics Button
        viewMetricsButton = createButton("Metrics", new Color(128, 128, 128), 14, 100);
        viewMetricsButton.addActionListener(new ViewMetricsButtonListener());
        viewMetricsButton.setVisible(false);

        //startRaceButton
        startRaceButton = createButton("Start Race", new Color(50, 205, 50), 14, 100);
        startRaceButton.addActionListener(new StartRaceButtonListener());
        startRaceButton.setVisible(false);

        //placeBetsButton
        placeBetsButton = createButton("Place Bets", new Color(200, 80, 180), 14, 100);
        placeBetsButton.addActionListener(new PlaceBetsButtonListener());
        placeBetsButton.setVisible(false);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.add(readyButton);
        buttonPanel.add(exitButton);
        buttonPanel.add(viewMetricsButton);        
        buttonPanel.add(restartButton);
        buttonPanel.add(placeBetsButton);
        buttonPanel.add(startRaceButton);
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
    }

    private JButton createButton(String text, Color bgColor, int fontSize, int width) 
    {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, fontSize));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(width, 30));
        button.setMargin(new Insets(2, 5, 2, 5));
        return button;
    }

    private void setupFonts() 
    {
        Font labelFont = new Font("Arial", Font.PLAIN, 14);
        for (Component comp : customisingPanel.getComponents()) 
        {
            if (comp instanceof JLabel) 
            {
                comp.setFont(labelFont);
            }
        }
    }

    //this method updates the number of horses that show up on the screen
    private void updateHorseInputs() 
    {
        horsePanel.removeAll();
        
        // Use a GridLayout with 1 row and 0 columns (it will create as many columns as needed)
        horsePanel.setLayout(new GridLayout(1, 0, 10, 5));

        // Create header panel
        JPanel headerPanel = new JPanel(new GridLayout(0, 1, 0, 5));
        headerPanel.add(new JLabel("Name"));
        headerPanel.add(new JLabel("Breed"));
        headerPanel.add(new JLabel("Color"));
        headerPanel.add(new JLabel("Horseshoes"));
        headerPanel.add(new JLabel("Saddle"));
        headerPanel.add(new JLabel("Symbol"));
        headerPanel.add(new JLabel("Confidence"));
        horsePanel.add(headerPanel);

        int numLanes = Integer.parseInt((String) laneCountList.getSelectedItem());

        for (int i = 0; i < numLanes; i++) 
        {
            JPanel columnPanel = new JPanel(new GridLayout(0, 1, 0, 5));

            // Name field
            horseNames[i] = new JTextField(horses[i] != null ? horses[i].getName() : "Horse" + (i + 1));
            columnPanel.add(horseNames[i]);

            // Breed combo box
            horseBreeds[i] = new JComboBox<>(new String[]{
                HorseBreed.THOROUGHBRED,
                HorseBreed.ARABIAN,
                HorseBreed.QUARTER_HORSE,
                HorseBreed.CLYDESDALE,
                HorseBreed.APPALOOSA
            });
            columnPanel.add(horseBreeds[i]);

            // Color combo box
            horseColors[i] = new JComboBox<>(new String[]{
                CoatColor.BAY,
                CoatColor.BLACK,
                CoatColor.CHESTNUT,
                CoatColor.WHITE,
                CoatColor.GREY,
                CoatColor.PALOMINO,
                CoatColor.PINTO
            });
            columnPanel.add(horseColors[i]);

            // Horseshoe type combo box
            horseshoeTypes[i] = new JComboBox<>(new String[]{"Standard", "Grip"});
            columnPanel.add(horseshoeTypes[i]);

            horseSaddles[i] = new JComboBox<>(new String[]{"Racing Style", "Western Style"});
            columnPanel.add(horseSaddles[i]);

            // Symbol field (read-only)
            horseSymbols[i] = new JTextField();
            horseSymbols[i].setEditable(false);
            horseSymbols[i].setBackground(Color.LIGHT_GRAY);
            columnPanel.add(horseSymbols[i]);

            // Confidence field (read-only)
            horseConfidences[i] = new JTextField();
            horseConfidences[i].setEditable(false);
            horseConfidences[i].setBackground(Color.LIGHT_GRAY);
            columnPanel.add(horseConfidences[i]);

            horsePanel.add(columnPanel);

            // Set initial values for existing horses
            if (i < horses.length && horses[i] != null) 
            {
                horseConfidences[i].setText(String.format("%.2f", horses[i].getConfidence()));
                horseBreeds[i].setSelectedItem(horses[i].getBreed().getBreedName());
                horseColors[i].setSelectedItem(horses[i].getCoatColor().getColorName());
                horseshoeTypes[i].setSelectedItem(horses[i].getHorseshoeType());
                horseSaddles[i].setSelectedItem(horses[i].getSaddleType());
                updateSymbolFromSelections(i);
            }

            final int index = i;
            
            // Add listeners
            Runnable updateFields = () -> 
            {
                String horseName = horseNames[index].getText().trim();
                Horse foundHorse = null;
                for (Horse h : allHorses) 
                {
                    if (h.getName().equals(horseName)) 
                    {
                        foundHorse = h;
                        break;
                    }
                }
                
                if (foundHorse != null) 
                {
                    horseConfidences[index].setText(String.format("%.2f", foundHorse.getConfidence()));
                    
                    String breedName = foundHorse.getBreed().getBreedName();
                    String colorName = foundHorse.getCoatColor().getColorName();
                    String horseshoeType = foundHorse.getHorseshoeType();
                    String saddleType = foundHorse.getSaddleType();
                    
                    horseBreeds[index].setSelectedItem(breedName);
                    horseColors[index].setSelectedItem(colorName);
                    horseshoeTypes[index].setSelectedItem(horseshoeType);
                    horseSaddles[index].setSelectedItem(saddleType);
                    updateSymbolFromSelections(index);
                }
                
            };

            ActionListener symbolUpdater = e -> updateSymbolFromSelections(index);
            
            horseBreeds[index].addActionListener(symbolUpdater);
            horseshoeTypes[index].addActionListener(symbolUpdater);
            horseSaddles[index].addActionListener(symbolUpdater);

            horseNames[index].addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    updateFields.run();
                }
            });

            horseNames[index].addActionListener(e -> updateFields.run());
            updateFields.run();
        }

        horsePanel.revalidate();
        horsePanel.repaint();
        trackShapeString = (String) trackShape.getSelectedItem();
        numberOfLanes = Integer.parseInt((String) laneCountList.getSelectedItem());
    }
    
    private void updateSymbolFromSelections(int index)
    {
        String selectedBreed = (String)horseBreeds[index].getSelectedItem();
        String selectedHorseshoe = (String)horseshoeTypes[index].getSelectedItem();
        HorseBreed breed = new HorseBreed(selectedBreed);
        String symbol = breed.getBreedSymbol() + "-" + (selectedHorseshoe.equals("Grip") ? "G" : "S");
        horseSymbols[index].setText(symbol);
    }

    // Action Listener for "Ready" button
    private class ReadyButtonListener implements ActionListener 
    {
        @Override
        public void actionPerformed(ActionEvent e) 
        {
            metricsPanel.setVisible(false);
            viewMetricsButton.setText("Metrics");

            trackLengthInteger = 0;
            try
            {
                trackLengthInteger = Integer.parseInt((String) trackLengthList.getSelectedItem());                
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
            
            Horse[] newHorses = new Horse[numberOfLanes];

            for (int i = 0; i < numberOfLanes; i++) 
            {
                String horseName = horseNames[i].getText();
                double confidence = 0.5;  // default confidence
                String selectedBreed = (String) horseBreeds[i].getSelectedItem();
                String selectedColor = (String) horseColors[i].getSelectedItem();
                String selectedHorseshoe = (String) horseshoeTypes[i].getSelectedItem();
                String selectedSaddle = (String) horseSaddles[i].getSelectedItem();

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
                    existingHorse.goBackToStart();
                    // Update the horse's attributes
                    Horse updatedHorse = new Horse(horseName, confidence, trackLengthInteger, selectedBreed, selectedHorseshoe, selectedColor, selectedSaddle);
                    updatedHorse.setWins(existingHorse.getWins());
                    updatedHorse.setLosses(existingHorse.getLosses());
                    updatedHorse.setFalls(existingHorse.getFalls());
                    newHorses[i] = updatedHorse;
                    
                    // Replace the old horse in allHorses
                    allHorses.remove(existingHorse);
                    allHorses.add(updatedHorse);
                } 
                else 
                {
                    // Create new horse
                    Horse newHorse = new Horse(horseName, confidence, trackLengthInteger, selectedBreed, selectedHorseshoe, selectedColor, selectedSaddle);
                    newHorses[i] = newHorse;
                    allHorses.add(newHorse);
                }
            }
            
            horses = newHorses;

            // this updates the race display panel
            readyButton.setVisible(false);
            customisingPanel.setVisible(false);
            horsePanel.setVisible(false);
            exitButton.setVisible(true);
            viewMetricsButton.setVisible(true);
            placeBetsButton.setVisible(true);
            startRaceButton.setVisible(true);
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
            if (currentRacePanel != null) 
            {
                currentRacePanel.stopRace();
                mainPanel.remove(currentRacePanel);
            }
    
            metricsPanel.setVisible(false);
            viewMetricsButton.setText("Metrics");
    
            // Reseting button states
            customisingPanel.setVisible(true);
            horsePanel.setVisible(true);
            readyButton.setVisible(true);
            restartButton.setVisible(false);
            exitButton.setVisible(false);
            viewMetricsButton.setVisible(false);
            startRaceButton.setVisible(false);
            placeBetsButton.setVisible(false);
            
            mainPanel.revalidate();
            mainPanel.repaint();
    
            updateHorseInputs();
        }
    }

    private class ViewMetricsButtonListener implements ActionListener 
    {
        @Override
        public void actionPerformed(ActionEvent e) 
        {
            if (metricsPanel.isVisible()) 
            {
                metricsPanel.setVisible(false);
                viewMetricsButton.setText("Metrics");
                if (currentRacePanel != null) 
                {
                    currentRacePanel.resumeRace();
                }
            } 
            else 
            {
                if (currentRacePanel != null) 
                {
                    currentRacePanel.pauseRace();
                }
                
                // Clear existing tabs
                metricsTabs.removeAll();
                
                // Create tabs for each horse
                for (Horse horse : allHorses) 
                {
                    // Main tab panel with sub-tabs
                    JTabbedPane horseSubTabs = new JTabbedPane();
                    
                    // 1. Basic Information Tab
                    JPanel basicInfoPanel = new JPanel(new GridLayout(0, 2, 5, 5));
                    basicInfoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                    
                    basicInfoPanel.add(new JLabel("Name:"));
                    basicInfoPanel.add(new JLabel(horse.getName()));
                    
                    basicInfoPanel.add(new JLabel("Symbol:"));
                    basicInfoPanel.add(new JLabel(String.valueOf(horse.getSymbol())));
                    
                    basicInfoPanel.add(new JLabel("Current Confidence:"));
                    basicInfoPanel.add(new JLabel(String.format("%.2f", horse.getConfidence())));
                    
                    horseSubTabs.addTab("Basic Info", basicInfoPanel);
                        
                    // 2. Performance Metrics Tab
                    JPanel performancePanel = new JPanel(new GridLayout(0, 2, 5, 5));
                    performancePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                    
                    // Current Statistics
                    performancePanel.add(new JLabel("Total Races:"));
                    performancePanel.add(new JLabel(String.valueOf(horse.getWins() + horse.getLosses())));
                    
                    performancePanel.add(new JLabel("Wins:"));
                    performancePanel.add(new JLabel(String.valueOf(horse.getWins())));
                    
                    performancePanel.add(new JLabel("Losses:"));
                    performancePanel.add(new JLabel(String.valueOf(horse.getLosses())));
                    
                    performancePanel.add(new JLabel("Falls:"));
                    performancePanel.add(new JLabel(String.valueOf(horse.getFalls())));
                    
                    // Calculate win percentage
                    int totalRaces = horse.getWins() + horse.getLosses();
                    double winPercentage = totalRaces > 0 ? (double) horse.getWins() / totalRaces * 100 : 0;

                    //Calculate average speed
                    double raceTime = 0.0;
                    String timeDisplay = "Not started";
                    double averageSpeed = 0.0;

                    if (currentRacePanel != null) 
                    {
                        raceTime = currentRacePanel.getHorseRaceTime(horse);
                        timeDisplay = raceTime > 0 ? String.format("%.2f seconds", raceTime) : "Did not finish";
                        if (raceTime > 0) 
                        {
                            averageSpeed = trackLengthInteger / raceTime;
                        }
                    }

                    performancePanel.add(new JLabel("Race Time:"));
                    performancePanel.add(new JLabel(timeDisplay));

                    //Win percentage
                    performancePanel.add(new JLabel("Win Percentage:"));
                    performancePanel.add(new JLabel(String.format("%.1f%%", winPercentage)));

                    // Average speed
                    performancePanel.add(new JLabel("Average Speed:"));
                    performancePanel.add(new JLabel(String.format("%.2f m/s", averageSpeed)));
                    
                    //Status
                    String status = horse.getFinishTime()>0 ? "FINISHED" : horse.hasFallen() ? "FALLEN" : "RACING";
                    performancePanel.add(new JLabel("Status:"));
                    performancePanel.add(new JLabel(status));
                    
                    horseSubTabs.addTab("Performance", performancePanel);
                        
                        // 3. Race History Tab
                    JPanel historyPanel = new JPanel(new BorderLayout());
                    historyPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                    
                    // Table for race history
                    String[] columnNames = {"Date", "Track", "Position", "Time", "Speed", "Conf Before", "Conf After", "Change"};
                    Object[][] data = loadConfidenceHistory(horse.getName());
                    JTable historyTable = new JTable(data, columnNames);
                    historyTable.setFillsViewportHeight(true);
                    
                    JScrollPane scrollPane = new JScrollPane(historyTable);
                    historyPanel.add(scrollPane, BorderLayout.CENTER);
                    
                    horseSubTabs.addTab("Race History", historyPanel);
                    
                    // 4. Track Records Tab
                    JPanel trackRecordsPanel = new JPanel(new BorderLayout());
                    trackRecordsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                    
                    // Table for track records
                    String[] trackColumns = {"Track Type", "Condition", "Best Time", "Races Completed"};
                    Object[][] trackData = loadTrackRecords(); 
                    JTable trackTable = new JTable(trackData, trackColumns);
                    trackTable.setFillsViewportHeight(true);
                    
                    JScrollPane trackScrollPane = new JScrollPane(trackTable);
                    trackRecordsPanel.add(trackScrollPane, BorderLayout.CENTER);
                    
                    horseSubTabs.addTab("Track Records", trackRecordsPanel);

                    // 5. Trends Tab - Initial Setup
                    JPanel trendsPanel = new JPanel(new BorderLayout());
                    trendsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                    // Create sections for different trends
                    JPanel trendStatsPanel = new JPanel(new GridLayout(0, 2, 5, 5));
                    trendStatsPanel.setBorder(BorderFactory.createTitledBorder("Performance Trends"));

                        // Recent Performance
                    trendStatsPanel.add(new JLabel("Recent Performance:"));
                    String trend = horse.getWins() > horse.getLosses() ? "Improving" : "Needs Improvement";
                    trendStatsPanel.add(new JLabel(trend));

                    // Confidence Trend
                    trendStatsPanel.add(new JLabel("Confidence Trend:"));
                    String confidenceTrend = horse.getConfidence() >= 0.5 ? "Strong" : "Building";
                    trendStatsPanel.add(new JLabel(confidenceTrend));

                    // Fall Rate
                    trendStatsPanel.add(new JLabel("Fall Rate:"));
                    trendStatsPanel.add(new JLabel(String.format("%.1f%%", (double)horse.getFalls() / totalRaces * 100)));

                    // Success by Track Type && weather performance
                    Object[][] historyData = loadConfidenceHistory(horse.getName());
                    Map<String, Integer> trackWins = new HashMap<>();
                    Map<String, Integer> trackRaces = new HashMap<>();
                    Map<String, Integer> weatherWins = new HashMap<>();
                    Map<String, Integer> weatherRaces = new HashMap<>();

                    // Analysis of the history data
                    for (Object[] raceData : historyData) 
                    {
                        String trackType = (String)raceData[1];  // Track type is in column 1
                        int position = (int)raceData[2];         // Position is in column 2
                        
                        // Track type analysis
                        trackRaces.put(trackType, trackRaces.getOrDefault(trackType, 0) + 1);
                        if (position == 1) 
                        {
                            trackWins.put(trackType, trackWins.getOrDefault(trackType, 0) + 1);
                        }
                        
                        // Get weather condition from track records
                        Object[][] trackRecords = loadTrackRecords();
                        for (Object[] record : trackRecords) 
                        {
                            if (record[0].equals(trackType)) 
                            {
                                String weather = (String)record[1];  // Weather condition
                                weatherRaces.put(weather, weatherRaces.getOrDefault(weather, 0) + 1);
                                if (position == 1) 
                                {
                                    weatherWins.put(weather, weatherWins.getOrDefault(weather, 0) + 1);
                                }
                                break;
                            }
                        }
                    }

                    // Find preferred track (highest win percentage)
                    String preferredTrack = "No races yet";
                    double bestTrackRate = 0;
                    for (String track : trackRaces.keySet()) 
                    {
                        int races = trackRaces.get(track);
                        int wins = trackWins.getOrDefault(track, 0);
                        double winRate = (double)wins / races;
                        if (winRate > bestTrackRate) 
                        {
                            bestTrackRate = winRate;
                            preferredTrack = track + " (" + String.format("%.1f%%", winRate * 100) + " wins)";
                        }
                    }

                    // Find best weather condition
                    String bestWeather = "No races yet";
                    double bestWeatherRate = 0;
                    for (String weather : weatherRaces.keySet()) 
                    {
                        int races = weatherRaces.get(weather);
                        int wins = weatherWins.getOrDefault(weather, 0);
                        double winRate = (double)wins / races;
                        if (winRate > bestWeatherRate) 
                        {
                            bestWeatherRate = winRate;
                            bestWeather = weather + " (" + String.format("%.1f%%", winRate * 100) + " wins)";
                        }
                    }

                    // Update the labels
                    trendStatsPanel.add(new JLabel("Preferred Track:"));
                    trendStatsPanel.add(new JLabel(preferredTrack));

                    trendStatsPanel.add(new JLabel("Best Weather Condition:"));
                    trendStatsPanel.add(new JLabel(bestWeather));

                    trendsPanel.add(trendStatsPanel, BorderLayout.NORTH);

                        // Create a panel for all graphs
                    JPanel allGraphsPanel = new JPanel(new GridLayout(2, 2, 10, 10));  // 2x2 grid with gaps

                    // 1. Position Graph
                    JPanel positionGraphPanel = new JPanel() 
                    {
                        @Override
                        protected void paintComponent(Graphics g) 
                        {
                            super.paintComponent(g);
                            int width = getWidth();
                            int height = getHeight();
                            
                            // Drawing the axes
                            g.setColor(Color.BLACK);
                            g.drawLine(70, height-50, width-50, height-50);  // X axis
                            g.drawLine(70, height-50, 70, 50);  // Y axis
                            
                            // Getting race history data
                            Object[][] historyData = loadConfidenceHistory(horse.getName());
                            if (historyData.length > 0) 
                            {
                                int maxEntries = Math.min(historyData.length, 10);  // Shows last 10 races
                                int barWidth = (width-120) / maxEntries;
                                
                                // Draw bars for each race
                                for (int i = 0; i < maxEntries; i++) 
                                {
                                    int index = historyData.length - maxEntries + i;
                                    if (index >= 0) 
                                    {
                                        int position = (int)historyData[index][2];
                                        int barHeight = (height-100) * (6-position) / 5;
                                        
                                        g.setColor(position == 1 ? Color.GREEN : Color.RED);
                                        g.fillRect(70 + i*barWidth, height-50-barHeight, barWidth-2, barHeight);
                                    }
                                }
                                
                                g.setColor(Color.BLACK);
                                g.drawString("Recent Races →", width/2-30, height-20);
                                g.drawString("Position", 15, height/2);
                            } 
                            else 
                            {
                                g.drawString("No race history available", width/2-50, height/2);
                            }
                        }
                    };
                    positionGraphPanel.setPreferredSize(new Dimension(400, 200));
                    positionGraphPanel.setBorder(BorderFactory.createTitledBorder("Race Positions (Last 10 Races)"));

                    // 2. Confidence Graph
                    JPanel confidenceGraphPanel = new JPanel() 
                    {
                        @Override
                        protected void paintComponent(Graphics g) 
                        {
                            super.paintComponent(g);
                            int width = getWidth();
                            int height = getHeight();
                            
                            // Drawing the axes with increased left margin
                            g.setColor(Color.BLACK);
                            g.drawLine(70, height-50, width-50, height-50);  // X axis
                            g.drawLine(70, height-50, 70, 50);  // Y axis
                            
                            Object[][] historyData = loadConfidenceHistory(horse.getName());
                            if (historyData.length > 1)  // Changed from > 0 to > 1 since we need at least 2 points
                            {
                                int maxEntries = Math.min(historyData.length, 10);
                                int xStep = (width-120) / Math.max(maxEntries-1, 1);  // Prevent division by zero
                                
                                // Draw confidence line
                                g.setColor(Color.BLUE);
                                for (int i = 0; i < maxEntries-1; i++) 
                                {
                                    int index = historyData.length - maxEntries + i;
                                    if (index >= 0 && index+1 < historyData.length) 
                                    {
                                        double conf1 = (double)historyData[index][5];
                                        double conf2 = (double)historyData[index+1][5];
                                        
                                        int x1 = 70 + i*xStep;
                                        int x2 = 70 + (i+1)*xStep;
                                        int y1 = height-50 - (int)((height-100) * conf1);
                                        int y2 = height-50 - (int)((height-100) * conf2);
                                        
                                        g.drawLine(x1, y1, x2, y2);
                                    }
                                }
                                
                                g.setColor(Color.BLACK);
                                g.drawString("Races →", width/2-30, height-20);
                                g.drawString("Confidence", 5, height/2);
                            } 
                            else if (historyData.length == 1) 
                            {
                                // Draw single point if only one data point exists
                                g.setColor(Color.BLUE);
                                double conf = (double)historyData[0][5];
                                int y = height-50 - (int)((height-100) * conf);
                                g.fillOval(width/2-3, y-3, 6, 6);  // Draw a dot
                                
                                g.setColor(Color.BLACK);
                                g.drawString("Races →", width/2-30, height-20);
                                g.drawString("Confidence",5, height/2);
                            }
                            else 
                            {
                                g.drawString("No confidence history available", width/2-50, height/2);
                            }
                        }
                    };
                    confidenceGraphPanel.setPreferredSize(new Dimension(400, 200));
                    confidenceGraphPanel.setBorder(BorderFactory.createTitledBorder("Confidence Trend"));

                    // 3. Win Rate Graph
                    JPanel winRateGraphPanel = new JPanel() 
                    {
                        @Override
                        protected void paintComponent(Graphics g) 
                        {
                            super.paintComponent(g);
                            int width = getWidth();
                            int height = getHeight();
                            
                            // Drawing the axes
                            g.setColor(Color.BLACK);
                            g.drawLine(70, height-50, width-50, height-50);  // X axis
                            g.drawLine(70, height-50, 70, 50);  // Y axis
                            
                            Object[][] historyData = loadConfidenceHistory(horse.getName());
                            if (historyData.length > 0) 
                            {
                                int maxEntries = Math.min(historyData.length, 10);
                                int xStep = (width-120) / maxEntries;
                                
                                int wins = 0;
                                int total = 0;
                                
                                // Draw win rate bars
                                for (int i = 0; i < maxEntries; i++) 
                                {
                                    int index = historyData.length - maxEntries + i;
                                    if (index >= 0) 
                                    {
                                        total++;
                                        if ((int)historyData[index][2] == 1) 
                                        {  // Position is 1 (win)
                                            wins++;
                                        }
                                        
                                        double winRate = (double)wins / total;
                                        int barHeight = (int)((height-100) * winRate);
                                        
                                        g.setColor(new Color(0, 150, 0));  // Dark green
                                        g.fillRect(70 + i*xStep, height-50-barHeight, xStep-2, barHeight);
                                    }
                                }
                                
                                g.setColor(Color.BLACK);
                                g.drawString("Races →", width/2-30, height-20);
                                g.drawString("Win Rate", 15, height/2);
                            } 
                            else 
                            {
                                g.drawString("No race history available", width/2-50, height/2);
                            }
                        }
                    };
                    winRateGraphPanel.setPreferredSize(new Dimension(400, 200));
                    winRateGraphPanel.setBorder(BorderFactory.createTitledBorder("Win Rate Progress"));

                    // 4. Track Performance Graph
                    JPanel trackPerformanceGraphPanel = new JPanel() 
                    {
                        @Override
                        protected void paintComponent(Graphics g) 
                        {
                            super.paintComponent(g);
                            int width = getWidth();
                            int height = getHeight();
                            
                            // Drawing the axes with increased left margin
                            g.setColor(Color.BLACK);
                            g.drawLine(70, height-50, width-50, height-50);  // X axis
                            g.drawLine(70, height-50, 70, 50);  // Y axis
                            
                            Map<String, Double> trackWinRates = new HashMap<>();
                            for (String track : trackRaces.keySet()) 
                            {
                                int races = trackRaces.get(track);
                                int wins = trackWins.getOrDefault(track, 0);
                                trackWinRates.put(track, (double)wins / races);
                            }

                            // Fixed colors for each track type
                            Map<String, Color> trackColors = new HashMap<>();
                            trackColors.put("Oval", new Color(65, 105, 225));      
                            trackColors.put("Straight", new Color(34, 139, 34));   
                            trackColors.put("Figure-Eight", new Color(220, 20, 60)); 
                            trackColors.put("Zig-Zag", new Color(148, 0, 211));    
                            
                            if (!trackWinRates.isEmpty()) 
                            {
                                int barWidth = (width-120) / trackWinRates.size();  // Adjusted for new margin
                                int i = 0;
                                
                                for (Map.Entry<String, Double> entry : trackWinRates.entrySet()) 
                                {
                                    int barHeight = (int)((height-100) * entry.getValue());
                                    
                                    g.setColor(trackColors.getOrDefault(entry.getKey(), new Color(128, 128, 128)));
                                    g.fillRect(70 + i*barWidth, height-50-barHeight, barWidth-2, barHeight);
                                    
                                    g.setColor(Color.BLACK);
                                    g.drawString(entry.getKey(), 70 + i*barWidth, height-35);
                                    
                                    i++;
                                }
                                
                                g.setColor(Color.BLACK);
                                g.drawString("Track Types", width/2-30, height-10);
                                g.drawString("Win Rate", 15, height/2);
                            } 
                            else 
                            {
                                g.drawString("No track performance data available", width/2-50, height/2);
                            }
                        }
                    };
                    trackPerformanceGraphPanel.setPreferredSize(new Dimension(400, 200));
                    trackPerformanceGraphPanel.setBorder(BorderFactory.createTitledBorder("Track Type Performance"));

                    // Add all graphs to the panel
                    allGraphsPanel.add(positionGraphPanel);
                    allGraphsPanel.add(confidenceGraphPanel);
                    allGraphsPanel.add(winRateGraphPanel);
                    allGraphsPanel.add(trackPerformanceGraphPanel);

                    // Add components to the trends panel
                    trendsPanel.add(trendStatsPanel, BorderLayout.NORTH);
                    trendsPanel.add(allGraphsPanel, BorderLayout.CENTER);

                    // Wrap the trendsPanel in a JScrollPane
                    JScrollPane trendsScrollPane = new JScrollPane(trendsPanel);
                    trendsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                    trendsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

                    // Add the Trends tab with the scroll pane
                    horseSubTabs.addTab("Trends", trendsScrollPane);

                    // Add this horse's complete tab set to the main metrics tabs
                    metricsTabs.addTab(horse.getName(), horseSubTabs);
                    
                    raceTime = 0.0;
                    timeDisplay = "Not started";
                    averageSpeed = 0.0;

                    if (currentRacePanel != null) {
                        raceTime = currentRacePanel.getHorseRaceTime(horse);
                        timeDisplay = raceTime > 0 ? String.format("%.2f seconds", raceTime) : "Did not finish";
                        if (raceTime > 0) {
                            averageSpeed = trackLengthInteger / raceTime;
                        }
                    }

                    performancePanel.add(new JLabel("Race Time:"));
                    performancePanel.add(new JLabel(timeDisplay));
                }

                metricsPanel.setVisible(true);
                viewMetricsButton.setText("Hide Metrics");


                //Horse Comparison Tab
                JPanel comparisonPanel = new JPanel(new BorderLayout());
                comparisonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                // Horse selection panel
                JPanel selectionPanel = new JPanel(new FlowLayout());
                JComboBox<String> horse1Selector = new JComboBox<>();
                JComboBox<String> horse2Selector = new JComboBox<>();

                // Populate horse selectors
                for (Horse horse : allHorses) 
                {
                    horse1Selector.addItem(horse.getName());
                    horse2Selector.addItem(horse.getName());
                }

                JButton compareButton = new JButton("Compare Horses");
                compareButton.setBackground(new Color(70, 130, 180));
                compareButton.setForeground(Color.WHITE);
                compareButton.setFont(new Font("Arial", Font.BOLD, 14));

                selectionPanel.add(new JLabel("Horse 1: "));
                selectionPanel.add(horse1Selector);
                selectionPanel.add(new JLabel("Horse 2: "));
                selectionPanel.add(horse2Selector);
                selectionPanel.add(compareButton);

                // Results panel with grid layout
                JPanel resultsPanel = new JPanel(new GridLayout(0, 3, 10, 5));
                resultsPanel.setBorder(BorderFactory.createTitledBorder("Performance Comparison"));

                compareButton.addActionListener(ey -> {
                    resultsPanel.removeAll();
                    
                    // Get selected horses
                    String name1 = (String) horse1Selector.getSelectedItem();
                    String name2 = (String) horse2Selector.getSelectedItem();
                    Horse horse1 = null;
                    Horse horse2 = null;
                    
                    // Find selected horses in allHorses
                    for (Horse horse : allHorses) 
                    {
                        if (horse.getName().equals(name1)) 
                        {
                            horse1 = horse;
                        }
                        if (horse.getName().equals(name2)) 
                        {
                            horse2 = horse;
                        }
                    }
                    
                    if (horse1 != null && horse2 != null) 
                    {
                        // Headers
                        resultsPanel.add(new JLabel("Metric", SwingConstants.CENTER));
                        resultsPanel.add(new JLabel(name1, SwingConstants.CENTER));
                        resultsPanel.add(new JLabel(name2, SwingConstants.CENTER));
                        
                        // Statistics
                        int totalRaces1 = horse1.getWins() + horse1.getLosses();
                        int totalRaces2 = horse2.getWins() + horse2.getLosses();
                        double winRate1 = totalRaces1 > 0 ? (double)horse1.getWins() / totalRaces1 * 100 : 0;
                        double winRate2 = totalRaces2 > 0 ? (double)horse2.getWins() / totalRaces2 * 100 : 0;
                        double fallRate1 = totalRaces1 > 0 ? (double)horse1.getFalls() / totalRaces1 * 100 : 0;
                        double fallRate2 = totalRaces2 > 0 ? (double)horse2.getFalls() / totalRaces2 * 100 : 0;
                        
                        // Total Races
                        resultsPanel.add(new JLabel("Total Races"));
                        JLabel races1 = new JLabel(String.valueOf(totalRaces1), SwingConstants.CENTER);
                        JLabel races2 = new JLabel(String.valueOf(totalRaces2), SwingConstants.CENTER);
                        if (totalRaces1 > totalRaces2) 
                        {
                            races1.setForeground(new Color(0, 150, 0));
                            races2.setForeground(Color.RED);
                        } 
                        else if (totalRaces2 > totalRaces1) 
                        {
                            races2.setForeground(new Color(0, 150, 0));
                            races1.setForeground(Color.RED);
                        }
                        resultsPanel.add(races1);
                        resultsPanel.add(races2);
                        
                        // Wins
                        resultsPanel.add(new JLabel("Wins"));
                        JLabel wins1 = new JLabel(String.valueOf(horse1.getWins()), SwingConstants.CENTER);
                        JLabel wins2 = new JLabel(String.valueOf(horse2.getWins()), SwingConstants.CENTER);
                        if (horse1.getWins() > horse2.getWins()) 
                        {
                            wins1.setForeground(new Color(0, 150, 0));
                            wins2.setForeground(Color.RED);
                        } 
                        else if (horse2.getWins() > horse1.getWins()) 
                        {
                            wins2.setForeground(new Color(0, 150, 0));
                            wins1.setForeground(Color.RED);
                        }
                        resultsPanel.add(wins1);
                        resultsPanel.add(wins2);
                        
                        // Win Rate
                        resultsPanel.add(new JLabel("Win Rate"));
                        JLabel winRate1Label = new JLabel(String.format("%.1f%%", winRate1), SwingConstants.CENTER);
                        JLabel winRate2Label = new JLabel(String.format("%.1f%%", winRate2), SwingConstants.CENTER);
                        if (winRate1 > winRate2) 
                        {
                            winRate1Label.setForeground(new Color(0, 150, 0));
                            winRate2Label.setForeground(Color.RED);
                        } 
                        else if (winRate2 > winRate1) 
                        {
                            winRate2Label.setForeground(new Color(0, 150, 0));
                            winRate1Label.setForeground(Color.RED);
                        }
                        resultsPanel.add(winRate1Label);
                        resultsPanel.add(winRate2Label);
                        
                        // Falls
                        resultsPanel.add(new JLabel("Falls"));
                        JLabel falls1 = new JLabel(String.valueOf(horse1.getFalls()), SwingConstants.CENTER);
                        JLabel falls2 = new JLabel(String.valueOf(horse2.getFalls()), SwingConstants.CENTER);
                        if (horse1.getFalls() < horse2.getFalls()) 
                        {
                            falls1.setForeground(new Color(0, 150, 0));
                            falls2.setForeground(Color.RED);
                        } 
                        else if (horse2.getFalls() < horse1.getFalls()) 
                        {
                            falls2.setForeground(new Color(0, 150, 0));
                            falls1.setForeground(Color.RED);
                        }
                        resultsPanel.add(falls1);
                        resultsPanel.add(falls2);
                        
                        // Fall Rate
                        resultsPanel.add(new JLabel("Fall Rate"));
                        JLabel fallRate1Label = new JLabel(String.format("%.1f%%", fallRate1), SwingConstants.CENTER);
                        JLabel fallRate2Label = new JLabel(String.format("%.1f%%", fallRate2), SwingConstants.CENTER);
                        if (fallRate1 < fallRate2) 
                        {
                            fallRate1Label.setForeground(new Color(0, 150, 0));
                            fallRate2Label.setForeground(Color.RED);
                        } 
                        else if (fallRate2 < fallRate1) 
                        {
                            fallRate2Label.setForeground(new Color(0, 150, 0));
                            fallRate1Label.setForeground(Color.RED);
                        }
                        resultsPanel.add(fallRate1Label);
                        resultsPanel.add(fallRate2Label);
                        
                        // Current Confidence
                        resultsPanel.add(new JLabel("Current Confidence"));
                        JLabel conf1 = new JLabel(String.format("%.2f", horse1.getConfidence()), SwingConstants.CENTER);
                        JLabel conf2 = new JLabel(String.format("%.2f", horse2.getConfidence()), SwingConstants.CENTER);
                        if (horse1.getConfidence() > horse2.getConfidence()) 
                        {
                            conf1.setForeground(new Color(0, 150, 0));
                            conf2.setForeground(Color.RED);
                        } 
                        else if (horse2.getConfidence() > horse1.getConfidence()) 
                        {
                            conf2.setForeground(new Color(0, 150, 0));
                            conf1.setForeground(Color.RED);
                        }
                        resultsPanel.add(conf1);
                        resultsPanel.add(conf2);
                    }
                    
                    resultsPanel.revalidate();
                    resultsPanel.repaint();
                });

                comparisonPanel.add(selectionPanel, BorderLayout.NORTH);
                comparisonPanel.add(new JScrollPane(resultsPanel), BorderLayout.CENTER);

                // Add to main tabs
                metricsTabs.addTab("Horse Comparison", comparisonPanel);
                
                // Force revalidation
                metricsPanel.revalidate();
                metricsPanel.repaint();
                mainPanel.revalidate();
                mainPanel.repaint();
            }
        }
    }

    private class StartRaceButtonListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e) 
        {
            // this creates the graphical track panel
            startRaceButton.setVisible(false);
            placeBetsButton.setVisible(false);
            metricsPanel.setVisible(false);
            viewMetricsButton.setVisible(true);
            restartButton.setVisible(true);
    
            currentRacePanel = new RaceTrackPanel(trackShapeString, numberOfLanes, horses, trackLengthInteger, StartRaceGUI.this, weatherConditionString);
            setCurrentRacePanel(currentRacePanel);
            
            
            
            
            // Add race panel and refresh display
            mainPanel.add(currentRacePanel, BorderLayout.CENTER);
            currentRacePanel.resetRace();
            
            mainPanel.revalidate();
            mainPanel.repaint();

        }
    }

    private class PlaceBetsButtonListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e) 
        {

        }
    }

    public void saveHorsesToCSV() 
    {
        try 
        {
            File dataDir = new File(DATA_DIR);
            if (!dataDir.exists() && !dataDir.mkdirs()) 
            {
                throw new IOException("Could not create data directory");
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter(CSV_FILE))) 
            {
                writer.println("Name,Symbol,Confidence,Wins,Losses,Falls,Breed,HorseshoeType,CoatColor,SaddleType");
                for (Horse horse : allHorses) 
                {
                    writer.println(String.format("%s,%s,%.2f,%d,%d,%d,%s,%s,%s",
                        horse.getName(),
                        horse.getSymbol(),
                        horse.getConfidence(),
                        horse.getWins(),
                        horse.getLosses(),
                        horse.getFalls(),
                        horse.getBreed().getBreedName(),
                        horse.getHorseshoeType(),
                        horse.getCoatColor().getColorName(),
                        horse.getSaddleType()));

                }
                System.out.println("Horse data saved to: " + new File(CSV_FILE).getAbsolutePath());
            }
        } 
        catch (IOException e) 
        {
            JOptionPane.showMessageDialog(this, 
                "Error saving horse data: " + e.getMessage() + 
                "\nFile location: " + new File(CSV_FILE).getAbsolutePath(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadHorsesFromCSV() 
    {
        if (trackLengthInteger <= 0) 
        {
           return;
        }

        File file = new File(CSV_FILE);
        if (!file.exists()) 
        {
            System.out.println("No existing horse data file found at: " + file.getAbsolutePath());
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE))) 
        {
            String line;
            reader.readLine(); // Skip header
            allHorses.clear(); // Clear existing horses
            
            while ((line = reader.readLine()) != null) 
            {
                String[] data = line.split(",");
                // Changed condition from >= 10 to >= 9
                if (data.length >= 9) 
                {
                    String name = data[0].trim();
                    double confidence = Double.parseDouble(data[2]);
                    int wins = Integer.parseInt(data[3]);
                    int losses = Integer.parseInt(data[4]);
                    int falls = Integer.parseInt(data[5]);
                    String breedName = data[6].trim();
                    String horseshoeType = data[7].trim();
                    String coatColor = data[8].trim();
                    String saddleType = "Racing Style";  // Default saddle type

                    // Convert breed name to correct constant
                    String breed = HorseBreed.THOROUGHBRED; // default value
                    if (breedName.equals("Arabian")) {
                        breed = HorseBreed.ARABIAN;
                    } else if (breedName.equals("Quarter Horse")) {
                        breed = HorseBreed.QUARTER_HORSE;
                    } else if (breedName.equals("Clydesdale")) {
                        breed = HorseBreed.CLYDESDALE;
                    } else if (breedName.equals("Appaloosa")) {
                        breed = HorseBreed.APPALOOSA;
                    } else if (breedName.equals("Thoroughbred")) {
                        breed = HorseBreed.THOROUGHBRED;
                    }

                    // Convert color name to correct constant
                    String color = CoatColor.BAY; // default value
                    if (coatColor.equals("Black")) {
                        color = CoatColor.BLACK;
                    } else if (coatColor.equals("Chestnut")) {
                        color = CoatColor.CHESTNUT;
                    } else if (coatColor.equals("White")) {
                        color = CoatColor.WHITE;
                    } else if (coatColor.equals("Grey")) {
                        color = CoatColor.GREY;
                    } else if (coatColor.equals("Palomino")) {
                        color = CoatColor.PALOMINO;
                    } else if (coatColor.equals("Pinto")) {
                        color = CoatColor.PINTO;
                    } else if (coatColor.equals("Bay")) {
                        color = CoatColor.BAY;
                    }

                    // Converts horseshoe type
                    String horseshoes = horseshoeType.equals("Grip") ? "Grip" : "Standard";
                    
                    // Creates horse with all attributes
                    Horse horse = new Horse(name, confidence, trackLengthInteger, breed, horseshoes, color, saddleType);
                    horse.setWins(wins);
                    horse.setLosses(losses);
                    horse.setFalls(falls);
                    
                    allHorses.add(horse);
                }
            }
            
            // Convert allHorses to array
            horses = allHorses.toArray(new Horse[0]);
            System.out.println("Horse data loaded from: " + file.getAbsolutePath());
        } 
        catch (IOException e) 
        {
            JOptionPane.showMessageDialog(this, 
                "Error loading horse data: " + e.getMessage() + 
                "\nFile location: " + file.getAbsolutePath(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void saveConfidenceHistory(Horse horse, String trackShape, int position, double time, double speed, double confidenceBefore, double confidenceAfter) 
    {
        try 
        {
            // Creates data directory if it doesn't exist
            File dataDir = new File(DATA_DIR);
            if (!dataDir.exists() && !dataDir.mkdirs()) 
            {
                throw new IOException("Could not create data directory");
            }
    
            try (PrintWriter writer = new PrintWriter(new FileWriter(CONFIDENCE_HISTORY_FILE, true))) 
            {
                // If file is empty, write header
                if (new File(CONFIDENCE_HISTORY_FILE).length() == 0) 
                {
                    writer.println("Date,HorseName,TrackShape,Position,Time,Speed,ConfidenceBefore,ConfidenceAfter,Change");
                }
                
                String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                
                // Calculate change based on confidence values
                double change = confidenceAfter - confidenceBefore;
                
                String line = String.format("%s,%s,%s,%d,%.2f,%.2f,%.2f,%.2f,%.2f",
                    date,
                    horse.getName(),
                    trackShape,
                    position,
                    time,
                    speed,
                    confidenceBefore,
                    confidenceAfter,
                    change);
                
                writer.println(line);
            }
        } 
        catch (IOException e) 
        {
            JOptionPane.showMessageDialog(this, 
                "Error saving confidence history: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Object[][] loadConfidenceHistory(String horseName) 
    {
        List<Object[]> history = new ArrayList<>();
        File file = new File(CONFIDENCE_HISTORY_FILE);
        
        if (!file.exists()) 
        {
            return new Object[0][0];
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(CONFIDENCE_HISTORY_FILE))) 
        {
            String line;
            reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) 
            {
                String[] data = line.split(",");
                if (data[1].equals(horseName)) // Checks if this entry is for the current horse
                { 
                    history.add(new Object[]{
                        data[0], // Date
                        data[2], // TrackShape
                        Integer.parseInt(data[3]), // Position
                        Double.parseDouble(data[4]), // Time
                        Double.parseDouble(data[5]), // Speed
                        Double.parseDouble(data[6]), // ConfidenceBefore
                        Double.parseDouble(data[7]), // ConfidenceAfter
                        Double.parseDouble(data[8])  // Change
                    });
                }
            }
        } 
        catch (IOException e) 
        {
            JOptionPane.showMessageDialog(this, 
                "Error loading confidence history: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        return history.toArray(new Object[0][0]);
    }

    public void saveTrackRecord(String trackType, String condition, double raceTime) {
        try {
            
            File dataDir = new File(DATA_DIR);
            if (!dataDir.exists() && !dataDir.mkdirs()) {
                throw new IOException("Could not create data directory");
            }
    
            // Load all existing records
            List<String[]> allRecords = loadAllTrackRecords();
            boolean recordExists = false;
            
            // Check if this track/condition combination exists
            for (String[] record : allRecords) {
                if (record[0].equals(trackType) && record[1].equals(condition)) {
                    recordExists = true;
                    double currentBest = Double.parseDouble(record[2]);
                    int racesCount = Integer.parseInt(record[3]);
                    racesCount++;
                    
                    // Update best time if this race was faster
                    if (raceTime < currentBest) {
                        record[2] = String.format("%.2f", raceTime);
                    }
                    record[3] = String.valueOf(racesCount);
                    break;
                }
            }
            
            // If no existing record found, create new one
            if (!recordExists) {
                System.out.println("Creating new record");  // Debug print
                allRecords.add(new String[]{
                    trackType,
                    condition,
                    String.format("%.2f", raceTime),
                    "1"
                });
            }
            
            // Rewrite the entire file with updated records
            try (PrintWriter writer = new PrintWriter(new FileWriter(TRACK_RECORDS_FILE))) {
                writer.println("TrackType,Condition,BestTime,RacesCompleted");
                for (String[] record : allRecords) {
                    writer.println(String.join(",", record));
                }
            }
        }
        catch (IOException e) 
        {
            JOptionPane.showMessageDialog(this, "Error saving track record: " + e.getMessage(),"Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private List<String[]> loadAllTrackRecords() throws IOException 
    {
        List<String[]> records = new ArrayList<>();
        File file = new File(TRACK_RECORDS_FILE);
        
        if (!file.exists()) 
        {
            return records;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) 
        {
            String line;
            reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) 
            {
                records.add(line.split(","));
            }
        }
        return records;
    }

    private Object[][] loadTrackRecords() 
    {
        List<Object[]> records = new ArrayList<>();
        File file = new File(TRACK_RECORDS_FILE);
        
        if (!file.exists()) 
        {
            return new Object[0][0];
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) 
        {
            String line;
            reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) 
            {
                String[] data = line.split(",");
                if (data.length >= 4) {
                    records.add(new Object[]{
                        data[0], // TrackType
                        data[1], // Condition
                        formatTime(Double.parseDouble(data[2])), // Formatted BestTime
                        data[3]  // RacesCompleted
                    });
                }
            }
        } 
        catch (IOException e) 
        {
            JOptionPane.showMessageDialog(this, 
                "Error loading track records: " + e.getMessage(),"Error", JOptionPane.ERROR_MESSAGE);
        }
        
        // Sort by track type then condition
        records.sort((a, b) -> {
            int typeCompare = a[0].toString().compareTo(b[0].toString());
            if (typeCompare != 0) return typeCompare;
            return a[1].toString().compareTo(b[1].toString());
        });
        
        return records.toArray(new Object[0][0]);
    }
    
    private String formatTime(double seconds) 
    {
        int minutes = (int) (seconds / 60);
        double remainingSeconds = seconds % 60;
        return String.format("%d:%.2f", minutes, remainingSeconds);
    }
}

class RaceTrackPanel extends JPanel 
{
    // Instance Variables
    private StartRaceGUI parentGUI;
    private Horse[] horses;
    private String trackShape;
    private String weatherCondition;
    private int laneCount;
    private Timer raceTimer;
    private int trackLength;
    private boolean raceFinished;
    private long startTime;
    private boolean raceStarted;
    private boolean isPaused;

    // Constructor
    public RaceTrackPanel(String trackShape, int laneCount, Horse[] horses, int trackLength, StartRaceGUI parent, String weatherCondition) 
    {
        this.parentGUI = parent;
        this.trackShape = trackShape;
        this.weatherCondition = weatherCondition;
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

    // Race Control Methods
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
        raceStarted = false;
        isPaused = false;
        startTime = 0;
        
        // Create new timer
        raceTimer = new Timer(50, e -> {
            if (!raceStarted) 
            {
                startTime = System.currentTimeMillis();
                raceStarted = true;
            }
            updateRace();
        });
        raceTimer.start();
        
        // Repaint to show horses at starting positions
        repaint();
    }

    public void stopRace()
    {
        raceTimer.stop();
    }

    // Race State Methods
    public long getStartTime() 
    {
        return startTime;
    }

    public double getHorseRaceTime(Horse horse) 
    {
        if (horse.getFinishTime() == -1) 
        {
            return -1; // Horse didn't finish
        }
        return (horse.getFinishTime()) / 1000.0; // Convert to seconds
    }

    // Race Update Methods
    private void updateRace() 
    {
        if (isPaused || raceFinished) return;
    
        boolean allHorsesDone = true;
        
        for (Horse horse : horses) 
        {
            if (!horse.hasFallen() && horse.getFinishTime() == -1) 
            {
                if (horse.getDistanceTravelled() < trackLength) 
                {
                    horse.moveHorse(trackShape, weatherCondition);
                    allHorsesDone = false;
                } 
                else 
                {
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    horse.setFinishTime(elapsedTime);
                }
            }
        }
    
        if (allHorsesDone) 
        {
            raceFinished = true;
            raceTimer.stop();
    
            // Creating a list of all horses that participated
            List<Horse> raceParticipants = new ArrayList<>();
            for (Horse horse : horses) 
            {
                if (!horse.hasFallen())   // Only adding horses that haven't fallen
                {
                    raceParticipants.add(horse);
                }
            }
            
            // Process fallen horses separately
            List<Horse> fallenHorses = new ArrayList<>();
            for (Horse horse : horses) 
            {
                if (horse.hasFallen()) 
                {
                    fallenHorses.add(horse);
                    horse.setLosses(horse.getLosses() + 1);
                    horse.setFalls(horse.getFalls() + 1);
                }
            }
            // Sort by finish time to determine positions
            raceParticipants.sort(Comparator.comparingLong(Horse::getFinishTime));
    

            // Record history for each horse
            for (int i = 0; i < raceParticipants.size(); i++) 
            {
                Horse horse = raceParticipants.get(i);
                double time = horse.getFinishTime() / 1000.0;
                double speed = trackLength / time;
                double confidenceBefore = horse.getConfidence();

                if (i == 0) 
                {
                    horse.setWins(horse.getWins() + 1);
                    parentGUI.saveTrackRecord(trackShape, weatherCondition, time);
                } 
                else 
                {
                    horse.setLosses(horse.getLosses() + 1);
                }
                
                boolean won = (i == 0);
                boolean fell = false;  // These horses haven't fallen
                
                horse.updateConfidenceAfterRace(won, fell, weatherCondition);
                
                // Save confidence history
                parentGUI.saveConfidenceHistory(horse, trackShape, i + 1, time, speed, confidenceBefore, horse.getConfidence());
            }

            // Process fallen horses confidence history
            for (int i = 0; i < fallenHorses.size(); i++) 
            {
                Horse horse = fallenHorses.get(i);
                double time = horse.getFinishTime() / 1000.0;
                double speed = horse.getDistanceTravelled() / time;
                double confidenceBefore = horse.getConfidence();
                
                horse.updateConfidenceAfterRace(false, true, weatherCondition);
                
                // Save confidence history (fallen horses are placed after finishing horses)
                parentGUI.saveConfidenceHistory(horse, trackShape, raceParticipants.size() + i + 1, time, speed, confidenceBefore, horse.getConfidence());
            }

            // Save all horse data to CSV
            parentGUI.saveHorsesToCSV();

            // Show results
            String message = (raceParticipants.size() > 0) 
                ? "Race Over! Winner: " + raceParticipants.get(0).getName() + "!"
                : "Race Over! All horses have fallen!";
            JOptionPane.showMessageDialog(this, message);
        }
        repaint();
    }
    // Drawing Methods
    @Override
    protected void paintComponent(Graphics g) 
    {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Drawing weather background effects 
        if (weatherCondition.equals("Muddy")) 
        {
            g2d.setColor(new Color(139, 69, 19, 40));  // Very light brown tint
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            // Drawing muddy patches
            g2d.setColor(new Color(101, 67, 33, 60));  // Semi-transparent brown
            for (int i = 0; i < 30; i++) 
            {
                int x = (i * 73) % getWidth();
                int y = (i * 89) % getHeight();
                g2d.fillOval(x, y, 60, 35);  // Muddy patches

                g2d.setColor(new Color(82, 46, 23, 60));
                g2d.fillOval(x + 15, y + 10, 30, 15);
            }
        } 
        else if (weatherCondition.equals("Icy")) 
        {
            //Icy background
            g2d.setColor(new Color(180, 200, 255, 150));  //Blue tint
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            // Drawing ice patches
            g2d.setColor(new Color(255, 255, 255, 100));  //White
            for (int i = 0; i < 35; i++) 
            {
                int x = (i * 83) % getWidth();
                int y = (i * 97) % getHeight();
                
                g2d.fillOval(x, y, 70, 40);
                
                //Ice shine effect
                g2d.setColor(new Color(220, 240, 255, 120));
                g2d.fillOval(x + 20, y + 10, 30, 20);
                g2d.setColor(new Color(255, 255, 255, 100));
            }
        }

        else if (weatherCondition.equals("Dry")) 
        {
            g2d.setColor(new Color(255, 253, 208, 50));  // Light warm yellow tint
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
            
        int width = getWidth();
        int height = getHeight();
        
        g.setColor(Color.BLACK);
        
        if (trackShape.equals("Oval")) 
        {
            drawOvalTrack(g, g2d, width, height);
        }
        else if (trackShape.equals("Figure-Eight")) 
        {  
            drawFigureEightTrack(g, g2d, width, height);
        } 
        else if (trackShape.equals("Straight")) 
        {
            drawStraightTrack(g, g2d, width, height);
        } 
    }

    private void drawOvalTrack(Graphics g, Graphics2D g2d, int width, int height) 
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

        // Draw horses using the new method
        for (int i = 0; i < laneCount; i++) 
        {
            Horse horse = horses[i];
            int x = horse.getX(width, height, trackShape, i);
            int y = horse.getY(width, height, trackShape, i);
            drawHorse(g, x, y, horse, 35);  // Use drawHorse instead of direct drawing
        }
    }

    private void drawFigureEightTrack(Graphics g, Graphics2D g2d, int width, int height) 
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

        // Draw horses using the new method
        for (int i = 0; i < laneCount; i++) 
        {
            Horse horse = horses[i];
            int x = horse.getX(width, height, trackShape, i);
            int y = horse.getY(width, height, trackShape, i);
            drawHorse(g, x, y, horse, 35);  // Use drawHorse instead of direct drawing
        }
    }

    private void drawStraightTrack(Graphics g, Graphics2D g2d, int width, int height) 
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

        // Draw horses using the new method
        for (int i = 0; i < laneCount; i++) 
        {
            Horse horse = horses[i];
            int x = horse.getX(width, height, trackShape, i);
            int y = horse.getY(width, height, trackShape, i);
            drawHorse(g, x, y, horse, 35);  // Use drawHorse instead of direct drawing
        }
    }
    private void drawHorse(Graphics g, int x, int y, Horse horse, int size) 
    {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // If horse has fallen, draw an X
        if (horse.hasFallen()) 
        {
            g2d.setColor(Color.RED);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawLine(x - size/2, y - size/2, x + size/2, y + size/2);
            g2d.drawLine(x + size/2, y - size/2, x - size/2, y + size/2);
            return;
        }

        // Drawing horse body based on breed
        g2d.setColor(horse.getCoatColor().getColor());
        String breedName = horse.getBreed().getBreedName();

        int bodyX = x - size/2;
        int bodyY = y - size/3;
        int bodyWidth = size;
        int bodyHeight = size/2;

        if (breedName.equals(HorseBreed.THOROUGHBRED)) 
        {
            // Sleek, racing shape
            g2d.fillOval(x - size/2, y - size/3, size, size/2);
        }
        else if (breedName.equals(HorseBreed.ARABIAN)) 
        {
            // Elegant, curved neck
            g2d.fillOval(x - size/2, y - size/3, size, size/2);
            g2d.fillArc(x + size/4, y - size/2, size/3, size/2, 0, 180);
        }
        else if (breedName.equals(HorseBreed.QUARTER_HORSE)) 
        {
            // Muscular shape
            g2d.fillRoundRect(x - size/2, y - size/3, size, size/2, 10, 10);
        }
        else if (breedName.equals(HorseBreed.CLYDESDALE)) 
        {
            // Large horse shape
            g2d.fillRoundRect(x - size/2, y - size/3, size + size/4, size/2, 15, 15);
        }
        else if (breedName.equals(HorseBreed.APPALOOSA)) 
        {
            // Spotted pattern
            g2d.fillOval(bodyX, bodyY, bodyWidth, bodyHeight);
            g2d.setColor(Color.WHITE);
            
            // Use stored spot positions from the breed
            int[] spotXOffsets = horse.getBreed().getSpotXOffsets();
            int[] spotYOffsets = horse.getBreed().getSpotYOffsets();
            if (spotXOffsets != null && spotYOffsets != null) 
            {
                for (int i = 0; i < 5; i++) 
                {
                    int spotX = x + spotXOffsets[i];
                    int spotY = y + spotYOffsets[i];
                    g2d.fillOval(spotX - size/20, spotY - size/20, size/10, size/10);
                }
            }
        }

        if (horse.getSaddleType().equals("Racing Style")) 
        {
            // Racing saddle 
            g2d.setColor(new Color(139, 69, 19)); // Saddle brown
            g2d.fillRoundRect(x - size/6, y - size/3, size/3, size/4, 5, 5);
            
        
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setStroke(new BasicStroke(2));
          
            g2d.drawLine(x - size/8, y - size/4, x - size/8, y);
          
            g2d.drawLine(x + size/8, y - size/4, x + size/8, y);
        } 
        else // Western Style
        {
            // Western saddle
            g2d.setColor(new Color(139, 69, 19)); // Saddle brown
            g2d.fillRoundRect(x - size/4, y - size/3, size/2, size/3, 10, 10);
            
            // Decorative pattern
            g2d.setColor(new Color(205, 133, 63)); 
            g2d.setStroke(new BasicStroke(1));
            for (int i = 0; i < 3; i++) 
            {
                g2d.drawRoundRect(x - size/4 + i*5, y - size/3 + i*3, size/2 - i*10, size/3 - i*6, 8, 8);
            }
            
           
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.setStroke(new BasicStroke(2));
           
            g2d.drawLine(x - size/5, y - size/4, x - size/5, y + size/8);
            g2d.drawArc(x - size/5 - 5, y + size/8 - 5, 10, 10, 0, 180);
           
            g2d.drawLine(x + size/5, y - size/4, x + size/5, y + size/8);
            g2d.drawArc(x + size/5 - 5, y + size/8 - 5, 10, 10, 0, 180);
        }

        // Draws horseshoes
        int horseshoeSize = size/6;
        g2d.setStroke(new BasicStroke(2));
        if (horse.getHorseshoeType().equals("Grip")) 
        {
            // Grip horseshoes 
            g2d.setColor(Color.LIGHT_GRAY);
            for (int i = 0; i < 4; i++) 
            {
                int hx = x - size/3 + (i * size/3);
                int hy = y + size/4;
                g2d.fillOval(hx - horseshoeSize/2, hy - horseshoeSize/2, horseshoeSize, horseshoeSize);
                // Add grip pattern
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawLine(hx - horseshoeSize/4, hy, hx + horseshoeSize/4, hy);
                g2d.setColor(Color.LIGHT_GRAY);
            }
        } 
        else 
        {
            // Standard horseshoes - bronze colored
            g2d.setColor(new Color(205, 127, 50)); // Bronze color
            for (int i = 0; i < 4; i++) {
                int hx = x - size/3 + (i * size/3);
                int hy = y + size/4;
                g2d.fillOval(hx - horseshoeSize/2, hy - horseshoeSize/2, horseshoeSize, horseshoeSize);
            }
        }

        // Horse name above the horse
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, size/4));
        FontMetrics metrics = g2d.getFontMetrics();
        int nameWidth = metrics.stringWidth(horse.getName());
        g2d.drawString(horse.getName(), x - nameWidth/2, y - size/2);
    }
}


class Horse 
{
    //instance variable set to private to improve security
    private String horseName;
    private String horseSymbol;
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

    //metrics
    private int wins = 0;
    private int losses = 0;
    private int falls = 0;

    //Horse Customising variable
    private HorseBreed breed;
    private String horseshoeType;  
    private CoatColor coatColor;
    private String saddleType;

    // Constructor
    public Horse(String horseName, double horseConfidence, int trackLength, String breedName, String horseshoesType, String colorName, String saddleType) 
    {
        this.horseName = horseName;
        this.horseConfidence = horseConfidence;
        this.hasFallen = false;
        this.distanceTravelled = 0;
        this.trackLength = trackLength;
        this.currentSpeed = 0.0;

        this.breed = new HorseBreed(breedName);
        this.horseshoeType = horseshoesType;  // Fixed parameter name
        this.coatColor = new CoatColor(colorName);
        this.saddleType = saddleType;

        this.acceleration = 0.05 + (horseConfidence * 0.01) * breed.getSpeedModifier();

        updateSymbol();  // This will set the horseSymbol
    }

    private void updateSymbol()
    {
        // Example symbol format: T-G (Thoroughbred with Grip horseshoes)
        StringBuilder symbol = new StringBuilder();
        symbol.append(breed.getBreedSymbol());
        symbol.append('-');
        symbol.append(horseshoeType.equals("Grip") ? 'G' : 'S');
        this.horseSymbol = symbol.toString();
    }

    // Basic Getters and Setters
    public String getSaddleType() 
    {
        return saddleType;
    }   

    public CoatColor getCoatColor() 
    {
        return coatColor;
    }

    public String getHorseshoeType() 
    {
        return horseshoeType;
    }

    public HorseBreed getBreed()
    {
        return this.breed;
    }

    public String getName() 
    {
        return this.horseName;
    }

    public String getSymbol() 
    {
        return this.horseSymbol;
    }

    public void setSymbol(String newSymbol) 
    {
        this.horseSymbol = newSymbol;
    }

    public double getConfidence() 
    {
        return this.horseConfidence;
    }

    public void setConfidence(double newConfidence) 
    {
        if(newConfidence >= 0 && newConfidence <=1)
        {
            this.horseConfidence = newConfidence;
        }
        else
        {
            throw new IllegalArgumentException("Confidence must be between 0 and 1");
        }
    }

    // Race State Methods
    public boolean hasFallen() 
    {
        return this.hasFallen;
    }

    public boolean hasFinished() 
    {
        return hasFinished;
    }
    
    public void setFinished(boolean finished) 
    {
        this.hasFinished = finished;
    }

    public long getFinishTime() 
    {
        return finishTime;
    }

    public void setFinishTime(long time) 
    {
        this.finishTime = time;
    }

    public void goBackToStart() 
    {
        this.distanceTravelled = 0;
        this.currentSpeed = 0;
        this.hasFallen = false;
    }

    // Performance Metrics Methods
    public int getWins() 
    {
        return wins;
    }
    
    public int getLosses() 
    {
        return losses;
    }
    
    public int getFalls() 
    {
        return falls;
    }
    
    public void setWins(int wins) 
    {
        this.wins = wins;
    }
    
    public void setLosses(int losses) 
    {
        this.losses = losses;
    }
    
    public void setFalls(int falls) 
    {
        this.falls = falls;
    }

    public void updateConfidenceAfterRace(boolean wonRace, boolean fell, String weatherCondition) 
    {
        double oldConfidence = horseConfidence; 
        double change;
        if (wonRace) 
        {
            change = 0.1 * (1 - oldConfidence); // Bigger boost if confidence was low
        } 
        else if (fell) 
        {
            if (weatherCondition.equals("Icy")) 
            {
                change = -0.05 * oldConfidence; 
            } 
            else if (weatherCondition.equals("Muddy")) 
            {
                change = -0.08 * oldConfidence; 
            } 
            else 
            {
                change = -0.15 * oldConfidence;
            }
        }
        else 
        {
            change = -0.05 * oldConfidence; // Small penalty for losing but not falling
        }
        
        // Calculate new confidence and round to 2 decimal places
        double newConfidence = Math.round((oldConfidence + change) * 100.0) / 100.0;
        newConfidence = Math.max(0, Math.min(1, newConfidence));
        horseConfidence = newConfidence;
    }

    // Movement and Position Methods
    public double getDistanceTravelled() 
    {
        return this.distanceTravelled;
    }

    public double getCurrentSpeed()
    {
        return this.currentSpeed;
    }

    public void moveHorse(String trackShape, String weatherCondition) 
    {
        if (hasFinished)  
        {
            return;
        }

        // Weather effects
        double speedModifier = 1.0;  
        double fallRiskModifier = 1.0;      
        
        if (weatherCondition.equals("Muddy")) 
        {
            speedModifier = 0.7;  // 30% slower
            fallRiskModifier = 1.3;  
            if (horseshoeType.equals("Grip")) 
            {
                fallRiskModifier *= 0.8;
            }
        } 
        else if (weatherCondition.equals("Icy")) 
        {
            speedModifier = 0.8;  // 20% slower
            fallRiskModifier = 1.6;  
            if (horseshoeType.equals("Grip")) 
            {
                fallRiskModifier *= 0.7;
            }
        }

        boolean inDecelZone = false;
        double baseSpeed = 1.0;  // Constant base speed instead of scaling with track length

        if (trackShape.equals("Oval") && (progress >= 0.4 && progress <= 0.5)) 
        {
            inDecelZone = true;
        } 
        else if (trackShape.equals("Figure-Eight") && (progress >= 0.45 && progress < 0.5 || progress >= 0.95 && progress < 1.0)) 
        {
            inDecelZone = true;
        }

        if (inDecelZone) 
        {
            currentSpeed = Math.max(currentSpeed - acceleration*2, this.horseConfidence * baseSpeed);
        }
        else 
        {
            double targetSpeed = (1.0 + (Math.random() * 5 * this.horseConfidence)) * 
                            speedModifier * breed.getSpeedModifier() * baseSpeed;
            currentSpeed = Math.min(currentSpeed + acceleration * baseSpeed, targetSpeed);
        }

        this.distanceTravelled += currentSpeed;

        // Apply weather effects to falling chance
        double weatherAdjustedFallRisk = 0.0001 * Math.exp(this.horseConfidence) * fallRiskModifier;
        if (Math.random() < weatherAdjustedFallRisk) 
        {
            this.hasFallen = true;
        }
    }

    public int getCurrentLap(String trackShape) 
    {
        if (trackShape.equals("Straight")) 
        {
            return 1;
        }
        return (int)(distanceTravelled / 200) + 1;
    }

    public int getRequiredLaps(String trackShape) 
    {
        if (trackShape.equals("Straight")) 
        {
            return 1;
        }
        return trackLength / 200;
    }

    public int getX(int width, int height, String trackShape, int lane) 
    {
        if (trackShape.equals("Oval")) 
        {
            progress = this.distanceTravelled / 200;
            progress = progress % 1.0;
            int ovalWidth = width - 200;
            int ovalHeight = height - 100;
            int centerX = width / 2;
            int centerY = height / 2;
            int laneOffset = 40 * lane;
            
            double angle = progress * 2 * Math.PI + Math.PI;
            
            return centerX + (int)((ovalWidth/2 - laneOffset) * Math.cos(angle));
        }
        else if (trackShape.equals("Straight")) 
        {
            progress = this.distanceTravelled / this.trackLength;
            progress = progress % 1.0;
            return 50 + (int)((width - 100) * progress);
        }
        else if(trackShape.equals("Figure-Eight"))
        {
            progress = (this.distanceTravelled / 200) % 1.0;
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
            progress = this.distanceTravelled / 200;
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
            return 100 + (lane * 40);
        }  
        else if (trackShape.equals("Figure-Eight")) 
        {
            progress = (this.distanceTravelled / 200) % 1.0;
            double angle = progress * 2 * Math.PI + Math.PI/2;

            int laneOffset = lane * 40;
            double scale = (Math.min(width, height) / 2) - laneOffset;
            
            return (int) (height/2 + (scale/1.5) * Math.sin(angle) * Math.cos(angle));
        }
        return 0;
    }
}

class HorseBreed 
{
    private String breedName;
    private double speedModifier;
    private char breedSymbol;
    private int[] spotXOffsets;
    private int[] spotYOffsets;

    public static final String THOROUGHBRED = "Thoroughbred";
    public static final String ARABIAN = "Arabian";
    public static final String QUARTER_HORSE = "Quarter Horse";
    public static final String CLYDESDALE = "Clydesdale";
    public static final String APPALOOSA = "Appaloosa";

    public HorseBreed(String breedName) 
    {
        this.breedName = breedName;
        setBreedAttributes(breedName);
    }

    private void setBreedAttributes(String breedName) 
    {
        if (breedName.equals(THOROUGHBRED))
        {
            this.speedModifier = 1.2;
            this.breedSymbol = 'T';
        }
        else if (breedName.equals(ARABIAN))
        {
            this.speedModifier = 1.1;
            this.breedSymbol = 'A';
        }
        else if (breedName.equals(QUARTER_HORSE))
        {
            this.speedModifier = 1.0;
            this.breedSymbol = 'Q';
        }
        else if (breedName.equals(CLYDESDALE))
        {
            this.speedModifier = 0.9;
            this.breedSymbol = 'C';
        }
        else if (breedName.equals(APPALOOSA))
        {
            this.speedModifier = 1.0;
            this.breedSymbol = 'P';
            spotXOffsets = new int[5];
            spotYOffsets = new int[5];
            for (int i = 0; i < 5; i++) 
            {
                spotXOffsets[i] = (int)(Math.random() * 30) - 15; 
                spotYOffsets[i] = (int)(Math.random() * 17) - 10; 
            }
        }
        else
        {
            throw new IllegalArgumentException("Invalid breed name: " + breedName);
        }
    }

    public int[] getSpotXOffsets() 
    {
        return spotXOffsets;
    }

    public int[] getSpotYOffsets() 
    {
        return spotYOffsets;
    }

    public String getBreedName() 
    {
        return breedName;
    }

    public double getSpeedModifier() 
    {
        return speedModifier;
    }

    public char getBreedSymbol() 
    {
        return breedSymbol;
    }
}

// CoatColor class
class CoatColor 
{
    private String colorName;
    private Color color;  // java.awt.Color

    public static final String BAY = "Bay";
    public static final String BLACK = "Black";
    public static final String CHESTNUT = "Chestnut";
    public static final String WHITE = "White";
    public static final String GREY = "Grey";
    public static final String PALOMINO = "Palomino";
    public static final String PINTO = "Pinto";

    public CoatColor(String colorName) 
    {
        this.colorName = colorName;
        setColorValue(colorName);
    }

    private void setColorValue(String colorName) 
    {
        if (colorName.equals(BAY))
        {
            this.color = new Color(139, 69, 19);  // Saddle brown
        }
        else if (colorName.equals(BLACK))
        {
            this.color = Color.BLACK;
        }
        else if (colorName.equals(CHESTNUT))
        {
            this.color = new Color(205, 92, 92);  // Indian red
        }
        else if (colorName.equals(WHITE))
        {
            this.color = Color.WHITE;
        }
        else if (colorName.equals(GREY))
        {
            this.color = Color.GRAY;
        }
        else if (colorName.equals(PALOMINO))
        {
            this.color = new Color(238, 232, 170);  // Pale goldenrod
        }
        else if (colorName.equals(PINTO))
        {
            this.color = new Color(255, 235, 205);  // Blanched almond
        }
        else
        {
            throw new IllegalArgumentException("Invalid coat color: " + colorName);
        }
    }

    public String getColorName() 
    {
        return colorName;
    }

    public Color getColor() 
    {
        return color;
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