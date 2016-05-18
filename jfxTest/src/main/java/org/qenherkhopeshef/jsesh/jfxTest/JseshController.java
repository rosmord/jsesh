/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qenherkhopeshef.jsesh.jfxTest;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/**
 * FXML Controller class
 *
 * @author rosmord
 */
public class JseshController implements Initializable {

    private JSeshControl jSeshControl;
    @FXML
    private ScrollPane main;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        jSeshControl = new JSeshControl();
        main.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
      
//        AnchorPane anchorPane= new AnchorPane();     
//        final TextArea textArea = new TextArea(s.toString());
//        AnchorPane.setBottomAnchor(textArea, 0.0);
//        AnchorPane.setTopAnchor(textArea, 0.0);
//        AnchorPane.setLeftAnchor(textArea, 0.0);
//        AnchorPane.setRightAnchor(textArea, 0.0);
//        anchorPane.getChildren().add(textArea);
//        //anchorPane.setStyle("-fx-background: #FF0000;");
//        anchorPane.setBackground(new Background(new BackgroundFill(Color.YELLOW, CornerRadii.EMPTY, Insets.EMPTY)));
//        anchorPane.setPrefHeight(300);
//        anchorPane.setPrefWidth(300);
        main.setFitToHeight(true);
        main.setFitToWidth(true);
        main.setContent(jSeshControl);
    }

    public void open() throws IOException {
        Window window= main.getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open JSesh File");
        File f= fileChooser.showOpenDialog(window);
        System.err.println(f.toPath());
        List<String> lines= Files.readAllLines(f.toPath());
        Optional<String> content = lines.stream().reduce((s1,s2)-> s1+"\n"+ s2);
        jSeshControl.setMdc(content.orElse(""));
    }
}
