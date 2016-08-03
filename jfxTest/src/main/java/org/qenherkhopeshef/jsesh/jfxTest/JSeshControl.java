/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qenherkhopeshef.jsesh.jfxTest;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 *
 * @author rosmord
 */
public class JSeshControl extends Control {

    private final StringProperty mdc = new SimpleStringProperty("r:a-ra-m-p*t:pt");

    public JSeshControl() {   
        //mdc.addListener(this::updateDrawing);   
        getChildren().add(new Circle(100, 100, 50, Color.RED));     
        setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
        setMaxHeight(USE_PREF_SIZE);
        setMaxWidth(USE_PREF_SIZE);
        setMinHeight(USE_PREF_SIZE);
        setMinWidth(USE_PREF_SIZE);
        setPrefSize(3000, 1_000);
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

      /** {@inheritDoc} */
    @Override protected Skin<JSeshControl> createDefaultSkin() {
        //return new JSeshSkin(this, new JSeshBehaviour(this, new ArrayList<>()));
        return new JSeshSkin(this);
    }
}
