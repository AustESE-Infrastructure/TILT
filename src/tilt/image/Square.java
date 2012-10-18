/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tilt.image;

import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.image.WritableRaster;
import java.awt.Polygon;
import java.util.ArrayList;
/**
 * Manipulate a square of pixels starting at a given point in the top-left
 * @author desmond
 */
public class Square 
{
    int width;
    int height;
    int x;
    int y;
    int threshold;
    Area area;
    WritableRaster raster;
    public Square( WritableRaster raster, Area area, int x, int y, 
        int width, int height, int threshold )
    {
        this.raster = raster;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.area = area;
        this.threshold = threshold;
    }
    /**
     * Alternative constructor
     * @param raster the raster
     * @param area the area
     * @param r a java awt Rectangle
     */
    public Square( WritableRaster raster, Area area, Rectangle r )
    {
        this.raster = raster;
        this.x = r.x;
        this.y = r.y;
        this.width = r.width;
        this.height = r.height;
        this.area = area;
    }
    /**
     * Split an existing Square into at most four sub-rectangles
     * @return an array of up to 4 sub-Squares
     */
    Square[] split()
    {
        ArrayList<Square> splits = new ArrayList<Square>();
        if ( x > width && y > height )
        {
            int topHeight,leftWidth,botHeight,rightWidth;
            Square sq;
            topHeight = height/2;
            leftWidth = width/2;
            botHeight = height-topHeight;
            rightWidth = width-leftWidth;
            // try to create top-left rectangle
            Rectangle r = new Rectangle( x, y, leftWidth, topHeight );
            if ( !area.contains(r) )
            {
                sq = new Square(this.raster, this.area, r );
                splits.add( sq );
            }
            // try to create top-right rectangle
            r = new Rectangle( x+leftWidth, y, rightWidth, topHeight );
            if ( !area.contains(r) )
            {
                sq = new Square(this.raster, this.area, r );
                splits.add( sq );
            }
            // bot-left
            r = new Rectangle( x,y+topHeight, leftWidth,botHeight );
            if ( !area.contains(r) )
            {
                sq = new Square(this.raster, this.area, r );
                splits.add( sq );
            }
            // bot-right
            r = new Rectangle( x+leftWidth,y+topHeight, rightWidth,botHeight );
            if ( !area.contains(r) )
            {
                sq = new Square(this.raster, this.area, r );
                splits.add( sq );
            }
        }
        Square[] sArray = new Square[splits.size()];
        splits.toArray( sArray );
        return sArray;
    }
    /**
     * Add ourselves to the area
     */
    void add()
    {
        Rectangle r = new Rectangle( x, y, width, height );
        area.add( new Area(r) );
    }
    /**
     * Propagate a rectangle
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param w the rect-width
     * @param h the rect-height
     */
    private void propagateRect( int x, int y, int w, int h )
    {
        Rectangle r = new Rectangle(x,y,w,h);
        if ( !area.contains(r) )
        {
            Square s = new Square( raster, area, x, y, w, h, threshold );
            int avg = s.average();
            if ( avg < threshold )
            {
                s.add();
                s.propagate();
            }
            else
            {
                // try subdividing
                Square[] sArray = s.split();
                boolean added = false;
                for ( int i=0;i<sArray.length;i++ )
                {
                    if ( sArray[i].average()<threshold )
                    {
                        sArray[i].add();
                        added = true;
                    }
                }
                if ( added )
                    s.propagate();
            }
        }
    }
    /**
     * Try to expand to the left, right, up and down
     */
    public void propagate()
    {
        // top
            
        if ( y > 0 )
        {
            int topY = Math.max(y-height,0);
            propagateRect( x, topY, width, y-topY );
        }
        // left
        if ( x > 0 )
        {
            int leftX = Math.max(x-width,0);
            propagateRect( leftX, y, x-leftX, height );
        }
        // bottom
        if ( y < raster.getHeight() )
        {
            int botY = y+height;
            int botHeight = Math.min(botY+height,raster.getHeight())-botY;
            propagateRect( x, botY, width, botHeight );
        }
        // right
        if ( x < raster.getWidth() )
        {
            int rightX = x+width;
            int rightWidth = Math.min(rightX+width,raster.getWidth())-rightX;
            propagateRect( rightX, y, rightWidth, height );
        }
    }
    /**
     * Get the average Pixel intensity
     * @return an int
     */
    public int average()
    {
        int total=0;
        int[] dArray = new int[width*height];
        raster.getPixels( x, y, width, height, dArray );
        for ( int i=0;i<dArray.length;i++ )
            total += dArray[i];
        return total/dArray.length;
    }
    /**
     * Get a simple rectangle surrounding this object's area
     * @return a Rectangle
     */
    Rectangle getRect()
    {
        return area.getBounds();
    }
    /**
     * Get a possibly smooth polygon of this object's area
     * @return a Polygon
     */
    Polygon getPolygon()
    {
        return null;
    }
}
