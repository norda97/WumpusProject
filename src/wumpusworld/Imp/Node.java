/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wumpusworld.Imp;

import java.util.List;
import java.util.ArrayList;
import wumpusworld.Imp.Vector2;
/**
 *
 * @author Adrian Nordin, Jonathan Åleskog, Daniel Cheh
 */
public class Node {
    public List<Node> neighbours;
    public Vector2 index;
   
    public Node(int x, int y) {
        this.index = new Vector2(x, y);
        this.neighbours = new ArrayList<Node>();
    }
    
    public void addNeighbour(Node n) {
        this.neighbours.add(n);
    }

    public String toString()
    {
        return this.index.toString() + ", #Neighbours: " + this.neighbours.size(); 
    }
}
