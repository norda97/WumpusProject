package wumpusworld.Imp;

import wumpusworld.Imp.Vector2;
import wumpusworld.Imp.Node;
import wumpusworld.Imp.Env;

import java.util.List;

import wumpusworld.World;

public class ModelBatch
{
    public List<Vector2> frontier;
    
    private WorldKnowledgeBase kb;
    private Env env;
    
    public ModelBatch(KnowledgeBase kb)
    {
        this.kb = kb;
        this.env = new Env();
    }

    public double predict(int pitLeft, boolean wumpusLeft, List<Vector2> frontier, int x, int y, String prediction)
    {
        // Copy world and set it to current.
        KnowledgeBase kbCpy = this.kb.clone();
        this.env.setWorld(wCpy);

        final double probP = 3.0/15.0;
        final double probW = 1.0/15.0;
        double sumModelsProbPositive = 0.0;
        double sumModelsProbNegative = 0.0;

        // Calc combinations and probabilities when pit/wumpus is not present.
        switch(prediction)
        {
            case World.PIT:
                kbCpy.addPit(x, y);
            break;
            case World.WUMPUS:
                kbCpy.addWumpus(x, y);
            break;
        }

        // We will not ignore the query type when calculating positive probability.
        this.env.resetQuery();

        sumModelsProbPositive = getProbFromModels(pitLeft, wumpusLeft, frontier, x, y);
        
        // We will ignore the query type when calculating negative probability.
        this.env.ignoreQuery(new Vector2(x, y));
        
        // Calc combinations and probabilities when pit/wumpus is not present.
        sumModelsProbPositive = getProbFromModels(pitLeft, wumpusLeft, frontier, x, y);

        // Calculate probability.
        double prob = 0.0;
        if(prediction.contains(World.PIT))
            prob = probP;
        else if(prediction.contains(World.WUMPUS))
            prob = probW;
        
        double probPositive = sumModelsProbPositive * prob;
        double probNegative = sumModelsProbNegative * (1.0 - prob);
        sum = probPositive+probNegative;

        return probPositive/sum;
    }

    private double getProbFromModels(int pitLeft, boolean wumpusLeft, List<Vector2> frontier, int x, int y)
    {
        double probResult = 0.0;

        // Calc combinations and probabilities when pit/wumpus is present.
        int n = frontier.size();

        int nLegalPitPos = 0;
        int nLegalWumpPos = 0;
        int nMustBePitPos = 0;
        int nMustBeWumpPos = 0; // Can only be 1 or 0.
        // Get number of places which a wumpus and a pit can be.
        for(Vector2 f : frontier)
        {
            if(this.env.isLegal(f.x, f.y, World.PIT)) {
                nLegalPitPos++;
                if(this.env.mustBePit(f.x, f.y, pitLeft))
                    nMustBePitPos++;
            }
            if(this.env.isLegal(f.x, f.y, World.WUMPUS)) {
                nLegalWumpPos++;
                if(this.env.mustBeWumpus(f.x, f.y, wumpusLeft))
                    nMustBeWumpPos++;
            }
        }
        int nPits = nLegalPitPos > pitLeft ? pitLeft : nLegalPitPos;
        // Go through each number of pits which can be present and calculate number of combinations (models) for that number of pits.
        // Also calculate number of combinations for wumpus.
        // TODO: Use number of places a wumpus/pit must be => will be 1 combination not more.
        // TODO: When p is 0, there might be cases when there must be at least one pit for it to be legal. Make this!
        for(int p = nPits; p >= 0; p--)
        {
            int pitsComb = binomial(nLegalPitPos, p);
            // Can also be no wumpus if wumpus left, check it!
            int wumpComb = 1;
            for(int w = 0; w <= (int)wumpusLeft; w++)
            {
                int nn = nLegalWumpPos > n-p ? n-p : nLegalWumpPos;
                wumpComb *= binomial(nn, w);
            }
            // Total number of combinations of nPits and wumpusPositions for this iteration.
            int totComb = pitsComb*wumpComb;
            // Each combination is one model.
            for(int i = 0; i < totComb; i++)
            {
                // Multiply with the probability that it is not a pit and wumput too.
                double modelProbPit = Math.pow(probP, p)*Math.pow(1.0-probP, nLegalPitPos-p);
                double modelProbWump = 1.0;
                if(nLegalWumpPos != 0)
                    modelProbWump = Math.pow(probW, (int)wumpusLeft) * Math.pow(1.0 - probW, (int)(!wumpusLeft));
                probResult += modelProbPit*modelProbWump;
            }
        }

        return probResult;
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