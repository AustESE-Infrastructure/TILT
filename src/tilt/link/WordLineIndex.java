/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tilt.link;
import java.util.ArrayList;

/**
 * The location of a word in a line on a page
 * @author desmond
 */
public class WordLineIndex 
{
    /** name of the page it is on */
    String page;
    /** index of the line on the page */
    int line;
    /** text length */
    int tLen;
    /** indices of the words on the line */
    ArrayList<Integer> words;
    public WordLineIndex( String page )
    {
        this.page = page;
        this.words = new ArrayList<Integer>();
    }
    /**
     * Construct a finished WordLineIndex
     * @param page the name of the page
     * @param line the index of the line of interest
     * @param word the index of the word on that line
     */
    public WordLineIndex( String page, int line, int word )
    {
        this.page = page;
        this.line = line;
        this.words = new ArrayList<Integer>();
        this.words.add( word );
    }
    /**
     * Set the line for a lightly constructed object
     * @param line the line index
     */
    public void setLine( int line )
    {
        this.line = line;
    }
    /**
     * Add a word (there maybe several on this line)
     * @param offset offset of the word-start
     * @param len length of the word
     */
    public void addWord( int offset, int len )
    {
        this.words.add( offset );
        this.tLen = offset+len-this.words.get(0).intValue();
    }
    /**
     * Overwrite the word list with just one word
     * @param word the word to set us to 
     */
    public void setWord( int word )
    {
        words.clear();
        words.add( word );
    }
    /**
     * How many words have we got?
     * @return the number of words as an int
     */
    public int length()
    {
        return words.size();
    }
    /**
     * Get the indexed word from our list
     * @return an int which may be -1 if there was no word
     */
    public int word( int index )
    {
        if ( words.size()>index )
            return words.get(index).intValue();
        else
            return -1;
    }
    /**
     * Get the overall text length
     * @return an int
     */
    public int textLen()
    {
        return tLen;
    }
}
