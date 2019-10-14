package wumpusworld.Imp;
/**
 *
 * @author norda
 */
public class Vector2 {
    public int x = 0;
    public int y = 0;
    
    public Vector2(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public String toString() 
    {
        return "(" + Integer.toString(this.x) + ", " + Integer.toString(this.y) + ")"; 
    }
    
}
