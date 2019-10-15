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
        UNKNOWN
    }
    
    public Type type;
    
    public Fact(Type type) {
        this.type = type;
    }

    public String toString()
    {
        return "Type: " + type.name();
    }
}
