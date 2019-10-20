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
                boolean succeeded = !w.wumpusAlive();
                if(succeeded) {
                    Cell[] adj = kb.getAdjacent(kb.wumpusPos);
                    for(int i = 0; i < 4; i++) {
                        if(adj[i] != null) {
                            if(kb.hasFact(adj[i].pos.x, adj[i].pos.y, Fact.Type.STENCH))
                                kb.removeFact(adj[i].pos.x, adj[i].pos.y, Fact.Type.STENCH);
                        }
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
        }

        // Update GUI numbers
        boolean wumpusFound = false;
        for(Vector2 v : this.kb.frontier) {
            Cell c = kb.grid[v.x-1][v.y-1];
            int numUnknowns = kb.getNumUnknowns();
            
            // Get the probability of it being a wumpus at the cell.
            c.probWump = (float)this.mb.predict(3-kb.knownPits, !wumpusFound, numUnknowns, v.x, v.y, World.WUMPUS);
            // If certain it is a wumpus, tell the rest of the cells that the wumpus was found.
            if(c.probWump > 0.99999f) wumpusFound = true;
            
            // Get the probability of it being a pit at the cell. 
            c.probPit = (float)this.mb.predict(3-kb.knownPits, !wumpusFound, numUnknowns, v.x, v.y, World.PIT);

            // Get the probability of it being both a pit and a wumpus at the cell.
            float probPitAndWump = (float)this.mb.predict(3-kb.knownPits, !wumpusFound, numUnknowns, v.x, v.y, World.PIT + World.WUMPUS);
            
            // If it is more likly to be both than one => set it to the new probability.
            if(probPitAndWump > c.probPit && probPitAndWump > c.probWump) {
                c.probPit = probPitAndWump;
                c.probWump = probPitAndWump;
            }
            
            // Update GUI
            w.probs[v.x-1][v.y-1][0] = c.probPit;
            w.probs[v.x-1][v.y-1][1] = c.probWump;
        }

        // Set all probabilities (except the one with a probability of 1.0) for wumpus to 0.0 if wumpus was found.
        if(wumpusFound) {
            for(Vector2 v : this.kb.frontier) {
                if(kb.grid[v.x-1][v.y-1].probWump < 0.99999f) {
                    kb.grid[v.x-1][v.y-1].probWump = 0.0f;
                    w.probs[v.x-1][v.y-1][1] = 0.0f;
                }
            }
        }

        // Calculate new path if reached the end of the old one.
        if (currPath.isEmpty()) {
            Node[] startGoal = kb.calcPathData(cX, cY);        
            AStar.make(startGoal[1], startGoal[0], this.currPath);
            this.currPathIndex = 1;
        }

        // Only move if more than the starting node is in the path.
        if(this.currPath.size() != 1) {
            Node nextNode = this.currPath.get(this.currPathIndex);
            
            // Move to new block
            moveTo(getDirection(cX, cY, nextNode.index.x, nextNode.index.y));
            
            // Update frontier
            this.kb.updateFrontier(nextNode.index.x, nextNode.index.y);
        } else {
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

