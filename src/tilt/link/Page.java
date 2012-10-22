/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tilt.link;
import tilt.image.WordBase;
import java.util.ArrayList;

/**
 * A page consists of lines and a name
 * @author desmond
 */
public class Page extends ArrayList<Line>
{
    String name;
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
     * Recalculate the line-breaks based on the new text
     * @param text the new text of all the pages
     */
    public void recalcLines( String text )
    {
        clear();
        ArrayList<Line> newLines = new ArrayList<Line>();
        int state=0;
        int len = text.length();
        int start = 0;
        int end = 0;
        for ( int i=0;i<len;i++ )
        {
            char token = text.charAt(i);
            switch ( state )
            {
                case 0: // looking for line-end
                    if ( token == '\r' )
                    {
                        end = i;
                        state = 1;
                    }
                    else if ( token == '\n' )
                        add( new Line(start,text.substring(start,i)) );
                    break;
                case 1: // looking for first non-whitespace
                    if ( !Character.isWhitespace(token) )
                    {
                        add( new Line(start,text.substring(start,end)) );
                        start = i;
                    }
                    break;
            }
        }
    }
}
