package wumpusworld.Imp;

import java.util.ArrayList;
import java.util.List;

public class KnowledgeBase 
{       
    public Fact[][] factGrid;
    public int size;

    public KnowledgeBase(int gridSize) 
    {
        this.size = gridSize;
        this.factGrid = new Fact[4][4];

        for (int x = 0; x < this.size; x++) {
                for (int y = 0; y < this.size; y++) {
                        factGrid[x][y] = new Fact(Fact.Type.UNKNOWN, new Vector2(x+1, y+1));
                }
        }

        factGrid[0][0].type = Fact.Type.EMPTY;
    }
    
    private Fact[] getAdjacent(Vector2 pos) {
        Fact[] neighbours = new Fact[4];

        if (pos.x+1 <= this.size)
                neighbours[0] = factGrid[(pos.x+1)-1][(pos.y)-1];
        if (pos.x-1 >= 1)
                neighbours[1] = factGrid[(pos.x-1)-1][(pos.y)-1];
        if (pos.y+1 <= this.size)
                neighbours[2] = factGrid[(pos.x)-1][(pos.y+1)-1];
        if (pos.y-1 >= 1)
                neighbours[3] = factGrid[(pos.x)-1][(pos.y-1)-1];

        return neighbours;
    }
    
    public void update(Vector2 pos, Fact.Type foundType) {
        factGrid[pos.x-1][pos.y-1].type = foundType;

         for (int x = 0; x < this.size; x++) {
                for (int y = 0; y < this.size; y++) {
                       handleFact(factGrid[x][y]);
                }
        }   
    }

    private void handleFact(Fact f) {
        if (f.type != Fact.Type.UNKNOWN) {
            switch(f.type) {
                case STENCH:
                    handleStench(f);
                    break;
                case BREEZE:

                    break;
            }

        }
    }

    private void handleStench(Fact f) {
        Fact[] adjacent = this.getAdjacent(f.pos);
        
        for (Fact adj : adjacent) {
            if (adj != null) {
                switch(adj.type) {
                   case UNKNOWN:
                       adj.wump++;

                       if (adj.wump >= 2) {
                           adj.type = Fact.Type.WUMPUS;
                       }
                       break;
               }
            }
        }
    }
}