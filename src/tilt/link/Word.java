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
     * Select a shape
     * @param shape the shape to select
     */
    public void select( Shape shape )
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
