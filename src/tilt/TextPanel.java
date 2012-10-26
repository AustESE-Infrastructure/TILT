/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tilt;
import java.awt.Color;
import tilt.link.Links;
import javax.swing.JEditorPane;
import java.awt.Dimension;
import javax.swing.border.Border;
import javax.swing.BorderFactory;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.text.DefaultHighlighter;
import java.awt.SystemColor;
/**
 *
 * @author desmond
 */
public class TextPanel extends JEditorPane
{
    static final String DEFAULT_TEXT = "No text";
    int width,height;
    JEditorPane editPane;
    Links links;
    DefaultHighlighter.DefaultHighlightPainter highlightPainter;
    public TextPanel( int width, int height, Links links )
    {
        this.setText( DEFAULT_TEXT );
        setEditable( true );
        this.setPreferredSize( new Dimension(width,height));
        Border margin = BorderFactory.createEmptyBorder(10,10,10,10);
        this.setBorder( margin );
        this.links = links;
        this.addKeyListener(new TextKeyListener() );
        DefaultHighlighter hl = new DefaultHighlighter();
        this.setHighlighter( hl );
        highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(
            SystemColor.textHighlight);
        setVisible( true );
    }
    public TextPanel( int width, int height, String text, Links links )
    {
        this.width = width;
        this.height = height;
        this.links = links;
        setEditable( false );
        this.setText( text );
        this.setPreferredSize( new Dimension(width,height));
        Border margin = BorderFactory.createEmptyBorder(10,10,10,10);
        this.setBorder( margin );
        links.recalcLines(getText());
        DefaultHighlighter hl = new DefaultHighlighter();
        this.setHighlighter( hl );
        highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(
            SystemColor.textHighlight);
        setVisible( true );
    }
    public DefaultHighlighter.DefaultHighlightPainter getHighlightPainter()
    {
        return highlightPainter;
    }
    @Override
    public void setSize( int width, int height )
    {
        if ( width > 0 && height > 0 )
        {
            super.setSize( width, height );
            this.width = width;
            this.height = height;
        }
        else
            super.setSize( width, height );
    }
    public Dimension getPreferredSize()
    {
        return new Dimension( width, height );
    }
    /**
     * Handle key events to enable selection. Handling them here intercepts 
     * them and prevents editing, while enabling selection via mouse events.
     */
    private class TextKeyListener implements KeyListener
    {
        /**
         * Key pressed but not yet released (low-level)
         * @param e the key event
         */
        public void keyPressed( KeyEvent e ) 
        {
        }
        /**
         * Key released after pressing (low-level)
         * @param e 
         */
        public void keyReleased( KeyEvent e ) 
        {
        }
        /**
         * Your basic typed character event
         * @param e the key event
         */
        public void keyTyped( KeyEvent e ) 
        {
        }
    }
}
