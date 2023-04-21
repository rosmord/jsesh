/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsesh.jhotdraw;

import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import jsesh.graphics.export.BitmapExporter;
import jsesh.graphics.export.ExportData;
import jsesh.mdc.MDCParserModelGenerator;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdc.model.MDCPosition;
import jsesh.mdc.model.TopItemList;
import jsesh.mdcDisplayer.preferences.DrawingSpecification;
import jsesh.mdcDisplayer.preferences.DrawingSpecificationsImplementation;

/**
 * Test class to ensure that ExportAsBitmap doesn't leak memory.
 * @author rosmord
 */
public class ExportAsBitmapTest {

    public void run() {
        try {
            DrawingSpecification spec = new DrawingSpecificationsImplementation();
            TopItemList list;
            MDCParserModelGenerator parser= new MDCParserModelGenerator();
            list= parser.parse(new StringReader("i-w-r:a-C1-m-p*t:pt"));
            MDCPosition start = new MDCPosition(list, 0);
            MDCPosition end = new MDCPosition(list, 2);

            BitmapExporter exporter = new BitmapExporter(null);
            ExportData data = new ExportData(spec, start, end, list, 1.0);
            exporter.askUser(true);
            exporter.exportSelection(data);
        } catch (MDCSyntaxError ex) {
            Logger.getLogger(ExportAsBitmapTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ExportAsBitmapTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    public static void main(String args[]) {
        while (true) {
            System.out.println("exporting");
            new ExportAsBitmapTest().run();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                Logger.getLogger(ExportAsBitmapTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
