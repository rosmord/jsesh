package jsesh.mdcDisplayer.swing.preferencesEditor;


import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;


public class DrawingSpecificationsForm extends JPanel
{
   JFormattedTextField normalSignHeightField = new JFormattedTextField();
   JFormattedTextField cartoucheLineWidthField = new JFormattedTextField();
   JFormattedTextField lineSkipField = new JFormattedTextField();
   JFormattedTextField columnSkipField = new JFormattedTextField();
   JFormattedTextField maxCadratHeightField = new JFormattedTextField();
   JFormattedTextField MaxCadratWidthField = new JFormattedTextField();
   JComboBox unitField = new JComboBox();

   /**
    * Default constructor
    */
   public DrawingSpecificationsForm()
   {
      initializePanel();
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

   
   public JPanel createPanel()
   {
      JPanel jpanel1 = new JPanel();
      FormLayout formlayout1 = new FormLayout("FILL:4DLU:NONE,FILL:DEFAULT:NONE,FILL:4DLU:NONE,FILL:PREF:NONE,FILL:4DLU:NONE,FILL:DEFAULT:NONE,FILL:4DLU:NONE,FILL:DEFAULT:NONE,FILL:4DLU:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:NONE","CENTER:4DLU:NONE,CENTER:DEFAULT:NONE,CENTER:4DLU:NONE,CENTER:DEFAULT:NONE,CENTER:4DLU:NONE,CENTER:DEFAULT:NONE,CENTER:4DLU:NONE,CENTER:DEFAULT:NONE,CENTER:4DLU:NONE,CENTER:DEFAULT:NONE,CENTER:4DLU:NONE,CENTER:DEFAULT:NONE,CENTER:4DLU:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE,CENTER:DEFAULT:NONE");
      CellConstraints cc = new CellConstraints();
      jpanel1.setLayout(formlayout1);

      JLabel jlabel1 = new JLabel();
      jlabel1.setText("'A1' sign Height");
      jpanel1.add(jlabel1,cc.xy(2,2));

      JLabel jlabel2 = new JLabel();
      jlabel2.setText("Cartouche Line Width");
      jpanel1.add(jlabel2,cc.xy(2,4));

      JLabel jlabel3 = new JLabel();
      jlabel3.setText("Line skip");
      jpanel1.add(jlabel3,cc.xy(2,6));

      JLabel jlabel4 = new JLabel();
      jlabel4.setText("Column skip");
      jpanel1.add(jlabel4,cc.xy(2,8));

      JLabel jlabel5 = new JLabel();
      jlabel5.setText("Maximal Cadrat Height");
      jpanel1.add(jlabel5,cc.xy(2,10));

      JLabel jlabel6 = new JLabel();
      jlabel6.setText("Maximal Cadrat width");
      jpanel1.add(jlabel6,cc.xy(2,12));

      normalSignHeightField.setColumns(5);
      normalSignHeightField.setName("normalSignHeightField");
      jpanel1.add(normalSignHeightField,cc.xy(4,2));

      cartoucheLineWidthField.setName("cartoucheLineWidthField");
      jpanel1.add(cartoucheLineWidthField,cc.xy(4,4));

      lineSkipField.setName("lineSkipField");
      jpanel1.add(lineSkipField,cc.xy(4,6));

      columnSkipField.setName("columnSkipField");
      jpanel1.add(columnSkipField,cc.xy(4,8));

      maxCadratHeightField.setName("maxCadratHeightField");
      jpanel1.add(maxCadratHeightField,cc.xy(4,10));

      MaxCadratWidthField.setName("MaxCadratWidthField");
      jpanel1.add(MaxCadratWidthField,cc.xy(4,12));

      JLabel jlabel7 = new JLabel();
      jlabel7.setText("Units");
      jpanel1.add(jlabel7,cc.xy(6,2));

      unitField.setName("unitField");
      jpanel1.add(unitField,cc.xy(8,2));

      addFillComponents(jpanel1,new int[]{ 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20 },new int[]{ 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20 });
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
