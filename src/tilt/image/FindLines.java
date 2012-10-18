package tilt.image;
import java.util.ArrayList;
import java.io.FileOutputStream;
import java.awt.Dimension;
import java.awt.Toolkit;
import tilt.Shape;
import tilt.Region;
import tilt.Rect;
import java.util.Iterator;
import java.awt.image.BufferedImage;
import java.awt.geom.PathIterator;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.*;
import java.awt.color.ColorSpace;
import java.awt.geom.Area;
import java.awt.Point;
import java.awt.Graphics;
import java.awt.Color;

public class FindLines
{
	/** original image */
	BufferedImage bi;
	/** grayscale, later converted to B&W */
	BufferedImage bw;
    /** faint version of grayscale with lines marked */
    BufferedImage linesImage;
	/** scaled image in JFrame */
	Image image;
	/** scale for viewing */
	float scale;
	/** width and height of JFrame */
	int width,height;
	/** square size for localised contrast conversion from gray to B&W */
	int SQUARE = 64;
	/** square size for word-recognition */
	int WORD_SQUARE = 32;
	/** block of pixels for line detection */
	int BLOCK_WIDTH = 20;
	int BLOCK_HEIGHT = 30;
	/** fraction of bound-rect's area below which polygon is preferred */
	float POLYGON_ACCEPT_RATIO = 0.67f;
	/** overall average number of black pixels per pixel (fraction) */
	float average;
	/** black pixel totals in ROWS */
	int[] verticals;
	/** black pixel totals in COLUMNS */
	int[] horizontals;
	/** average number of pixels per row */
	int averageV;
	/** updateable raster for B&W image */
	WritableRaster wr;
	/** list of recognised words */
	ArrayList<WordComponent> words;
	/** list of recognised lines */
	ArrayList<Area> lines;
	/**
	 * Create a find-lines object and manage the recognition process. 
	 * @param bi an already loaded rgb image
	 */
	public FindLines( BufferedImage bi ) throws Exception
	{
		words = new ArrayList<WordComponent>();
		// compute scale for display
	    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	    scale = 1.0f;
	    if ( bi.getHeight()>(screenSize.height-80) )
	        scale = ((float)(screenSize.height-80))/((float)bi.getHeight());
	    width = Math.round(bi.getWidth()*scale);
	    height = Math.round(bi.getHeight()*scale);
		// create B&W image
	    bw = convertToGrayscale( bi );
        // make faint copy
		RescaleOp rescaleOp = new RescaleOp(1.0f, 120, null);
        linesImage = rescaleOp.filter( bw, null );
        bw = convertToBAndW( bw );
		// clean and line-recognise
		cleanImage( bw );
		recogniseLines();
        drawLines();
        // prepare converted image for display
		//image = bw.getScaledInstance(width,height, Image.SCALE_SMOOTH);
	}
    /**
     * Make a faint rendition of the grayscale plus the lines
     */
    void drawLines()
    {
        Graphics g = linesImage.getGraphics();
        for ( int i=0;i<words.size();i++ )
        {
            WordComponent wc = words.get( i );
            g.setColor( Color.BLACK );
            g.fillRect( wc.x, wc.y-2, wc.length, 4 );
        }
    }
	/**
	 * Get an iterator through recognised words
	 * @return a Shape iterator (AWT Polygons or Rectangles)
	 */
	public WordIterator wordIterator()
	{
		return new WordIterator( this );
	}
	/**
	 * Get an iterator through recognised lines
	 * @return a Shape iterator (AWT Polygons or Rectangles)
	 */
	public LineIterator lineIterator()
	{
		return new LineIterator( this );
	}
	/**
	 * Display the image for debugging
	 * @param img a scaled image for display
	 */
    private void displayImage( Image img )
    {
        JFrame frame = new JFrame("");
        JLabel a = new javax.swing.JLabel();
        frame.getContentPane().add( a );
        a.setIcon(new ImageIcon(img));
        frame.pack();
        frame.setVisible(true);
    }
	/**
	 * Convert an input RGB image to grayscale
	 * @param rgb a full colour image
	 * @return a grayscale image, 8 bbp
	 */
	private BufferedImage convertToGrayscale( BufferedImage rgb )
    { 
        BufferedImageOp op = new ColorConvertOp(
            ColorSpace.getInstance(ColorSpace.CS_GRAY), null); 
        BufferedImage img = op.filter(rgb, null);
        return img;
    }
	/**
	 * COnvert a grayscale image to pure B&W using a local contrast filter
	 * @param gray the input gray image
	 * @return the same image turned into B&W (only 0,255 pixel values)
	 */
	private BufferedImage convertToBAndW( BufferedImage gray )
	{
		//BufferedImage img = new BufferedImage(bi.getWidth(), 
        //    bi.getHeight(), BufferedImage.TYPE_BYTE_BINARY );
		WritableRaster wr = gray.getRaster();
		for ( int y=0;y<wr.getHeight();y+=Math.min(SQUARE,wr.getHeight()-y) )
		{
			int h = Math.min(SQUARE,wr.getHeight()-y);
			for ( int x=0;x<wr.getWidth();x+=Math.min(SQUARE,wr.getWidth()-x) )
			{
				// compute average pixel value
				int w = Math.min(SQUARE,wr.getWidth()-x);
				int[] dArray = new int[h*w];
				int[] res = wr.getPixels(x,y,w,h,dArray);
				int total = 0;
				for ( int i=0;i<dArray.length;i++ )
					total += dArray[i];
				int average = total/dArray.length;
				// set pixels that are darker to black and the rest to white
				for ( int j=y;j<y+h;j++ )
				{
					int[] iArray = new int[1];
					for ( int k=x;k<x+w;k++ )
					{
						res = wr.getPixel( k, j, iArray );
						// +25 darker filters out noise
						if ( res[0]+25 < average )
							iArray[0] = 0;
						else
						{
							iArray[0] = 255;
						}
						wr.setPixel( k, j, iArray );
					}
				}
			}
		}
		return gray;
	}
	/**
	 * Debug: write an integer array to disk for analysis
	 * @param array the int array
	 * @param file the debug file name
	 */
	private void writeIntArray( int[] array, String file )
	{
		try
		{
			FileOutputStream fos = new FileOutputStream(file);
			for ( int i=0;i<array.length;i++ )
			{
				fos.write(Integer.toString(array[i]).getBytes());
				fos.write("\n".getBytes());
			}
		}
		catch ( Exception e )
		{
			e.printStackTrace(System.out);
		}
	}
	/**
	 * Compute the horizontal and vertical pixel counts and the overall 
	 * average pixel density per pixel
	 * @param img the img to compute pixel counts for
	 */
	private void computePixelCounts( BufferedImage img )
	{
		verticals = new int[img.getHeight()];
		horizontals = new int[img.getWidth()];
		int[] row = new int[img.getWidth()];
		int[] col = new int[img.getHeight()];
		int totalBPixels = 0;
		wr = img.getRaster();
		// compute vertical view of page
		for ( int y=0;y<verticals.length;y++ )
		{
			wr.getPixels( 0,y,row.length,1,row );
			for ( int x=0;x<row.length;x++ )
			{
				if ( row[x]==0 )
					verticals[y]++;
			}
		}
		// average blackness per pixel
		for ( int i=0;i<verticals.length;i++ )
			totalBPixels += verticals[i];
		average = (float)totalBPixels/(float)(verticals.length*horizontals.length);
		averageV = totalBPixels/verticals.length;
		//System.out.println("average="+average+" averageV="+averageV);
		//writeIntArray(verticals,"verticals.txt");
		// now compute horizontal view
		for ( int x=0;x<horizontals.length;x++ )
		{
			wr.getPixels( x,0,1,col.length,col );
			for ( int y=0;y<col.length;y++ )
			{
				if ( col[y]==0 )
				horizontals[x]++;
			}
		}
		//writeIntArray(horizontal,"horizontals.txt");
	}
	/**
	 * Remove extraneous pixels from margins and between lines
	 * @param img the image to remove stuff from
	 */
	private void cleanImage( BufferedImage img )
	{
		if ( verticals == null )
			computePixelCounts( img );
		// now white out pixels that have high values
		int[] iArray = new int[1];
		int xMargin = img.getWidth()*5/100;
		int yMargin = img.getHeight()*5/100;
		// clean
		for ( int y=0;y<verticals.length;y++ )
		{
			if ( y<yMargin || verticals.length-y<yMargin )
				verticals[y] = 0;
			for ( int x=0;x<horizontals.length;x++ )
			{
				if ( x<xMargin || horizontals.length-x<xMargin )
					horizontals[x] = 0;
				if ( y<yMargin || verticals.length-y<yMargin || x<xMargin 
					|| horizontals.length-x<xMargin )
				{
					iArray[0] = 255;
					wr.setPixel( x, y, iArray );
				}
			}
		}
	}
	/**
	 * Recognise lines and words within lines using a prepared B&W image
	 * with verticals and horizontals already computed
	 */
	private void recogniseLines( )
	{
		int localPeak = 0;
		int peakPos=0;
		int[] iArray = new int[1];
		iArray[0] = 0;
		int vLimit = verticals.length-BLOCK_HEIGHT;
		int averageBlockBPixels = Math.round(average*(float)BLOCK_HEIGHT
			*(float)BLOCK_WIDTH);
		// compute this
		int minPeakSep = 50;
		for ( int i=BLOCK_HEIGHT/2;i<vLimit;i++ )
		{
			if ( verticals[i] > localPeak && verticals[i] > averageV )
			{
				localPeak = verticals[i];
				peakPos = i;
			}			
			else if ( localPeak > 0 && i-peakPos > minPeakSep )
			{
				int[] block = new int[BLOCK_HEIGHT*BLOCK_WIDTH];
				int hLimit = horizontals.length-BLOCK_WIDTH;
				WordComponent current = null;
				for ( int j=0;j<hLimit;j+=BLOCK_WIDTH )
				{
					wr.getPixels( j, peakPos-BLOCK_HEIGHT/2, BLOCK_WIDTH, 
						BLOCK_HEIGHT, block );
					int blockTotal = 0;
					for ( int k=0;k<block.length;k++ )
						blockTotal += (block[k]==0)?1:0;
					if ( blockTotal > averageBlockBPixels )
					{
						if ( current == null )
						{
							current = new WordComponent(j, peakPos, 
								BLOCK_WIDTH );
							words.add( current );
						}
						else
							current.length += BLOCK_WIDTH;
					}
					else	// word-break
						current = null;
				}
				localPeak = peakPos = 0;
			}
		}
	}
	/**
	 * Try to expand an area by testing to the top, left, bottom and right 
	 * of where we are
	 * @param a the area to add shapes to
	 * @param x the left coordinate of the top-left of the square
	 * @param y the top coordinate of the top-left of the square 
	 * @param xsize the width of the square in pixels
	 * @param ysize the height of the square in pixels
	 */
	private void propagateRect( Area a, int x, int y, int xsize, int ysize )
	{
		// accept current rectangle
		Rectangle r = new Rectangle( x, y, xsize, ysize );
		a.add( new Area(r) );
		// above
		int yValue = Math.max(y-WORD_SQUARE,0);
		int yHeight = Math.min(WORD_SQUARE,y);
		if ( yHeight > 0 )
			augmentArea( a, x, yValue, WORD_SQUARE, yHeight );
		// below
		yValue = y+ysize;
		yHeight = Math.min(WORD_SQUARE,wr.getHeight()-yValue);
		if ( yHeight > 0 )
			augmentArea( a, x, yValue, xsize, yHeight );
		// left
		int xValue = Math.max(x-WORD_SQUARE,0);
		int xWidth = x-xValue;
		if ( xWidth > 0 )
			augmentArea( a, xValue, y, xWidth, ysize );
		// right
		xValue = x+xsize;
		xWidth = Math.min(WORD_SQUARE,wr.getWidth()-xValue);
		if ( xWidth > 0 )
			augmentArea( a, xValue, y, xWidth, ysize );
	}
	/**
	 * Grow an area recursively, working with the wr raster
	 * @param a the are to add shapes to
	 * @param x the left coordinate of the top-left of the square
	 * @param y the top coordinate of the top-left of the square 
	 * @param xsize the width of the square in pixels
	 * @param ysize the height of the square in pixels
	 */
	private void augmentArea( Area a, int x, int y, int xsize, int ysize )
	{
		Rectangle r = new Rectangle( x, y, xsize, ysize );
		if ( !a.contains(r) )
		{
			int[] dArray = new int[xsize*ysize];
			wr.getPixels( x, y, xsize, ysize, dArray );
			int total = 0;
			for ( int i=0;i<dArray.length;i++ )
				if ( dArray[i]==0 )
					total++;
			float areaAverage = (float)total/(float)dArray.length;
			if ( areaAverage > average )
			{
				propagateRect(a, x, y, xsize, ysize );
			}
			else if ( xsize==WORD_SQUARE && ysize==WORD_SQUARE )
			{
				// try subdivision
				if ( testSmallArray(x,y,WORD_SQUARE/2,WORD_SQUARE/2) )
					propagateRect(a,x,y,xsize,ysize);
				else if ( testSmallArray(x+WORD_SQUARE/2,y,WORD_SQUARE/2,
					WORD_SQUARE/2) )
					propagateRect(a,x,y,xsize,ysize);
				else if ( testSmallArray(x,y+WORD_SQUARE/2,WORD_SQUARE/2,
					WORD_SQUARE/2) )
					propagateRect(a,x,y,xsize,ysize);
				else if ( testSmallArray(x+WORD_SQUARE/2,y+WORD_SQUARE/2,
					WORD_SQUARE/2, WORD_SQUARE/2) )
					propagateRect(a,x,y,xsize,ysize);					
			}
		}
	}
	/**
	 * Retest one quarter of a failed big square
	 * @param x the x-coordinate of the sub-square
	 * @param y the y-coordinate of the sub-square
	 * @param xsize the x-size
	 * @param ysize its y-size
	 * @return true if it made the cut
	 */
	private boolean testSmallArray( int x, int y, int xsize, int ysize )
	{
		int[] smallArray = new int[xsize*ysize];
		int total = 0;
		wr.getPixels( x, y, xsize, ysize, smallArray );
		for ( int j=0;j<smallArray.length;j++ )
			if ( smallArray[j] == 0 )
				total++;
		float smallAverage = (float)total/(float)smallArray.length;
		return smallAverage > average;
	}
	/**
	 * Use convex hull algorithm to compute the outer reaches of Area
	 * @param a the input area
	 * @return a polygon 
	 */
	Region areaToPolygon( Area a )
	{
		// convert to ArrayList<Point>
		ArrayList<Point> points = new ArrayList<Point>();
		PathIterator iter = a.getPathIterator(null);
		float[] coords = new float[6];
		while ( !iter.isDone() )
		{
			int type = iter.currentSegment( coords );
			switch ( type )
			{
				case PathIterator.SEG_MOVETO: case PathIterator.SEG_LINETO:
					points.add( new Point(Math.round(coords[0]), 
						Math.round(coords[1])) );
				default:
					break;
			}
			iter.next();
		}
		ArrayList<Point> hull = FastConvexHull.execute( points );
		Region poly = new Region();
		if ( hull != null )
		{
			for ( int i=0;i<hull.size();i++ )
			{
				Point p = hull.get( i );
				poly.addPoint( p.x, p.y );
			}
		}
		return poly;
	}
	/** 
	 * Create a Polygon or Rectangle, whichever fits best.
	 * @param a the area already constructed
	 */
	Shape createAppropriateShape( Area a )
	{
		Region p = areaToPolygon( a );
		Rect r = p.getBounds();
		if ( p.area() < POLYGON_ACCEPT_RATIO*r.width*r.height )
			return p;
		else
			return r;
	}
    /**
     * Get the estimated line height
     * @return an estimate of the number of lines
     */
    public int getLineHeight()
    {
        int numLines = 0;
        if ( lines==null || lines.size()<=0 )
        {   
            WordComponent prev = null;
            for ( int i=0;i<words.size();i++ )
            {
                WordComponent wc = words.get(i);
                if ( prev == null )
                    numLines = 1;
                else if ( wc.x < prev.x )
                    numLines++;
                prev = wc;
            }
        }
        else
            numLines = lines.size();
        return wr.getHeight()/numLines*2;
    }
    /**
     * Get the black and white image
     * @return a buffered image
     */
    public BufferedImage getBWImage()
    {
        return bw;
    }
    /**
     * Get a faint version of the image with the lines clearly marked
     * @return a buffered image
     */
    public BufferedImage getLinesImage()
    {
        return linesImage;
    }
    /**
     * Recognise a single word starting at a single point
     * @param x the absolute x-coordinate
     * @param y the absolute y-coordinate
     * @return a Rect or Region as appropriate
     */
    public Shape recogniseWord( int x, int y )
    {
        Area a = new Area();
        augmentArea( a, x, y-WORD_SQUARE/2, Math.min(WORD_SQUARE,x), 
            Math.min(WORD_SQUARE,y) );
        return createAppropriateShape( a );
    }
	/**
	 * Location of a purely horizontal line where there is a word
	 */
	class WordComponent
	{
		int x,y;
		int length;
		WordComponent( int x, int y, int length )
		{
			this.x = x;
			this.y = y;
			this.length = length;
		}
	}
	/**
	 * An iterator to run through recognised words and generate appropriate 
	 * shapes from them
	 */
	public class WordIterator implements Iterator<Shape>
	{
		int pos;
		FindLines parent;
		public WordIterator( FindLines parent )
		{
			this.parent = parent;
		}
		public boolean hasNext() 
        {
			return pos < parent.words.size();
		}
		public Shape next()
		{
			Area a = new Area();
			WordComponent wc = parent.words.get(pos++);
			augmentArea( a, wc.x, wc.y-WORD_SQUARE/2, Math.min(WORD_SQUARE,wc.x), 
				Math.min(WORD_SQUARE,wc.y) );
			return parent.createAppropriateShape( a );
		}
		public void remove() throws UnsupportedOperationException
		{
			throw new UnsupportedOperationException("remove not supported");
		}
	}
	/**
	 * An iterator to run through recognised lines and generate appropriate 
	 * shapes from them
	 */
	public class LineIterator implements Iterator<Shape>
	{
		int pos;
		FindLines parent;
		/**
		 * Create the lines on the fly and then iterate through them. Assume
		 * they are sorted on x-position
		 * @param parent the FindLines object to query
		 */
		public LineIterator( FindLines parent )
		{
			if ( lines == null )
			{
				lines = new ArrayList<Area>();
				if ( words.size()>0 )
				{
					WordComponent prev = null;
					Area a=null;
					for ( int i=0;i<words.size();i++ )
					{
						WordComponent wc = words.get( i );
						if ( prev == null || prev.x > wc.x )
						{
							a = new Area();
							lines.add( a );
						}
						augmentArea( a, wc.x, wc.y-WORD_SQUARE/2, 
							Math.min(WORD_SQUARE,wc.x), 
							Math.min(WORD_SQUARE,wc.y) );
						prev = wc;
					}
				}
				// now we have the lines array built
			}
			this.parent = parent;
		}
		public boolean hasNext() 
        {
			return pos < parent.lines.size();
		}
		public Shape next()
		{
			return parent.createAppropriateShape( parent.lines.get(pos++) );
		}
		public void remove() throws UnsupportedOperationException
		{
			throw new UnsupportedOperationException("remove not supported");
		}
	}
}
