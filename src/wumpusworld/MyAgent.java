package wumpusworld;

import wumpusworld.Imp.KnowledgeBase;

import java.util.List;
import java.util.ArrayList;

import wumpusworld.Imp.Fact;
import wumpusworld.Imp.Vector2;
import wumpusworld.Imp.Fact.Type;
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
    
    /**
     * Creates a new instance of your solver agent.
     * 
     * @param world Current world state 
     */
    public MyAgent(World world)
    {
        w = world;
        this.kb = new KnowledgeBase(4);
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
            kb.update(new Vector2(cX, cY), Fact.Type.BREEZE);
            System.out.println("I am in a Breeze");
        }
        if (w.hasStench(cX, cY))
        {
            kb.update(new Vector2(cX, cY), Fact.Type.STENCH);
            System.out.println("I am in a Stench");
        }
        if (w.hasPit(cX, cY))
        {
            kb.factGrid[cX-1][cY-1].type = Fact.Type.PIT;
            System.out.println("I am in a Pit");
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
        
        // Update GUI numbers
        for(int i = 0; i < kb.size; i++) {
            for(int j = 0; j < kb.size; j++) {
                Fact f = kb.factGrid[i][j];
                
                w.probs[i][j][0] = f.probStench;
                w.probs[i][j][1] = f.wump;
            }
        }

        Fact[] neighbours = kb.getAdjacent(new Vector2(cX, cY));
        
        List<Integer> goodMoves = new ArrayList<Integer>();
        for(int i = 0; i < 4; i++)
        {
            Fact f = neighbours[i];
            if(f != null)
            {
                if(f.wump <= 0 && f.probStench <= 0.0f)
                {
                    goodMoves.add(i);
                }
            }
        }        

        // Move to desired direction.
        int s = goodMoves.size();
        int d = goodMoves.get(decideRandomMove(s));
        moveTo(d);

        kb.reset();

        //decide next move
        /*rnd = decideRandomMove();
        if (rnd==0)
        {
            w.doAction(World.A_TURN_LEFT);
            w.doAction(World.A_MOVE);
        }
        
        if (rnd==1)
        {
            w.doAction(World.A_MOVE);
        }
                
        if (rnd==2)
        {
            w.doAction(World.A_TURN_LEFT);
            w.doAction(World.A_TURN_LEFT);
            w.doAction(World.A_MOVE);
        }
                        
        if (rnd==3)
        {
            w.doAction(World.A_TURN_RIGHT);
            w.doAction(World.A_MOVE);
        }*/
                
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

