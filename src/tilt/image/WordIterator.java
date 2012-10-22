/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tilt.image;

import java.awt.geom.Area;
import java.util.Iterator;
import tilt.Shape;

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
       return pos < parent.bases.size();
   }
   public Shape next()
   {
       Area a = new Area();
       WordBase wb = parent.bases.get(pos++);
       parent.augmentArea( a, wb.x, wb.y-parent.wordSquare/2, 
           Math.min(parent.wordSquare,wb.x), 
           Math.min(parent.wordSquare,wb.y), wb );
       return parent.createAppropriateShape( a );
   }
   public void remove() throws UnsupportedOperationException
   {
       throw new UnsupportedOperationException("remove not supported");
   }
}