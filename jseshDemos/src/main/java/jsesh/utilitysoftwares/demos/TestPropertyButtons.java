package jsesh.utilitysoftwares.demos;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JRadioButton;
import javax.swing.JToolBar;

import org.qenherkhopeshef.guiFramework.PropertyButtonModel;



public class TestPropertyButtons {
	
	private Essai essai;
	
	public TestPropertyButtons() {
		JToolBar toolbar= new JToolBar();
		
		
		essai= new Essai();
		PropertyButtonModel modelx= new PropertyButtonModel(essai,"value", "x");
		PropertyButtonModel modely= new PropertyButtonModel(essai,"value", "y");
		PropertyButtonModel boolTrue= new PropertyButtonModel(essai,"indicateur",Boolean.TRUE);
		JRadioButton r1= new JRadioButton("x");
		r1.setModel(modelx);
		
		JRadioButton r2= new JRadioButton("y");
		r2.setModel(modely);
		
		JCheckBox r3= new JCheckBox("indicateur");
		r3.setModel(boolTrue);
				
		JButton swap = new JButton("swap");
		
		ActionListener displayer= new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println(essai);
			}
		};
		
		swap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (essai.getValue().equals("x"))
					essai.setValue("y");
				else
					essai.setValue("x");
				essai.setIndicateur(! essai.isIndicateur());
			}
		});
		toolbar.add(r1);
		toolbar.add(r2);
		toolbar.add(r3);
		toolbar.add(swap);
		
		ButtonGroup g= new ButtonGroup();
		g.add(r1);
		g.add(r2);
		
		r1.addActionListener(displayer);
		r2.addActionListener(displayer);
		r3.addActionListener(displayer);
		
		JFrame testF= new JFrame();		
		testF.getContentPane().add(toolbar);
		testF.pack();
		testF.setVisible(true);
		testF.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public static void main(String[] args) {
		TestPropertyButtons t= new TestPropertyButtons();
	}
}
