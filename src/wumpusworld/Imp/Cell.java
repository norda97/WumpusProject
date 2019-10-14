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
    public int wump;
    public float probPit;
    public boolean unknown;
    
    public Cell(int x, int y) {
        this.facts = new ArrayList();
        this.pos = new Vector2(x, y);
        this.wump = 0;
        this.probPit = 0.0f;
        this.unknown = true;
    }
    
    public void addFact(Fact f) {
        this.facts.add(f);
        this.unknown = false;
    }
    
    public String toString()
    {
        return "probPit: " + Float.toString(this.probPit) + ", wump: " + Integer.toString(this.wump) + ", Pos: " + this.pos.toString();
    }
    
}
