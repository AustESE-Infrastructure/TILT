/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tilt;
import tilt.link.Links;
import javax.swing.JFrame;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseListener;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.EventQueue;
import java.io.File;
import java.io.FileInputStream;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;


/**
 * Simple desktop app or applet for creating image/text links
 * @author desmond
 */
public class TILT
{
    File imageFile;
    File textFile;
    TiltGui gui;
    BufferedImage bi;
    String text;
    /**
     * Read commandline arguments
     * @param args the arguments
     */
    void readArgs( String[] args )
    {
        if ( args.length > 0 )
            imageFile = new File( args[0] );
        if ( args.length > 1 )
            textFile = new File( args[1] );
    }
    /**
     * Create a TILT instance
     * @param args the arguments: image file then text file
     */
    public TILT( String[] args )
    {
        if ( args.length == 2 )
        {
            readArgs( args );
            EventQueue.invokeLater( new Runnable() {
                @Override
                public void run() 
                {
                    JFrame frame = new JFrame("Text-Image Link Tool");
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    try
                    {
                        bi = ImageIO.read( imageFile );
                        text = loadTextFile( textFile );            
                        gui = new TiltGui( text, bi );
                        frame.setContentPane( gui );
//                        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
//                            public void eventDispatched(AWTEvent event) {
//                            System.out.println(event.getSource());
//                            }
//                        }, AWTEvent.MOUSE_EVENT_MASK);
                        frame.addComponentListener(new ComponentAdapter() 
                        {
                            public void componentResized(ComponentEvent e) 
                            {
                                JFrame frame = ((JFrame)e.getSource());
                                super.componentResized(e);
                                frame.pack();
                            }
                        });
                        frame.pack();
                        frame.setVisible(true);
                    }
                    catch ( Exception e )
                    {
                        e.printStackTrace( System.out );
                    }
                }
            } );
        }
        else
            System.out.println("syntax: java TILT <image-file> <text-file>");
    }
    /**
     * Load the text file as a String
     * @param input the input text file (for the right hand side)
     * @return the file's contents as a string in UTF-8 format
     */
    String loadTextFile( File input )
    {
        try
        {
            FileInputStream fis = new FileInputStream( textFile );
            int length = (int) textFile.length();
            byte[] data = new byte[length];
            fis.read( data );
            fis.close();
            return new String( data, "UTF-8" );
        }
        catch ( Exception e )
        {
            return "Couldn't open "+input.getName();
        }
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) 
    {
        try
        {
            TILT t = new TILT( args );
        }
        catch ( Exception e )
        {
            e.printStackTrace( System.out );
        }
    }
}
