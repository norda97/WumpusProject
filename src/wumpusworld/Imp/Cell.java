/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wumpusworld.Imp;
import java.util.List;
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
    public boolean unknown;
    public boolean safe;
    
    public Cell(int x, int y) {
        this.facts = new ArrayList<Fact>();
        this.pos = new Vector2(x, y);
        this.probWump = 0.0f;
        this.probPit = 0.0f;
        this.unknown = true;
        this.safe = false;
    }
    
    public void addFact(Fact f) {
        this.facts.add(f);
        this.unknown = false;
        if (f.type == Fact.Type.EMPTY)
            this.safe = true;
    }
    
    public String toString()
    {
        return "probPit: " + Float.toString(this.probPit) + ", wump: " + Float.toString(this.probWump) + ", Pos: " + this.pos.toString();
    }
    
}
