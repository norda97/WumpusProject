package wumpusworld.Imp;

import java.util.ArrayList;
import java.util.List;

public class KnowledgeBase 
{       
    public List<Fact> factGrid = new ArrayList<Fact>();
    public int size;

    public KnowledgeBase(int gridSize) 
    {
                    this.size = gridSize;

                    for (int x = 1; x <= this.size; x++) {
                            for (int y = 1; y <= this.size; y++) {
                                    factGrid.add(new Fact(Fact.Type.UNKNOWN,new Vector2(x, y)));
                            }
                    }

                    factGrid.get(0).type = Fact.Type.EMPTY;
    }
    
    private List<Fact> getAdjacent(Vector2 pos) {
        List<Fact> neighbours = new ArrayList<Fact>();

        if (pos.x+1 < this.size)
                neighbours.add(factGrid.get(pos.x+1 + pos.y * this.size));
        if (pos.x-1 >= 0)
                neighbours.add(factGrid.get(pos.x-1 + pos.y * this.size));
        if (pos.y+1 < this.size)
                neighbours.add(factGrid.get(pos.x + (pos.y+1) * this.size));
        if (pos.y-1 >= 0)
                neighbours.add(factGrid.get(pos.x + (pos.y-1) * this.size));

        return neighbours;
    }
    
    public void update(Vector2 pos, Fact.Type foundType) {
        factGrid.get((pos.x-1) + (pos.y-1) * this.size).type = foundType;

         for (int x = 0; x < this.size; x++) {
                for (int y = 0; y < this.size; y++) {
                        Fact f = factGrid.get(x + y * this.size);


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
        List<Fact> adjacent = this.getAdjacent(f.pos);
        
        for (Fact adj : adjacent) {
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