package wumpusworld;

import wumpusworld.Imp.KnowledgeBase;
import wumpusworld.Imp.Node;

import java.util.List;
import java.util.ArrayList;

import wumpusworld.Imp.*;

/**
 * Contains starting code for creating your own Wumpus World agent.
 * Currently the agent only make a random decision each turn.
 * 
 * @author Johan Hagelbäck
 */
public class MyAgent implements Agent
{
    private World w;
    int rnd;
    
    private KnowledgeBase kb;
    private ModelBatch mb;
    
    private List<Node> currPath;
    private int currPathIndex;
    
    /**
     * Creates a new instance of your solver agent.
     * 
     * @param world Current world state 
     */
    public MyAgent(World world)
    {
        this.w = world;
        
        this.kb = new KnowledgeBase(this.w);
        this.mb = new ModelBatch(this.kb);
        
        this.currPath = new ArrayList<Node>();
        this.currPathIndex = 0;
    }
   
            
    /**
     * Asks your solver agent to execute an action.
     */

    public void doAction()
    {
        //Location of the player
        int cX = w.getPlayerX();
        int cY = w.getPlayerY();
        
        //Basic action:
        //Grab Gold if we can.
        if (w.hasGlitter(cX, cY))
        {
            w.doAction(World.A_GRAB);
            return;
        }
        
        //Basic action:
        //We are in a pit. Climb up.
        if (w.isInPit())
        {
            w.doAction(World.A_CLIMB);
            return;
        }

        // Clear path if at the end and shoot if appropriate.
        if(this.currPathIndex == (this.currPath.size()-1)) {
            this.currPath.clear();
            if(kb.shouldTryShootingWumpus) {
                // Turn towards wumpus and shoot.
                shootTo(getDirection(cX, cY, kb.wumpusPos.x, kb.wumpusPos.y));
                System.out.println("Shoot!!!");
                if(w.wumpusAlive() == false) {
                    System.out.println("-->Wumpus was there and is now dead.");
                } else {
                    System.out.println("-->Wumpus was not there.");
                }
                kb.removeFact(kb.wumpusPos.x, kb.wumpusPos.y, Fact.Type.WUMPUS);
                Cell[] adj = kb.getAdjacent(kb.wumpusPos);
                for(int i = 0; i < 4; i++) {
                    if(adj[i] != null) {
                        if(kb.hasFact(adj[i].pos.x, adj[i].pos.y, Fact.Type.STENCH))
                            kb.removeFact(adj[i].pos.x, adj[i].pos.y, Fact.Type.STENCH);
                    }
                }
                kb.grid[kb.wumpusPos.x-1][kb.wumpusPos.y-1].probWump = 0.0f;
                w.probs[kb.wumpusPos.x-1][kb.wumpusPos.y-1][1] = 0.0f;
                kb.shouldTryShootingWumpus = false;
                kb.hasArrow = false;
            }
        }
        this.currPathIndex++;
        
        //Test the environment
        if (w.hasBreeze(cX, cY))
        {
            kb.addType(new Vector2(cX, cY), Fact.Type.BREEZE);
        }
        if (w.hasStench(cX, cY))
        {
            kb.addType(new Vector2(cX, cY), Fact.Type.STENCH);
        }
        if (w.hasPit(cX, cY))
        {
            kb.addType(new Vector2(cX, cY), Fact.Type.PIT);
            kb.knownPits++;
        }
        if(kb.hasFact(cX, cY, Fact.Type.UNKNOWN)) {
            kb.addType(new Vector2(cX, cY), Fact.Type.EMPTY);
            //System.out.println("Added empty at " + Integer.toString(cX) + ", " + Integer.toString(cY));
        }
        //if(kb.grid[cX-1][cY-1].unknown) {
        //    kb.addType(new Vector2(cX, cY), Fact.Type.EMPTY);
        //}
        
        // Update knowledgebase with current knowledge
        //kb.update();
        
        // Update GUI numbers
        boolean wumpusFound = false;
        for(Vector2 v : this.kb.Frontier) {
            Cell c = kb.grid[v.x-1][v.y-1];
            int numUnknowns = kb.getNumUnknowns();
            //System.out.println("P(P AND W): " + Double.toString(probPitAndWump));
            
            c.probWump = (float)this.mb.predict(3-kb.knownPits, !wumpusFound, numUnknowns, v.x, v.y, World.WUMPUS);
            if(c.probWump > 0.99999f) wumpusFound = true;
            
            c.probPit = (float)this.mb.predict(3-kb.knownPits, !wumpusFound, numUnknowns, v.x, v.y, World.PIT);
            float probPitAndWump = (float)this.mb.predict(3-kb.knownPits, !wumpusFound, numUnknowns, v.x, v.y, World.PIT + World.WUMPUS);
            if(probPitAndWump > c.probPit && probPitAndWump > c.probWump) {
                c.probPit = probPitAndWump;
                c.probWump = probPitAndWump;
            }
            
            w.probs[v.x-1][v.y-1][0] = c.probPit;
            w.probs[v.x-1][v.y-1][1] = c.probWump;
        }

        // Set all probabilities (except the one with a probability of 1.0) for wumpus to 0.0 if wumpus was found.
        if(wumpusFound) {
            for(Vector2 v : this.kb.Frontier) {
                if(kb.grid[v.x-1][v.y-1].probWump < 0.99999f) {
                    kb.grid[v.x-1][v.y-1].probWump = 0.0f;
                    w.probs[v.x-1][v.y-1][1] = 0.0f;
                }
            }
        }

        System.out.println("Frontier: ");
        for (Vector2 f : this.kb.Frontier)
        {
            Cell c = kb.grid[f.x-1][f.y-1];
            System.out.println("F: " + f.toString() + ", ProbPit: " + Float.toString(c.probPit) + ", ProbWump: " + Float.toString(c.probWump));
        }
        System.out.println("##########");
        
        if (currPath.isEmpty()) {
            Node[] startGoal = kb.calcPathData(cX, cY);        
            AStar.make(startGoal[1], startGoal[0], this.currPath);
            this.currPathIndex = 1;
        }

        System.out.println("Current path");
        for(int i = 0; i < this.currPath.size(); i++) {
            boolean isCP = this.currPathIndex == i;
            Node n = this.currPath.get(i);
            System.out.print("-> " + (isCP?"[":"") + n.index.toString() + (isCP?"] ":" "));
        }
        System.out.print("\n");
        
        if(this.currPath.size() != 1) {
            Node nextNode = this.currPath.get(this.currPathIndex);
            
            // Move to new block
            moveTo(getDirection(cX, cY, nextNode.index.x, nextNode.index.y));
            
            // Update frontier
            this.kb.updateFrontier(nextNode.index.x, nextNode.index.y);
        } else {
            //this.currPath.clear();
            this.currPathIndex = 0;
        }

        kb.reset();
    }
    
    private int getDirection(int cX, int cY, int nX, int nY) {
        int x = nX - cX;
        int y = nY - cY;

        if (x != 0) {
            if (x > 0)
                return World.DIR_RIGHT;
            else if(x < 0)
                return World.DIR_LEFT;
        }
        else if (y != 0) {
            if (y > 0)
                return World.DIR_UP;
            else if(y < 0)
                return World.DIR_DOWN;
        }
        System.out.println("Warning: The difference in direction is 0!");
        return World.DIR_DOWN;
    }
    
    private void shootTo(int dir)
    {
        // TODO: Turn as little as possible.
        for(int i = 0; i < 4; i++)
        {
            if(w.getDirection() == dir)
            {
                w.doAction(World.A_SHOOT);
                break;
            }
            else w.doAction(World.A_TURN_RIGHT);
        }
    }

    private void moveTo(int dir)
    {
        // TODO: Turn as little as possible.
        for(int i = 0; i < 4; i++)
        {
            if(w.getDirection() == dir)
            {
                w.doAction(World.A_MOVE);
                break;
            }
            else w.doAction(World.A_TURN_RIGHT);
        }
    }
     /**
     * Genertes a random instruction for the Agent.
     */
    public int decideRandomMove(int s)
    {
      return (int)(Math.random() * s);
    }
    
    
}

