/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tilt.link;
import tilt.Shape;

/**
 * Represent a word and its optional shape
 * @author desmond
 */
public class Word 
{
    int start;
    int len;
    Shape shape;
    /**
     * Create a new word
     * @param start the start offset in the text
     * @param len the length of the word (for highlighting)
     */
    public Word( int start, int len )
    {
        this.start = start;
        this.len = len;
    }
    /**
     * Do we have an assigned shape?
     * @return true if we do
     */
    public boolean hasShape()
    {
        return this.shape != null;
    }
    /**
     * Get our shape
     * @return the shape we have been assigned
     */
    public Shape getShape()
    {
        return this.shape;
    }
    /**
     * Assign a shape to this word
     * @param shape the shape to assign
     */
    public void assignShape( Shape shape )
    {
        this.shape = shape;
    }
    /**
     * Get the length of this word
     * @return an int
     */
    public int length()
    {
        return len;
    }
}
