package wumpusworld.Imp;

import wumpusworld.World;
import wumpusworld.Imp.*;
import java.util.ArrayList;
import java.util.List;

public class KnowledgeBase 
{       
    public Cell[][] grid;
    
    public int knownPits;
    public boolean wumpusFound;
    private List<Vector2> knownStenches; 
    private List<Vector2> knownBreezes; 
    public List<Vector2> Frontier; 
    public int size;

    public KnowledgeBase(World w) 
    {
        this.size = w.getSize();
        this.grid = new Cell[this.size][this.size];
        this.knownStenches = new ArrayList<Vector2>();
        this.knownBreezes = new ArrayList<Vector2>();
        this.Frontier = new ArrayList<Vector2>();
        
        // Player always start in (1, 1)
        this.Frontier.add(new Vector2(1, 2));
        this.Frontier.add(new Vector2(2, 1));
        
        this.knownPits = 0;
        this.wumpusFound = false;

        for (int x = 0; x < this.size; x++) {
                for (int y = 0; y < this.size; y++) {
                        grid[x][y] = new Cell(x+1, y+1);
                }
        }
    }
    
    public KnowledgeBase(KnowledgeBase other) {
        this.size = other.size;
        this.knownPits = other.knownPits;
        this.wumpusFound = other.wumpusFound;

        this.knownStenches = new ArrayList<Vector2>();
        this.knownBreezes = new ArrayList<Vector2>();
        this.Frontier = new ArrayList<Vector2>();
        
        for (Vector2 c : other.knownStenches)
            this.knownStenches.add(new Vector2(c.x, c.y));
        
        for (Vector2 c : other.knownBreezes)
            this.knownBreezes.add(new Vector2(c.x, c.y));
        
        for (Vector2 c : other.Frontier)
            this.Frontier.add(new Vector2(c.x, c.y));
        
        this.grid = new Cell[this.size][this.size];
        
        for (int x = 0; x < this.size; x++) {
                for (int y = 0; y < this.size; y++) {
                        grid[x][y] = other.grid[x][y].clone();
                }
        }
    }
    
    public boolean hasPit(int x, int y) {
         if (!isValidPosition(x, y))
            return false;
        Cell c = grid[x-1][y-1];
        
        boolean pit = false;
        for (Fact f : c.facts) 
            if (f.type == Fact.Type.PIT)
                pit = true;
        
        return pit;
    }
    
    public boolean hasStench(int x, int y) {
         if (!isValidPosition(x, y))
            return false;
        Cell c = grid[x-1][y-1];
        
        boolean res = false;
        for (Fact f : c.facts) 
            if (f.type == Fact.Type.STENCH)
                res = true;
        
        return res;
    }
    
    public boolean hasWumpus(int x, int y) {
         if (!isValidPosition(x, y))
            return false;
         
        Cell c = grid[x-1][y-1];
        boolean res = false;
        for (Fact f : c.facts) 
            if (f.type == Fact.Type.WUMPUS)
                res = true;
        
        return res;
    }
    
    public boolean hasBreeze(int x, int y) {
        if (!isValidPosition(x, y))
            return false;
        
        Cell c = grid[x-1][y-1];
        boolean res = false;
        for (Fact f : c.facts) 
            if (f.type == Fact.Type.BREEZE)
                res = true;
        
        return res;
    }
    
    public boolean isUnknown(int x, int y) {
         if (!isValidPosition(x, y))
            return false;
        Cell c = grid[x-1][y-1];
        
        return c.unknown;
    }
    
    public boolean isValidPosition(int x, int y) {
        if (x < 1) return false;
        if (y < 1) return false;
        if (x > size) return false;
        if (y > size) return false;
        return true;
    }
    
    public boolean addPit(int x, int y) {
        if (!hasPit(x, y)) {
            Cell c = grid[x-1][y-1];
            c.addFact(new Fact(Fact.Type.PIT));
            c.unknown = false;
            return true;
        }
        return false;
    }
    
    public boolean removePit(int x, int y) {
        Cell c = grid[x-1][y-1];
        for (int i = 0; i < c.facts.size(); i++) {
            Fact f = c.facts.get(i);
            if (f.type == Fact.Type.PIT) {
                c.facts.remove(i);
                c.unknown = false;
                return true;
            }
        }
        return false;
    }
    
    public boolean addWumpus(int x, int y) {
         if (!hasWumpus(x, y)) {
            Cell c = grid[x-1][y-1];
            c.addFact(new Fact(Fact.Type.WUMPUS));
            return true;
        }
        return false;
    }
    
    public boolean removeWumpus(int x, int y) {
        Cell c = grid[x-1][y-1];
        for (int i = 0; i < c.facts.size(); i++) {
            Fact f = c.facts.get(i);
            if (f.type == Fact.Type.WUMPUS) {
                c.facts.remove(i);
                return true;
            }
        }
        return false;
    }
    
    public Node[] calcPathData(int cx, int cy) {
        
        Cell start = grid[cx-1][cy-1];
        List<Node> visited = new ArrayList<Node>();
        Vector2 bestBet = new Vector2(start.pos.x, start.pos.y);
        Node[] result = new Node[2];
        result[0] = getSafeNeighbours(start, visited, bestBet);
        result[1] = new Node(bestBet.x, bestBet.y); // Goal Node
        
        System.out.println("Best bet: (" + bestBet.x + ","+ bestBet.y + ")");
        System.out.println("###########################\n");
        return result;
    }
    
    private Node getSafeNeighbours(Cell c, List<Node> visited, Vector2 bb) {
        
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
                        n = getSafeNeighbours(adj, visited, bb);
                    else   
                        n = new Node(adj.pos.x, adj.pos.y);
                    currNode.addNeighbour(n);    

                }
                else {
                    if (adj.probPit < grid[bb.x-1][bb.y-1].probPit || !grid[bb.x-1][bb.y-1].unknown) {
                        bb.x = adj.pos.x;
                        bb.y = adj.pos.y;
                        currNode.addNeighbour(new Node(adj.pos.x, adj.pos.y));
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
                grid[i][j].probWump = 0.f;
                grid[i][j].probPit = 0.f;
            }
        }
    }

    public Cell[] getAdjacent(Vector2 pos) {
        Cell[] neighbours = new Cell[4];
        
        if (isValidPosition(pos.x, pos.y+1)) // Up
            neighbours[0] = grid[(pos.x)-1][(pos.y+1)-1];
        if (isValidPosition(pos.x+1, pos.y)) // Right
            neighbours[1] = grid[(pos.x+1)-1][(pos.y)-1];
        if (isValidPosition(pos.x, pos.y-1)) // Down
            neighbours[2] = grid[(pos.x)-1][(pos.y-1)-1];
        if (isValidPosition(pos.x-1, pos.y)) // Left
            neighbours[3] = grid[(pos.x-1)-1][(pos.y)-1];

        return neighbours;
    }
    
    public void update() {
         for (int x = 1; x <= this.size; x++) {
            for (int y = 1; y <= this.size; y++) {
                this.calcProbs(x, y);
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
    
    // Returns -1 if not found else return index in array
    private int isFrontier(Vector2 pos) {
        int index = 0;
        for (Vector2 f : this.Frontier) {
            if (f.equals(pos))
                return index;
            
            index++;
        }
        return -1;
    }
    
    public void updateFrontier(int newX, int newY) {
        Cell newNode = this.grid[newX-1][newY-1];
        int frontierIndex = isFrontier(newNode.pos);
        if (frontierIndex != -1) {
            // Remove node from frontier
            this.Frontier.remove(frontierIndex);
            Cell[] adjacent = this.getAdjacent(newNode.pos);
            
            for (Cell adj : adjacent) {
                if (adj != null) {
                    if (adj.unknown && isFrontier(adj.pos) == -1)
                        this.Frontier.add(adj.pos);
                }
            }
        }
        System.out.println("Frontier: ");
        for (Vector2 f : this.Frontier)
            System.out.println("F: (" + f.x + ", " + f.y + ")");
        System.out.println("##########");
    }
    
    private void calcProbs(int x, int y) {
        
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
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private void handleStench(Fact f, Vector2 pos) {
        Cell[] adjacent = this.getAdjacent(pos);
        /*
        for (Cell adj : adjacent) {
            if (adj != null) {
                if (!adj.safe) {
                    adj.wump++;

                    if (adj.wump >= 3)
                        adj.addFact(new Fact(Fact.Type.WUMPUS));
               }
            }
        }*/
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
        
        ArrayList<Cell> unknownAdjacent = new ArrayList<Cell>();
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