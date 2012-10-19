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
/**
 * A polygon which remembers the first and last coordinates
 * @author desmond
 */
public class Region extends ArrayList<Point> implements Shape
{
    boolean closed;
    Polygon poly;
    static float TRANSPARENCY_FACTOR = 0.3f;
    public Region()
    {
        super();
    }
    /**
     * Alternative constructor
     * @param x x-coordinate of first point
     * @param y the y-coordinate
     */
    public Region( int x, int y )
    {
        super();
        addPoint( x, y );
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
     * Add a point taking note of whether this is the first
     */
    public void addPoint( int x, int y )
    {
        if ( size() > 1 )
        {
            Point first = get( 0 );
            if ( Math.abs(x-first.x)<= 3 && Math.abs(y-first.y)<=3 )
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
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return the point clicked inside or null
     */
    public Point pointClicked( int x, int y )
    {
        for ( int i=0;i<size();i++ )
        {
            Point p = get( i );
            if ( Math.abs(x-p.x)<=3 && Math.abs(y-p.y)<=3 )
                 return p;
        }
        return null;
    }
    /**
     * Update the position of a point
     * @param dragged the point that was dragged
     * @param x its new x-coordinate
     * @param y its new y-coordinate
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
     * @param x the new x-coordinate of the drag
     * @param y the new y coordinate of the drag
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
     * @param x the x-coordinate to test
     * @param y the y-coordinate to test
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
     */
    public void paint( Graphics g, Color color )
    {
        Color old = g.getColor();
        g.setColor( color );
        int lastX = -1;
        int lastY = -1;
        for ( int i=0;i<size();i++ )
        {
            Point p = get( i );
            if ( lastX != -1 )
                g.drawLine(lastX,lastY,p.x,p.y);
            lastX = p.x;
            lastY = p.y;
            if ( color.equals(Utils.RED) )
                g.drawOval(p.x-3, p.y-3, 6, 6);
        }
        if ( closed )
        {
            g.setColor( Utils.CLEAR );
            g.fillPolygon( poly );
            Color newColor = Utils.makeTransparent(color,TRANSPARENCY_FACTOR);
            g.setColor( newColor );
            g.fillPolygon( poly );
        }
        g.setColor( old );
    }
    /**
     * Scale everything
     * @param scale the scale to set it by
     */
    public void scale( float scale )
    {
        for ( int i=0;i<size();i++ )
        {
            Point p = get( i );
            p.x *= scale;
            p.y *= scale;
        }
        computePoly();
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
}
