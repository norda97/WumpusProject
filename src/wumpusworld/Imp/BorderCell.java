package wumpusworld.Imp;

import wumpusworld.Imp.Vector2;

/**
 *
 * @author Adrian Nordin, Jonathan Åleskog, Daniel Cheh
 */
public class BorderCell
{
    public Vector2 v;
    boolean active;

    public BorderCell(Vector2 v, boolean active)
    {
        this.v = v;
        this.active = active;
    }
}