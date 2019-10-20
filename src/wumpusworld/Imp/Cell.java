package wumpusworld.Imp;
import java.util.List;

import wumpusworld.Imp.Fact.Type;

import java.util.ArrayList;

/**
 *
 * @author Adrian Nordin, Jonathan Åleskog, Daniel Cheh
 */
public class Cell {
    public List<Fact> facts;
    public Vector2 pos;
    public float probWump;
    public float probPit;
    
    public Cell(int x, int y) {
        this.facts = new ArrayList<Fact>();
        this.facts.add(new Fact(Type.UNKNOWN));
        this.pos = new Vector2(x, y);
        this.probWump = 0.0f;
        this.probPit = 0.0f;
    }
    
    public Cell clone() {
        Cell clone = new Cell(this.pos.x, this.pos.y);
        clone.probWump = this.probWump;
        clone.probPit = this.probPit;

        clone.facts = new ArrayList<Fact>();
        
        for (Fact f : this.facts)
            clone.facts.add(new Fact(f.type));
        
        return clone;
    }
    
    public void addFact(Fact f) {
        this.facts.add(f);
    }
    
    public String toString()
    {
        return "probPit: " + Float.toString(this.probPit) + ", wump: " + Float.toString(this.probWump) + ", Pos: " + this.pos.toString();
    }
    
}
