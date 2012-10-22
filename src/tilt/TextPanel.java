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
        setEditable( false );
        this.setPreferredSize( new Dimension(width,height));
        Border margin = BorderFactory.createEmptyBorder(10,10,10,10);
        this.setBorder( margin );
        this.links = links;
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
        if ( width > 0 && height > 0 )
            this.setPreferredSize( new Dimension(width,height) );
        else
            super.setSize( width, height );
        System.out.println("TextPanel: width="+this.width+" height="+this.height);
        this.width = width;
        this.height = height;
    }
}
