/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wumpusworld.Imp;
import wumpusworld.Imp.Vector2;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import wumpusworld.Imp.Node;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;

/**
 *
 * @author Adrian Nordin, Jonathan Åleskog, Daniel Cheh
 */

 public class AStar
 {
    public static void make(Node start, Node goal, List<Node> path)
    {      
        Set<Node> openSet = new HashSet<Node>();
        openSet.add(start);
        Set<Node> closedSet = new HashSet<Node>();

        Map<Node, Node> cameFrom = new HashMap<Node, Node>();

        // Cost to chepest path from start to n.
        Map<Node, Float> gScore = new HashMap<Node, Float>();
        gScore.put(start, 0.0f);

        // f(n) = g(n) + h(n)
        Map<Node, Float>  fScore = new HashMap<Node, Float>();
        fScore.put(start, h(start, goal));
        
        while(openSet.isEmpty() == false)
        {
            // Get the node which has lowest fScore.
            Node current = getLowest(openSet, fScore);
            if(current == goal)
            {
                // Return path.
                getPath(cameFrom, current, path);
                return;
            }

            openSet.remove(current);
            closedSet.add(current);

            List<Node> neighbours = current.neighbours;
            for(Node n : neighbours)
            {
                if(closedSet.contains(n))
                {
                    continue;
                }
                
                float tmpGScore = gScore.get(current) + 1.0f;
                
                float g = Float.MAX_VALUE;
                if(gScore.containsKey(n))
                    g = gScore.get(n);

                if(tmpGScore < g)
                {
                    cameFrom.put(n, current);
                    gScore.put(n, tmpGScore);
                    fScore.put(n, tmpGScore + h(n, goal));
                    if(openSet.contains(n) == false)
                        openSet.add(n);
                }
            }
        }
        return;
    }

    private static Node getLowest(Set<Node> set, Map<Node, Float> map)
    {
        float lo = Float.MAX_VALUE;
        Node loNode = null;
        for(Node n : set)
        {
            float value = map.get(n); 
            if(value < lo)
            {
                lo = value;
                loNode = n;
            }
        }
        return loNode;
    }

    private static void getPath(Map<Node, Node> cameFrom, Node current, List<Node> path)
    {
        path.add(current);
        while(cameFrom.containsKey(current))
        {
            current = cameFrom.get(current);
            path.add(current);
        }
        Collections.reverse(path);
    }

    private static float h(Node a, Node b)
    {
        Vector2 v = a.index;
        Vector2 u = b.index;
        return (float)Math.sqrt((float)(v.x*u.x) + (float)(v.y*u.y));
    }
 }