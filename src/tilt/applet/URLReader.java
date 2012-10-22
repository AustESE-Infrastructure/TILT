package tilt.applet;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.net.URL;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.net.URLConnection;
/**
 * Read byte data from a url
 * @author desmond
 */
public class URLReader 
{
    ByteArrayOutputStream bos;
    URL url;
    URLReader( URL url )
    {
        this.url = url;
        bos = new ByteArrayOutputStream();
    }
    public byte[] read() throws Exception 
    {
        URLConnection conn = url.openConnection();
        InputStream is = conn.getInputStream();
        while ( is.available()>0 )
        {
            byte[] data = new byte[is.available()];
            is.read( data );
            bos.write( data );
        }
        is.close();
        return bos.toByteArray();
    }
}