/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tilt;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Container;
import javax.swing.JFrame;
import java.awt.Insets;
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
    public TextPanel( int width, int height )
    {
        this.setText( DEFAULT_TEXT );
        this.setPreferredSize( new Dimension(width,height));
        Border margin = BorderFactory.createEmptyBorder(10,10,10,10);
        this.setBorder( margin );
        
    }
    public TextPanel( int width, int height, String text )
    {
        this.width = width;
        this.height = height;this.setText( text );
        this.setPreferredSize( new Dimension(width,height));
        Border margin = BorderFactory.createEmptyBorder(10,10,10,10);
        this.setBorder( margin );
    }
    @Override
    public void setSize( int width, int height )
    {
        if ( width > 0 && height > 0 )
            this.setPreferredSize( new Dimension(width,height) );
        else
            super.setSize( width, height );
        this.width = width;
        this.height = height;
    }
    public void printSizes()
    {
        System.out.println("text panel width="+width+" height="+height);
    }
}
