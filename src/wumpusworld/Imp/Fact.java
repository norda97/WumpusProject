/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wumpusworld.Imp;

/**
 *
 * @author Adrian Nordin, Jonathan Åleskog, Daniel Cheh
 */
public class Fact {
        
    public enum Type {
        EMPTY,
        BREEZE,
        STENCH,
        WUMPUS,
        PIT,
        GLITTER,
        UNKNOWN
    }
    
    public Vector2 pos;
    public Type type;
    public int wump;
    public float probPit;
    
    public Fact(Type type, Vector2 pos) {
        this.type = type;
        this.pos = pos;
        this.probPit = 0.f;
        this.wump = 0;
    }

    public String toString()
    {
        return "Type: " + type.name() + ", probPit: " + Float.toString(this.probPit) + ", wump: " + Integer.toString(this.wump) + ", Pos: " + this.pos.toString();
    }
}
