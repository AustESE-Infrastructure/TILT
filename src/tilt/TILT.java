/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tilt;
import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JToolBar;
import javax.swing.JButton;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.SwingConstants;
import javax.swing.Icon;
import javax.swing.JSeparator;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.awt.event.ActionListener;
import java.io.FileInputStream;


/**
 * Simple desktop app or applet for creating image/text links
 * @author desmond
 */
public class TILT implements ActionListener
{
    ImagePanel image;
    TextPanel text;
    File imageFile;
    File textFile;
    static final int DEFAULT_WIDTH = 250;
    static final int DEFAULT_HEIGHT = 350;
    JButton rectButton, regionButton,bAndWButton,wordButton,linesButton,lineButton,
        pageButton;
    JToolBar topToolBar;
    void readArgs( String[] args )
    {
        if ( args.length > 0 )
            imageFile = new File( args[0] );
        if ( args.length > 1 )
            textFile = new File( args[1] );
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
     * Create a TILT instance form commandline arguments
     * @param args the args passed in
     */
    TILT( String[] args ) throws Exception
    {
        if ( args.length > 0 )
            readArgs( args );
        JFrame frame = new JFrame("Text-Image Link Tool");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        topToolBar = new JToolBar("File Actions");
        topToolBar.add(makeButton("Save24","SAVE","Save to disk or server","SAVE"));
        topToolBar.add(makeButton("Properties24","PERFS","Set Preferences","PREFS"));
        topToolBar.add(makeButton("Open24","OPEN","Open text file","OPEN"));
        topToolBar.add(makeButton("Import24","IMAGE","Open image file","IMAGE"));
        topToolBar.addSeparator();
        rectButton = makeButton("Rectangle24","RECTANGLE","Rectangle","RECTANGLE");
        topToolBar.add(rectButton);
        regionButton = makeButton("Region24","REGION","Region","REGION");
        topToolBar.add(regionButton);
        wordButton = makeButton("WordSelect24","RECOGNISE_WORD","Word select","WORD");
        topToolBar.add(wordButton);
        lineButton = makeButton("LineSelect24","RECOGNISE_LINE","Line select","LINE");
        topToolBar.add(lineButton);
        pageButton = makeButton("Page24","PAGE","Recognise page","PAGE");
        topToolBar.add(pageButton);
        bAndWButton = makeButton("BlackWhite24","B&W","Switch to black and white","B&W");
        topToolBar.add(makeButton("Link24","LINK","Link selected region to selected text","LINK"));
        topToolBar.add(bAndWButton);
        linesButton = makeButton("AlignJustify24","LINES","Show lines","LINES");
        topToolBar.add(linesButton);
        regionButton.setSelected(true);
        frame.add( topToolBar, BorderLayout.NORTH );
        if ( imageFile == null )
            image = new ImagePanel(frame);
        else
            image = new ImagePanel( imageFile, frame );
        image.setMode( Mode.REGION );
        if ( textFile==null )
            text = new TextPanel(image.width,image.height);
        else
            text = new TextPanel(image.width,image.height,loadTextFile(textFile));
        frame.getContentPane().add(image, BorderLayout.WEST);
        frame.getContentPane().add(new JSeparator(SwingConstants.VERTICAL), BorderLayout.CENTER);
        frame.getContentPane().add(text, BorderLayout.EAST);
        
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
    /**
     * Make a single button for the toolbar
     * @param imageName the name of the image in the jar
     * @param actionCommand the string action
     * @param toolTipText the tooltip displayed when mousing over it
     * @param altText alternatieve text if no image
     * @return the button
     */
    protected JButton makeButton(String imageName, 
        String actionCommand,String toolTipText, String altText) 
    {
        //Look for the image.
        String imgLocation = "/graphics/"+imageName+".gif";
        URL imageURL = TILT.class.getResource(imgLocation);

        //Create and initialize the button.
        JButton button = new JButton();
        button.setActionCommand(actionCommand);
        button.setToolTipText(toolTipText);
        button.addActionListener(this);

        if (imageURL != null) 
            button.setIcon(new ImageIcon(imageURL, altText));
        else    
        {
            button.setText(altText);
            System.err.println("Resource not found: " + imgLocation);
        }
        return button;
    }
    public void actionPerformed(ActionEvent e) 
    {
        String command = e.getActionCommand();
        if ( command != null )
        {
            try
            {
                Mode mode = Mode.valueOf(command);
                image.setMode( mode );
                if ( rectButton.isSelected() )
                    rectButton.setSelected( false );
                if ( regionButton.isSelected() )
                    regionButton.setSelected( false );
                if ( wordButton.isSelected() )
                    wordButton.setSelected( false );
                if ( lineButton.isSelected() )
                    lineButton.setSelected( false );
                switch ( mode )
                {
                    case REGION:
                        regionButton.setSelected( true );
                        break;
                    case RECTANGLE:
                        rectButton.setSelected( true );
                        break;
                    case RECOGNISE_WORD:
                        wordButton.setSelected( true );
                        break;
                    case RECOGNISE_LINE:
                        lineButton.setSelected( true );
                        break;
                }
            }
            catch ( IllegalArgumentException iae )
            {
                if ( command.equals("B&W") )
                {
                    bAndWButton.setSelected( !bAndWButton.isSelected() );
                    if ( bAndWButton.isSelected() )
                        image.switchToBW();
                    else
                        image.switchToColor();
                }
                else if ( command.equals("LINES") )
                {
                    linesButton.setSelected( !linesButton.isSelected() );
                    if ( linesButton.isSelected() )
                        image.switchToLines();
                    else
                        image.switchToColor();
                }
                else if ( command.equals("PAGE") )
                {
                    image.recognisePage();
                }
            }
        }
    }
}
