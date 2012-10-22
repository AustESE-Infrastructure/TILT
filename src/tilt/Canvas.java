package tilt;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
/**
 * Transparent panel overlaying image. Handles mouse clicks, draws shapes etc. 
 * Most x,y coordinates are global (image).
 * @author desmond
 */
public class Canvas extends JPanel 
{
    /** width of canvas LOCAL coordinates */
    int width;
    /** height of canvas LOCAL coordinates */
    int height;
    /** RECT_MODE or REGION_MODE */
    Mode mode;
    /** currently selected shape */
    Shape current;
    /** current position of drag point, global coordinates */
    Point draggedPoint;
    /** start of drag, global coordinates */
    Point dragStart;
    /** current point of dragged shape, global coordinates */
    Point draggedCurrent;
    /** local copy of regions and rectangles to facilitate highlighting */
    ArrayList<Shape> shapes;
    /** parent imagepanel */
    ImagePanel parent;
    
    /**
     * Create a new canvas
     * @param width the desired canvas width within the window
     * @param height the desired canvas height within the window
     */
    public Canvas( int width, int height, ImagePanel parent )
    {
        this.height = height;
        this.width = width;
        this.parent = parent;
        this.shapes = new ArrayList<Shape>();     
        this.setOpaque(false);
        this.setBounds(0, 0, width, height);
        CanvasMouseListener cml = new CanvasMouseListener();
        this.addMouseListener(cml);
        this.addMouseMotionListener( cml );
        this.addKeyListener( new KeyAdapter()
        {
            public void keyPressed( KeyEvent ke )
            {
                int code = ke.getKeyCode();
                if ( current != null && (code == KeyEvent.VK_DELETE 
                    || code==KeyEvent.VK_BACK_SPACE))
                {
                    shapes.remove( current );
                    if ( shapes.size()>0 )
                        current = shapes.get(shapes.size()-1);
                    else
                        current = null;
                    Canvas parent = (Canvas)ke.getSource();
                    parent.repaint();
                }
            }
        });
    }
    /**
     * Mouse click, mouse down, mouse up, shape select, start and end drag
     */
    class CanvasMouseListener extends MouseAdapter
    {
        public void mouseDragged(MouseEvent e)
        {
            //System.out.println("mouse dragged");
            // point on a region selected: drag it
            float scale = parent.getScale();
            int globalX = Math.round(e.getX()/scale);
            int globalY = Math.round(e.getY()/scale);
            if ( draggedPoint != null )
            {
                current.updatePoint( draggedPoint, globalX, globalY );
                draggedPoint = new Point( globalX, globalY );
                Canvas parent = (Canvas)e.getSource();
                parent.repaint();
            }
            // entire shape selected
            else if ( draggedCurrent != null )
            {
                current.translate( draggedCurrent, globalX, globalY );
                draggedCurrent = new Point( globalX, globalY );
                Canvas parent = (Canvas)e.getSource();
                parent.repaint();
            }
        }
        public void mouseMoved(MouseEvent e)
        {
            super.mouseMoved(e);
        }
        public void mouseClicked(MouseEvent e) 
        {
            //System.out.println("mouse clicked");
            float scale = parent.getScale();
            int globalX = Math.round(e.getX()/scale);
            int globalY = Math.round(e.getY()/scale);
            Shape hit = getHitRegion(globalX,globalY);
            if ( hit != null && current != null && current.isClosed() )
                current = hit;
            else if ( mode==Mode.REGION )
            {
                if ( current == null || current.isClosed() )
                    current = new Region( globalX, globalY, scale );
                else 
                {
                    current.addPoint( globalX, globalY, scale );
                    if ( current.isClosed() )
                    {
                        shapes.add( current );
                        // set word and line index
                    }
                }
            }
            else if ( mode == Mode.RECOGNISE_WORD )
            {
                Shape s = parent.recogniseWord( globalX, globalY );
                if ( s != null )
                {
                    parent.getLinks().addWordShape( s );
                    current = s;
                    shapes.add( current );
                }
            }
            else if ( mode == Mode.RECOGNISE_LINE )
            {
                Shape s = parent.recogniseLine( globalX, globalY );
                if ( s != null )
                {
                    parent.getLinks().addLineShape( s );
                    current = s;
                    shapes.add( current );
                }
            }
            Canvas parent = (Canvas)e.getSource();
            parent.repaint();
            parent.requestFocus();
        }
        /**
         * Create a new Rect and set it to current
         * @param globalX the x-coordinate in the global (image) system
         * @param globalY y-coordinate in the global (image) system
         * @return the new displayable version of the Rect
         */
        private Shape createRect( int globalX, int globalY )
        {
            Rect r = new Rect( globalX, globalY );
            parent.getLinks().addShape( r );
            addShape( r );
            draggedPoint = new Point( globalX, globalY );
            return r;
        }
        /**
         * Mouse pressed down but not released yet (start of drag)
         * @param e the mouse event
         */
        public void mousePressed( MouseEvent e ) 
        {
            draggedPoint = null;
            //System.out.println("mouse pressed");
            float scale = parent.getScale();
            int globalX = Math.round(e.getX()/scale);
            int globalY = Math.round(e.getY()/scale);
            //parent.requestFocus();
            if ( current != null )
            {
                draggedPoint = current.pointClicked(globalX,globalY,scale);
                if ( draggedPoint == null )
                {
                    // dragging an entire shape
                    Shape r = getHitRegion( globalX,globalY );
                    if ( r != null )
                    {
                        if ( r == current )
                            draggedCurrent = new Point(globalX,globalY);
                    // else ignore attempt to drag non-selected shape
                    }
                    else if ( mode == Mode.RECTANGLE )
                    {
                        current = createRect( globalX, globalY );
                        Canvas parent = (Canvas)e.getSource();
                        parent.repaint();
                        parent.requestFocus();
                    }
                }
            }
            else if ( mode == Mode.RECTANGLE )
            {
                current = createRect( globalX, globalY );
                Canvas parent = (Canvas)e.getSource();
                parent.repaint();
                parent.requestFocus();
            }
        }
        /**
         * Mouse released after a drag
         * @param e the mouse event
         */
        @Override
        public void mouseReleased( MouseEvent e ) 
        {
            //System.out.println("mouse released");
            if ( current != null )
            {
                float scale = parent.getScale();
                int globalX = Math.round(e.getX()/scale);
                int globalY = Math.round(e.getY()/scale);
                if ( draggedPoint != null )
                {
                    current.updatePoint( draggedPoint, globalX, globalY );
                    draggedPoint = null;
                    Canvas parent = (Canvas)e.getSource();
                    parent.repaint();
                }
                else if ( draggedCurrent != null )
                {
                    current.translate( draggedCurrent, globalX, globalY );
                    draggedCurrent = null;
                    Canvas parent = (Canvas)e.getSource();
                    parent.repaint();
                }
            }
        }
        public void mouseExited( MouseEvent me )
        { 
        }
        public void mouseEntered( MouseEvent me )
        { 
        }
        /**
         * Was one of the regions clicked inside?
         * @param x the global x-coordinate of the click
         * @param y the global y-coordinate ditto
         * @return the region if it was inside one
         */
        private Shape getHitRegion( int globalX, int globalY )
        {
            for ( int i=0;i<shapes.size();i++ )
            {
                Shape r = shapes.get( i );
                if ( r.contains(globalX,globalY) )
                    return r;
            }
            return null;
        }
    }
    @Override
    public Dimension getPreferredSize()
    {
        return new Dimension( width, height );
    }
    /**
     * Add a shape to the canvas and make it current
     * @param s the shape to add
     */
    void addShape( Shape s )
    {
        current = s;
        shapes.add( s );
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
            this.width = width;
            this.height = height;   
        }
        else
            super.setSize(width,height);
    }
    /**
     * Paint the regions
     * @param g the graphics context
     */
    public void paint(Graphics g) 
    {
        if ( shapes != null )
        {
            for ( int i=0;i<shapes.size();i++ )
            {
                Shape s = shapes.get( i );
                if ( s != current )
                    s.paint( g, Utils.BLACK, parent.getScale() );
            }
        }
        // paint current even if it is not closed
        if ( current != null )
            current.paint(g,Utils.RED,parent.getScale());
    }
    /**
     * Set the drawing mode
     * @param mode the mode: rectangle or region and maybe others later
     */
    public void setMode( Mode mode )
    {
        this.mode = mode;
    }
}
