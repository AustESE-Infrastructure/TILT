/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tilt.link;
import java.util.ArrayList;
import tilt.Shape;
import tilt.TextPanel;
import java.awt.Color;
import javax.swing.text.DefaultHighlighter;
/**
 * Manage the links between the facsimiles and the text
 * @author desmond
 */
public class Links extends ArrayList<Page>
{
    int current;
    TextPanel text;
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
        WordLineIndex wli = get(current).matchShape( s );
        Line l = get(current).get(wli.line);
        int wStart = wli.word(0);
        int wEnd = wStart+wli.textLen();
        int selStart = l.start+wStart;
        int selEnd = l.start+wEnd;
        try
        {
            text.getHighlighter().removeAllHighlights();
            text.getHighlighter().addHighlight( selStart, selEnd, 
                text.getHighlightPainter() );
        }
        catch ( Exception e )
        {
        }
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
    /**
     * Add a page to our collection; becomes current page.
     * @param p the page in question
     * @return true if it worked
     */
    @Override
    public boolean add( Page p )
    {
        current = size();
        return super.add( p );
    }
    /**
     * Move to the next page in the list if possible
     * @return true if we could advance, else false
     */
    public boolean next()
    {
        if ( size()-1 < current )
        {
            current++;
            return true;
        }
        else
            return false;
    }
    /**
     * Move to the next page in the list if possible
     * @return true if we could advance, else false
     */
    public boolean previous()
    {
        if ( 0 < current )
        {
            current--;
            return true;
        }
        else
            return false;
    }
    /**
     * Remember the text panel. Highlight this when we receive a new shape
     * @param text the text panel in question
     */
    public void setTextPanel( TextPanel text )
    {
        this.text = text;
    }
}
