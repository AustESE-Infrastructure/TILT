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
import java.awt.event.MouseMotionListener;
/**
 * Transparent panel overlaying image. Handles mouse clicks, draws shapes etc.
 * @author desmond
 */
public class Canvas extends JPanel 
{
    /** width of canvas */
    int width;
    /** height of canvas */
    int height;
    /** RECT_MODE or REGION_MODE */
    Mode mode;
    /** currently selected shape */
    Shape current;
    /** current position of drag point */
    Point draggedPoint;
    /** start of drag */
    Point dragStart;
    /** current point of dragged shape */
    Point draggedCurrent;
    /** array of regions and rectangles */
    ArrayList<Shape> shapes;
    /** parent imagepanel */
    ImagePanel parent;
    
    /**
     * Create a new canvas
     * @param width the desired canvas width
     * @param height the desired canvas height
     */
    public Canvas( int width, int height, ImagePanel parent )
    {
        this.height = height;
        this.width = width;
        this.parent = parent;
        this.shapes = new ArrayList<Shape>();     
        this.setOpaque(false);
        this.setBounds(0, 0, width, height);
        this.addMouseListener(new CanvasMouseListener());
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
        this.addMouseMotionListener( new CanvasMouseMotionListener() );
    }
    /**
     * Listen for mouse-dragged events
     */
    class CanvasMouseMotionListener implements MouseMotionListener
    {
        public void mouseDragged(MouseEvent e)
        {
            // point on a region selected: drag it
            if ( draggedPoint != null )
            {
                current.updatePoint( draggedPoint, e.getX(), e.getY() );
                draggedPoint = new Point( e.getX(), e.getY() );
                Canvas parent = (Canvas)e.getSource();
                parent.repaint();
            }
            // entire shape selected
            else if ( draggedCurrent != null )
            {
                current.translate( draggedCurrent, e.getX(), e.getY() );
                draggedCurrent = new Point( e.getX(), e.getY() );
                Canvas parent = (Canvas)e.getSource();
                parent.repaint();
            }
        }
        public void mouseMoved(MouseEvent e)
        {
        }
    }
    /**
     * Mouse click, mouse down, mouse up, shape select, start and end drag
     */
    class CanvasMouseListener implements MouseListener
    {
        public void mouseClicked(MouseEvent e) 
        {
            Shape hit = getHitRegion(e.getX(),e.getY());
            if ( hit != null && current != null && current.isClosed() )
                current = hit;
            else if ( mode==Mode.REGION )
            {
                if ( current == null || current.isClosed() )
                    current = new Region( e.getX(), e.getY() );
                else 
                {
                    current.addPoint( e.getX(), e.getY() );
                    if ( current.isClosed() )
                    {
                        shapes.add( current );
                    }
                }
            }
            else if ( mode == Mode.RECOGNISE_WORD )
            {
                Shape s = parent.recogniseWord( e.getX(), e.getY() );
                if ( s != null )
                {
                    current = s;
                    current.scale( parent.getScale() );
                    shapes.add( current );
                }
            }
            Canvas parent = (Canvas)e.getSource();
            parent.repaint();
            parent.requestFocus();
        }
        /**
         * Mouse pressed down but not released yet (start of drag)
         * @param e the mouse event
         */
        public void mousePressed( MouseEvent e ) 
        {
            draggedPoint = null;
            if ( current != null )
            {
                draggedPoint = current.pointClicked(e.getX(),e.getY());
                if ( draggedPoint == null )
                {
                    // dragging an entire shape
                    Shape r = getHitRegion( e.getX(),e.getY() );
                    if ( r != null )
                    {
                        if ( r == current )
                            draggedCurrent = new Point(e.getX(),e.getY());
                    // else ignore attempt to drag non-selected shape
                    }
                    else if ( mode == Mode.RECTANGLE )
                    {
                        current = new Rect( e.getX(), e.getY() );
                        shapes.add( current );
                        draggedPoint = new Point( e.getX(), e.getY() );
                        Canvas parent = (Canvas)e.getSource();
                        parent.repaint();
                        parent.requestFocus();
                    }
                }
            }
            else if ( mode == Mode.RECTANGLE )
            {
                current = new Rect( e.getX(), e.getY() );
                shapes.add( current );
                draggedPoint = new Point( e.getX(), e.getY() );
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
            if ( current != null )
            {
                if ( draggedPoint != null )
                {
                    current.updatePoint( draggedPoint, e.getX(), e.getY() );
                    draggedPoint = null;
                    Canvas parent = (Canvas)e.getSource();
                    parent.repaint();
                }
                else if ( draggedCurrent != null )
                {
                    current.translate( draggedCurrent, e.getX(), e.getY() );
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
         * @param x the x-coordinate of the click
         * @param y the y-coordinate ditto
         * @return the region if it was inside one
         */
        private Shape getHitRegion( int x, int y )
        {
            for ( int i=0;i<shapes.size();i++ )
            {
                Shape r = shapes.get( i );
                if ( r.contains(x,y) )
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
            for ( int i=0;i<shapes.size();i++ )
            {
                Shape s = shapes.get( i );
                s.scale( parent.getScale() );
            }
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
                Shape r = shapes.get( i );
                if ( r != current )
                    r.paint( g, Utils.BLACK );
            }
        }
        // paint current even if it is not closed
        if ( current != null )
            current.paint(g,Utils.RED);
    }
    /**
     * Set the drawing mode
     * @param mode the mode: rectangle or region and maybe others later
     */
    public void setMode( Mode mode )
    {
        this.mode = mode;
    }
    public void printSizes()
    {
        System.out.println("canvas width="+width+" height="+height);
    }
}
