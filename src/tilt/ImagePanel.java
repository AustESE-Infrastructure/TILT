package tilt;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import java.util.Iterator;
import tilt.image.FindLines;

/**
 * Object to contain buffered image and canvas to draw on
 * @author desmond
 */
public class ImagePanel extends JLayeredPane
{
    /** scaled image for display */
    private Image image;
    int width;
    int height;
    /** original unscaled image */
    BufferedImage bi;
    /** skeleton of recognised lines */
    FindLines lines;
    JFrame parent;
    Canvas canvas;
    float scale;
    /** median number of RECT_HEIGHT rects more dense than the background */
    int median;
    /** average value in gray image */
    int grayAverage;
    static final String DEFAULT_MESSAGE = "empty image";
    static int RECT_WIDTH = 128;
    static int RECT_HEIGHT = 16;
    static int DOT_SIZE = 8;
    /**
     * Create an image panel without an image file
     * @param parent 
     */
    public ImagePanel( JFrame parent ) throws Exception
    {
        this.parent = parent;
        bi = new BufferedImage( TILT.DEFAULT_WIDTH, TILT.DEFAULT_HEIGHT, 
            BufferedImage.TYPE_INT_RGB);
        image = bi;
        width = TILT.DEFAULT_WIDTH;
        height = TILT.DEFAULT_HEIGHT;
        Graphics g = image.getGraphics();
        FontMetrics fm = g.getFontMetrics();
        int strWidth = fm.stringWidth( DEFAULT_MESSAGE );
        int strHeight = fm.getAscent()+fm.getDescent();
        g.drawString( DEFAULT_MESSAGE, (width/2)-strWidth/2,
            (height/2)-strHeight/2 );
        canvas = new Canvas( width, height, this );
        scale = 1.0f;
        this.add( canvas, new Integer(1) );
        lines = new FindLines( bi );
    }
    /**
     * Constructor when we have an image file
     * @param file the image file
     * @param parent the frame parent
     */
    public ImagePanel( File file, JFrame parent ) throws Exception
    {
        try 
        {          
            this.parent = parent;
            bi = ImageIO.read(file);
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            scale = 1.0f;
            if ( bi.getHeight()>(screenSize.height-80) )
                scale = ((float)(screenSize.height-80))/((float)bi.getHeight());
            width = Math.round(bi.getWidth()*scale);
            height = Math.round(bi.getHeight()*scale);
            image = bi.getScaledInstance(width,height, Image.SCALE_SMOOTH);
            canvas = new Canvas( width, height, this );
            this.add( canvas, new Integer(1) );
            lines = new FindLines( bi );
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
        g.drawImage(image, 0, 0, null); // see javadoc for more info on the parameters            
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
            this.width = Math.round(height*aspectRatio);
            this.height = height;
            image = bi.getScaledInstance( width, height, Image.SCALE_SMOOTH );
            canvas.setSize( width, height );
            canvas.setPreferredSize( new Dimension(width, height) );
            scale = (float)height/(float)bi.getHeight();
        }
        else
            super.setSize(width,height);
    }
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
        Iterator<Shape> iter = lines.wordIterator();
        while ( iter.hasNext() )
        {
            Shape s = iter.next();
            s.scale( scale );
            canvas.addShape( s );
        }
    }
    /**
     * Recognise a word given the coordinates of a click in the window
     * @param localX the local X-coordinate
     * @param localY the local Y-coordinate
     * @return a Rect or Region
     */
    public Shape recogniseWord( int localX, int localY )
    {
        return lines.recogniseWord( Math.round((float)localX/scale), 
            Math.round((float)localY/scale));
    }
}