package tilt;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import tilt.link.Links;
import tilt.image.WordIterator;
import javax.swing.JLayeredPane;
import java.util.Iterator;
import javax.swing.JOptionPane;
import tilt.image.FindLines;

/**
 * Object to contain buffered image and canvas to draw on
 * @author desmond
 */
public class ImagePanel extends JLayeredPane
{
    /** scaled image for display */
    private Image image;
    /** scaled width of panel */
    int width;
    /** scaled height of panel */
    int height;
    /** original unscaled image */
    BufferedImage bi;
    /** skeleton of recognised lines */
    FindLines lines;
    /** links between canvas and text */
    Links links;
    /** child canvas where drawing of shapes happens */
    Canvas canvas;
    /** fraction of total image HEIGHT that fits in the window */
    float scale;
    /** median number of RECT_HEIGHT rects more dense than the background */
    int median;
    /** average value in gray image */
    int grayAverage;
    static final String DEFAULT_MESSAGE = "empty image";
    static final int DEFAULT_WIDTH = 250;
    static final int DEFAULT_HEIGHT = 350;
    
    /**
     * Create an image panel without an image file
     * @param links manages image to text selections 
     */
    public ImagePanel( Links links ) throws Exception
    {
        this.links = links;
        bi = new BufferedImage( DEFAULT_WIDTH, DEFAULT_HEIGHT, 
            BufferedImage.TYPE_INT_RGB);
        image = bi;
        width = DEFAULT_WIDTH;
        height = DEFAULT_HEIGHT;
        Graphics g = image.getGraphics();
        FontMetrics fm = g.getFontMetrics();
        int strWidth = fm.stringWidth( DEFAULT_MESSAGE );
        int strHeight = fm.getAscent()+fm.getDescent();
        g.drawString( DEFAULT_MESSAGE, (width/2)-strWidth/2,
            (height/2)-strHeight/2 );
        canvas = new Canvas( width, height, this );
        scale = 1.0f;
        this.add( canvas, new Integer(1) );
        lines = new FindLines( bi, links );
    }
    /**
     * Constructor when we have an image file
     * @param the image
     * @param links the links to the text panel
     */
    public ImagePanel( BufferedImage bi, Links links ) 
        throws Exception
    {
        try 
        {          
            this.links = links;
            this.bi = bi;
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            scale = 1.0f;
            if ( bi.getHeight()>(screenSize.height-80) )
                scale = ((float)(screenSize.height-80))/((float)bi.getHeight());
            width = Math.round(bi.getWidth()*scale);
            height = Math.round(bi.getHeight()*scale);
            image = bi.getScaledInstance(width,height, Image.SCALE_SMOOTH);
            canvas = new Canvas( width, height, this );
            this.add( canvas, new Integer(1) );
            lines = new FindLines( bi, links );
        } 
        catch (IOException ex) 
        {
            System.out.println("Error: "+ex.getMessage());
        }
    }
    /**
     * Get the current scale
     * @return the scale as a float
     */
    float getScale()
    {
        return scale;
    }
    /**
     * This is only thing called when packing a frame
     * @return the size as a Dimension
     */
    @Override
    public Dimension getPreferredSize()
    {
        return new Dimension(width,height);
    }
    /**
     * Draw ourselves
     * @param g the graphics environment
     */
    @Override
    public void paintComponent(Graphics g) 
    {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, null); // see javadoc for more info             
    }
    /**
     * Set the size of the image panel preserving the aspect ratio
     * @param width the suggested width
     * @param height the suggested height
     */
    @Override
    public void setSize( int width, int height )
    {
        if ( width>0&&height>0 )
        {
            float aspectRatio = ((float)bi.getWidth())/((float)bi.getHeight());
            int wd1 = Math.round(height*aspectRatio);
            int ht2 = Math.round(width/aspectRatio);
            if ( wd1 <= width )
            {
                this.height = height;
                this.width = wd1;
            }
            else    // width is a constraint
            {
                this.height = ht2;
                this.width = width;
            }
            image = bi.getScaledInstance( this.width, this.height, 
                Image.SCALE_SMOOTH );
            canvas.setSize( this.width, this.height );
            System.out.println("ImagePanel: width="+this.width+" height="+this.height);
            canvas.setPreferredSize( new Dimension(this.width, this.height) );
            // prioritise image hight, preserve aspect ratio
            scale = (float)this.height/(float)bi.getHeight();
            //System.out.println("scale="+scale);
        }
        else
            super.setSize(width,height);
    }
    /**
     * Set current tool in use
     * @param mode the mode or tool
     */
    public void setMode( Mode mode )
    {
        canvas.setMode( mode );
    }
    /**
     * Recompute the image at the same scale but using the B&W image
     */
    public void switchToBW()
    {
        image = lines.getBWImage().getScaledInstance(width,height, Image.SCALE_SMOOTH);
        this.repaint();
    }
    /**
     * Recompute the image at the same scale but using the B&W image
     */
    public void switchToLines()
    {
        image = lines.getLinesImage().getScaledInstance( width,height, 
            Image.SCALE_SMOOTH);
        this.repaint();
    }/**
     * Get the black and white image
     * @return the B&W image
     */
    BufferedImage getBWImage()
    {
        return lines.getBWImage();
    }
    /**
     * Recompute the image at the same scale but using the B&W image
     */
    public void switchToColor()
    {
        image = bi.getScaledInstance(width,height, Image.SCALE_SMOOTH);
        this.repaint();
    }
    /**
     * Call the page-recognition routine. Add all the generated shapes to 
     * the canvas
     */
    public void recognisePage()
    {
        // check that the user has already selected a page in the text window
        Iterator<Shape> iter = new WordIterator( lines );
        while ( iter.hasNext() )
        {
            Shape s = iter.next();
            canvas.addShape( s );
        }
    }
    /**
     * Call the page-recognition routine. Add all the generated shapes to 
     * the canvas
     * @param globalX global (image) x-coordinate
     * @param globalY global (image) y-coordinate
     */
    public Shape recogniseLine( int globalX, int globalY )
    {
        return lines.recogniseLine( globalX, globalY );
    }
    /**
     * Recognise a word given the coordinates of a click in the window
     * @param global the global X-coordinate
     * @param globalY the global Y-coordinate
     * @return a Rect or Region
     */
    public Shape recogniseWord( int globalX, int globalY )
    {
        return lines.recogniseWord( globalX, globalY );
    }
    /**
     * Get the Links object. Used by Canvas.
     * @return a Links object
     */
    Links getLinks()
    {
        return links;
    }
}