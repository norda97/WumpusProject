package wumpusworld.Imp;

import wumpusworld.World;
import java.util.List;
import java.util.ArrayList;

public class Env
{
    private World w;

    public Env()
    {
    }

    public void setWorld(World w)
    {
        this.w = w;
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
                    if(this.w.isUnknown(x+i, y+j) == false && this.w.isValidPosition(x+i, y+j))
                        legal &= this.w.hasBreeze(x+i, y+j);
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
                    if(this.w.isUnknown(x+i, y+j) == false && this.w.isValidPosition(x+i, y+j))
                        legal &= this.w.hasStench(x+i, y+j);
                }
            }
        return legal;
    }

    public boolean mustBePit(int x, int y)
    {
        if(isLegal(x, y, World.PIT) == false) return false;

        for(int i = -1; i < 2; i++) {
            for(int j = -1; j < 2; j++)
            {
                if(Math.abs(i) != Math.abs(j))
                {
                    if(this.w.hasBreeze(x+i, y+j))
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

    public boolean mustBeWumpus(int x, int y)
    {
        if(isLegal(x, y, World.WUMPUS) == false) return false;
        
        int numNeighboursWithStench = 0;
        for(int i = -1; i < 2; i++) {
            for(int j = -1; j < 2; j++)
            {
                if(Math.abs(i) != Math.abs(j))
                {
                    if(this.w.hasStench(x+i, y+j))
                    {
                        numNeighboursWithStench++;
                        int u = getNumUnknownNeighbours(x+i, y+i);
                        if(u == 1) return true;
                    }
                }
            }
        }
        return numNeighboursWithStench > 1;
    }

    private int getNumNeighboursWithPit(int x, int y)
    {
        int n = 0;
        for(int i = -1; i < 2; i++) {
            for(int j = -1; j < 2; j++)
            {
                if(Math.abs(i) != Math.abs(j))
                {
                    if(this.w.isValidPosition(x+i, y+j))
                    {
                        if(this.w.hasPit(x+i, y+j)) 
                            n++;
                        else if(this.w.isUnknown(x+i, y+j))
                            if(isLegal(x+i, y+j, World.PIT))
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
                    if(this.w.isUnknown(x+i, y+j)) 
                        n++;
                }
            }
        }
        return n;
    }
}