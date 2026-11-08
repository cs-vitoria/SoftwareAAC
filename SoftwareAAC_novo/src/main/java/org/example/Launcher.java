package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class Launcher {

    // Metodo universal para mudar de tela a partir de qualquer controller
    public static void mudarTela(ActionEvent event, String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(Launcher.class.getResource("/org/example/" + fxmlFile));
            Node node = (Node) event.getSource();
            Stage stage = (Stage) node.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}