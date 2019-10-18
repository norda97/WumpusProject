package wumpusworld.Imp;

import wumpusworld.Imp.Vector2;
import wumpusworld.Imp.Node;
import wumpusworld.Imp.Env;

import java.util.List;
import java.util.Vector;
import java.util.ArrayList;

import wumpusworld.Imp.KnowledgeBase;
import wumpusworld.World;

import wumpusworld.Imp.BorderCell;
import wumpusworld.Imp.Model;

public class ModelBatch
{
    public List<Vector2> frontier;
    
    private KnowledgeBase kb;
    private Env env;

    
    double probP = 3.0/15.0;
    double probW = 1.0/15.0;
    
    public ModelBatch(KnowledgeBase kb)
    {
        this.kb = kb;
        this.env = new Env();
    }

    public double predict(int pitLeft, boolean wumpusLeft, int numUnknowns, int x, int y, String prediction)
    {
        this.probP = (double)pitLeft/numUnknowns;
        this.probW = (wumpusLeft?1.0:0.0)/numUnknowns;

        List<Vector2> frontier = new ArrayList<Vector2>();
        for(Vector2 v : this.kb.Frontier)
        {
            if(v.x != x || v.y != y)
                frontier.add(v);
        }

        // Copy world and set it to current.
        KnowledgeBase kbCpy = new KnowledgeBase(this.kb);
        this.env.setKB(kbCpy);

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

        sumModelsProbPositive = getProbFromModels(kbCpy, pitLeft, wumpusLeft, frontier, x, y);
        System.out.println("PrbPositive: " + Double.toString(sumModelsProbPositive));
        
        kbCpy = null;
        kbCpy = new KnowledgeBase(this.kb);
        this.env.setKB(kbCpy);
        switch(prediction)
        {
            case World.PIT:
                kbCpy.removePit(x, y);
            break;
            case World.WUMPUS:
                kbCpy.removeWumpus(x, y);
            break;
        }

        // Calc combinations and probabilities when pit/wumpus is not present.
        sumModelsProbNegative = getProbFromModels(kbCpy, pitLeft, wumpusLeft, frontier, x, y);
        System.out.println("PrbNegative: " + Double.toString(sumModelsProbNegative));

        // Calculate probability.
        double prob = 0.0;
        if(prediction.contains(World.PIT))
            prob = probP;
        else if(prediction.contains(World.WUMPUS))
            prob = probW;
        
        double probPositive = sumModelsProbPositive * prob;
        double probNegative = sumModelsProbNegative * (1.0 - prob);
        double sum = probPositive+probNegative;

        return probPositive/sum;
    }

    private double getProbFromModels(KnowledgeBase kb, int pitLeft, boolean wumpusLeft, List<Vector2> frontier, int x, int y)
    {
        double probability = 0.0;
        // Generate all possible models and compute its total probability.
        int n = frontier.size();

        int totCominationsPits = (int)Math.pow(2.0, n); // Includes one with 0 pits and all pits.
        List<BorderCell[]> combinationsPits = new ArrayList<BorderCell[]>();
        for(int i = 0; i < totCominationsPits; i++) {
            BorderCell[] border = new BorderCell[n];
            for(int j = 0; j < n; j++)
            {
                boolean active = (i & (1 << j)) == 0 ? false : true;
                Vector2 f = frontier.get(j); 
                border[j] = new BorderCell(f, active);
            }
            combinationsPits.add(border);
        }

        int totCominationsWumpus = n+1; // Includes one with 0 wumpus.
        List<BorderCell[]> combinationsWump = new ArrayList<BorderCell[]>();
        for(int i = 0; i < totCominationsWumpus; i++) {
            BorderCell[] border = new BorderCell[n];
            for(int j = 0; j < n; j++)
            {
                boolean active = i==j;
                Vector2 f = frontier.get(j); 
                border[j] = new BorderCell(f, active);
            }
            combinationsWump.add(border);
        }

        System.out.println("Num combs, Pit: " + Integer.toString(totCominationsPits) + ", Wump: " + Integer.toString(totCominationsWumpus));
        for(BorderCell[] borderPits : combinationsPits)
        {
            for(BorderCell[] borderWump : combinationsWump)
            {
                Model model = new Model(kb);
                for(BorderCell cell : borderPits) {
                    if(cell.active)
                    model.addType(cell.v.x, cell.v.y, World.PIT);
                    else
                    model.addEmpty(cell.v.x, cell.v.y);
                }
                for(BorderCell cell : borderWump) {
                    if(cell.active)
                        model.addType(cell.v.x, cell.v.y, World.WUMPUS);
                    else
                        model.addEmpty(cell.v.x, cell.v.y);
                }

    
                if(model.isLegal())
                {
                    model.print();
                    double prob = model.getProbability();
                    System.out.println("Model: " + Double.toString(prob));
                    probability += prob;
                }
            }
        }

        return probability;

        /*double probResult = 0.0;

        // Calc combinations and probabilities when pit/wumpus is present.
        int n = frontier.size();

        int nLegalPitPos = 0;
        int nLegalWumpPos = 0;
        int nMustBePitPos = 0;
        int nMustBeWumpPos = 0; // Can only be 1 or 0.
        // Get number of places which a wumpus and a pit can be.
        System.out.println("FrontierSize: " + Integer.toString(n));
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

        // Wumpus combinations.
        if(nMustBeWumpPos > 0)
            nLegalWumpPos = nMustBeWumpPos;
        int wumpusComb = binomial(nLegalWumpPos, 1);
        System.out.println("WumpCombo: " + Integer.toString(wumpusComb) + ", nLegalWumpPos: " + Integer.toString(nLegalWumpPos) + ", nMustBeWumpPos: " + Integer.toString(nMustBeWumpPos));
        for(int i = 0; i < wumpusComb; i++)
        {
            double modelProbWumpus = Math.pow(probW, 1)*Math.pow(1.0-probW, wumpusComb-1);
            probResult += modelProbWumpus;
        }
        return probResult;
        */
        /*
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
            int wumpLeft = wumpusLeft ? 1 : 0;
            for(int w = 0; w <= wumpLeft; w++)
            {
                int nn = nLegalWumpPos > n-p ? n-p : nLegalWumpPos;
                if (nn > 0)
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
                if(nLegalWumpPos != 0 && wumpLeft != 0)
                    modelProbWump = Math.pow(probW, wumpLeft) * Math.pow(1.0 - probW, 1 - wumpLeft);
                probResult += modelProbPit*modelProbWump;
            }
        }
        
        return probResult;*/
    }

    private int binomial(int n, int k)
    {
        int ret = 1;
        for(int i = 0; i < k; i++) {
            ret *= (n-i) / (i+1); 
        }
        return ret;
    }
}