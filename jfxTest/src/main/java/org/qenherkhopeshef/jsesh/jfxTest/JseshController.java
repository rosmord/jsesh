/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.qenherkhopeshef.jsesh.jfxTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
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
