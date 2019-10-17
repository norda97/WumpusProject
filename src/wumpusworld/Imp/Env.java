package wumpusworld.Imp;

import wumpusworld.World;
import java.util.List;
import wumpusworld.Imp.KnowledgeBase;

public class Env
{
    private KnowledgeBase kb;

    public Env()
    {
    }

    public void setKB(KnowledgeBase kb)
    {
        this.kb = kb;
    }

    public boolean isLegal(int x, int y, String percepts)
    {
        switch(percepts) 
        {
            case World.PIT:
                return canBePit(x, y);
            case World.WUMPUS:
                return canBeWumpus(x, y);
            default:
                return false;
        }
    }

    private boolean canBePit(int x, int y)
    {
        boolean legal = true;
        for(int i = -1; i < 2; i++)
            for(int j = -1; j < 2; j++)
            {
                if(Math.abs(i) != Math.abs(j))
                {
                    if(this.kb.isUnknown(x+i, y+j) == false && this.kb.isValidPosition(x+i, y+j))
                        legal &= this.kb.hasBreeze(x+i, y+j);
                }
            }
        return legal;  
    }

    private boolean canBeWumpus(int x, int y)
    {
        boolean legal = true;
        for(int i = -1; i < 2; i++)
            for(int j = -1; j < 2; j++)
            {
                if(Math.abs(i) != Math.abs(j))
                {
                    if(this.kb.isUnknown(x+i, y+j) == false && this.kb.isValidPosition(x+i, y+j))
                        legal &= this.kb.hasStench(x+i, y+j);
                }
            }
        return legal;
    }

    public boolean mustBePit(int x, int y, int pitsFound)
    {
        if(pitsFound >= 3) return false;
        if(isLegal(x, y, World.PIT) == false) return false;

        for(int i = -1; i < 2; i++) {
            for(int j = -1; j < 2; j++)
            {
                if(Math.abs(i) != Math.abs(j))
                {
                    if(this.kb.hasBreeze(x+i, y+j))
                    {
                        int u = getNumUnknownNeighbours(x+i, y+i);
                        int n = getNumNeighboursWithPit(x+i, y+i);
                        if(n == 0 && u == 1) return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean mustBeWumpus(int x, int y, boolean wumpusFound)
    {
        if(wumpusFound) return false;
        if(isLegal(x, y, World.WUMPUS) == false) return false;
        
        int numNeighboursWithStench = 0;
        List<Vector2> m = new ArrayList<Vector2>();
        for(int i = -1; i < 2; i++) {
            for(int j = -1; j < 2; j++)
            {
                if(Math.abs(i) != Math.abs(j))
                {
                    if(this.kb.hasStench(x+i, y+j))
                    {
                        numNeighboursWithStench++;
                        int u = getNumUnknownNeighbours(x+i, y+i);
                        if(u == 1) return true;
                        
                        for(Vector2 v : m)
                        {
                            if(Math.abs(v.x - (x+i)) == 2 || Math.abs(v.y - (y+j)) == 2) {
                                return true;
                            }
                        }
                        m.add(new Vector2(x+i, y+i));
                    }
                }
            }
        }
        return numNeighboursWithStench > 2;
    }

    private int getNumNeighboursWithPit(int x, int y)
    {
        int n = 0;
        for(int i = -1; i < 2; i++) {
            for(int j = -1; j < 2; j++)
            {
                if(Math.abs(i) != Math.abs(j))
                {
                    if(this.kb.hasPit(x+i, y+j))
                    {
                        n++;
                    }
                }
            }
        }
        return n;
    }

    private int getNumUnknownNeighbours(int x, int y)
    {
        int n = 0;
        for(int i = -1; i < 2; i++) {
            for(int j = -1; j < 2; j++)
            {
                if(Math.abs(i) != Math.abs(j))
                {
                    if(this.kb.isUnknown(x+i, y+j)) 
                    {
                        n++;
                    }
                }
            }
        }
        return n;
    }
}