/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tilt;
import java.awt.Color;
import java.util.ArrayList;
import java.awt.Polygon;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
/**
 * A polygon which remembers the first and last coordinates
 * @author desmond
 */
public class Region extends ArrayList<Point> implements Shape
{
    /** is the path closed */
    boolean closed;
    /** equivalent awt polygon for hit-testing */
    Polygon poly;
    /** index into word on line used to create us */
    int wordIndex;
    /** index into lie on page used t create us */
    int lineIndex;
    /** amount of transparency for display */
    static float TRANSPARENCY_FACTOR = 0.3f;
    public Region()
    {
        super();
        wordIndex = lineIndex = -1;
    }
    /**
     * Alternative constructor
     * @param x global x-coordinate of first point
     * @param y global the y-coordinate
     * @param scale the current scale
     */
    public Region( int x, int y, float scale )
    {
        super();
        addPoint( x, y, scale );
        wordIndex = lineIndex = -1;
    }
    /**
     * Recompute the polygon after changing the points
     */
    private void computePoly()
    {
        int[] xPoints = new int[size()];
        int[] yPoints = new int[size()];
        for ( int i=0;i<size();i++ )
        {
            Point p = get( i );
            xPoints[i] = p.x;
            yPoints[i] = p.y;
        }
        poly = new Polygon( xPoints, yPoints, size() );
    }
    /**
     * Compute the centre of this rectangle
     * @return a point that is the approximate centre
     */
    public Point getCentre()
    {
        Rectangle r = poly.getBounds();
        return new Point( r.x+r.width/2, r.y+r.height/2 );
    }
    /**
     * Add a point taking note of whether this is the first
     * @param x the global x-coordinate
     * @param y the global y-coordinate
     * @param scale the scale of the image
     */
    public void addPoint( int x, int y, float scale )
    {
        if ( size() > 1 )
        {
            int range = Math.round(3/scale);
            Point first = get( 0 );
            if ( Math.abs(x-first.x)<= range && Math.abs(y-first.y)<=range )
            {
                x = first.x;
                y = first.y;
                closed = true;
                computePoly();
            }
        }
        add( new Point(x,y) );
    }
    /**
     * Has the user clicked inside one of our points?
     * @param x the global x-coordinate
     * @param y the global y-coordinate
     * @param scale the scale of the image
     * @return the point clicked inside or null
     */
    public Point pointClicked( int x, int y, float scale )
    {
        int range = Math.round(3/scale);
        for ( int i=0;i<size();i++ )
        {
            Point p = get( i );
            if ( Math.abs(x-p.x)<=range && Math.abs(y-p.y)<=range )
                 return p;
        }
        return null;
    }
    /**
     * Update the position of a point
     * @param dragged the point in global coordinates that was dragged
     * @param x its new global x-coordinate
     * @param y its new global y-coordinate
     */
    public void updatePoint( Point dragged, int x, int y )
    {
        for ( int i=0;i<size();i++ )
        {
            Point p = get( i );
            if ( p.equals(dragged) )
            {
                p.x = x;
                p.y = y;
                if ( closed )
                {
                    if ( i==size()-1 )
                    {
                        Point q = get( 0 );
                        q.x = x;
                        q.y = y;
                    }
                    else if ( i==0 )
                    {
                        Point q = get( size()-1 );
                        q.x = x;
                        q.y = y;
                    }
                }
                computePoly();
                break;
            }
        }
    }   
    /**
     * Move the entire region from one point to another
     * @param previous the point it was last at
     * @param x the new global x-coordinate of the drag
     * @param y the new global y coordinate of the drag
     */
    @Override
    public void translate( Point previous, int x, int y )
    {
        int deltaX = x-previous.x;
        int deltaY = y-previous.y;
        for ( int i=0;i<size();i++ )
        {
            Point p = get( i );
            p.x += deltaX;
            p.y += deltaY;
        }
        computePoly();
    }
    /**
     * Surrogate method for our polygon
     * @param x the global x-coordinate to test
     * @param y the global y-coordinate to test
     * @return true if x,y is in the polygon that represents us
     */
    public boolean contains( int x, int y )
    {
        if ( poly != null )
            return poly.contains( x, y );
        return false;
    }
    /**
     * Is this region closed?
     * @return true if it is 
     */
    public boolean isClosed()
    {
        return closed;
    }
    /**
     * Paint the region in its graphic context
     * @param g the context
     * @param color the color to paint it
     * @param scale the scale we are currently drawing at
     */
    public void paint( Graphics g, Color color, float scale )
    {
        Color old = g.getColor();
        g.setColor( color );
        int lastX = -1;
        int lastY = -1;
        int[] xPoints = new int[size()];
        int[] yPoints = new int[size()];
        for ( int i=0;i<size();i++ )
        {
            Point p = get( i );
            int localX = Math.round( p.x*scale );
            int localY = Math.round( p.y*scale );
            xPoints[i] = localX;
            yPoints[i] = localY;
            if ( lastX != -1 )
                g.drawLine(lastX,lastY,localX,localY);
            lastX = localX;
            lastY = localY;
            if ( color.equals(Utils.RED) )
                g.drawOval(localX-3, localY-3, 6, 6);
        }
        if ( closed )
        {
            Polygon localPoly = new Polygon( xPoints, yPoints, size() );
            g.setColor( Utils.CLEAR );
            g.fillPolygon( localPoly );
            Color newColor = Utils.makeTransparent(color,TRANSPARENCY_FACTOR);
            g.setColor( newColor );
            g.fillPolygon( localPoly );
        }
        g.setColor( old );
    }
    /**
     * Compute the area of the Polygon
     * @return an int
     */
    public int area()
    {
        int area = 0;
        for (int i=0;i<size()-2; i++)
        {
            Point p = get( i );
            Point q = get( i+1 );
            area += (p.x*q.y)-(q.x*p.y);
        }
        area /= 2;
        // if they enter points counterclockwise 
        // the area will be negative but correct.
        if ( area < 0 )
            area *= -1;
        return area;
    }
    /**
     * Get the bounds of this polygon
     * @return a Rectangle
     */
    public Rect getBounds()
    {
        int top,left,right,bottom;
        bottom = right = Integer.MIN_VALUE;
        top = left = Integer.MAX_VALUE;
        for ( int i=0;i<size();i++ )
        {
            Point p = get( i );
            if ( p.x < left )
                left = p.x;
            if ( p.y < top )
                top = p.y;
            if ( p.y > bottom )
                bottom = p.y;
            if ( p.x > right )
                right = p.x;
        }
        return new Rect( left, top, right-left, bottom-top );
    }
    /**
     * Convert to an Area
     * @return java.awt.geom.Area
     */
    public Area toArea()
    {
        Polygon p = new Polygon();
        for ( int i=0;i<size();i++ )
        {
            Point pt = get( i );
            p.addPoint( pt.x, pt.y );
        }
        return new Area( p );
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
