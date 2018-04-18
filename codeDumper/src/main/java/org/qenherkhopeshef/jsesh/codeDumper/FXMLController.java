package org.qenherkhopeshef.jsesh.codeDumper;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import org.qenherkhopeshef.jsesh.codeDumper.CodeDump;

public class FXMLController implements Initializable {

    @FXML
    private TextField sourceFolderField;

    @FXML
    private TextField destFolderField;

    @FXML
    private Label messageField;

    private final SimpleObjectProperty<File> sourcePath = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<File> destPath = new SimpleObjectProperty<>();
    private final StringProperty message = new SimpleStringProperty();

    @FXML
    private void changeSourceAction(ActionEvent event) {
        DirectoryChooser fileChooser = new DirectoryChooser();
        fileChooser.setInitialDirectory(sourcePath.getValue());
        fileChooser.setTitle("Choose source folder");
        File choice = fileChooser.showDialog(sourceFolderField.getScene().getWindow());
        if (choice != null) {
            sourcePath.setValue(choice);
        }
    }

    @FXML
    private void changeDestAction(ActionEvent event) {
        DirectoryChooser fileChooser = new DirectoryChooser();
        fileChooser.setInitialDirectory(destPath.getValue());
        fileChooser.setTitle("Choose destination folder");
        File choice = fileChooser.showDialog(sourceFolderField.getScene().getWindow());
        if (choice != null) {
            destPath.setValue(choice);
        }
    }

    @FXML
    private void extractCodesAction(ActionEvent event) {
        if (pathIsOk(sourcePath, "please choose a source folder") 
                && pathIsOk(destPath, "please choose a target folder")) {
            try {
                new CodeDump().dumpFolders(sourcePath.getValue().toPath(), destPath.getValue().toPath());
                message.set("dump done");
                Desktop.getDesktop().open(destPath.get());
            } catch (IllegalArgumentException ex) {
                message.set("problems with folders");
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                message.set("problems reading or writing files");
                Logger.getLogger(FXMLController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        messageField.textProperty().bind(message);
        sourceFolderField.textProperty().bind(Bindings.when(sourcePath.isNull()).then("").otherwise(sourcePath.asString()));
        destFolderField.textProperty().bind(Bindings.when(destPath.isNull()).then("").otherwise(destPath.asString()));
    }

   

    private boolean pathIsOk(SimpleObjectProperty<File> pathProperty,
            String badMessage) {
        File pathValue = pathProperty.getValue();
        if (pathValue == null || ! pathValue.isDirectory()) {
            message.set(badMessage);
            return false;
        } else {
            return true;
        }
    }

}
