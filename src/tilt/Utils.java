/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tilt;

import java.awt.Color;

/**
 * Shared routines
 * @author desmond
 */
public class Utils 
{
    static Color RED = new Color( 1.0f,0.0f,0.0f );
    static Color BLACK = new Color( 0.0f,0.0f,0.0f );
    static Color CLEAR = new Color( 1.0f, 1.0f, 1.0f, 0.0f );
    /**
     * Turn a colour into a transparent version of itself
     * @param color the complete colour
     * @param transparencyFactor the fraction of visibility to give the colour
     * @return its transparent self
     */
    public static Color makeTransparent( Color color, float transparencyFactor )
    {
        float red = (float)color.getRed()/255.0f;
        float blue = (float)color.getBlue()/255.0f;
        float green = (float)color.getGreen()/255.0f;
        return new Color(red,green,blue,transparencyFactor);
    }
    public static float percent( int a, int b )
    {
        if ( b == 0 )
            return 100.0f;
        else
        {
            float af = (float)a;
            float bf = (float)b;
            return (af*100.0f)/bf;
        }
    }
}
