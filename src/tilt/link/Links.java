/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tilt.link;
import java.util.ArrayList;
import tilt.Shape;

/**
 * Manage the links between the facsimiles and the text
 * @author desmond
 */
public class Links extends ArrayList<Page>
{
    /**
     * Recompute the line-breaks
     * @param text the current text of the document
     */
    public void recalcLines( String text )
    {
        for ( int i=0;i<size();i++ )
        {
            Page p = get( i );
            p.recalcLines( text );
        }
    }
    /**
     * Add a newly recognised shape of a word to our register
     * @param s a reference to a shape (lives in canvas)
     */
    public void addWordShape( Shape s )
    {
        // work out form its coordinates which line it is on and 
        // which word-index it has
    }
    /**
     * Add a newly recognised shape of a line to our register
     * @param s a reference to a shape (lives in canvas)
     */
    public void addLineShape( Shape s )
    {
        // work out form its coordinates which line it is on and 
        // which line-index it has
    } 
    /**
     * Add a newly recognised shape of a line or word or nothing
     * @param s a reference to a shape (lives in canvas)
     */
    public void addShape( Shape s )
    {
        // work out form its coordinates which line it is on and 
        // if it has a line or word index
    } 
}
