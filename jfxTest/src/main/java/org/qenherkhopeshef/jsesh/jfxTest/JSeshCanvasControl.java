/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qenherkhopeshef.jsesh.jfxTest;

import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.Observable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ListView;
import jsesh.mdc.MDCSyntaxError;
import jsesh.mdcDisplayer.draw.MDCDrawingFacade;
import org.jfree.fx.FXGraphics2D;

/**
 *
 * @author rosmord
 */
public class JSeshCanvasControl extends Canvas {

    private ListView<Object> a;
    private final StringProperty mdc = new SimpleStringProperty("r:a-ra-m-p*t:pt");

    public JSeshCanvasControl() {
        setWidth(10000);
        setHeight(10000);
        mdc.addListener(this::updateDrawing);
        updateDrawing(mdc);
    }

    private void updateDrawing(Observable observable) {
        System.out.println(getClip());
        getGraphicsContext2D().save();
        getGraphicsContext2D().setFill(javafx.scene.paint.Color.WHITE);
        getGraphicsContext2D().clearRect(0, 0, 1.0e10, 1.0e10);
        getGraphicsContext2D().fillRect(0, 0, getWidth(), getHeight());

        getGraphicsContext2D().setFill(javafx.scene.paint.Color.BLACK);
//        getGraphicsContext2D().beginPath();
//        getGraphicsContext2D().rect(0, 0, 300, 300);
//        getGraphicsContext2D().closePath();
//        
//        getGraphicsContext2D().clip();
         FXGraphics2D g = new FXGraphics2D(getGraphicsContext2D());
       // MyG2D g = new MyG2D(getGraphicsContext2D());
        MDCDrawingFacade drawing = new MDCDrawingFacade();
        // Change the scale, choosing the cadrat height in pixels.
        drawing.setCadratHeight(25);
        g.setBackground(Color.WHITE);
        g.setColor(Color.BLACK);
        try {
            System.err.println("ICI");
            // Change a number of parameters
            drawing.draw(mdc.get(), g, 0, 0);
        } catch (MDCSyntaxError ex) {
            Logger.getLogger(JSeshControl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            g.dispose();
            getGraphicsContext2D().restore();
        }

    }

    public StringProperty getMdcProperty() {
        return mdc;
    }

    public void setMdc(String mdc) {
        this.mdc.set(mdc);
    }

    public String getMdc() {
        return mdc.get();
    }

}
