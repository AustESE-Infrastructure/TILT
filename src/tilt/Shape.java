/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tilt;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

/**
 * General contract to manipulate shapes
 * @author desmond
 */
public interface Shape 
{
    public abstract void translate( Point previous, int x, int y );
    public abstract boolean contains( int x, int y );
    public abstract void paint( Graphics g, Color color );
    public abstract void updatePoint( Point previous, int x, int y );
    public abstract boolean isClosed();
    public abstract void addPoint( int x, int y );
    public abstract Point pointClicked( int x, int y );
    public abstract void scale( float scale );
    public abstract Rect getBounds();
}
