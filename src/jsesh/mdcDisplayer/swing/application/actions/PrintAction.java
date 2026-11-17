/**
 * author : Serge ROSMORDUC
 * This file is distributed according to the LGPL (GNU lesser public license)
 */
package jsesh.mdcDisplayer.swing.application.actions;

import java.awt.event.ActionEvent;
import java.awt.print.PrinterJob;

import javax.swing.JOptionPane;

import jsesh.mdcDisplayer.draw.ViewPrinter;
import jsesh.mdcDisplayer.swing.application.MDCDisplayerAppliWorkflow;
import jsesh.mdcDisplayer.swing.application.actions.generic.BasicAction;

/**
 * Rather outdated printing class. Should be rewritten. Use PDF instead.
 * @author rosmord
 * @deprecated
 */

public class PrintAction extends BasicAction {
	public PrintAction(String name, MDCDisplayerAppliWorkflow workflow) {
		super(name,workflow);
	}

	public void actionPerformed(ActionEvent e) {
		String message = "Please note that the printing facilities still suffer from some bugs.\n";
		message += "(page layout problems; shading don't print well)\n";
		message += "\nI suggest you use export as pdf instead.\n";
		message += "Do you want to continue printing ?";
		int ok = JOptionPane.showConfirmDialog(null, message, "warning",
				JOptionPane.YES_NO_OPTION);
		if (ok == JOptionPane.YES_OPTION) {
			// IMPORTANT : we need some central drawer somewhere.
			PrinterJob printJob = PrinterJob.getPrinterJob();
			ViewPrinter printer = new ViewPrinter();
	
			printJob.setPageable(printer);
			printer.setModel(workflow.getData().getModel());
			if (printJob.printDialog()) {
				try {
					printJob.print();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}
}