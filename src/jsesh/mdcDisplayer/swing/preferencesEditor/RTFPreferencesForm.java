package jsesh.mdcDisplayer.swing.preferencesEditor;


import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class RTFPreferencesForm extends JPanel
{
   JComboBox exportModeField = new JComboBox();
   JFormattedTextField cadratHeightField = new JFormattedTextField();
   JComboBox unitField = new JComboBox();

   /**
    * Default constructor
    */
   public RTFPreferencesForm()
   {
      initializePanel();
   }

   /**
    * Main method for panel
    */
   public static void main(String[] args)
   {
      JFrame frame = new JFrame();
      frame.setSize(600, 400);
      frame.setLocation(100, 100);
      frame.getContentPane().add(new RTFPreferencesForm());
      frame.setVisible(true);

      frame.addWindowListener( new WindowAdapter()
      {
         public void windowClosing( WindowEvent evt )
         {
            System.exit(0);
         }
      });
   }

   /**
    * Adds fill components to empty cells in the first row and first column of the grid.
    * This ensures that the grid spacing will be the same as shown in the designer.
    * @param cols an array of column indices in the first row where fill components should be added.
    * @param rows an array of row indices in the first column where fill components should be added.
    */
   void addFillComponents( Container panel, int[] cols, int[] rows )
   {
      Dimension filler = new Dimension(10,10);

      boolean filled_cell_11 = false;
      CellConstraints cc = new CellConstraints();
      if ( cols.length > 0 && rows.length > 0 )
      {
         if ( cols[0] == 1 && rows[0] == 1 )
         {
            /** add a rigid area  */
            panel.add( Box.createRigidArea( filler ), cc.xy(1,1) );
            filled_cell_11 = true;
         }
      }

      for( int index = 0; index < cols.length; index++ )
      {
         if ( cols[index] == 1 && filled_cell_11 )
         {
            continue;
         }
         panel.add( Box.createRigidArea( filler ), cc.xy(cols[index],1) );
      }

      for( int index = 0; index < rows.length; index++ )
      {
         if ( rows[index] == 1 && filled_cell_11 )
         {
            continue;
         }
         panel.add( Box.createRigidArea( filler ), cc.xy(1,rows[index]) );
      }

   }

   /**
    * Helper method to load an image file from the CLASSPATH
    * @param imageName the package and name of the file to load relative to the CLASSPATH
    * @return an ImageIcon instance with the specified image file
    * @throws IllegalArgumentException if the image resource cannot be loaded.
    */
   public ImageIcon loadImage( String imageName )
   {
      try
      {
         ClassLoader classloader = getClass().getClassLoader();
         java.net.URL url = classloader.getResource( imageName );
         if ( url != null )
         {
            ImageIcon icon = new ImageIcon( url );
            return icon;
         }
      }
      catch( Exception e )
      {
         e.printStackTrace();
      }
      throw new IllegalArgumentException( "Unable to load image: " + imageName );
   }

   public JPanel createPanel()
   {
      JPanel jpanel1 = new JPanel();
      FormLayout formlayout1 = new FormLayout("FILL:4DLU:NONE,FILL:DEFAULT:NONE,FILL:4DLU:NONE,FILL:159PX:NONE,FILL:4DLU:NONE,FILL:DEFAULT:NONE,FILL:4DLU:NONE","CENTER:2DLU:NONE,CENTER:DEFAULT:NONE,CENTER:2DLU:NONE,CENTER:DEFAULT:NONE,CENTER:2DLU:NONE,CENTER:DEFAULT:NONE,CENTER:2DLU:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      exportModeField.setName("exportModeField");
      exportModeField.setToolTipText("select the way the text will be exported : as a large monolithic picture, or as a mix of latin text and picture(s)");
      exportModeField.addItem("as one large picture");
      exportModeField.addItem("grouped cadrats");
      exportModeField.addItem("one picture per cadrat");
      jpanel1.add(exportModeField,cc.xywh(4,2,3,1));

      JLabel jlabel1 = new JLabel();
      jlabel1.setText("export mode");
      jpanel1.add(jlabel1,cc.xy(2,2));

      JLabel jlabel2 = new JLabel();
      jlabel2.setText("cadrat height");
      jpanel1.add(jlabel2,cc.xy(2,4));

      cadratHeightField.setName("cadratHeightField");
      jpanel1.add(cadratHeightField,cc.xy(4,4));

      unitField.setName("unitField");
      jpanel1.add(unitField,cc.xy(6,4));

      addFillComponents(jpanel1,new int[]{ 1,2,3,4,5,6,7 },new int[]{ 1,2,3,4,5,6,7 });
      return jpanel1;
   }

   /**
    * Initializer
    */
   protected void initializePanel()
   {
      setLayout(new BorderLayout());
      add(createPanel(), BorderLayout.CENTER);
   }


}
