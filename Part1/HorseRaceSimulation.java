/**
 * This class stores personal information about each horse object. 
 * 
 * @author Balazs Mano Lovasz 
 * @version 1
 */

import java.util.concurrent.TimeUnit;

class Horse
{
    private String horseName;
    private char horseSymbol;
    private int horseDistance;
    private boolean hasFallen;
    private double horseConfidence;

    public Horse(char horseSymbol, String horseName, double horseConfidence)
    {
        this.horseName = horseName;
        this.horseSymbol = horseSymbol;
        this.horseConfidence = horseConfidence;
        this.hasFallen = false;
        this.horseDistance = 0;
    }

    public void fall()
    {
        this.hasFallen = true;
    }

    public double getConfidence()
    {
        return this.horseConfidence;
    }

    public int getDistanceTravelled()
    {
        return this.horseDistance;
    }

    public String getName()
    {
        return this.horseName;
    }

    public char getSymbol()
    {
        return this.horseSymbol;
    }

    public void goBackToStart()
    {
        this.horseDistance = 0;
        this.hasFallen = false;
    }

    public boolean hasFallen()
    {
       return this.hasFallen;
    }

    public void moveForward()
    {
        this.horseDistance++;
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
    private int raceLength;
    private Horse lane1Horse;
    private Horse lane2Horse;
    private Horse lane3Horse;

    /**
     * Constructor for objects of class Race
     * Initially there are no horses in the lanes
     * 
     * @param distance the length of the racetrack (in metres/yards...)
     */
    public Race(int distance)
    {
        // initialise instance variables
        raceLength = distance;
        lane1Horse = null;
        lane2Horse = null;
        lane3Horse = null;
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
        else
        {
            System.out.println("Cannot add horse to lane " + laneNumber + " because there is no such lane");
        }
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
        lane1Horse.goBackToStart();
        lane2Horse.goBackToStart();
        lane3Horse.goBackToStart();
                      
        while (!finished)
        {
            //move each horse
            moveHorse(lane1Horse);
            moveHorse(lane2Horse);
            moveHorse(lane3Horse);
                        
            //print the race positions
            printRace();
            
            //if any of the three horses has won the race is finished
            if ( raceWonBy(lane1Horse) || raceWonBy(lane2Horse) || raceWonBy(lane3Horse) )
            {
                finished = true;
            }
            if (lane1Horse.hasFallen() && lane2Horse.hasFallen() && lane3Horse.hasFallen()) 
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
        if (theHorse.getDistanceTravelled() >= raceLength)
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

        multiplePrint('=',raceLength+3); //top edge of track
        System.out.println();
        
        printLane(lane1Horse);
        System.out.println();
        
        printLane(lane2Horse);
        System.out.println();
        
        printLane(lane3Horse);
        System.out.println();
        
        multiplePrint('=',raceLength+3); //bottom edge of track
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
        int spacesAfter = raceLength - theHorse.getDistanceTravelled();
        
        //print a | for the beginning of the lane
        System.out.print('|');
        
        //print the spaces before the horse
        multiplePrint(' ',spacesBefore);
        
        //if the horse has fallen then print dead
        //else print the horse's symbol
        if(theHorse.hasFallen())
        {
            System.out.println("\u001B[31mX\u001B[0m");
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
}

class HorseRaceSimulation
{
    public static void main(String[] args) 
    {   
        Race race = new Race(10);
        race.addHorse(new Horse('#', "Bob", 0.2), 1);
        race.addHorse(new Horse('I', "Jeff", 0.5), 2);
        race.addHorse(new Horse('O', "Chad", 0.8), 3);
        race.startRace();  // This ensures `main` exists
    }
}




