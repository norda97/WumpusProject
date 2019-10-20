package wumpusworld.Imp;

import wumpusworld.Imp.Vector2;

import java.util.List;
import java.util.ArrayList;

import wumpusworld.Imp.KnowledgeBase;
import wumpusworld.World;

import wumpusworld.Imp.BorderCell;
import wumpusworld.Imp.Model;

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
        //System.out.println("Predict [" + prediction + "] at (" + Integer.toString(x) + ", " + Integer.toString(y) + ")");
        if(prediction.contains(World.WUMPUS) && wumpusLeft == false) {
            //System.out.println("---> Wumpus already found => [Normalized] prob positive: 0.0");
            return 0.0;
        }

        this.probP = (double)pitLeft/numUnknowns;
        this.probW = (wumpusLeft ? 1.0 : 1.0)/numUnknowns;

        List<Vector2> frontier = new ArrayList<Vector2>();
        for(Vector2 v : this.kb.Frontier)
        {
            if(v.x != x || v.y != y)
                frontier.add(v);
        }

        // Copy world and set it to current.
        KnowledgeBase kbCpy = new KnowledgeBase(this.kb);

        double sumModelsProbPositive = 0.0;
        double sumModelsProbNegative = 0.0;

        if(prediction.contains(World.PIT))
            kbCpy.addPit(x, y);
        if(prediction.contains(World.WUMPUS))
            kbCpy.addWumpus(x, y);

        sumModelsProbPositive = getProbFromModels(kbCpy, pitLeft, wumpusLeft, frontier, x, y);
        //System.out.println("--->Positive: " + Double.toString(sumModelsProbPositive));
        
        kbCpy = null;
        kbCpy = new KnowledgeBase(this.kb);
        
        if(prediction.contains(World.PIT))
            kbCpy.removePit(x, y);
        if(prediction.contains(World.WUMPUS))
            kbCpy.removeWumpus(x, y);

        // Calc combinations and probabilities when pit/wumpus is not present.
        sumModelsProbNegative = getProbFromModels(kbCpy, pitLeft, wumpusLeft, frontier, x, y);
        //System.out.println("--->Negative: " + Double.toString(sumModelsProbNegative));

        // Calculate probability.
        double prob = 0.0;
        if(prediction.contains(World.PIT))
            prob = probP;
        else if(prediction.contains(World.WUMPUS))
            prob = probW;
        
        double probPositive = sumModelsProbPositive * prob;
        double probNegative = sumModelsProbNegative * (1.0 - prob);
        double sum = probPositive+probNegative;
        double finalProb = 0.0;
        if(sum > 0.00000001) finalProb = probPositive/sum;

        //System.out.println("--->[Normalized] Prob positive: " + Double.toString(finalProb));
        return finalProb;
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

        //System.out.println("Num combs, Pit: " + Integer.toString(totCominationsPits) + ", Wump: " + Integer.toString(totCominationsWumpus));
        for(BorderCell[] borderPits : combinationsPits)
        {
            for(BorderCell[] borderWump : combinationsWump)
            {
                //System.out.println("\n================== New Model ==================");
                Model model = new Model(kb);
                model.addEmpty(x, y);
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
                    double prob = model.getProbability(this.probP, this.probW);
                    //System.out.println("[PICKED] Model with prob: " + Double.toString(prob));
                    probability += prob;
                }
            }
        }

        return probability;
    }
}