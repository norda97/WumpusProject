package wumpusworld.Imp;

import java.util.ArrayList;
import java.util.List;

public class KnowledgeBase 
{       
    public Cell[][] grid;
    private List<Vector2> knownStenches; 
    private List<Vector2> knownBreezes; 
    public int size;

    public KnowledgeBase(int gridSize) 
    {
        this.size = gridSize;
        this.grid = new Cell[gridSize][gridSize];
        this.knownStenches = new ArrayList();
        this.knownBreezes = new ArrayList();

        for (int x = 0; x < this.size; x++) {
                for (int y = 0; y < this.size; y++) {
                        grid[x][y] = new Cell(x+1, y+1);
                }
        }

        grid[0][0].addFact(new Fact(Fact.Type.EMPTY));
    }
    
    public void reset()
    {
        for(int i = 0; i < this.size; i++) {
            for(int j = 0; j < this.size; j++) {
                grid[i][j].wump = 0;
                grid[i][j].probPit = 0.0f;
            }
        }
    }

    public Cell[] getAdjacent(Vector2 pos) {
        Cell[] neighbours = new Cell[4];
        
        if (pos.y+1 <= this.size) // Up
            neighbours[0] = grid[(pos.x)-1][(pos.y+1)-1];
        if (pos.x+1 <= this.size) // Right
            neighbours[1] = grid[(pos.x+1)-1][(pos.y)-1];
        if (pos.y-1 >= 1) // Down
            neighbours[2] = grid[(pos.x)-1][(pos.y-1)-1];
        if (pos.x-1 >= 1) // Left
            neighbours[3] = grid[(pos.x-1)-1][(pos.y)-1];

        return neighbours;
    }
    
    public void update() {
         for (int x = 0; x < this.size; x++) {
                for (int y = 0; y < this.size; y++) {
                       handleFacts(grid[x][y]);
                }
        }   
    }
    
    // Only adds fact if it isn't already added
    public void addType(Vector2 pos, Fact.Type foundType) {
        boolean alreadyAdded = false;
        Cell c = grid[pos.x-1][pos.y-1];
        for (int i = 0; i < c.facts.size(); i++) {
            if (c.facts.get(i).type == foundType)
                alreadyAdded = true;
        }
        
        if (!alreadyAdded) {
            grid[pos.x-1][pos.y-1].addFact(new Fact(foundType));
            
            if (foundType == Fact.Type.STENCH)
                this.knownStenches.add(new Vector2(pos.x-1 , pos.y-1));
            if (foundType == Fact.Type.BREEZE)
                this.knownBreezes.add(new Vector2(pos.x-1 , pos.y-1));
        }
           
    }

    private void handleFacts(Cell c) {
        
        if (c.unknown == false) {
            for (int i = 0; i < c.facts.size(); i++) {
                Fact f = c.facts.get(i);

                switch(f.type) {
                    case STENCH:
                        handleStench(f, c.pos);
                        break;
                    case BREEZE:
                        handleBreeze(f, c.pos);
                        break;
                    case PIT:
                        f.type = Fact.Type.PIT;
                        c.probPit = 1.0f;
                        break;
                    case WUMPUS:
                        c.wump = 3;
                        break;
                }
            }
        }
    }

    private void handleStench(Fact f, Vector2 pos) {
        Cell[] adjacent = this.getAdjacent(pos);
        
        for (Cell adj : adjacent) {
            if (adj != null) {
                if (adj.unknown) {
                    adj.wump++;

                    if (adj.wump >= 3)
                        adj.addFact(new Fact(Fact.Type.WUMPUS));
               }
            }
        }
    }
    
    private void handleBreeze(Fact f, Vector2 pos) {
        Cell[] adjacent = this.getAdjacent(pos);
        
        int unknownCount = 0;
        for (Cell adj : adjacent) {
            if (adj != null && adj.unknown) {
                unknownCount++;
            }
        }
        
        for (Cell adj : adjacent) {
            if (adj != null && adj.unknown) {
                adj.probPit += 1.f/unknownCount;
                if (adj.probPit >= 1.0f)
                    adj.addFact(new Fact(Fact.Type.PIT));
            }
        }
    }
}