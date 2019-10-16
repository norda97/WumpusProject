package wumpusworld.Imp;

import wumpusworld.Imp.Node;
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
    }
    
    public Node calcPathData(int cx, int cy) {
        
        Cell start = grid[cx-1][cy-1];
        List<Node> visited = new ArrayList();
        Vector2 bestBet = new Vector2(start.pos.x, start.pos.y);
        Node startNode = getNeighbours(start, visited, bestBet);
        
        System.out.println("Best bet: (" + bestBet.x + ","+ bestBet.y + ")");
        System.out.println("###########################\n");
        return startNode;
    }
    
    private Node getNeighbours(Cell c, List<Node> visited, Vector2 bb) {
        
        Cell[] adjacent = getAdjacent(new Vector2(c.pos.x, c.pos.y));
        
        Node currNode = new Node(c.pos.x, c.pos.y);
        visited.add(currNode);
        
        for (Cell adj : adjacent) {
            if (adj != null) {
                if (adj.safe) {
                    boolean alreadyExists = false;
                    for (int i = 0; i < visited.size(); i++) {
                        Node n = visited.get(i);

                        if(adj.pos.x == n.index.x &&
                                adj.pos.y == n.index.y) {
                            alreadyExists = true;
                        }
                    }
                    Node n;
                    if (!alreadyExists)
                        n = getNeighbours(adj, visited, bb);
                    else   
                        n = new Node(adj.pos.x, adj.pos.y);
                    currNode.addNeighbour(n);    

                }
                else {
                    if (adj.probPit < grid[bb.x-1][bb.y-1].probPit) {
                        bb.x = adj.pos.x;
                        bb.y = adj.pos.y;
                    }
                }
            }
        }
        System.out.println("Added (" + currNode.index.x +
                                ", " + currNode.index.y + ")");
        
        for (int i = 0; i < currNode.neighbours.size(); i++) {
            Node n = currNode.neighbours.get(i);
            System.out.println("    Neigh(" + n.index.x +
                                ", " + n.index.y + ")");
        }
        
        return currNode;
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
                    case EMPTY:
                        handleEmpty(f, c.pos);
                        break;
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
                if (!adj.safe) {
                    adj.wump++;

                    if (adj.wump >= 3)
                        adj.addFact(new Fact(Fact.Type.WUMPUS));
               }
            }
        }
    }
    
    private void handleEmpty(Fact f, Vector2 pos) {
        Cell[] adjacent = this.getAdjacent(pos);
        for (Cell adj : adjacent) {
            if (adj != null && adj.unknown && !adj.safe) {
                adj.safe = true;
            }
        }
    }
    
    private void handleBreeze(Fact f, Vector2 pos) {
        Cell[] adjacent = this.getAdjacent(pos);
        
        ArrayList<Cell> unknownAdjacent = new ArrayList();
        for (Cell adj : adjacent) {
            if (adj != null && adj.unknown) {
                unknownAdjacent.add(adj);
            }
        }
        
        int unknownCount = unknownAdjacent.size();
        for (int i = 0; i < unknownCount; i++) {
            Cell unknown = unknownAdjacent.get(i);
            float newPerc =  1.f/unknownCount;
            if (unknown.probPit < newPerc)
                unknown.probPit = 1.f/unknownCount;
        }
    }
}