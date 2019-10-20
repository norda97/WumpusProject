package wumpusworld.Imp;

import wumpusworld.World;
import wumpusworld.Imp.Vector2;
import wumpusworld.Imp.Cell;
import java.util.ArrayList;
import java.util.List;

public class KnowledgeBase 
{       
    public Cell[][] grid;
    
    public int knownPits;
    public boolean wumpusFound;
    //private List<Vector2> knownStenches; 
    //private List<Vector2> knownBreezes; 
    public List<Vector2> frontier; 
    public int size;
    
    public boolean shouldTryShootingWumpus = false;
    public boolean hasArrow = true;
    public Vector2 wumpusPos;

    public KnowledgeBase(World w) 
    {
        this.size = w.getSize();
        this.grid = new Cell[this.size][this.size];
        //this.knownStenches = new ArrayList<Vector2>();
        //this.knownBreezes = new ArrayList<Vector2>();
        this.frontier = new ArrayList<Vector2>();
        
        // Player always start in (1, 1)
        this.frontier.add(new Vector2(1, 2));
        this.frontier.add(new Vector2(2, 1));
        
        this.knownPits = 0;
        //this.wumpusFound = false;

        for (int x = 0; x < this.size; x++) {
            for (int y = 0; y < this.size; y++) {
                    this.grid[x][y] = new Cell(x+1, y+1);
            }
        }
    }
    
    public KnowledgeBase(KnowledgeBase other) {
        this.size = other.size;
        this.knownPits = other.knownPits;
        this.wumpusFound = other.wumpusFound;

        //this.knownStenches = new ArrayList<Vector2>();
        //this.knownBreezes = new ArrayList<Vector2>();
        this.frontier = new ArrayList<Vector2>();
        
        //for (Vector2 c : other.knownStenches)
        //    this.knownStenches.add(new Vector2(c.x, c.y));
        
        //for (Vector2 c : other.knownBreezes)
        //    this.knownBreezes.add(new Vector2(c.x, c.y));
        
        for (Vector2 c : other.frontier)
            this.frontier.add(new Vector2(c.x, c.y));
        
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
        return hasFact(c.pos.x, c.pos.y, Fact.Type.PIT);
    }
    
    /*
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
    */
    
    public boolean hasWumpus(int x, int y) {
         if (!isValidPosition(x, y))
            return false;
        Cell c = grid[x-1][y-1];
        return hasFact(c.pos.x, c.pos.y, Fact.Type.WUMPUS);
    }
    
    /*
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
    */
    /*
    public boolean isUnknown(int x, int y) {
         if (!isValidPosition(x, y))
            return false;
        Cell c = grid[x-1][y-1];
        
        return c.unknown;
    }
    */
    
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
            removeFact(x, y, Fact.Type.UNKNOWN);
            return true;
        }
        return false;
    }
    
    public boolean removePit(int x, int y) {
        removeFact(x, y, Fact.Type.PIT);
        removeFact(x, y, Fact.Type.UNKNOWN);
        return false;
    }
    
    public boolean addWumpus(int x, int y) {
         if (!hasWumpus(x, y)) {
            Cell c = grid[x-1][y-1];
            c.addFact(new Fact(Fact.Type.WUMPUS));
            removeFact(x, y, Fact.Type.UNKNOWN);
            return true;
        }
        return false;
    }
    
    public boolean removeWumpus(int x, int y) {
        removeFact(x, y, Fact.Type.WUMPUS);
        removeFact(x, y, Fact.Type.UNKNOWN);
        return false;
    }

    public int getNumUnknowns() {
        int n = 0;
        for(int x = 0; x < 4; x++) {
            for(int y = 0; y < 4; y++) {
                if(hasFact(x+1, y+1, Fact.Type.UNKNOWN))
                    n++;
            }   
        }
        return n;
    }

    public Node[] calcPathData(int x, int y) {
        Node[][] nodes = new Node[4][4];

        // Get best position (one with the smallest probabilities of wumpus and pit).
        // And the cell with the highest probability of a wumpus.
        final int n = this.frontier.size();
        Vector2 bestBet = this.frontier.get(0);
        Vector2 bestWump = bestBet;
        for(int i = 1; i < n; i++) {
            Vector2 f = this.frontier.get(i);
            Cell best = this.grid[bestBet.x-1][bestBet.y-1];
            Cell bestW = this.grid[bestWump.x-1][bestWump.y-1];
            Cell other = this.grid[f.x-1][f.y-1];
            // Pick the cell with the highest probability of a wumpus.
            if(bestW.probWump < other.probWump) bestWump = other.pos;
            // Compare the highest probability of the two cells, and pick the one with the lowest probability.
            if(Math.max(best.probPit, best.probWump) > Math.max(other.probPit, other.probWump))
                bestBet = other.pos;
        }

        // If uncertain and a wumpus is likely to be on a cell, then try to shoot it.
        Cell best = this.grid[bestBet.x-1][bestBet.y-1];
        double prob = Math.max(best.probPit, best.probWump);
        if(prob > 0.000001 && this.grid[bestWump.x-1][bestWump.y-1].probWump > 0.00001) {
            // Try to shoot wumpus!
            if(hasArrow) {
                this.shouldTryShootingWumpus = true;
                this.wumpusPos = new Vector2(bestWump.x, bestWump.y);
            }
        }

        // Add all known cells and the target cell. 
        for(int i = 0; i < 4; i++) {
            for(int j = 0; j < 4; j++) {
                if(!hasFact(i+1, j+1, Fact.Type.UNKNOWN) || (i == bestBet.x-1 && j == bestBet.y-1))
                    nodes[i][j] = new Node(i+1, j+1);
            }
        }

        // Add neighbours to each cell which will be in the search graph.
        for(int i = 0; i < 4; i++) {
            for(int j = 0; j < 4; j++) {
                if(!hasFact(i+1, j+1, Fact.Type.UNKNOWN) || (i == bestBet.x-1 && j == bestBet.y-1)) {
                    // Only add cells which is in a neighbouring position, is valid and is in the search graph. 
                    if (isValidPosition(i+1, j+2))
                        if(nodes[i][j+1] != null) // Up
                            nodes[i][j].neighbours.add(nodes[i][j+1]);
                    if (isValidPosition(i+2, j+1))
                        if(nodes[i+1][j] != null) // Right
                            nodes[i][j].neighbours.add(nodes[i+1][j]);
                    if (isValidPosition(i+1, j))
                        if(nodes[i][j-1] != null) // Down
                            nodes[i][j].neighbours.add(nodes[i][j-1]);
                    if (isValidPosition(i, j+1))
                        if(nodes[i-1][j] != null) // Left
                            nodes[i][j].neighbours.add(nodes[i-1][j]);
                }
            }
        }

        // Try to find a position close to wumpus.
        if(this.shouldTryShootingWumpus) {
            List<Node> neighbours = new ArrayList<Node>();
            Node wn = nodes[wumpusPos.x-1][wumpusPos.y-1];
            // If wumpus is in the search graph, fetch its neighbours.
            if(wn != null)
                neighbours = wn.neighbours;
            else {
                // If it was not in the search graph, fetch each neighbour one by one if they exist.
                int i = wumpusPos.x-1;
                int j = wumpusPos.y-1;
                if (isValidPosition(i+1, j+2))
                    if(nodes[i][j+1] != null) // Up
                        neighbours.add(nodes[i][j+1]);
                if (isValidPosition(i+2, j+1))
                    if(nodes[i+1][j] != null) // Right
                        neighbours.add(nodes[i+1][j]);
                if (isValidPosition(i+1, j))
                    if(nodes[i][j-1] != null) // Down
                        neighbours.add(nodes[i][j-1]);
                if (isValidPosition(i, j+1))
                    if(nodes[i-1][j] != null) // Left
                        neighbours.add(nodes[i-1][j]);
            }
            // TODO: Should pick the closest position from current.
            bestBet = neighbours.get(0).index;
            System.out.println("Should shoot wumpus from position " + bestBet.toString() + " towards position " + wumpusPos.toString());
        }

        // Add the starting node and the target to the path.
        Node[] result = new Node[2];
        result[1] = nodes[x-1][y-1];
        result[0] = nodes[bestBet.x-1][bestBet.y-1];
        
        System.out.println("Best bet: (" + bestBet.x + ","+ bestBet.y + ")");
        System.out.println("###########################\n");
        /*
        Cell start = grid[cx-1][cy-1];
        List<Node> visited = new ArrayList<Node>();
        Vector2 bestBet = new Vector2(start.pos.x, start.pos.y);
        Node[] result = new Node[2];
        result[0] = getSafeNeighbours(start, visited, bestBet);
        result[1] = new Node(bestBet.x, bestBet.y); // Goal Node
        
        System.out.println("Best bet: (" + bestBet.x + ","+ bestBet.y + ")");
        System.out.println("###########################\n");
        */
        return result;
    }
    /*
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
                    if (Math.max(adj.probPit, adj.probWump) < Math.max(grid[bb.x-1][bb.y-1].probPit, grid[bb.x-1][bb.y-1].probWump) && !grid[bb.x-1][bb.y-1].unknown) {
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
    }*/
    
    public void reset()
    {
        for(int i = 0; i < this.size; i++) {
            for(int j = 0; j < this.size; j++) {
                grid[i][j].probWump = 1.f;
                grid[i][j].probPit = 1.f;
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
    /*
    public void update() {
         for (int x = 1; x <= this.size; x++) {
            for (int y = 1; y <= this.size; y++) {
                this.calcProbs(x, y);
            }
        }   
    }
    */
    public void removeFact(int x, int y, Fact.Type type) {
        Cell c = grid[x-1][y-1];
        for (int i = 0; i < c.facts.size(); i++) {
            Fact f = c.facts.get(i);
            if (f.type == type) {
                c.facts.remove(i);
            }
        }
    } 

    public boolean hasFact(int x, int y, Fact.Type type) {
        Cell c = this.grid[x-1][y-1];
        for (int i = 0; i < c.facts.size(); i++) {
            Fact f = c.facts.get(i);
            if (f.type == type) {
                return true;
            }
        }
        return false;
    }

    // Only adds fact if it isn't already added
    public void addType(Vector2 pos, Fact.Type foundType) {
        if(foundType != Fact.Type.UNKNOWN) removeFact(pos.x, pos.y, Fact.Type.UNKNOWN);
        boolean alreadyAdded = hasFact(pos.x, pos.y, foundType);
        /*Cell c = grid[pos.x-1][pos.y-1];
        for (int i = 0; i < c.facts.size(); i++) {
            if (c.facts.get(i).type == foundType)
                alreadyAdded = true;
        }*/
        
        if (!alreadyAdded) {
            grid[pos.x-1][pos.y-1].addFact(new Fact(foundType));
            /*
            if (foundType == Fact.Type.STENCH)
                this.knownStenches.add(new Vector2(pos.x-1 , pos.y-1));
            if (foundType == Fact.Type.BREEZE)
                this.knownBreezes.add(new Vector2(pos.x-1 , pos.y-1));*/
        }
           
    }
    
    // Returns -1 if not found else return index in array
    private int isFrontier(Vector2 pos) {
        int index = 0;
        for (Vector2 f : this.frontier) {
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
            this.frontier.remove(frontierIndex);
            Cell[] adjacent = this.getAdjacent(newNode.pos);
            
            for (Cell adj : adjacent) {
                if (adj != null) {
                    if (isFrontier(adj.pos) == -1 && hasFact(adj.pos.x, adj.pos.y, Fact.Type.UNKNOWN))
                        this.frontier.add(adj.pos);
                }
            }
        }
    }
    /*
    private void calcProbs(int x, int y) {
        
    }*/
    /*
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
    }*/
}