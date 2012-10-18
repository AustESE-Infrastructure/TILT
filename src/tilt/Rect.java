/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tilt;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

/**
 * Ordinary rectangle shape, with handles
 * @author desmond
 */
public class Rect extends Rectangle implements Shape
{
    static float TRANSPARENCY_FACTOR = 0.3f;
    /**
     * Create a new Rect with no width or height
     * @param x its x-location
     * @param y  its y-location
     */
    public Rect( int x, int y )
    {
        super( x, y, 0, 0 );
    }
    /**
     * Create a new Rect with a given width or height
     * @param x its x-location
     * @param y  its y-location
     * @param width the width of the rectangle
     * @param height the height of the rectangle
     */
    public Rect( int x, int y, int width, int height )
    {
        super( x, y, width, height );
    }
    /**
     * Initialise from an existing java.awt.Rectangle
     * @param r the rect in awt
     */
    public Rect( Rectangle r )
    {
        super( r.x, r.y, r.width, r.height );
    }
    /**
     * Was a handle on this rectangle clicked?
     * @param x the x coordinate of the click
     * @param y the y coordinate of the click
     * @return the handle's point if it was clicked or null
     */
    public Point pointClicked( int x, int y )
    {
        if ( Math.abs(this.x+width/2-x)<=3 && Math.abs(this.y-y)<=3 )
            return new Point( this.x+width/2, this.y );
        if ( Math.abs(this.x-x)<=3 && Math.abs(this.y+height/2-y)<=3 )
            return new Point( this.x,this.y/2);
        if ( Math.abs((this.x+width/2)-x)<=3 && Math.abs(this.y+height-y)<=3 )
            return new Point( this.x+width/2, this.y+height );
        if ( Math.abs(this.x+width-x)<=3 && Math.abs(this.y+height/2-y)<=3 )
            return new Point( this.x+width, this.y+height/2 );
        return null;
    }
    /**
     * Move the entire rectangle from a previous point to a new point
     * @param previous the previous point of drag
     * @param x the new x-position
     * @param y the new y-position
     */
    public void translate( Point previous, int x, int y )
    {
        int deltaX = x-previous.x;
        int deltaY = y-previous.y;
        this.x += deltaX;
        this.y += deltaY;
    }
    /**
     * Does this shape contain a given point?
     * @param x the x-coordinate of the click
     * @param y the y-coordinate of the click
     * @return true if it was inside, else false
     */
    public boolean contains( int x, int y )
    {
        return super.contains( x,y );
    }
    /**
     * Paint the rectangle and fill it with a colour
     * @param g the graphics environment for drawing
     * @param colour the color to use for lines and transparent for fill
     */
    public void paint( Graphics g, Color colour )
    {
        Color old = g.getColor();
        g.setColor( colour );
        g.drawRect( x, y, width, height );
        Color fill = Utils.makeTransparent( colour, TRANSPARENCY_FACTOR );
        g.setColor( fill );
        g.fillRect(x, y, width, height );
        if ( colour.equals(Utils.RED) )
        {
            g.setColor( colour );
            g.fillRect( x+width/2-3, y-3, 6, 6 ); // top
            g.fillRect( x-3, y+height/2-3, 6, 6 ); // left
            g.fillRect( x+(width/2)-3, y+height-3, 6, 6 ); // bottom
            g.fillRect( x+width-3, y+height/2-3, 6, 6 ); // right
        }
        g.setColor( old );
    }
    /**
     * Move the bottom right corner
     * @param previous the previous drag-point
     * @param x new x-coordinate
     * @param y new y coordinate
     */
    public void updatePoint( Point previous, int x, int y )
    {
        int deltaY = y-previous.y;
        int deltaX = x-previous.x;
        // first test if we are in top left
        if ( previous.x==this.x && previous.y==this.y )
        {
            this.width += deltaX;
            this.height += deltaY;
        }
        else if ( previous.x==this.x+width&& previous.y==this.y+height )
        {
            this.width += deltaX;
            this.height += deltaY;
        }
        else if ( previous.y == this.y )
        {
            this.height -= deltaY;
            this.y = y;
        }
        else if ( previous.y == this.y+height )
            this.height += deltaY;
        else if ( previous.x == this.x )
        {
            this.width -= deltaX;
            this.x = x;
        }
        else if ( previous.x == this.x+width )
            this.width += deltaX;
        // other cases should not arise
    }
    /**
     * Is this shape closed?
     * @return true in all cases, since rectangles are always closed
     */
    public boolean isClosed()
    {
        return true;
    }
    /**
     * Add a point to a rectangle
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public void addPoint( int x, int y )
    {
        // do nothing
    }
    /**
     * Scale everything
     * @param scale the scale to set it by
     */
    public void scale( float scale )
    {
        this.x *= scale;
        this.y *= scale;
        this.width *= scale;
        this.height *= scale;
    }
    /**
     * The bounds of a rect is itself
     * @return a rectangle
     */
    public Rect getBounds()
    {
        return this;
    }
}
