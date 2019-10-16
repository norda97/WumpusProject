package wumpusworld;

import wumpusworld.Imp.KnowledgeBase;
import wumpusworld.Imp.Node;
import wumpusworld.Imp.Env;

import java.util.List;
import java.util.ArrayList;

import wumpusworld.Imp.AStar;
import wumpusworld.Imp.Cell;
import wumpusworld.Imp.Fact;
import wumpusworld.Imp.Vector2;
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
        this.kb = new KnowledgeBase(4);
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
        
        //Test the environment
        if (w.hasBreeze(cX, cY))
        {
            kb.addType(new Vector2(cX, cY), Fact.Type.BREEZE);
            System.out.println("I am in a Breeze");
        }
        if (w.hasStench(cX, cY))
        {
            kb.addType(new Vector2(cX, cY), Fact.Type.STENCH);
            System.out.println("I am in a Stench");
        }
        if (w.hasPit(cX, cY))
        {
            kb.addType(new Vector2(cX, cY), Fact.Type.PIT);
            System.out.println("I am in a Pit");
        }
        if(kb.grid[cX-1][cY-1].unknown) {
            kb.addType(new Vector2(cX, cY), Fact.Type.EMPTY);
            System.out.println("I am in a Empty");
        }
        if (w.getDirection() == World.DIR_RIGHT)
        {
            System.out.println("I am facing Right");
        }
        if (w.getDirection() == World.DIR_LEFT)
        {
            System.out.println("I am facing Left");
        }
        if (w.getDirection() == World.DIR_UP)
        {
            System.out.println("I am facing Up");
        }
        if (w.getDirection() == World.DIR_DOWN)
        {
            System.out.println("I am facing Down");
        }
        
        // Update knowledgebase with current knowledge
        kb.update();
        
        // Update GUI numbers
        for(int i = 0; i < kb.size; i++) {
            for(int j = 0; j < kb.size; j++) {
                Cell c = kb.grid[i][j];
                
                w.probs[i][j][0] = c.probPit;
                w.probs[i][j][1] = c.wump;
            }
        }
        
        if (currPath.isEmpty()) {
            kb.calcPathData(cX, cY);
            Node[] startGoal = kb.calcPathData(cX, cY);        
            AStar.make(startGoal[0], startGoal[1], this.currPath);
            this.currPathIndex = 1;
        }
        
        Node nextNode = this.currPath.get(this.currPathIndex);
        moveTo(getDirection(cX, cY, nextNode.index.x, nextNode.index.y));
        if(this.currPathIndex == (this.currPath.size()-1)) {
            this.currPath.clear();
        }
        this.currPathIndex++;

        
        /*
        List<Integer> good Moves = new ArrayList();
        for(int i = 0; i < 4; i++)
        {
            Cell c = neighbours[i];
            if(c != null)
            {
                if(c.safe)
                {
                    goodMoves.add(i);
                }
                else if (c.wump == 1 && (c.probPit > 0.0f && c.probPit <= 0.75f)) {
                    goodMoves.add(i);
                }
            }
        }        
       
        // Move to desired direction.
        int s = goodMoves.size();
        if (s > 0) {
            int d = goodMoves.get(decideRandomMove(s));
            moveTo(d);
        }
        */
        kb.reset();
    }
    
    private int getDirection(int cX, int cY, int nX, int nY) {
        Node nextNode = this.currPath.get(this.currPathIndex);
            
        int x = nextNode.index.x - cX;
        int y = nextNode.index.y - cY;

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
        System.out.println("Couldn't find dir!");
        return World.DIR_DOWN;
    }
    
    private void moveTo(int dir)
    {
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

