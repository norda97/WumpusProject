package wumpusworld.Imp;

import wumpusworld.Imp.Vector2;
import wumpusworld.Imp.Node;
import wumpusworld.Imp.Env;

import java.util.List;

import wumpusworld.World;

public class ModelBatch
{
    public List<Vector2> frontier;
    
    private World w;
    private Env env;
    
    public ModelBatch()
    {
        this.env = new Env();
    }

    public double predict(World w, int pitLeft, boolean wumpusLeft, List<Vector2> frontier, int x, int y, String prediction)
    {
        // Copy world and set it to current.
        this.w = w.cloneWorld();
        this.env.setWorld(this.w);

        // Place the prediction on the copy.
        if(this.env.isLegal(x, y, prediction))
        {
            switch(prediction)
            {
                case World.PIT:
                    this.w.addPit(x, y);
                break;
                case World.WUMPUS:
                    this.w.addWumpus(x, y);
                break;
            }
        }

        int maxPitCount = 3;
        int pitCount = 0;
        int mustBePitCount = 0;
        boolean wumpusFound = false;
        int wumpusCount = 0;

        // Count number of places a pit or wumpus can be on.
        for(Vector2 f : frontier) {
            if(mustBePitCount < maxPitCount-pitLeft)
            {
                if(this.env.isLegal(f.x, f.y, World.PIT))
                {
                    pitCount++;
                    if(false)
                        mustBePitCount++;
                }
            }
            else
            {
                pitCount = 0;
            }

            // Check for wumpus if it is not found.
            if(wumpusLeft && (wumpusFound == false))
            {
                if(this.env.isLegal(f.x, f.y, World.WUMPUS))
                {
                    wumpusCount++;
                    if(false)
                    {
                        wumpusFound = true;
                        wumpusCount = 1;
                    }
                }
            }
        }

        // Calculate number of combinations
        int numPits = mustBePitCount;
        if(pitCount > 0) 
            numPits += binomial(pitCount, 0);
        int numWumpus = 0;
        
        // Calculate probability.
        final double probP = 3.0/15.0;
        final double probW = 1.0/15.0;
        double prob = 0.0;
        if(prediction.contains(World.PIT))
            prob = probP;
        else if(prediction.contains(World.WUMPUS))
            prob = probW;
        
        double sum = (double)numPits*probP + (double)numWumpus*probW;
        double probPositive = sum * prob;
        double probNegative = sum * (1.0 - prob);
        sum = probPositive+probNegative;

        return probPositive/sum;
    }

    private int binomial(int n, int k)
    {
        int ret = 1;
        for(int i = 0; i < k; i++) {
            ret *= (n-k) / (k+1); 
        }
        return ret;
    }
}