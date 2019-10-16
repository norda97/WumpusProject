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
    
    public boolean equals(Vector2 other) {
        int a = this.x - other.x;
        int b = this.y - other.y;
        
        if (a == 0 && b == 0)
            return true;
        return false;
    }
    
    public String toString() 
    {
        return "(" + Integer.toString(this.x) + ", " + Integer.toString(this.y) + ")"; 
    }
    
}
