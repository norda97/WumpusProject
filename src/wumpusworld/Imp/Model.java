package wumpusworld.Imp;

import java.util.*;

import wumpusworld.World;
import wumpusworld.Imp.Vector2;

public class Model
{
    public String[][] grid;
    private List<Vector2> frontier;

    public Model(KnowledgeBase kb)
    {
        this.grid = new String[4][4];
        fillGrid(kb);
        this.frontier = new ArrayList<Vector2>();
    }

    private void fillGrid(KnowledgeBase kb)
    {
        for(int y = 0; y < 4; y++) {
            for(int x = 0; x < 4; x++) {
                this.grid[x][y] = new String("");
                for(Fact f : kb.grid[x][y].facts) {
                    switch(f.type) {
                        case BREEZE:
                            this.grid[x][y] += World.BREEZE;
                            break;
                        case STENCH:
                            this.grid[x][y] += World.STENCH;
                            break;
                        case PIT:
                            this.grid[x][y] += World.PIT;
                            break;
                        case WUMPUS:
                            this.grid[x][y] += World.WUMPUS;
                            break;
                        case EMPTY:
                            this.grid[x][y] = "";
                            break;
                        case UNKNOWN:
                            this.grid[x][y] += World.UNKNOWN;
                            break;
                    }
                }
            }
        }
    }

    public void print()
    {
        for(int y = 3; y >= 0; y--) {
            for(int x = 0; x < 4; x++) {
                if(this.grid[x][y].isEmpty())
                    System.out.print("0 ");
                else System.out.print(this.grid[x][y] + " ");
            }
            System.out.println("|");
        }
    }

    public double getProbability(double probP, double probW)
    {
        double probability = 1.0;
        for(Vector2 v : this.frontier) {
            if(is(v.x, v.y, World.PIT))
                probability *= probP;
            else if(is(v.x, v.y, World.WUMPUS))
                probability *= probW;
            else
                probability *= (1.0 - probP)*(1.0 - probW);
        }
        return probability;
    }

    public void addType(int x, int y, String type)
    {
        this.grid[x-1][y-1] = this.grid[x-1][y-1].replaceAll(World.UNKNOWN, "");
        this.grid[x-1][y-1] += type;
        addEmpty(x, y);
    }

    public void addEmpty(int x, int y)
    {
        boolean exist = false;
        for(Vector2 v : this.frontier) {
            if(v.x == x && v.y == y)
                exist = true;
        }
        if(!exist)
            this.frontier.add(new Vector2(x, y));
    }

    public boolean isLegal()
    {
        convertFrontier();
        //print();
        
        boolean legal = true;
        for(int x = 0; x < 4; x++) {
            for(int y = 0; y < 4; y++) {
                if(is(x+1, y+1, World.STENCH)) {
                    legal = isCellLegalOR(x+1, y+1, World.WUMPUS);
                    if(legal == false) {
                        return false;
                    }
                }
                if(is(x+1, y+1, World.BREEZE)) {
                    legal = isCellLegalOR(x+1, y+1, World.PIT);
                    if(legal == false) {
                        return false;
                    }
                }
                if(is(x+1, y+1, World.PIT)) {
                    legal = isCellLegalAND(x+1, y+1, World.BREEZE);
                    if(legal == false)  {
                        return false;
                    }
                }
                if(is(x+1, y+1, World.WUMPUS)) {
                    legal = isCellLegalAND(x+1, y+1, World.STENCH);
                    if(legal == false)  {
                        return false;
                    }
                }
            }   
        }
        return true;
    }

    private boolean is(int x, int y, String type)
    {
        return this.grid[x-1][y-1].contains(type);
    }

    private void convertFrontier()
    {
        //System.out.print("Frontier: ");
        for(Vector2 v : this.frontier) {
            //System.out.print(v.toString() + " ");
            if(isCellLegalOR(v.x, v.y, World.PIT))
                this.grid[v.x-1][v.y-1] += World.BREEZE;
            if(isCellLegalOR(v.x, v.y, World.WUMPUS))
                this.grid[v.x-1][v.y-1] += World.STENCH;
            if(is(v.x, v.y, World.UNKNOWN))
                this.grid[v.x-1][v.y-1] = this.grid[v.x-1][v.y-1].replaceAll(World.UNKNOWN, "");
        }
        //System.out.print("\n");
    }

    private boolean isCellLegalAND(int x, int y, String type) 
    {
        boolean legal = true;
        for(int i = -1; i < 2; i++) {
            for(int j = -1; j < 2; j++) {
                if(Math.abs(i) != Math.abs(j)) {
                    if(isPositionLegal(x+i, y+j) && !is(x+i, y+j, World.UNKNOWN)) {
                        legal = legal && is(x+i, y+j, type); 
                    }    
                }
            }
        }
        return legal;
    }

    private boolean isCellLegalOR(int x, int y, String type) 
    {
        boolean legal = false;
        for(int i = -1; i < 2; i++) {
            for(int j = -1; j < 2; j++) {
                if(Math.abs(i) != Math.abs(j)) {
                    if(isPositionLegal(x+i, y+j) && !is(x+i, y+j, World.UNKNOWN)) {
                        legal = legal || is(x+i, y+j, type); 
                    }    
                }
            }
        }
        return legal;
    }

    private boolean isPositionLegal(int x, int y)
    {
        if(x < 1 || x > 4) return false;
        if(y < 1 || y > 4) return false;
        return true;
    }
}