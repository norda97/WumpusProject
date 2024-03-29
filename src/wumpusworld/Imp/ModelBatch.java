package wumpusworld.Imp;

import wumpusworld.Imp.Vector2;

import java.util.List;
import java.util.ArrayList;

import wumpusworld.Imp.KnowledgeBase;
import wumpusworld.World;

import wumpusworld.Imp.BorderCell;
import wumpusworld.Imp.Model;

/**
 *
 * @author Adrian Nordin, Jonathan Åleskog, Daniel Cheh
 */
public class ModelBatch
{
    public List<Vector2> frontier;
    
    private KnowledgeBase kb;

    double probP = 3.0/15.0;
    double probW = 1.0/15.0;
    
    public ModelBatch(KnowledgeBase kb)
    {
        this.kb = kb;
    }

    public double predict(int pitLeft, boolean wumpusLeft, int numUnknowns, int x, int y, String prediction)
    {
        if(prediction.contains(World.WUMPUS) && wumpusLeft == false) {
            return 0.0;
        }

        // Calculate probabilities depending on the number of unknown cells, pits left and if wumpus was found or not.
        this.probP = (double)pitLeft/numUnknowns;
        this.probW = (wumpusLeft ? 1.0 : 1.0)/numUnknowns;

        // Remove the query from the frontier.
        List<Vector2> frontier = new ArrayList<Vector2>();
        for(Vector2 v : this.kb.frontier) {
            if(v.x != x || v.y != y)
                frontier.add(v);
        }

        // Copy world and set it to current.
        KnowledgeBase kbCpy = new KnowledgeBase(this.kb);

        double sumModelsProbPositive = 0.0;
        double sumModelsProbNegative = 0.0;

        // Calc combinations and probabilities when pit and/or wumpus is present.
        {
            if(prediction.contains(World.PIT))
                kbCpy.addPit(x, y);
            if(prediction.contains(World.WUMPUS))
                kbCpy.addWumpus(x, y);

            sumModelsProbPositive = getProbFromModels(kbCpy, pitLeft, wumpusLeft, frontier, x, y);
        }

        // Reset copy for next calculations to work.
        kbCpy = null;
        kbCpy = new KnowledgeBase(this.kb);

        // Calc combinations and probabilities when pit and/or wumpus is not present.
        {
            if(prediction.contains(World.PIT))
                kbCpy.removePit(x, y);
            if(prediction.contains(World.WUMPUS))
                kbCpy.removeWumpus(x, y);

            sumModelsProbNegative = getProbFromModels(kbCpy, pitLeft, wumpusLeft, frontier, x, y);
        }

        // --------------------- Calculate probability ---------------------
        double prob = 0.0;
        // Get probability if both wumpus and pit is present.
        if(prediction.contains(World.WUMPUS + World.PIT)) {
            prob = probP * probW;
        }
        // Get probability if only pit is present.
        else if(prediction.contains(World.PIT))
            prob = probP;
        // Get probability if only wumpus is present.
        else if(prediction.contains(World.WUMPUS))
            prob = probW;

        // Calculate the positive and negative probabilities for the prediction.
        double probPositive = sumModelsProbPositive * prob;
        double probNegative = sumModelsProbNegative * (1.0 - prob);

        // Normalize the positive probability.
        double sum = probPositive+probNegative;
        double finalProb = 0.0;
        if(sum > 0.00000001) finalProb = probPositive/sum;

        return finalProb;
    }

    private double getProbFromModels(KnowledgeBase kb, int pitLeft, boolean wumpusLeft, List<Vector2> frontier, int x, int y)
    {
        double probability = 0.0;
        // Generate all possible models and compute its total probability.
        int n = frontier.size();

        // Get the total number of combination a pit can be in (does not check if it is against the rules or not)
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

        // Get the total number of combination a wumpus can be in (does not check if it is against the rules or not)
        int totCominationsWumpus = n+1; // Includes one with no wumpus.
        List<BorderCell[]> combinationsWump = new ArrayList<BorderCell[]>();
        // i = -1 so that active becomes false for every cell in the first iteration. With other words, no wumpus for the first combination.
        for(int i = -1; i < totCominationsWumpus-1; i++) {
            BorderCell[] border = new BorderCell[n];
            for(int j = 0; j < n; j++)
            {
                boolean active = i==j;
                Vector2 f = frontier.get(j); 
                border[j] = new BorderCell(f, active);
            }
            combinationsWump.add(border);
        }
        
        // Generate a model for each combination of wumpus and pits. Only add those who are legal. 
        for(BorderCell[] borderPits : combinationsPits)
        {
            for(BorderCell[] borderWump : combinationsWump)
            {
                Model model = new Model(kb);
                model.addFrontier(x, y); // This will ensure that the model accounts for the prediction.
                // Add combinations of pits.
                for(BorderCell cell : borderPits) {
                    if(cell.active)
                        model.addType(cell.v.x, cell.v.y, World.PIT);
                    else
                    model.addFrontier(cell.v.x, cell.v.y);
                }
                // Add combinations of wumpus.
                for(BorderCell cell : borderWump) {
                    if(cell.active)
                        model.addType(cell.v.x, cell.v.y, World.WUMPUS);
                        else
                        model.addFrontier(cell.v.x, cell.v.y);
                }
                
                // Check if it is a legal combination of pits and wumpus.
                if(model.isLegal())
                {
                    double prob = model.getProbability(this.probP, this.probW);
                    probability += prob;
                }
            }
        }

        return probability;
    }
}