/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tilt.image;

/**
 * Position where a word is suspected in the image
 * @author desmond
 */
public class WordBase 
{
    /** the global x-coordinate */
    public int x;
    /** the global y-coordinate */
    public int y;
    /** length in pixels */
    public int len;
    /** index of the word's base within bases */
    public int index;
    public WordBase( int x, int y, int len )
    {
        this.x = x;
        this.y = y;
        this.len = len;
        index = -1;
    }
}
