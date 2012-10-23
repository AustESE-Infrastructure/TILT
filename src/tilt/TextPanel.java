/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tilt;
import tilt.link.Links;
import javax.swing.JEditorPane;
import java.awt.Dimension;
import javax.swing.border.Border;
import javax.swing.BorderFactory;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.Point;

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
    public TextPanel( int width, int height, Links links )
    {
        this.setText( DEFAULT_TEXT );
        setEditable( true );
        this.setPreferredSize( new Dimension(width,height));
        Border margin = BorderFactory.createEmptyBorder(10,10,10,10);
        this.setBorder( margin );
        this.links = links;
        this.addKeyListener(new TextKeyListener() );
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
    }
    @Override
    public void setSize( int width, int height )
    {
        super.setSize( width, height );
        this.width = width;
        this.height = height;
    }
    public Dimension getPreferredSize()
    {
        return new Dimension( width, height );
    }
    /**
     * Handle click events to enable selection
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
