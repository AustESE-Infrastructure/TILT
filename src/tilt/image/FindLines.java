package tilt.image;
import java.util.ArrayList;
import java.awt.Dimension;
import java.awt.Toolkit;
import tilt.Shape;
import tilt.Region;
import tilt.Rect;
import tilt.link.Links;
import java.awt.image.BufferedImage;
import java.awt.geom.PathIterator;
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
	int wordSquare = 32;
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
    /** bases of where words could be */
    ArrayList<WordBase> bases;
    /** links to text */
    Links links;
	/**
	 * Create a find-lines object and manage the recognition process. 
	 * @param bi an already loaded rgb image
	 */
	public FindLines( BufferedImage bi, Links links ) throws Exception
	{
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
        bases = new ArrayList<WordBase>();
		// clean and line-recognise
		cleanImage( bw );
        recogniseLines();
        int lineHeight = getLineHeight();
        // ensure even
        wordSquare = ((lineHeight/3)/2)*2;
        //System.out.println("wordSquare="+wordSquare+" lineHeight="+lineHeight);
        drawLines();
        // prepare converted image for display
		//image = bw.getScaledInstance(width,height, Image.SCALE_SMOOTH);
	}
    /**
     * Generate a faint rendition of the grayscale image plus the lines
     */
    void drawLines()
    {
        Graphics g = linesImage.getGraphics();
        for ( int i=0;i<bases.size();i++ )
        {
            WordBase wb = bases.get( i );
            g.setColor( Color.BLACK );
            g.fillRect( wb.x, wb.y-2, wb.len, 4 );
        }
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
		// now white out pixels on the margin
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
	 * Recognise lines and words within lines using the B&W image
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
				WordBase current = null;
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
							current = new WordBase(j, peakPos, BLOCK_WIDTH );
							bases.add( current );
						}
						else
							current.len += BLOCK_WIDTH;
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
     * @param wb don't go outside the horizontal limits if wb is not null
	 */
	private void propagateRect( Area a, int x, int y, int xsize, int ysize,
        WordBase wb)
	{
		// accept current rectangle
		Rectangle r = new Rectangle( x, y, xsize, ysize );
		a.add( new Area(r) );
		// above
		int yValue = Math.max(y-wordSquare,0);
		int yHeight = Math.min(wordSquare,y);
		if ( yHeight > 0 )
			augmentArea( a, x, yValue, wordSquare, yHeight, wb );
		// below
		yValue = y+ysize;
		yHeight = Math.min(wordSquare,wr.getHeight()-yValue);
		if ( yHeight > 0 )
			augmentArea( a, x, yValue, xsize, yHeight, wb );
		// left
		int xValue = Math.max(x-wordSquare,0);
		int xWidth = x-xValue;
		if ( xWidth > 0 && (wb==null||wb.x<xValue+xWidth) )
			augmentArea( a, xValue, y, xWidth, ysize, wb );
		// right
		xValue = x+xsize;
		xWidth = Math.min(wordSquare,wr.getWidth()-xValue);
		if ( xWidth > 0 && (wb==null||wb.x+wb.len>xValue) )
			augmentArea( a, xValue, y, xWidth, ysize, wb );
	}
	/**
	 * Grow an area recursively, working with the wr raster
	 * @param a the are to add shapes to
	 * @param x the left coordinate of the top-left of the square
	 * @param y the top coordinate of the top-left of the square 
	 * @param xsize the width of the square in pixels
	 * @param ysize the height of the square in pixels
     * @param wb don't go outside the horizontal limits if wb is not null
	 */
	void augmentArea( Area a, int x, int y, int xsize, int ysize, 
        WordBase wb )
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
				propagateRect(a, x, y, xsize, ysize, wb );
			}
			else if ( xsize==wordSquare && ysize==wordSquare )
			{
				// try subdivision
				if ( testSmallArray(x,y,wordSquare/2,wordSquare/2) )
					propagateRect(a,x,y,xsize,ysize,wb);
				else if ( testSmallArray(x+wordSquare/2,y,wordSquare/2,
					wordSquare/2) )
					propagateRect(a,x,y,xsize,ysize,wb);
				else if ( testSmallArray(x,y+wordSquare/2,wordSquare/2,
					wordSquare/2) )
					propagateRect(a,x,y,xsize,ysize,wb);
				else if ( testSmallArray(x+wordSquare/2,y+wordSquare/2,
					wordSquare/2, wordSquare/2) )
					propagateRect(a,x,y,xsize,ysize,wb);					
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
				poly.addPoint( p.x, p.y, 1.0f );
                if ( i==hull.size()-1 )
                {
                    // join back to start
                    Point q = hull.get(0);
                    poly.addPoint( q.x,q.y, 1.0f );
                }
			}
		}
		return poly;
	}
	/** 
	 * Create a Polygon or Rectangle, whichever fits best.
	 * @param a the area already constructed
	 */
	public Shape createAppropriateShape( Area a )
	{
		Region p = areaToPolygon( a );
		Rect r = p.getBounds();
		if ( p.area() < POLYGON_ACCEPT_RATIO*r.width*r.height )
			return p;
		else
			return r;
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
     * Recognise a single line starting at a single point
     * @param x the absolute x-coordinate
     * @param y the absolute y-coordinate
     * @return a Rect or Region as appropriate
     */
    public Shape recogniseLine( int x, int y )
    {
        Area a = new Area();
        WordBase wb = findNearestWord( x, y );
        if ( wb.index != -1 )
        {
            // get words previous to index
            WordBase prev = bases.get( wb.index );
            int start=wb.index;
            if ( wb.index > 0 )
            {
                for ( start=wb.index-1;start>=0;start-- )
                {
                    WordBase wc = bases.get( start );
                    if ( start == 0 )
                        break;
                    else if ( wc.x+wc.len > prev.x )
                    {
                        start = start+1;
                        break;
                    }
                    else
                        prev = wb;
                }
            }
            // get words after index
            int end=wb.index;
            prev = bases.get( wb.index );
            if ( wb.index < bases.size()-1 )
            {
                for ( end=wb.index+1;end<bases.size();end++ )
                {
                    WordBase wc = bases.get( end );
                    if ( end == bases.size()-1 )
                        break;
                    else if ( wc.x < prev.x+prev.len )
                    {
                        end = end-1;
                        break;
                    }
                    else
                        prev = wc;
                }
            }
            for ( int i=start;i<=end;i++ )
            {
                WordBase wc = bases.get( i );
                Shape s = recogniseWord( wc.x, wc.y );
                if ( s != null )
                    a.add( s.toArea() );
            }
            return createAppropriateShape( a );
        }
        return null;
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
        WordBase wb = findNearestWord( x, y );
        augmentArea( a, x, y-wordSquare/2, Math.min(wordSquare,x), 
            Math.min(wordSquare,y), wb );
        return createAppropriateShape( a );
    }
        /**
     * Get the estimated line height
     * @return an estimate of the number of lines
     */
    public int getLineHeight()
    {
        int top=0,bottom=0;
        int numLines = 0;
        WordBase prev = null;
        for ( int i=0;i<bases.size();i++ )
        {
            WordBase wb = bases.get(i);
            if ( prev == null )
            {
                numLines = 1;
                top = wb.y;
            }
            else if ( wb.x < prev.x )
            {
                numLines++;
                bottom = wb.y;
            }
            prev = wb;
        }
        // otherwise we will be one short
        numLines++;
        return (bottom-top)/((numLines-1)*2);
    }
    /**
     * Do a binary lookup in the words array. Words is sorted on y then on x.
     * @param x the x-coordinate of the click nearest the wb
     * @param y the y-coordinate nearest the wb
     * @return null if nothing close enough, else the wb
     */
    WordBase findNearestWord( int x, int y )
    {
        int top = 0;
        int bottom = bases.size()-1;
        while ( bottom >= top )
        {
            int middle = (bottom+top)/2;
            WordBase wb = bases.get( middle );
            if ( wb.y-y >= wordSquare )
                bottom = middle-1;
            else if ( y-wb.y >= wordSquare )
                top = middle+1;
            else if ( x > wb.x+wb.len )
                top = middle+1;
            else if ( x < wb.x )
                bottom = middle-1;
            else if ( x >= wb.x && x <= wb.x+wb.len )
            {
                wb.index = middle;
                return wb;
            }
        }
        // admit defeat
        return null;
    }
}
