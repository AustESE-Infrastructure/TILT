/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tilt;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Area;

/**
 * General contract to manipulate shapes
 * @author desmond
 */
public interface Shape 
{
    public abstract void translate( Point previous, int x, int y );
    public abstract boolean contains( int x, int y );
    public abstract void paint( Graphics g, Color color, float scale );
    public abstract void updatePoint( Point previous, int x, int y );
    public abstract boolean isClosed();
    public abstract void addPoint( int x, int y, float scale );
    public abstract Point pointClicked( int x, int y, float scale );
    public abstract Rect getBounds();
    public abstract Area toArea();
    public abstract int getWordIndex();
    public abstract int getLineIndex();
    public abstract Point getCentre();
}
