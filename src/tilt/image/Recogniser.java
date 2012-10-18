/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tilt.image;

import tilt.Shape;
import tilt.Rect;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.awt.geom.Area;
import java.awt.Rectangle;


/**
 * Recognise words, lines and pages
 * @author desmond
 */
public class Recogniser 
{
    public static final int BY_WORD = 1;
    public static final int BY_LINE = 2;
    private int lineHeight;
    /** out local pointer to the bw image in the ImagePanel */
    private BufferedImage bw;
    /** the current scale of the ImagePanel */
    private float scale;
    WritableRaster raster;
    
    public Recogniser( BufferedImage bw, float scale, int lineHeight )
    {
        this.bw = bw;
        this.scale = scale;
        this.lineHeight = lineHeight;
        raster = bw.getRaster();
    }
    /**
     * Recognise a single word starting from a given point within the word.
     * @param x the scaled x-coordinate of the click
     * @param y the scaled y coordinate of the click
     * @return a scaled shape enclosing the word
     */
    public Rectangle recogniseWord( int x, int y )
    {
        int absX = Math.round((float)x/scale);
        int absY = Math.round((float)y/scale);
        // average a square around the click-point
        int square = lineHeight/6;
        int sqx = absX-square/2;
        int sqy = absY-square/2;
        Area a = new Area();
        // 210 is magic for now. later compute it from average intensity
        Square core = new Square( raster, a, sqx, sqy, square, square, 210 );
        int cpAverage = core.average();
        if ( cpAverage < 210 )
            core.add();
        core.propagate();
        return core.getRect();
    }
    /**
     * Recognise a single word starting from a single point on the line
     * @param x the scaled x-coordinate of the click
     * @param y the scaled y coordinate of the click
     * @return a scaled shape enclosing the line
     */
    public Shape recogniseLine( int x, int y )
    {
        return null;
    }
    /**
     * Recognise an entire page
     * @param pageText the text of the page to aid recognition
     * @return an array of recognised and scaled shapes
     */
    public Shape[] recognisePage( String pageText, int level )
    {
        return null;
    }
}
