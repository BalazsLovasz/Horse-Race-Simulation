class Horse
{
    private String horseName;
    private char horseSymbol;
    private int horseDistance;
    private boolean flagForFalling;
    private double horseConfidence;

    public Horse(char horseSymbol, String horseName, double horseConfidence)
    {
        this.horseName = horseName;
        this.horseSymbol = horseSymbol;
        this.horseConfidence = horseConfidence;
        this.flagForFalling = false;
        this.horseDistance = 0;
    }

    public void fall()
    {
        this.flagForFalling = true;
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
        this.flagForFalling = false;
    }

    public boolean hasFallen()
    {
       return flagForFalling;
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
