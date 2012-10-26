/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tilt;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;

/**
 * Ordinary rectangle shape, with handles
 * @author desmond
 */
public class Rect extends Rectangle implements Shape
{
    static float TRANSPARENCY_FACTOR = 0.3f;
    int wordIndex;
    int lineIndex;
    /**
     * Create a new Rect with no width or height
     * @param x its global x-location
     * @param y  its global y-location
     */
    public Rect( int x, int y )
    {
        super( x, y, 0, 0 );
    }
    /**
     * Create a new Rect with a given width or height
     * @param x its global x-location
     * @param y  its global y-location
     * @param width the width of the rectangle
     * @param height the height of the rectangle
     */
    public Rect( int x, int y, int width, int height )
    {
        super( x, y, width, height );
    }
    /**
     * Compute the centre of this rectangle
     * @return a point
     */
    public Point getCentre()
    {
        return new Point( x+width/2, y+height/2 );
    }
    /**
     * Was a handle on this rectangle clicked?
     * @param x the global x coordinate of the click
     * @param y the global y coordinate of the click
     * @param scale the scale of the image
     * @return the handle's point if it was clicked or null
     */
    public Point pointClicked( int x, int y, float scale )
    {
        int range = Math.round(3/scale);
        if ( Math.abs(this.x+width/2-x)<=range && Math.abs(this.y-y)<=range )
            return new Point( this.x+width/2, this.y );
        if ( Math.abs(this.x-x)<=range && Math.abs(this.y+height/2-y)<=3 )
            return new Point( this.x,this.y/2);
        if ( Math.abs((this.x+width/2)-x)<=range && Math.abs(this.y+height-y)<=range )
            return new Point( this.x+width/2, this.y+height );
        if ( Math.abs(this.x+width-x)<=range && Math.abs(this.y+height/2-y)<=range )
            return new Point( this.x+width, this.y+height/2 );
        return null;
    }
    /**
     * Move the entire rectangle from a previous point to a new point
     * @param previous the previous point of drag
     * @param x the new global x-position
     * @param y the new global y-position
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
     * @param x the global x-coordinate of the click
     * @param y the global y-coordinate of the click
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
     * @param scale the scale to draw everything in
     */
    public void paint( Graphics g, Color colour, float scale )
    {
        Color old = g.getColor();
        g.setColor( colour );
        int localX = Math.round( x*scale );
        int localY = Math.round( y*scale );
        int localWidth = Math.round(width*scale);
        int localHeight = Math.round(height*scale);
        g.drawRect( localX, localY, localWidth, localHeight );
        Color fill = Utils.makeTransparent( colour, TRANSPARENCY_FACTOR );
        g.setColor( fill );
        g.fillRect( localX, localY, localWidth, localHeight );
        if ( colour.equals(Utils.RED) )
        {
            g.setColor( colour );
            g.fillRect( localX+localWidth/2-3, localY-3, 6, 6 ); // top
            g.fillRect( localX-3, localY+localHeight/2-3, 6, 6 ); // left
            g.fillRect( localX+(localWidth/2)-3, localY+localHeight-3, 6, 6 ); // bottom
            g.fillRect( localX+localWidth-3, localY+localHeight/2-3, 6, 6 ); // right
        }
        g.setColor( old );
    }
    /**
     * Move the bottom right corner
     * @param previous the previous global drag-point
     * @param x new global x-coordinate
     * @param y new global y coordinate
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
     * @param scale the scale of the image
     */
    public void addPoint( int x, int y, float scale )
    {
        // do nothing
    }
    /**
     * The bounds of a rect is itself
     * @return a rectangle
     */
    public Rect getBounds()
    {
        return this;
    }
    /**
     * Convert to an Area
     * @return java.awt.geom.Area
     */
    public java.awt.geom.Area toArea()
    {
        return new Area( this );
    }
    /**
     * Set the index of the word in the line
     * @param index the index of the word in the line
     */
    public void setWordIndex( int index )
    {
        wordIndex = index;
    }
    /**
     * Set the index of the line we are from
     * @param index the index of the line on the page 
     */
    public void setLineIndex( int index )
    {
        lineIndex = index;
    }
    /**
     * Get the index of the word in the line
     * @return -1 or the index 
     */
    public int getWordIndex()
    {
        return wordIndex;
    }
    /**
     * Get the index of the line we are from
     * @return -1 or the index 
     */
    public int getLineIndex()
    {
        return lineIndex;
    }
}
