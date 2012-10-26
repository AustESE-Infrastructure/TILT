/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tilt.link;
import tilt.image.WordBase;
import tilt.image.FindLines;
import tilt.Shape;
import java.util.ArrayList;
import java.awt.Rectangle;
import java.awt.Point;

/**
 * A page consists of lines and a name
 * @author desmond
 */
public class Page extends ArrayList<Line>
{
    String name;
    FindLines lines;
    int start;
    int len;
    /**
     * Create a new Page
     * @param name a name derived from the file name
     * @param start the offset in the text where it starts
     */
    public Page( String name, int start )
    {
        this.name = name;
        this.start = start;
    }
    /**
     * Set the findlines object
     * @param lines the object from the ImagePanel
     */
    public void setLines( FindLines lines )
    {
        this.lines = lines;
    }
    /**
     * Get the span of a piece of text
     * @param line the index into the lines array
     * @param word index into the words of the line
     * @return a TextSpan object
     */
    Word getWord( int line, int word )
    {
        Line l = get( line );
        return l.get( word );
    }
    /**
     * Fit the actual words to the selected shape by counting pixels
     * @param s the shape the user created or clicked on
     * @return a reference to a word(s)+line+page
     */
    WordLineIndex matchShape( Shape s )
    {
        ArrayList<Word> within = new ArrayList<Word>();            
        Point p = s.getCentre();
        WordLineIndex wli = lines.getLineIndex( p.y );
        if ( wli.line < size() )
        {
            Line l = get( wli.line );
            if ( l.len > 0 )
            {
                Rectangle sBounds = s.getBounds();
                int tLen = l.getLen();
                int pLen = lines.getLinePixelLen( wli.word(0) );
                float ppc = (float)pLen/(float)tLen;
                WordBase wb = lines.getWordBase( wli.word(0) );
                for ( int i=0;i<l.size();i++ )
                {
                    Word w = l.get(i);
                    int sPP = wb.x+Math.round(ppc*w.start);
                    int ePP = sPP+Math.round(ppc*w.len);
                    // examine word-start
                    if ( sPP > sBounds.x+sBounds.width )
                        break;
                    else if ( sPP >= sBounds.x )
                    {
                        int sEnd = sBounds.x+sBounds.width;
                        // is all or most of the word within the shape?
                        if ( ePP<sEnd || Math.abs(ePP-sEnd)<Math.abs(sPP-sEnd) )
                            within.add( w );
                    }
                    else if ( ePP > sBounds.x )
                    {
                        if ( Math.abs(ePP-sBounds.x)>Math.abs(sBounds.x-sPP) )
                            within.add( w );
                    } 
                }
            }
        }
        // compose return value
        WordLineIndex ret = new WordLineIndex( name );
        ret.setLine( wli.line );
        for ( int i=0;i<within.size();i++ )
            ret.addWord( within.get(i).start, within.get(i).len );
        return ret;
    }
    /**
     * Recalculate the line-breaks based on the new text
     * @param text the new text of all the pages
     */
    public void recalcLines( String text )
    {
        clear();
        int state=0;
        int tLen = text.length();
        int tStart = 0;
        int i;
        for ( i=0;i<tLen;i++ )
        {
            char token = text.charAt(i);
            switch ( state )
            {
                case 0: // looking for first non-whitespace
                    if ( !Character.isWhitespace(token) )
                    {
                        tStart = i;
                        state = 1;
                    }
                    break;
                case 1: // looking for line-end
                    if ( token=='\r' || token == '\n' )
                    {
                        add( new Line(tStart,text.substring(tStart,i)) );
                        state = 0;
                    }
                    break;
            }
        }
        if ( state == 1 )
            add( new Line(tStart,text.substring(tStart,i)) );
    }
}
