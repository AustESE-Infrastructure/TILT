/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tilt.link;
import java.util.ArrayList;

/**
 * Represent a line in a text-block
 * @author desmond
 */
public class Line extends ArrayList<Word>
{
    /** start offset in the text of the line itself */
    int start;
    /** length of the line */
    int len;
    /**
     * Create a new line
     * @param start the start offset in the text of the lin
     * @param text the text of the line
     */
    public Line( int start, String text )
    {
        this.start = start;
        this.len = text.length();
        ArrayList<Integer> temp = new ArrayList<Integer>();
        if ( text.length() > 0 && !Character.isWhitespace(text.charAt(0)) )
            temp.add( 0 );
        int state = 0;
        int offset = 0;
        for ( int i=0;i<text.length();i++ )
        {
            switch ( state )
            {
                case 0: // not seen a space
                    if ( Character.isWhitespace(text.charAt(i)) )
                    {
                        add( new Word(offset,i-offset) );
                        state = 1;
                    }
                    break;
                case 1: // seen a space
                    if ( !Character.isWhitespace(text.charAt(i)) )
                    {
                        state = 0;
                        offset = i;
                    }
                    break;
            }
        }
        if ( state == 0 )
            add( new Word(offset,text.length()-offset) );
    }
}
